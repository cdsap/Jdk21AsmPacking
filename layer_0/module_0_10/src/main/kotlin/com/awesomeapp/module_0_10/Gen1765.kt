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

data class GenModel_1765_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1765_ {
    data class Load(val id: Long) : GenEvent_1765_()
    data class Update(val model: GenModel_1765_) : GenEvent_1765_()
    data class Delete(val id: Long) : GenEvent_1765_()
    data object Refresh : GenEvent_1765_()
    data class Search(val query: String) : GenEvent_1765_()
    data class Filter(val predicate: String) : GenEvent_1765_()
}

sealed class GenState_1765_ {
    data object Idle : GenState_1765_()
    data object Loading : GenState_1765_()
    data class Success(val items: List<GenModel_1765_>) : GenState_1765_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1765_()
    data class Partial(val items: List<GenModel_1765_>, val hasMore: Boolean) : GenState_1765_()
}

interface GenRepository_1765_ {
    suspend fun getAll(): List<GenModel_1765_>
    suspend fun getById(id: Long): GenModel_1765_?
    suspend fun save(model: GenModel_1765_): GenModel_1765_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1765_>
}

@Singleton
class GenRepositoryImpl_1765_ @Inject constructor() : GenRepository_1765_ {
    private val store = mutableMapOf<Long, GenModel_1765_>()
    override suspend fun getAll(): List<GenModel_1765_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1765_? = store[id]
    override suspend fun save(model: GenModel_1765_): GenModel_1765_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1765_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1765_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1765_ @Inject constructor(
    private val repository: GenRepositoryImpl_1765_
) : GenUseCase_1765_<Unit, List<GenModel_1765_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1765_> = repository.getAll()
}

class GenSaveUseCase_1765_ @Inject constructor(
    private val repository: GenRepositoryImpl_1765_
) : GenUseCase_1765_<GenModel_1765_, GenModel_1765_> {
    override suspend fun invoke(params: GenModel_1765_): GenModel_1765_ = repository.save(params)
}

class GenDeleteUseCase_1765_ @Inject constructor(
    private val repository: GenRepositoryImpl_1765_
) : GenUseCase_1765_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1765_ @Inject constructor(
    private val repository: GenRepositoryImpl_1765_
) : GenUseCase_1765_<String, List<GenModel_1765_>> {
    override suspend fun invoke(params: String): List<GenModel_1765_> = repository.search(params)
}

abstract class GenMapper_1765_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1765_ : GenMapper_1765_<GenModel_1765_, String>() {
    override fun map(input: GenModel_1765_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1765_ : GenMapper_1765_<String, GenModel_1765_>() {
    override fun map(input: String): GenModel_1765_ {
        val parts = input.split(":")
        return GenModel_1765_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1765_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1765_,
    private val saveUseCase: GenSaveUseCase_1765_,
    private val deleteUseCase: GenDeleteUseCase_1765_,
    private val searchUseCase: GenSearchUseCase_1765_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1765_>(GenState_1765_.Idle)
    val state: StateFlow<GenState_1765_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1765_) {
        when (event) {
            is GenEvent_1765_.Load -> loadAll()
            is GenEvent_1765_.Update -> save(event.model)
            is GenEvent_1765_.Delete -> delete(event.id)
            is GenEvent_1765_.Refresh -> loadAll()
            is GenEvent_1765_.Search -> search(event.query)
            is GenEvent_1765_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1765_.Loading; _state.value = GenState_1765_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1765_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1765_.Success(searchUseCase(query)) } }
}
