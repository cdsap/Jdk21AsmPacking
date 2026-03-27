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

data class GenModel_1654_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1654_ {
    data class Load(val id: Long) : GenEvent_1654_()
    data class Update(val model: GenModel_1654_) : GenEvent_1654_()
    data class Delete(val id: Long) : GenEvent_1654_()
    data object Refresh : GenEvent_1654_()
    data class Search(val query: String) : GenEvent_1654_()
    data class Filter(val predicate: String) : GenEvent_1654_()
}

sealed class GenState_1654_ {
    data object Idle : GenState_1654_()
    data object Loading : GenState_1654_()
    data class Success(val items: List<GenModel_1654_>) : GenState_1654_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1654_()
    data class Partial(val items: List<GenModel_1654_>, val hasMore: Boolean) : GenState_1654_()
}

interface GenRepository_1654_ {
    suspend fun getAll(): List<GenModel_1654_>
    suspend fun getById(id: Long): GenModel_1654_?
    suspend fun save(model: GenModel_1654_): GenModel_1654_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1654_>
}

@Singleton
class GenRepositoryImpl_1654_ @Inject constructor() : GenRepository_1654_ {
    private val store = mutableMapOf<Long, GenModel_1654_>()
    override suspend fun getAll(): List<GenModel_1654_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1654_? = store[id]
    override suspend fun save(model: GenModel_1654_): GenModel_1654_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1654_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1654_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1654_ @Inject constructor(
    private val repository: GenRepositoryImpl_1654_
) : GenUseCase_1654_<Unit, List<GenModel_1654_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1654_> = repository.getAll()
}

class GenSaveUseCase_1654_ @Inject constructor(
    private val repository: GenRepositoryImpl_1654_
) : GenUseCase_1654_<GenModel_1654_, GenModel_1654_> {
    override suspend fun invoke(params: GenModel_1654_): GenModel_1654_ = repository.save(params)
}

class GenDeleteUseCase_1654_ @Inject constructor(
    private val repository: GenRepositoryImpl_1654_
) : GenUseCase_1654_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1654_ @Inject constructor(
    private val repository: GenRepositoryImpl_1654_
) : GenUseCase_1654_<String, List<GenModel_1654_>> {
    override suspend fun invoke(params: String): List<GenModel_1654_> = repository.search(params)
}

abstract class GenMapper_1654_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1654_ : GenMapper_1654_<GenModel_1654_, String>() {
    override fun map(input: GenModel_1654_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1654_ : GenMapper_1654_<String, GenModel_1654_>() {
    override fun map(input: String): GenModel_1654_ {
        val parts = input.split(":")
        return GenModel_1654_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1654_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1654_,
    private val saveUseCase: GenSaveUseCase_1654_,
    private val deleteUseCase: GenDeleteUseCase_1654_,
    private val searchUseCase: GenSearchUseCase_1654_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1654_>(GenState_1654_.Idle)
    val state: StateFlow<GenState_1654_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1654_) {
        when (event) {
            is GenEvent_1654_.Load -> loadAll()
            is GenEvent_1654_.Update -> save(event.model)
            is GenEvent_1654_.Delete -> delete(event.id)
            is GenEvent_1654_.Refresh -> loadAll()
            is GenEvent_1654_.Search -> search(event.query)
            is GenEvent_1654_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1654_.Loading; _state.value = GenState_1654_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1654_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1654_.Success(searchUseCase(query)) } }
}
