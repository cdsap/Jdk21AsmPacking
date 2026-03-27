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

data class GenModel_860_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_860_ {
    data class Load(val id: Long) : GenEvent_860_()
    data class Update(val model: GenModel_860_) : GenEvent_860_()
    data class Delete(val id: Long) : GenEvent_860_()
    data object Refresh : GenEvent_860_()
    data class Search(val query: String) : GenEvent_860_()
    data class Filter(val predicate: String) : GenEvent_860_()
}

sealed class GenState_860_ {
    data object Idle : GenState_860_()
    data object Loading : GenState_860_()
    data class Success(val items: List<GenModel_860_>) : GenState_860_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_860_()
    data class Partial(val items: List<GenModel_860_>, val hasMore: Boolean) : GenState_860_()
}

interface GenRepository_860_ {
    suspend fun getAll(): List<GenModel_860_>
    suspend fun getById(id: Long): GenModel_860_?
    suspend fun save(model: GenModel_860_): GenModel_860_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_860_>
}

@Singleton
class GenRepositoryImpl_860_ @Inject constructor() : GenRepository_860_ {
    private val store = mutableMapOf<Long, GenModel_860_>()
    override suspend fun getAll(): List<GenModel_860_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_860_? = store[id]
    override suspend fun save(model: GenModel_860_): GenModel_860_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_860_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_860_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_860_ @Inject constructor(
    private val repository: GenRepositoryImpl_860_
) : GenUseCase_860_<Unit, List<GenModel_860_>> {
    override suspend fun invoke(params: Unit): List<GenModel_860_> = repository.getAll()
}

class GenSaveUseCase_860_ @Inject constructor(
    private val repository: GenRepositoryImpl_860_
) : GenUseCase_860_<GenModel_860_, GenModel_860_> {
    override suspend fun invoke(params: GenModel_860_): GenModel_860_ = repository.save(params)
}

class GenDeleteUseCase_860_ @Inject constructor(
    private val repository: GenRepositoryImpl_860_
) : GenUseCase_860_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_860_ @Inject constructor(
    private val repository: GenRepositoryImpl_860_
) : GenUseCase_860_<String, List<GenModel_860_>> {
    override suspend fun invoke(params: String): List<GenModel_860_> = repository.search(params)
}

abstract class GenMapper_860_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_860_ : GenMapper_860_<GenModel_860_, String>() {
    override fun map(input: GenModel_860_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_860_ : GenMapper_860_<String, GenModel_860_>() {
    override fun map(input: String): GenModel_860_ {
        val parts = input.split(":")
        return GenModel_860_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_860_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_860_,
    private val saveUseCase: GenSaveUseCase_860_,
    private val deleteUseCase: GenDeleteUseCase_860_,
    private val searchUseCase: GenSearchUseCase_860_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_860_>(GenState_860_.Idle)
    val state: StateFlow<GenState_860_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_860_) {
        when (event) {
            is GenEvent_860_.Load -> loadAll()
            is GenEvent_860_.Update -> save(event.model)
            is GenEvent_860_.Delete -> delete(event.id)
            is GenEvent_860_.Refresh -> loadAll()
            is GenEvent_860_.Search -> search(event.query)
            is GenEvent_860_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_860_.Loading; _state.value = GenState_860_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_860_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_860_.Success(searchUseCase(query)) } }
}
