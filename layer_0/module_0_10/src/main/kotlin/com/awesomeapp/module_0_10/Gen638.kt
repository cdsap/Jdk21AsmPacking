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

data class GenModel_638_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_638_ {
    data class Load(val id: Long) : GenEvent_638_()
    data class Update(val model: GenModel_638_) : GenEvent_638_()
    data class Delete(val id: Long) : GenEvent_638_()
    data object Refresh : GenEvent_638_()
    data class Search(val query: String) : GenEvent_638_()
    data class Filter(val predicate: String) : GenEvent_638_()
}

sealed class GenState_638_ {
    data object Idle : GenState_638_()
    data object Loading : GenState_638_()
    data class Success(val items: List<GenModel_638_>) : GenState_638_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_638_()
    data class Partial(val items: List<GenModel_638_>, val hasMore: Boolean) : GenState_638_()
}

interface GenRepository_638_ {
    suspend fun getAll(): List<GenModel_638_>
    suspend fun getById(id: Long): GenModel_638_?
    suspend fun save(model: GenModel_638_): GenModel_638_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_638_>
}

@Singleton
class GenRepositoryImpl_638_ @Inject constructor() : GenRepository_638_ {
    private val store = mutableMapOf<Long, GenModel_638_>()
    override suspend fun getAll(): List<GenModel_638_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_638_? = store[id]
    override suspend fun save(model: GenModel_638_): GenModel_638_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_638_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_638_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_638_ @Inject constructor(
    private val repository: GenRepositoryImpl_638_
) : GenUseCase_638_<Unit, List<GenModel_638_>> {
    override suspend fun invoke(params: Unit): List<GenModel_638_> = repository.getAll()
}

class GenSaveUseCase_638_ @Inject constructor(
    private val repository: GenRepositoryImpl_638_
) : GenUseCase_638_<GenModel_638_, GenModel_638_> {
    override suspend fun invoke(params: GenModel_638_): GenModel_638_ = repository.save(params)
}

class GenDeleteUseCase_638_ @Inject constructor(
    private val repository: GenRepositoryImpl_638_
) : GenUseCase_638_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_638_ @Inject constructor(
    private val repository: GenRepositoryImpl_638_
) : GenUseCase_638_<String, List<GenModel_638_>> {
    override suspend fun invoke(params: String): List<GenModel_638_> = repository.search(params)
}

abstract class GenMapper_638_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_638_ : GenMapper_638_<GenModel_638_, String>() {
    override fun map(input: GenModel_638_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_638_ : GenMapper_638_<String, GenModel_638_>() {
    override fun map(input: String): GenModel_638_ {
        val parts = input.split(":")
        return GenModel_638_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_638_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_638_,
    private val saveUseCase: GenSaveUseCase_638_,
    private val deleteUseCase: GenDeleteUseCase_638_,
    private val searchUseCase: GenSearchUseCase_638_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_638_>(GenState_638_.Idle)
    val state: StateFlow<GenState_638_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_638_) {
        when (event) {
            is GenEvent_638_.Load -> loadAll()
            is GenEvent_638_.Update -> save(event.model)
            is GenEvent_638_.Delete -> delete(event.id)
            is GenEvent_638_.Refresh -> loadAll()
            is GenEvent_638_.Search -> search(event.query)
            is GenEvent_638_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_638_.Loading; _state.value = GenState_638_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_638_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_638_.Success(searchUseCase(query)) } }
}
