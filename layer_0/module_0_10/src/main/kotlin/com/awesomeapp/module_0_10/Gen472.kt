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

data class GenModel_472_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_472_ {
    data class Load(val id: Long) : GenEvent_472_()
    data class Update(val model: GenModel_472_) : GenEvent_472_()
    data class Delete(val id: Long) : GenEvent_472_()
    data object Refresh : GenEvent_472_()
    data class Search(val query: String) : GenEvent_472_()
    data class Filter(val predicate: String) : GenEvent_472_()
}

sealed class GenState_472_ {
    data object Idle : GenState_472_()
    data object Loading : GenState_472_()
    data class Success(val items: List<GenModel_472_>) : GenState_472_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_472_()
    data class Partial(val items: List<GenModel_472_>, val hasMore: Boolean) : GenState_472_()
}

interface GenRepository_472_ {
    suspend fun getAll(): List<GenModel_472_>
    suspend fun getById(id: Long): GenModel_472_?
    suspend fun save(model: GenModel_472_): GenModel_472_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_472_>
}

@Singleton
class GenRepositoryImpl_472_ @Inject constructor() : GenRepository_472_ {
    private val store = mutableMapOf<Long, GenModel_472_>()
    override suspend fun getAll(): List<GenModel_472_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_472_? = store[id]
    override suspend fun save(model: GenModel_472_): GenModel_472_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_472_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_472_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_472_ @Inject constructor(
    private val repository: GenRepositoryImpl_472_
) : GenUseCase_472_<Unit, List<GenModel_472_>> {
    override suspend fun invoke(params: Unit): List<GenModel_472_> = repository.getAll()
}

class GenSaveUseCase_472_ @Inject constructor(
    private val repository: GenRepositoryImpl_472_
) : GenUseCase_472_<GenModel_472_, GenModel_472_> {
    override suspend fun invoke(params: GenModel_472_): GenModel_472_ = repository.save(params)
}

class GenDeleteUseCase_472_ @Inject constructor(
    private val repository: GenRepositoryImpl_472_
) : GenUseCase_472_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_472_ @Inject constructor(
    private val repository: GenRepositoryImpl_472_
) : GenUseCase_472_<String, List<GenModel_472_>> {
    override suspend fun invoke(params: String): List<GenModel_472_> = repository.search(params)
}

abstract class GenMapper_472_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_472_ : GenMapper_472_<GenModel_472_, String>() {
    override fun map(input: GenModel_472_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_472_ : GenMapper_472_<String, GenModel_472_>() {
    override fun map(input: String): GenModel_472_ {
        val parts = input.split(":")
        return GenModel_472_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_472_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_472_,
    private val saveUseCase: GenSaveUseCase_472_,
    private val deleteUseCase: GenDeleteUseCase_472_,
    private val searchUseCase: GenSearchUseCase_472_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_472_>(GenState_472_.Idle)
    val state: StateFlow<GenState_472_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_472_) {
        when (event) {
            is GenEvent_472_.Load -> loadAll()
            is GenEvent_472_.Update -> save(event.model)
            is GenEvent_472_.Delete -> delete(event.id)
            is GenEvent_472_.Refresh -> loadAll()
            is GenEvent_472_.Search -> search(event.query)
            is GenEvent_472_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_472_.Loading; _state.value = GenState_472_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_472_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_472_.Success(searchUseCase(query)) } }
}
