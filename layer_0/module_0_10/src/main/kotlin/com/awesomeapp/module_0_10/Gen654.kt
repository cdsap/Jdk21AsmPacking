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

data class GenModel_654_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_654_ {
    data class Load(val id: Long) : GenEvent_654_()
    data class Update(val model: GenModel_654_) : GenEvent_654_()
    data class Delete(val id: Long) : GenEvent_654_()
    data object Refresh : GenEvent_654_()
    data class Search(val query: String) : GenEvent_654_()
    data class Filter(val predicate: String) : GenEvent_654_()
}

sealed class GenState_654_ {
    data object Idle : GenState_654_()
    data object Loading : GenState_654_()
    data class Success(val items: List<GenModel_654_>) : GenState_654_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_654_()
    data class Partial(val items: List<GenModel_654_>, val hasMore: Boolean) : GenState_654_()
}

interface GenRepository_654_ {
    suspend fun getAll(): List<GenModel_654_>
    suspend fun getById(id: Long): GenModel_654_?
    suspend fun save(model: GenModel_654_): GenModel_654_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_654_>
}

@Singleton
class GenRepositoryImpl_654_ @Inject constructor() : GenRepository_654_ {
    private val store = mutableMapOf<Long, GenModel_654_>()
    override suspend fun getAll(): List<GenModel_654_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_654_? = store[id]
    override suspend fun save(model: GenModel_654_): GenModel_654_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_654_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_654_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_654_ @Inject constructor(
    private val repository: GenRepositoryImpl_654_
) : GenUseCase_654_<Unit, List<GenModel_654_>> {
    override suspend fun invoke(params: Unit): List<GenModel_654_> = repository.getAll()
}

class GenSaveUseCase_654_ @Inject constructor(
    private val repository: GenRepositoryImpl_654_
) : GenUseCase_654_<GenModel_654_, GenModel_654_> {
    override suspend fun invoke(params: GenModel_654_): GenModel_654_ = repository.save(params)
}

class GenDeleteUseCase_654_ @Inject constructor(
    private val repository: GenRepositoryImpl_654_
) : GenUseCase_654_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_654_ @Inject constructor(
    private val repository: GenRepositoryImpl_654_
) : GenUseCase_654_<String, List<GenModel_654_>> {
    override suspend fun invoke(params: String): List<GenModel_654_> = repository.search(params)
}

abstract class GenMapper_654_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_654_ : GenMapper_654_<GenModel_654_, String>() {
    override fun map(input: GenModel_654_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_654_ : GenMapper_654_<String, GenModel_654_>() {
    override fun map(input: String): GenModel_654_ {
        val parts = input.split(":")
        return GenModel_654_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_654_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_654_,
    private val saveUseCase: GenSaveUseCase_654_,
    private val deleteUseCase: GenDeleteUseCase_654_,
    private val searchUseCase: GenSearchUseCase_654_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_654_>(GenState_654_.Idle)
    val state: StateFlow<GenState_654_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_654_) {
        when (event) {
            is GenEvent_654_.Load -> loadAll()
            is GenEvent_654_.Update -> save(event.model)
            is GenEvent_654_.Delete -> delete(event.id)
            is GenEvent_654_.Refresh -> loadAll()
            is GenEvent_654_.Search -> search(event.query)
            is GenEvent_654_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_654_.Loading; _state.value = GenState_654_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_654_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_654_.Success(searchUseCase(query)) } }
}
