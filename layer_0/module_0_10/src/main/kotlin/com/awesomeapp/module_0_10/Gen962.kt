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

data class GenModel_962_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_962_ {
    data class Load(val id: Long) : GenEvent_962_()
    data class Update(val model: GenModel_962_) : GenEvent_962_()
    data class Delete(val id: Long) : GenEvent_962_()
    data object Refresh : GenEvent_962_()
    data class Search(val query: String) : GenEvent_962_()
    data class Filter(val predicate: String) : GenEvent_962_()
}

sealed class GenState_962_ {
    data object Idle : GenState_962_()
    data object Loading : GenState_962_()
    data class Success(val items: List<GenModel_962_>) : GenState_962_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_962_()
    data class Partial(val items: List<GenModel_962_>, val hasMore: Boolean) : GenState_962_()
}

interface GenRepository_962_ {
    suspend fun getAll(): List<GenModel_962_>
    suspend fun getById(id: Long): GenModel_962_?
    suspend fun save(model: GenModel_962_): GenModel_962_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_962_>
}

@Singleton
class GenRepositoryImpl_962_ @Inject constructor() : GenRepository_962_ {
    private val store = mutableMapOf<Long, GenModel_962_>()
    override suspend fun getAll(): List<GenModel_962_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_962_? = store[id]
    override suspend fun save(model: GenModel_962_): GenModel_962_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_962_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_962_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_962_ @Inject constructor(
    private val repository: GenRepositoryImpl_962_
) : GenUseCase_962_<Unit, List<GenModel_962_>> {
    override suspend fun invoke(params: Unit): List<GenModel_962_> = repository.getAll()
}

class GenSaveUseCase_962_ @Inject constructor(
    private val repository: GenRepositoryImpl_962_
) : GenUseCase_962_<GenModel_962_, GenModel_962_> {
    override suspend fun invoke(params: GenModel_962_): GenModel_962_ = repository.save(params)
}

class GenDeleteUseCase_962_ @Inject constructor(
    private val repository: GenRepositoryImpl_962_
) : GenUseCase_962_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_962_ @Inject constructor(
    private val repository: GenRepositoryImpl_962_
) : GenUseCase_962_<String, List<GenModel_962_>> {
    override suspend fun invoke(params: String): List<GenModel_962_> = repository.search(params)
}

abstract class GenMapper_962_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_962_ : GenMapper_962_<GenModel_962_, String>() {
    override fun map(input: GenModel_962_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_962_ : GenMapper_962_<String, GenModel_962_>() {
    override fun map(input: String): GenModel_962_ {
        val parts = input.split(":")
        return GenModel_962_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_962_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_962_,
    private val saveUseCase: GenSaveUseCase_962_,
    private val deleteUseCase: GenDeleteUseCase_962_,
    private val searchUseCase: GenSearchUseCase_962_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_962_>(GenState_962_.Idle)
    val state: StateFlow<GenState_962_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_962_) {
        when (event) {
            is GenEvent_962_.Load -> loadAll()
            is GenEvent_962_.Update -> save(event.model)
            is GenEvent_962_.Delete -> delete(event.id)
            is GenEvent_962_.Refresh -> loadAll()
            is GenEvent_962_.Search -> search(event.query)
            is GenEvent_962_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_962_.Loading; _state.value = GenState_962_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_962_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_962_.Success(searchUseCase(query)) } }
}
