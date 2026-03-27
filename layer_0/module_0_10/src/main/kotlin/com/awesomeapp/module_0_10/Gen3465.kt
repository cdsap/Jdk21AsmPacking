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

data class GenModel_3465_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3465_ {
    data class Load(val id: Long) : GenEvent_3465_()
    data class Update(val model: GenModel_3465_) : GenEvent_3465_()
    data class Delete(val id: Long) : GenEvent_3465_()
    data object Refresh : GenEvent_3465_()
    data class Search(val query: String) : GenEvent_3465_()
    data class Filter(val predicate: String) : GenEvent_3465_()
}

sealed class GenState_3465_ {
    data object Idle : GenState_3465_()
    data object Loading : GenState_3465_()
    data class Success(val items: List<GenModel_3465_>) : GenState_3465_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3465_()
    data class Partial(val items: List<GenModel_3465_>, val hasMore: Boolean) : GenState_3465_()
}

interface GenRepository_3465_ {
    suspend fun getAll(): List<GenModel_3465_>
    suspend fun getById(id: Long): GenModel_3465_?
    suspend fun save(model: GenModel_3465_): GenModel_3465_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3465_>
}

@Singleton
class GenRepositoryImpl_3465_ @Inject constructor() : GenRepository_3465_ {
    private val store = mutableMapOf<Long, GenModel_3465_>()
    override suspend fun getAll(): List<GenModel_3465_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3465_? = store[id]
    override suspend fun save(model: GenModel_3465_): GenModel_3465_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3465_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3465_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3465_ @Inject constructor(
    private val repository: GenRepositoryImpl_3465_
) : GenUseCase_3465_<Unit, List<GenModel_3465_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3465_> = repository.getAll()
}

class GenSaveUseCase_3465_ @Inject constructor(
    private val repository: GenRepositoryImpl_3465_
) : GenUseCase_3465_<GenModel_3465_, GenModel_3465_> {
    override suspend fun invoke(params: GenModel_3465_): GenModel_3465_ = repository.save(params)
}

class GenDeleteUseCase_3465_ @Inject constructor(
    private val repository: GenRepositoryImpl_3465_
) : GenUseCase_3465_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3465_ @Inject constructor(
    private val repository: GenRepositoryImpl_3465_
) : GenUseCase_3465_<String, List<GenModel_3465_>> {
    override suspend fun invoke(params: String): List<GenModel_3465_> = repository.search(params)
}

abstract class GenMapper_3465_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3465_ : GenMapper_3465_<GenModel_3465_, String>() {
    override fun map(input: GenModel_3465_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3465_ : GenMapper_3465_<String, GenModel_3465_>() {
    override fun map(input: String): GenModel_3465_ {
        val parts = input.split(":")
        return GenModel_3465_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3465_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3465_,
    private val saveUseCase: GenSaveUseCase_3465_,
    private val deleteUseCase: GenDeleteUseCase_3465_,
    private val searchUseCase: GenSearchUseCase_3465_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3465_>(GenState_3465_.Idle)
    val state: StateFlow<GenState_3465_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3465_) {
        when (event) {
            is GenEvent_3465_.Load -> loadAll()
            is GenEvent_3465_.Update -> save(event.model)
            is GenEvent_3465_.Delete -> delete(event.id)
            is GenEvent_3465_.Refresh -> loadAll()
            is GenEvent_3465_.Search -> search(event.query)
            is GenEvent_3465_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3465_.Loading; _state.value = GenState_3465_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3465_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3465_.Success(searchUseCase(query)) } }
}
