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

data class GenModel_1641_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1641_ {
    data class Load(val id: Long) : GenEvent_1641_()
    data class Update(val model: GenModel_1641_) : GenEvent_1641_()
    data class Delete(val id: Long) : GenEvent_1641_()
    data object Refresh : GenEvent_1641_()
    data class Search(val query: String) : GenEvent_1641_()
    data class Filter(val predicate: String) : GenEvent_1641_()
}

sealed class GenState_1641_ {
    data object Idle : GenState_1641_()
    data object Loading : GenState_1641_()
    data class Success(val items: List<GenModel_1641_>) : GenState_1641_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1641_()
    data class Partial(val items: List<GenModel_1641_>, val hasMore: Boolean) : GenState_1641_()
}

interface GenRepository_1641_ {
    suspend fun getAll(): List<GenModel_1641_>
    suspend fun getById(id: Long): GenModel_1641_?
    suspend fun save(model: GenModel_1641_): GenModel_1641_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1641_>
}

@Singleton
class GenRepositoryImpl_1641_ @Inject constructor() : GenRepository_1641_ {
    private val store = mutableMapOf<Long, GenModel_1641_>()
    override suspend fun getAll(): List<GenModel_1641_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1641_? = store[id]
    override suspend fun save(model: GenModel_1641_): GenModel_1641_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1641_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1641_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1641_ @Inject constructor(
    private val repository: GenRepositoryImpl_1641_
) : GenUseCase_1641_<Unit, List<GenModel_1641_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1641_> = repository.getAll()
}

class GenSaveUseCase_1641_ @Inject constructor(
    private val repository: GenRepositoryImpl_1641_
) : GenUseCase_1641_<GenModel_1641_, GenModel_1641_> {
    override suspend fun invoke(params: GenModel_1641_): GenModel_1641_ = repository.save(params)
}

class GenDeleteUseCase_1641_ @Inject constructor(
    private val repository: GenRepositoryImpl_1641_
) : GenUseCase_1641_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1641_ @Inject constructor(
    private val repository: GenRepositoryImpl_1641_
) : GenUseCase_1641_<String, List<GenModel_1641_>> {
    override suspend fun invoke(params: String): List<GenModel_1641_> = repository.search(params)
}

abstract class GenMapper_1641_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1641_ : GenMapper_1641_<GenModel_1641_, String>() {
    override fun map(input: GenModel_1641_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1641_ : GenMapper_1641_<String, GenModel_1641_>() {
    override fun map(input: String): GenModel_1641_ {
        val parts = input.split(":")
        return GenModel_1641_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1641_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1641_,
    private val saveUseCase: GenSaveUseCase_1641_,
    private val deleteUseCase: GenDeleteUseCase_1641_,
    private val searchUseCase: GenSearchUseCase_1641_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1641_>(GenState_1641_.Idle)
    val state: StateFlow<GenState_1641_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1641_) {
        when (event) {
            is GenEvent_1641_.Load -> loadAll()
            is GenEvent_1641_.Update -> save(event.model)
            is GenEvent_1641_.Delete -> delete(event.id)
            is GenEvent_1641_.Refresh -> loadAll()
            is GenEvent_1641_.Search -> search(event.query)
            is GenEvent_1641_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1641_.Loading; _state.value = GenState_1641_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1641_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1641_.Success(searchUseCase(query)) } }
}
