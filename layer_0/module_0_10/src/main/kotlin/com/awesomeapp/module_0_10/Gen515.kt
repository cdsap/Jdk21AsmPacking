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

data class GenModel_515_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_515_ {
    data class Load(val id: Long) : GenEvent_515_()
    data class Update(val model: GenModel_515_) : GenEvent_515_()
    data class Delete(val id: Long) : GenEvent_515_()
    data object Refresh : GenEvent_515_()
    data class Search(val query: String) : GenEvent_515_()
    data class Filter(val predicate: String) : GenEvent_515_()
}

sealed class GenState_515_ {
    data object Idle : GenState_515_()
    data object Loading : GenState_515_()
    data class Success(val items: List<GenModel_515_>) : GenState_515_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_515_()
    data class Partial(val items: List<GenModel_515_>, val hasMore: Boolean) : GenState_515_()
}

interface GenRepository_515_ {
    suspend fun getAll(): List<GenModel_515_>
    suspend fun getById(id: Long): GenModel_515_?
    suspend fun save(model: GenModel_515_): GenModel_515_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_515_>
}

@Singleton
class GenRepositoryImpl_515_ @Inject constructor() : GenRepository_515_ {
    private val store = mutableMapOf<Long, GenModel_515_>()
    override suspend fun getAll(): List<GenModel_515_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_515_? = store[id]
    override suspend fun save(model: GenModel_515_): GenModel_515_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_515_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_515_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_515_ @Inject constructor(
    private val repository: GenRepositoryImpl_515_
) : GenUseCase_515_<Unit, List<GenModel_515_>> {
    override suspend fun invoke(params: Unit): List<GenModel_515_> = repository.getAll()
}

class GenSaveUseCase_515_ @Inject constructor(
    private val repository: GenRepositoryImpl_515_
) : GenUseCase_515_<GenModel_515_, GenModel_515_> {
    override suspend fun invoke(params: GenModel_515_): GenModel_515_ = repository.save(params)
}

class GenDeleteUseCase_515_ @Inject constructor(
    private val repository: GenRepositoryImpl_515_
) : GenUseCase_515_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_515_ @Inject constructor(
    private val repository: GenRepositoryImpl_515_
) : GenUseCase_515_<String, List<GenModel_515_>> {
    override suspend fun invoke(params: String): List<GenModel_515_> = repository.search(params)
}

abstract class GenMapper_515_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_515_ : GenMapper_515_<GenModel_515_, String>() {
    override fun map(input: GenModel_515_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_515_ : GenMapper_515_<String, GenModel_515_>() {
    override fun map(input: String): GenModel_515_ {
        val parts = input.split(":")
        return GenModel_515_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_515_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_515_,
    private val saveUseCase: GenSaveUseCase_515_,
    private val deleteUseCase: GenDeleteUseCase_515_,
    private val searchUseCase: GenSearchUseCase_515_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_515_>(GenState_515_.Idle)
    val state: StateFlow<GenState_515_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_515_) {
        when (event) {
            is GenEvent_515_.Load -> loadAll()
            is GenEvent_515_.Update -> save(event.model)
            is GenEvent_515_.Delete -> delete(event.id)
            is GenEvent_515_.Refresh -> loadAll()
            is GenEvent_515_.Search -> search(event.query)
            is GenEvent_515_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_515_.Loading; _state.value = GenState_515_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_515_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_515_.Success(searchUseCase(query)) } }
}
