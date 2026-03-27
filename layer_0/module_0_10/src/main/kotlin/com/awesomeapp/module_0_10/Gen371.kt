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

data class GenModel_371_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_371_ {
    data class Load(val id: Long) : GenEvent_371_()
    data class Update(val model: GenModel_371_) : GenEvent_371_()
    data class Delete(val id: Long) : GenEvent_371_()
    data object Refresh : GenEvent_371_()
    data class Search(val query: String) : GenEvent_371_()
    data class Filter(val predicate: String) : GenEvent_371_()
}

sealed class GenState_371_ {
    data object Idle : GenState_371_()
    data object Loading : GenState_371_()
    data class Success(val items: List<GenModel_371_>) : GenState_371_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_371_()
    data class Partial(val items: List<GenModel_371_>, val hasMore: Boolean) : GenState_371_()
}

interface GenRepository_371_ {
    suspend fun getAll(): List<GenModel_371_>
    suspend fun getById(id: Long): GenModel_371_?
    suspend fun save(model: GenModel_371_): GenModel_371_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_371_>
}

@Singleton
class GenRepositoryImpl_371_ @Inject constructor() : GenRepository_371_ {
    private val store = mutableMapOf<Long, GenModel_371_>()
    override suspend fun getAll(): List<GenModel_371_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_371_? = store[id]
    override suspend fun save(model: GenModel_371_): GenModel_371_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_371_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_371_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_371_ @Inject constructor(
    private val repository: GenRepositoryImpl_371_
) : GenUseCase_371_<Unit, List<GenModel_371_>> {
    override suspend fun invoke(params: Unit): List<GenModel_371_> = repository.getAll()
}

class GenSaveUseCase_371_ @Inject constructor(
    private val repository: GenRepositoryImpl_371_
) : GenUseCase_371_<GenModel_371_, GenModel_371_> {
    override suspend fun invoke(params: GenModel_371_): GenModel_371_ = repository.save(params)
}

class GenDeleteUseCase_371_ @Inject constructor(
    private val repository: GenRepositoryImpl_371_
) : GenUseCase_371_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_371_ @Inject constructor(
    private val repository: GenRepositoryImpl_371_
) : GenUseCase_371_<String, List<GenModel_371_>> {
    override suspend fun invoke(params: String): List<GenModel_371_> = repository.search(params)
}

abstract class GenMapper_371_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_371_ : GenMapper_371_<GenModel_371_, String>() {
    override fun map(input: GenModel_371_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_371_ : GenMapper_371_<String, GenModel_371_>() {
    override fun map(input: String): GenModel_371_ {
        val parts = input.split(":")
        return GenModel_371_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_371_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_371_,
    private val saveUseCase: GenSaveUseCase_371_,
    private val deleteUseCase: GenDeleteUseCase_371_,
    private val searchUseCase: GenSearchUseCase_371_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_371_>(GenState_371_.Idle)
    val state: StateFlow<GenState_371_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_371_) {
        when (event) {
            is GenEvent_371_.Load -> loadAll()
            is GenEvent_371_.Update -> save(event.model)
            is GenEvent_371_.Delete -> delete(event.id)
            is GenEvent_371_.Refresh -> loadAll()
            is GenEvent_371_.Search -> search(event.query)
            is GenEvent_371_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_371_.Loading; _state.value = GenState_371_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_371_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_371_.Success(searchUseCase(query)) } }
}
