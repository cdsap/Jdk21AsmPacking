package com.awesomeapp.module_0_10

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenModel_868_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_868_ {
    data class Load(val id: Long) : GenEvent_868_()
    data class Update(val model: GenModel_868_) : GenEvent_868_()
    data class Delete(val id: Long) : GenEvent_868_()
    data object Refresh : GenEvent_868_()
    data class Search(val query: String) : GenEvent_868_()
    data class Filter(val predicate: String) : GenEvent_868_()
}

sealed class GenState_868_ {
    data object Idle : GenState_868_()
    data object Loading : GenState_868_()
    data class Success(val items: List<GenModel_868_>) : GenState_868_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_868_()
    data class Partial(val items: List<GenModel_868_>, val hasMore: Boolean) : GenState_868_()
}

interface GenRepository_868_ {
    suspend fun getAll(): List<GenModel_868_>
    suspend fun getById(id: Long): GenModel_868_?
    suspend fun save(model: GenModel_868_): GenModel_868_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_868_>
}

@Singleton
class GenRepositoryImpl_868_ @Inject constructor() : GenRepository_868_ {
    private val store = mutableMapOf<Long, GenModel_868_>()
    override suspend fun getAll(): List<GenModel_868_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_868_? = store[id]
    override suspend fun save(model: GenModel_868_): GenModel_868_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_868_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_868_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_868_ @Inject constructor(
    private val repository: GenRepositoryImpl_868_
) : GenUseCase_868_<Unit, List<GenModel_868_>> {
    override suspend fun invoke(params: Unit): List<GenModel_868_> = repository.getAll()
}

class GenSaveUseCase_868_ @Inject constructor(
    private val repository: GenRepositoryImpl_868_
) : GenUseCase_868_<GenModel_868_, GenModel_868_> {
    override suspend fun invoke(params: GenModel_868_): GenModel_868_ = repository.save(params)
}

class GenDeleteUseCase_868_ @Inject constructor(
    private val repository: GenRepositoryImpl_868_
) : GenUseCase_868_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_868_ @Inject constructor(
    private val repository: GenRepositoryImpl_868_
) : GenUseCase_868_<String, List<GenModel_868_>> {
    override suspend fun invoke(params: String): List<GenModel_868_> = repository.search(params)
}

abstract class GenMapper_868_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_868_ : GenMapper_868_<GenModel_868_, String>() {
    override fun map(input: GenModel_868_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_868_ : GenMapper_868_<String, GenModel_868_>() {
    override fun map(input: String): GenModel_868_ {
        val parts = input.split(":")
        return GenModel_868_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_868_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_868_,
    private val saveUseCase: GenSaveUseCase_868_,
    private val deleteUseCase: GenDeleteUseCase_868_,
    private val searchUseCase: GenSearchUseCase_868_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_868_>(GenState_868_.Idle)
    val state: StateFlow<GenState_868_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_868_) {
        when (event) {
            is GenEvent_868_.Load -> loadAll()
            is GenEvent_868_.Update -> save(event.model)
            is GenEvent_868_.Delete -> delete(event.id)
            is GenEvent_868_.Refresh -> loadAll()
            is GenEvent_868_.Search -> search(event.query)
            is GenEvent_868_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_868_.Loading; _state.value = GenState_868_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_868_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_868_.Success(searchUseCase(query)) } }
}
