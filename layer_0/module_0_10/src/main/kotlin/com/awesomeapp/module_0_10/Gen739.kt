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

data class GenModel_739_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_739_ {
    data class Load(val id: Long) : GenEvent_739_()
    data class Update(val model: GenModel_739_) : GenEvent_739_()
    data class Delete(val id: Long) : GenEvent_739_()
    data object Refresh : GenEvent_739_()
    data class Search(val query: String) : GenEvent_739_()
    data class Filter(val predicate: String) : GenEvent_739_()
}

sealed class GenState_739_ {
    data object Idle : GenState_739_()
    data object Loading : GenState_739_()
    data class Success(val items: List<GenModel_739_>) : GenState_739_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_739_()
    data class Partial(val items: List<GenModel_739_>, val hasMore: Boolean) : GenState_739_()
}

interface GenRepository_739_ {
    suspend fun getAll(): List<GenModel_739_>
    suspend fun getById(id: Long): GenModel_739_?
    suspend fun save(model: GenModel_739_): GenModel_739_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_739_>
}

@Singleton
class GenRepositoryImpl_739_ @Inject constructor() : GenRepository_739_ {
    private val store = mutableMapOf<Long, GenModel_739_>()
    override suspend fun getAll(): List<GenModel_739_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_739_? = store[id]
    override suspend fun save(model: GenModel_739_): GenModel_739_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_739_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_739_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_739_ @Inject constructor(
    private val repository: GenRepositoryImpl_739_
) : GenUseCase_739_<Unit, List<GenModel_739_>> {
    override suspend fun invoke(params: Unit): List<GenModel_739_> = repository.getAll()
}

class GenSaveUseCase_739_ @Inject constructor(
    private val repository: GenRepositoryImpl_739_
) : GenUseCase_739_<GenModel_739_, GenModel_739_> {
    override suspend fun invoke(params: GenModel_739_): GenModel_739_ = repository.save(params)
}

class GenDeleteUseCase_739_ @Inject constructor(
    private val repository: GenRepositoryImpl_739_
) : GenUseCase_739_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_739_ @Inject constructor(
    private val repository: GenRepositoryImpl_739_
) : GenUseCase_739_<String, List<GenModel_739_>> {
    override suspend fun invoke(params: String): List<GenModel_739_> = repository.search(params)
}

abstract class GenMapper_739_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_739_ : GenMapper_739_<GenModel_739_, String>() {
    override fun map(input: GenModel_739_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_739_ : GenMapper_739_<String, GenModel_739_>() {
    override fun map(input: String): GenModel_739_ {
        val parts = input.split(":")
        return GenModel_739_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_739_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_739_,
    private val saveUseCase: GenSaveUseCase_739_,
    private val deleteUseCase: GenDeleteUseCase_739_,
    private val searchUseCase: GenSearchUseCase_739_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_739_>(GenState_739_.Idle)
    val state: StateFlow<GenState_739_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_739_) {
        when (event) {
            is GenEvent_739_.Load -> loadAll()
            is GenEvent_739_.Update -> save(event.model)
            is GenEvent_739_.Delete -> delete(event.id)
            is GenEvent_739_.Refresh -> loadAll()
            is GenEvent_739_.Search -> search(event.query)
            is GenEvent_739_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_739_.Loading; _state.value = GenState_739_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_739_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_739_.Success(searchUseCase(query)) } }
}
