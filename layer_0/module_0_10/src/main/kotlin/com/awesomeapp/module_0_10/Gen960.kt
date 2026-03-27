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

data class GenModel_960_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_960_ {
    data class Load(val id: Long) : GenEvent_960_()
    data class Update(val model: GenModel_960_) : GenEvent_960_()
    data class Delete(val id: Long) : GenEvent_960_()
    data object Refresh : GenEvent_960_()
    data class Search(val query: String) : GenEvent_960_()
    data class Filter(val predicate: String) : GenEvent_960_()
}

sealed class GenState_960_ {
    data object Idle : GenState_960_()
    data object Loading : GenState_960_()
    data class Success(val items: List<GenModel_960_>) : GenState_960_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_960_()
    data class Partial(val items: List<GenModel_960_>, val hasMore: Boolean) : GenState_960_()
}

interface GenRepository_960_ {
    suspend fun getAll(): List<GenModel_960_>
    suspend fun getById(id: Long): GenModel_960_?
    suspend fun save(model: GenModel_960_): GenModel_960_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_960_>
}

@Singleton
class GenRepositoryImpl_960_ @Inject constructor() : GenRepository_960_ {
    private val store = mutableMapOf<Long, GenModel_960_>()
    override suspend fun getAll(): List<GenModel_960_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_960_? = store[id]
    override suspend fun save(model: GenModel_960_): GenModel_960_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_960_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_960_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_960_ @Inject constructor(
    private val repository: GenRepositoryImpl_960_
) : GenUseCase_960_<Unit, List<GenModel_960_>> {
    override suspend fun invoke(params: Unit): List<GenModel_960_> = repository.getAll()
}

class GenSaveUseCase_960_ @Inject constructor(
    private val repository: GenRepositoryImpl_960_
) : GenUseCase_960_<GenModel_960_, GenModel_960_> {
    override suspend fun invoke(params: GenModel_960_): GenModel_960_ = repository.save(params)
}

class GenDeleteUseCase_960_ @Inject constructor(
    private val repository: GenRepositoryImpl_960_
) : GenUseCase_960_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_960_ @Inject constructor(
    private val repository: GenRepositoryImpl_960_
) : GenUseCase_960_<String, List<GenModel_960_>> {
    override suspend fun invoke(params: String): List<GenModel_960_> = repository.search(params)
}

abstract class GenMapper_960_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_960_ : GenMapper_960_<GenModel_960_, String>() {
    override fun map(input: GenModel_960_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_960_ : GenMapper_960_<String, GenModel_960_>() {
    override fun map(input: String): GenModel_960_ {
        val parts = input.split(":")
        return GenModel_960_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_960_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_960_,
    private val saveUseCase: GenSaveUseCase_960_,
    private val deleteUseCase: GenDeleteUseCase_960_,
    private val searchUseCase: GenSearchUseCase_960_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_960_>(GenState_960_.Idle)
    val state: StateFlow<GenState_960_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_960_) {
        when (event) {
            is GenEvent_960_.Load -> loadAll()
            is GenEvent_960_.Update -> save(event.model)
            is GenEvent_960_.Delete -> delete(event.id)
            is GenEvent_960_.Refresh -> loadAll()
            is GenEvent_960_.Search -> search(event.query)
            is GenEvent_960_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_960_.Loading; _state.value = GenState_960_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_960_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_960_.Success(searchUseCase(query)) } }
}
