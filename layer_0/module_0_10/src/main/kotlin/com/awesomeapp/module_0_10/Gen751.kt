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

data class GenModel_751_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_751_ {
    data class Load(val id: Long) : GenEvent_751_()
    data class Update(val model: GenModel_751_) : GenEvent_751_()
    data class Delete(val id: Long) : GenEvent_751_()
    data object Refresh : GenEvent_751_()
    data class Search(val query: String) : GenEvent_751_()
    data class Filter(val predicate: String) : GenEvent_751_()
}

sealed class GenState_751_ {
    data object Idle : GenState_751_()
    data object Loading : GenState_751_()
    data class Success(val items: List<GenModel_751_>) : GenState_751_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_751_()
    data class Partial(val items: List<GenModel_751_>, val hasMore: Boolean) : GenState_751_()
}

interface GenRepository_751_ {
    suspend fun getAll(): List<GenModel_751_>
    suspend fun getById(id: Long): GenModel_751_?
    suspend fun save(model: GenModel_751_): GenModel_751_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_751_>
}

@Singleton
class GenRepositoryImpl_751_ @Inject constructor() : GenRepository_751_ {
    private val store = mutableMapOf<Long, GenModel_751_>()
    override suspend fun getAll(): List<GenModel_751_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_751_? = store[id]
    override suspend fun save(model: GenModel_751_): GenModel_751_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_751_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_751_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_751_ @Inject constructor(
    private val repository: GenRepositoryImpl_751_
) : GenUseCase_751_<Unit, List<GenModel_751_>> {
    override suspend fun invoke(params: Unit): List<GenModel_751_> = repository.getAll()
}

class GenSaveUseCase_751_ @Inject constructor(
    private val repository: GenRepositoryImpl_751_
) : GenUseCase_751_<GenModel_751_, GenModel_751_> {
    override suspend fun invoke(params: GenModel_751_): GenModel_751_ = repository.save(params)
}

class GenDeleteUseCase_751_ @Inject constructor(
    private val repository: GenRepositoryImpl_751_
) : GenUseCase_751_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_751_ @Inject constructor(
    private val repository: GenRepositoryImpl_751_
) : GenUseCase_751_<String, List<GenModel_751_>> {
    override suspend fun invoke(params: String): List<GenModel_751_> = repository.search(params)
}

abstract class GenMapper_751_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_751_ : GenMapper_751_<GenModel_751_, String>() {
    override fun map(input: GenModel_751_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_751_ : GenMapper_751_<String, GenModel_751_>() {
    override fun map(input: String): GenModel_751_ {
        val parts = input.split(":")
        return GenModel_751_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_751_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_751_,
    private val saveUseCase: GenSaveUseCase_751_,
    private val deleteUseCase: GenDeleteUseCase_751_,
    private val searchUseCase: GenSearchUseCase_751_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_751_>(GenState_751_.Idle)
    val state: StateFlow<GenState_751_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_751_) {
        when (event) {
            is GenEvent_751_.Load -> loadAll()
            is GenEvent_751_.Update -> save(event.model)
            is GenEvent_751_.Delete -> delete(event.id)
            is GenEvent_751_.Refresh -> loadAll()
            is GenEvent_751_.Search -> search(event.query)
            is GenEvent_751_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_751_.Loading; _state.value = GenState_751_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_751_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_751_.Success(searchUseCase(query)) } }
}
