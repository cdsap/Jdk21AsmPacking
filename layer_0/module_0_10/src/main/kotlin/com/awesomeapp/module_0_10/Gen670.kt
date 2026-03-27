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

data class GenModel_670_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_670_ {
    data class Load(val id: Long) : GenEvent_670_()
    data class Update(val model: GenModel_670_) : GenEvent_670_()
    data class Delete(val id: Long) : GenEvent_670_()
    data object Refresh : GenEvent_670_()
    data class Search(val query: String) : GenEvent_670_()
    data class Filter(val predicate: String) : GenEvent_670_()
}

sealed class GenState_670_ {
    data object Idle : GenState_670_()
    data object Loading : GenState_670_()
    data class Success(val items: List<GenModel_670_>) : GenState_670_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_670_()
    data class Partial(val items: List<GenModel_670_>, val hasMore: Boolean) : GenState_670_()
}

interface GenRepository_670_ {
    suspend fun getAll(): List<GenModel_670_>
    suspend fun getById(id: Long): GenModel_670_?
    suspend fun save(model: GenModel_670_): GenModel_670_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_670_>
}

@Singleton
class GenRepositoryImpl_670_ @Inject constructor() : GenRepository_670_ {
    private val store = mutableMapOf<Long, GenModel_670_>()
    override suspend fun getAll(): List<GenModel_670_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_670_? = store[id]
    override suspend fun save(model: GenModel_670_): GenModel_670_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_670_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_670_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_670_ @Inject constructor(
    private val repository: GenRepositoryImpl_670_
) : GenUseCase_670_<Unit, List<GenModel_670_>> {
    override suspend fun invoke(params: Unit): List<GenModel_670_> = repository.getAll()
}

class GenSaveUseCase_670_ @Inject constructor(
    private val repository: GenRepositoryImpl_670_
) : GenUseCase_670_<GenModel_670_, GenModel_670_> {
    override suspend fun invoke(params: GenModel_670_): GenModel_670_ = repository.save(params)
}

class GenDeleteUseCase_670_ @Inject constructor(
    private val repository: GenRepositoryImpl_670_
) : GenUseCase_670_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_670_ @Inject constructor(
    private val repository: GenRepositoryImpl_670_
) : GenUseCase_670_<String, List<GenModel_670_>> {
    override suspend fun invoke(params: String): List<GenModel_670_> = repository.search(params)
}

abstract class GenMapper_670_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_670_ : GenMapper_670_<GenModel_670_, String>() {
    override fun map(input: GenModel_670_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_670_ : GenMapper_670_<String, GenModel_670_>() {
    override fun map(input: String): GenModel_670_ {
        val parts = input.split(":")
        return GenModel_670_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_670_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_670_,
    private val saveUseCase: GenSaveUseCase_670_,
    private val deleteUseCase: GenDeleteUseCase_670_,
    private val searchUseCase: GenSearchUseCase_670_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_670_>(GenState_670_.Idle)
    val state: StateFlow<GenState_670_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_670_) {
        when (event) {
            is GenEvent_670_.Load -> loadAll()
            is GenEvent_670_.Update -> save(event.model)
            is GenEvent_670_.Delete -> delete(event.id)
            is GenEvent_670_.Refresh -> loadAll()
            is GenEvent_670_.Search -> search(event.query)
            is GenEvent_670_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_670_.Loading; _state.value = GenState_670_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_670_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_670_.Success(searchUseCase(query)) } }
}
