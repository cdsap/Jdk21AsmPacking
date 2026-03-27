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

data class GenModel_1515_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1515_ {
    data class Load(val id: Long) : GenEvent_1515_()
    data class Update(val model: GenModel_1515_) : GenEvent_1515_()
    data class Delete(val id: Long) : GenEvent_1515_()
    data object Refresh : GenEvent_1515_()
    data class Search(val query: String) : GenEvent_1515_()
    data class Filter(val predicate: String) : GenEvent_1515_()
}

sealed class GenState_1515_ {
    data object Idle : GenState_1515_()
    data object Loading : GenState_1515_()
    data class Success(val items: List<GenModel_1515_>) : GenState_1515_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1515_()
    data class Partial(val items: List<GenModel_1515_>, val hasMore: Boolean) : GenState_1515_()
}

interface GenRepository_1515_ {
    suspend fun getAll(): List<GenModel_1515_>
    suspend fun getById(id: Long): GenModel_1515_?
    suspend fun save(model: GenModel_1515_): GenModel_1515_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1515_>
}

@Singleton
class GenRepositoryImpl_1515_ @Inject constructor() : GenRepository_1515_ {
    private val store = mutableMapOf<Long, GenModel_1515_>()
    override suspend fun getAll(): List<GenModel_1515_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1515_? = store[id]
    override suspend fun save(model: GenModel_1515_): GenModel_1515_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1515_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1515_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1515_ @Inject constructor(
    private val repository: GenRepositoryImpl_1515_
) : GenUseCase_1515_<Unit, List<GenModel_1515_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1515_> = repository.getAll()
}

class GenSaveUseCase_1515_ @Inject constructor(
    private val repository: GenRepositoryImpl_1515_
) : GenUseCase_1515_<GenModel_1515_, GenModel_1515_> {
    override suspend fun invoke(params: GenModel_1515_): GenModel_1515_ = repository.save(params)
}

class GenDeleteUseCase_1515_ @Inject constructor(
    private val repository: GenRepositoryImpl_1515_
) : GenUseCase_1515_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1515_ @Inject constructor(
    private val repository: GenRepositoryImpl_1515_
) : GenUseCase_1515_<String, List<GenModel_1515_>> {
    override suspend fun invoke(params: String): List<GenModel_1515_> = repository.search(params)
}

abstract class GenMapper_1515_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1515_ : GenMapper_1515_<GenModel_1515_, String>() {
    override fun map(input: GenModel_1515_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1515_ : GenMapper_1515_<String, GenModel_1515_>() {
    override fun map(input: String): GenModel_1515_ {
        val parts = input.split(":")
        return GenModel_1515_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1515_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1515_,
    private val saveUseCase: GenSaveUseCase_1515_,
    private val deleteUseCase: GenDeleteUseCase_1515_,
    private val searchUseCase: GenSearchUseCase_1515_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1515_>(GenState_1515_.Idle)
    val state: StateFlow<GenState_1515_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1515_) {
        when (event) {
            is GenEvent_1515_.Load -> loadAll()
            is GenEvent_1515_.Update -> save(event.model)
            is GenEvent_1515_.Delete -> delete(event.id)
            is GenEvent_1515_.Refresh -> loadAll()
            is GenEvent_1515_.Search -> search(event.query)
            is GenEvent_1515_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1515_.Loading; _state.value = GenState_1515_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1515_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1515_.Success(searchUseCase(query)) } }
}
