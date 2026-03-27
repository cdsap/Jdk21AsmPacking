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

data class GenModel_2465_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2465_ {
    data class Load(val id: Long) : GenEvent_2465_()
    data class Update(val model: GenModel_2465_) : GenEvent_2465_()
    data class Delete(val id: Long) : GenEvent_2465_()
    data object Refresh : GenEvent_2465_()
    data class Search(val query: String) : GenEvent_2465_()
    data class Filter(val predicate: String) : GenEvent_2465_()
}

sealed class GenState_2465_ {
    data object Idle : GenState_2465_()
    data object Loading : GenState_2465_()
    data class Success(val items: List<GenModel_2465_>) : GenState_2465_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2465_()
    data class Partial(val items: List<GenModel_2465_>, val hasMore: Boolean) : GenState_2465_()
}

interface GenRepository_2465_ {
    suspend fun getAll(): List<GenModel_2465_>
    suspend fun getById(id: Long): GenModel_2465_?
    suspend fun save(model: GenModel_2465_): GenModel_2465_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2465_>
}

@Singleton
class GenRepositoryImpl_2465_ @Inject constructor() : GenRepository_2465_ {
    private val store = mutableMapOf<Long, GenModel_2465_>()
    override suspend fun getAll(): List<GenModel_2465_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2465_? = store[id]
    override suspend fun save(model: GenModel_2465_): GenModel_2465_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2465_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2465_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2465_ @Inject constructor(
    private val repository: GenRepositoryImpl_2465_
) : GenUseCase_2465_<Unit, List<GenModel_2465_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2465_> = repository.getAll()
}

class GenSaveUseCase_2465_ @Inject constructor(
    private val repository: GenRepositoryImpl_2465_
) : GenUseCase_2465_<GenModel_2465_, GenModel_2465_> {
    override suspend fun invoke(params: GenModel_2465_): GenModel_2465_ = repository.save(params)
}

class GenDeleteUseCase_2465_ @Inject constructor(
    private val repository: GenRepositoryImpl_2465_
) : GenUseCase_2465_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2465_ @Inject constructor(
    private val repository: GenRepositoryImpl_2465_
) : GenUseCase_2465_<String, List<GenModel_2465_>> {
    override suspend fun invoke(params: String): List<GenModel_2465_> = repository.search(params)
}

abstract class GenMapper_2465_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2465_ : GenMapper_2465_<GenModel_2465_, String>() {
    override fun map(input: GenModel_2465_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2465_ : GenMapper_2465_<String, GenModel_2465_>() {
    override fun map(input: String): GenModel_2465_ {
        val parts = input.split(":")
        return GenModel_2465_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2465_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2465_,
    private val saveUseCase: GenSaveUseCase_2465_,
    private val deleteUseCase: GenDeleteUseCase_2465_,
    private val searchUseCase: GenSearchUseCase_2465_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2465_>(GenState_2465_.Idle)
    val state: StateFlow<GenState_2465_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2465_) {
        when (event) {
            is GenEvent_2465_.Load -> loadAll()
            is GenEvent_2465_.Update -> save(event.model)
            is GenEvent_2465_.Delete -> delete(event.id)
            is GenEvent_2465_.Refresh -> loadAll()
            is GenEvent_2465_.Search -> search(event.query)
            is GenEvent_2465_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2465_.Loading; _state.value = GenState_2465_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2465_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2465_.Success(searchUseCase(query)) } }
}
