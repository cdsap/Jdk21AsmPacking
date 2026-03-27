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

data class GenModel_1371_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1371_ {
    data class Load(val id: Long) : GenEvent_1371_()
    data class Update(val model: GenModel_1371_) : GenEvent_1371_()
    data class Delete(val id: Long) : GenEvent_1371_()
    data object Refresh : GenEvent_1371_()
    data class Search(val query: String) : GenEvent_1371_()
    data class Filter(val predicate: String) : GenEvent_1371_()
}

sealed class GenState_1371_ {
    data object Idle : GenState_1371_()
    data object Loading : GenState_1371_()
    data class Success(val items: List<GenModel_1371_>) : GenState_1371_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1371_()
    data class Partial(val items: List<GenModel_1371_>, val hasMore: Boolean) : GenState_1371_()
}

interface GenRepository_1371_ {
    suspend fun getAll(): List<GenModel_1371_>
    suspend fun getById(id: Long): GenModel_1371_?
    suspend fun save(model: GenModel_1371_): GenModel_1371_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1371_>
}

@Singleton
class GenRepositoryImpl_1371_ @Inject constructor() : GenRepository_1371_ {
    private val store = mutableMapOf<Long, GenModel_1371_>()
    override suspend fun getAll(): List<GenModel_1371_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1371_? = store[id]
    override suspend fun save(model: GenModel_1371_): GenModel_1371_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1371_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1371_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1371_ @Inject constructor(
    private val repository: GenRepositoryImpl_1371_
) : GenUseCase_1371_<Unit, List<GenModel_1371_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1371_> = repository.getAll()
}

class GenSaveUseCase_1371_ @Inject constructor(
    private val repository: GenRepositoryImpl_1371_
) : GenUseCase_1371_<GenModel_1371_, GenModel_1371_> {
    override suspend fun invoke(params: GenModel_1371_): GenModel_1371_ = repository.save(params)
}

class GenDeleteUseCase_1371_ @Inject constructor(
    private val repository: GenRepositoryImpl_1371_
) : GenUseCase_1371_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1371_ @Inject constructor(
    private val repository: GenRepositoryImpl_1371_
) : GenUseCase_1371_<String, List<GenModel_1371_>> {
    override suspend fun invoke(params: String): List<GenModel_1371_> = repository.search(params)
}

abstract class GenMapper_1371_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1371_ : GenMapper_1371_<GenModel_1371_, String>() {
    override fun map(input: GenModel_1371_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1371_ : GenMapper_1371_<String, GenModel_1371_>() {
    override fun map(input: String): GenModel_1371_ {
        val parts = input.split(":")
        return GenModel_1371_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1371_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1371_,
    private val saveUseCase: GenSaveUseCase_1371_,
    private val deleteUseCase: GenDeleteUseCase_1371_,
    private val searchUseCase: GenSearchUseCase_1371_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1371_>(GenState_1371_.Idle)
    val state: StateFlow<GenState_1371_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1371_) {
        when (event) {
            is GenEvent_1371_.Load -> loadAll()
            is GenEvent_1371_.Update -> save(event.model)
            is GenEvent_1371_.Delete -> delete(event.id)
            is GenEvent_1371_.Refresh -> loadAll()
            is GenEvent_1371_.Search -> search(event.query)
            is GenEvent_1371_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1371_.Loading; _state.value = GenState_1371_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1371_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1371_.Success(searchUseCase(query)) } }
}
