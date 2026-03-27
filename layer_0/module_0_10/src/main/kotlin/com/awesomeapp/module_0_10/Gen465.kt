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

data class GenModel_465_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_465_ {
    data class Load(val id: Long) : GenEvent_465_()
    data class Update(val model: GenModel_465_) : GenEvent_465_()
    data class Delete(val id: Long) : GenEvent_465_()
    data object Refresh : GenEvent_465_()
    data class Search(val query: String) : GenEvent_465_()
    data class Filter(val predicate: String) : GenEvent_465_()
}

sealed class GenState_465_ {
    data object Idle : GenState_465_()
    data object Loading : GenState_465_()
    data class Success(val items: List<GenModel_465_>) : GenState_465_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_465_()
    data class Partial(val items: List<GenModel_465_>, val hasMore: Boolean) : GenState_465_()
}

interface GenRepository_465_ {
    suspend fun getAll(): List<GenModel_465_>
    suspend fun getById(id: Long): GenModel_465_?
    suspend fun save(model: GenModel_465_): GenModel_465_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_465_>
}

@Singleton
class GenRepositoryImpl_465_ @Inject constructor() : GenRepository_465_ {
    private val store = mutableMapOf<Long, GenModel_465_>()
    override suspend fun getAll(): List<GenModel_465_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_465_? = store[id]
    override suspend fun save(model: GenModel_465_): GenModel_465_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_465_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_465_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_465_ @Inject constructor(
    private val repository: GenRepositoryImpl_465_
) : GenUseCase_465_<Unit, List<GenModel_465_>> {
    override suspend fun invoke(params: Unit): List<GenModel_465_> = repository.getAll()
}

class GenSaveUseCase_465_ @Inject constructor(
    private val repository: GenRepositoryImpl_465_
) : GenUseCase_465_<GenModel_465_, GenModel_465_> {
    override suspend fun invoke(params: GenModel_465_): GenModel_465_ = repository.save(params)
}

class GenDeleteUseCase_465_ @Inject constructor(
    private val repository: GenRepositoryImpl_465_
) : GenUseCase_465_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_465_ @Inject constructor(
    private val repository: GenRepositoryImpl_465_
) : GenUseCase_465_<String, List<GenModel_465_>> {
    override suspend fun invoke(params: String): List<GenModel_465_> = repository.search(params)
}

abstract class GenMapper_465_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_465_ : GenMapper_465_<GenModel_465_, String>() {
    override fun map(input: GenModel_465_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_465_ : GenMapper_465_<String, GenModel_465_>() {
    override fun map(input: String): GenModel_465_ {
        val parts = input.split(":")
        return GenModel_465_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_465_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_465_,
    private val saveUseCase: GenSaveUseCase_465_,
    private val deleteUseCase: GenDeleteUseCase_465_,
    private val searchUseCase: GenSearchUseCase_465_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_465_>(GenState_465_.Idle)
    val state: StateFlow<GenState_465_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_465_) {
        when (event) {
            is GenEvent_465_.Load -> loadAll()
            is GenEvent_465_.Update -> save(event.model)
            is GenEvent_465_.Delete -> delete(event.id)
            is GenEvent_465_.Refresh -> loadAll()
            is GenEvent_465_.Search -> search(event.query)
            is GenEvent_465_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_465_.Loading; _state.value = GenState_465_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_465_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_465_.Success(searchUseCase(query)) } }
}
