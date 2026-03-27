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

data class GenModel_748_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_748_ {
    data class Load(val id: Long) : GenEvent_748_()
    data class Update(val model: GenModel_748_) : GenEvent_748_()
    data class Delete(val id: Long) : GenEvent_748_()
    data object Refresh : GenEvent_748_()
    data class Search(val query: String) : GenEvent_748_()
    data class Filter(val predicate: String) : GenEvent_748_()
}

sealed class GenState_748_ {
    data object Idle : GenState_748_()
    data object Loading : GenState_748_()
    data class Success(val items: List<GenModel_748_>) : GenState_748_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_748_()
    data class Partial(val items: List<GenModel_748_>, val hasMore: Boolean) : GenState_748_()
}

interface GenRepository_748_ {
    suspend fun getAll(): List<GenModel_748_>
    suspend fun getById(id: Long): GenModel_748_?
    suspend fun save(model: GenModel_748_): GenModel_748_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_748_>
}

@Singleton
class GenRepositoryImpl_748_ @Inject constructor() : GenRepository_748_ {
    private val store = mutableMapOf<Long, GenModel_748_>()
    override suspend fun getAll(): List<GenModel_748_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_748_? = store[id]
    override suspend fun save(model: GenModel_748_): GenModel_748_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_748_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_748_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_748_ @Inject constructor(
    private val repository: GenRepositoryImpl_748_
) : GenUseCase_748_<Unit, List<GenModel_748_>> {
    override suspend fun invoke(params: Unit): List<GenModel_748_> = repository.getAll()
}

class GenSaveUseCase_748_ @Inject constructor(
    private val repository: GenRepositoryImpl_748_
) : GenUseCase_748_<GenModel_748_, GenModel_748_> {
    override suspend fun invoke(params: GenModel_748_): GenModel_748_ = repository.save(params)
}

class GenDeleteUseCase_748_ @Inject constructor(
    private val repository: GenRepositoryImpl_748_
) : GenUseCase_748_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_748_ @Inject constructor(
    private val repository: GenRepositoryImpl_748_
) : GenUseCase_748_<String, List<GenModel_748_>> {
    override suspend fun invoke(params: String): List<GenModel_748_> = repository.search(params)
}

abstract class GenMapper_748_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_748_ : GenMapper_748_<GenModel_748_, String>() {
    override fun map(input: GenModel_748_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_748_ : GenMapper_748_<String, GenModel_748_>() {
    override fun map(input: String): GenModel_748_ {
        val parts = input.split(":")
        return GenModel_748_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_748_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_748_,
    private val saveUseCase: GenSaveUseCase_748_,
    private val deleteUseCase: GenDeleteUseCase_748_,
    private val searchUseCase: GenSearchUseCase_748_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_748_>(GenState_748_.Idle)
    val state: StateFlow<GenState_748_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_748_) {
        when (event) {
            is GenEvent_748_.Load -> loadAll()
            is GenEvent_748_.Update -> save(event.model)
            is GenEvent_748_.Delete -> delete(event.id)
            is GenEvent_748_.Refresh -> loadAll()
            is GenEvent_748_.Search -> search(event.query)
            is GenEvent_748_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_748_.Loading; _state.value = GenState_748_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_748_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_748_.Success(searchUseCase(query)) } }
}
