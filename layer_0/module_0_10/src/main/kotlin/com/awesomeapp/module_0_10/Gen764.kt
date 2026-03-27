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

data class GenModel_764_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_764_ {
    data class Load(val id: Long) : GenEvent_764_()
    data class Update(val model: GenModel_764_) : GenEvent_764_()
    data class Delete(val id: Long) : GenEvent_764_()
    data object Refresh : GenEvent_764_()
    data class Search(val query: String) : GenEvent_764_()
    data class Filter(val predicate: String) : GenEvent_764_()
}

sealed class GenState_764_ {
    data object Idle : GenState_764_()
    data object Loading : GenState_764_()
    data class Success(val items: List<GenModel_764_>) : GenState_764_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_764_()
    data class Partial(val items: List<GenModel_764_>, val hasMore: Boolean) : GenState_764_()
}

interface GenRepository_764_ {
    suspend fun getAll(): List<GenModel_764_>
    suspend fun getById(id: Long): GenModel_764_?
    suspend fun save(model: GenModel_764_): GenModel_764_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_764_>
}

@Singleton
class GenRepositoryImpl_764_ @Inject constructor() : GenRepository_764_ {
    private val store = mutableMapOf<Long, GenModel_764_>()
    override suspend fun getAll(): List<GenModel_764_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_764_? = store[id]
    override suspend fun save(model: GenModel_764_): GenModel_764_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_764_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_764_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_764_ @Inject constructor(
    private val repository: GenRepositoryImpl_764_
) : GenUseCase_764_<Unit, List<GenModel_764_>> {
    override suspend fun invoke(params: Unit): List<GenModel_764_> = repository.getAll()
}

class GenSaveUseCase_764_ @Inject constructor(
    private val repository: GenRepositoryImpl_764_
) : GenUseCase_764_<GenModel_764_, GenModel_764_> {
    override suspend fun invoke(params: GenModel_764_): GenModel_764_ = repository.save(params)
}

class GenDeleteUseCase_764_ @Inject constructor(
    private val repository: GenRepositoryImpl_764_
) : GenUseCase_764_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_764_ @Inject constructor(
    private val repository: GenRepositoryImpl_764_
) : GenUseCase_764_<String, List<GenModel_764_>> {
    override suspend fun invoke(params: String): List<GenModel_764_> = repository.search(params)
}

abstract class GenMapper_764_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_764_ : GenMapper_764_<GenModel_764_, String>() {
    override fun map(input: GenModel_764_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_764_ : GenMapper_764_<String, GenModel_764_>() {
    override fun map(input: String): GenModel_764_ {
        val parts = input.split(":")
        return GenModel_764_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_764_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_764_,
    private val saveUseCase: GenSaveUseCase_764_,
    private val deleteUseCase: GenDeleteUseCase_764_,
    private val searchUseCase: GenSearchUseCase_764_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_764_>(GenState_764_.Idle)
    val state: StateFlow<GenState_764_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_764_) {
        when (event) {
            is GenEvent_764_.Load -> loadAll()
            is GenEvent_764_.Update -> save(event.model)
            is GenEvent_764_.Delete -> delete(event.id)
            is GenEvent_764_.Refresh -> loadAll()
            is GenEvent_764_.Search -> search(event.query)
            is GenEvent_764_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_764_.Loading; _state.value = GenState_764_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_764_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_764_.Success(searchUseCase(query)) } }
}
