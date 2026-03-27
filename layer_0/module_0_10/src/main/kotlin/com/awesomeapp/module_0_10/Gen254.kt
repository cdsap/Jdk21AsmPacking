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

data class GenModel_254_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_254_ {
    data class Load(val id: Long) : GenEvent_254_()
    data class Update(val model: GenModel_254_) : GenEvent_254_()
    data class Delete(val id: Long) : GenEvent_254_()
    data object Refresh : GenEvent_254_()
    data class Search(val query: String) : GenEvent_254_()
    data class Filter(val predicate: String) : GenEvent_254_()
}

sealed class GenState_254_ {
    data object Idle : GenState_254_()
    data object Loading : GenState_254_()
    data class Success(val items: List<GenModel_254_>) : GenState_254_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_254_()
    data class Partial(val items: List<GenModel_254_>, val hasMore: Boolean) : GenState_254_()
}

interface GenRepository_254_ {
    suspend fun getAll(): List<GenModel_254_>
    suspend fun getById(id: Long): GenModel_254_?
    suspend fun save(model: GenModel_254_): GenModel_254_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_254_>
}

@Singleton
class GenRepositoryImpl_254_ @Inject constructor() : GenRepository_254_ {
    private val store = mutableMapOf<Long, GenModel_254_>()
    override suspend fun getAll(): List<GenModel_254_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_254_? = store[id]
    override suspend fun save(model: GenModel_254_): GenModel_254_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_254_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_254_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_254_ @Inject constructor(
    private val repository: GenRepositoryImpl_254_
) : GenUseCase_254_<Unit, List<GenModel_254_>> {
    override suspend fun invoke(params: Unit): List<GenModel_254_> = repository.getAll()
}

class GenSaveUseCase_254_ @Inject constructor(
    private val repository: GenRepositoryImpl_254_
) : GenUseCase_254_<GenModel_254_, GenModel_254_> {
    override suspend fun invoke(params: GenModel_254_): GenModel_254_ = repository.save(params)
}

class GenDeleteUseCase_254_ @Inject constructor(
    private val repository: GenRepositoryImpl_254_
) : GenUseCase_254_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_254_ @Inject constructor(
    private val repository: GenRepositoryImpl_254_
) : GenUseCase_254_<String, List<GenModel_254_>> {
    override suspend fun invoke(params: String): List<GenModel_254_> = repository.search(params)
}

abstract class GenMapper_254_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_254_ : GenMapper_254_<GenModel_254_, String>() {
    override fun map(input: GenModel_254_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_254_ : GenMapper_254_<String, GenModel_254_>() {
    override fun map(input: String): GenModel_254_ {
        val parts = input.split(":")
        return GenModel_254_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_254_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_254_,
    private val saveUseCase: GenSaveUseCase_254_,
    private val deleteUseCase: GenDeleteUseCase_254_,
    private val searchUseCase: GenSearchUseCase_254_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_254_>(GenState_254_.Idle)
    val state: StateFlow<GenState_254_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_254_) {
        when (event) {
            is GenEvent_254_.Load -> loadAll()
            is GenEvent_254_.Update -> save(event.model)
            is GenEvent_254_.Delete -> delete(event.id)
            is GenEvent_254_.Refresh -> loadAll()
            is GenEvent_254_.Search -> search(event.query)
            is GenEvent_254_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_254_.Loading; _state.value = GenState_254_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_254_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_254_.Success(searchUseCase(query)) } }
}
