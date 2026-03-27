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

data class GenModel_1901_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1901_ {
    data class Load(val id: Long) : GenEvent_1901_()
    data class Update(val model: GenModel_1901_) : GenEvent_1901_()
    data class Delete(val id: Long) : GenEvent_1901_()
    data object Refresh : GenEvent_1901_()
    data class Search(val query: String) : GenEvent_1901_()
    data class Filter(val predicate: String) : GenEvent_1901_()
}

sealed class GenState_1901_ {
    data object Idle : GenState_1901_()
    data object Loading : GenState_1901_()
    data class Success(val items: List<GenModel_1901_>) : GenState_1901_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1901_()
    data class Partial(val items: List<GenModel_1901_>, val hasMore: Boolean) : GenState_1901_()
}

interface GenRepository_1901_ {
    suspend fun getAll(): List<GenModel_1901_>
    suspend fun getById(id: Long): GenModel_1901_?
    suspend fun save(model: GenModel_1901_): GenModel_1901_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1901_>
}

@Singleton
class GenRepositoryImpl_1901_ @Inject constructor() : GenRepository_1901_ {
    private val store = mutableMapOf<Long, GenModel_1901_>()
    override suspend fun getAll(): List<GenModel_1901_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1901_? = store[id]
    override suspend fun save(model: GenModel_1901_): GenModel_1901_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1901_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1901_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1901_ @Inject constructor(
    private val repository: GenRepositoryImpl_1901_
) : GenUseCase_1901_<Unit, List<GenModel_1901_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1901_> = repository.getAll()
}

class GenSaveUseCase_1901_ @Inject constructor(
    private val repository: GenRepositoryImpl_1901_
) : GenUseCase_1901_<GenModel_1901_, GenModel_1901_> {
    override suspend fun invoke(params: GenModel_1901_): GenModel_1901_ = repository.save(params)
}

class GenDeleteUseCase_1901_ @Inject constructor(
    private val repository: GenRepositoryImpl_1901_
) : GenUseCase_1901_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1901_ @Inject constructor(
    private val repository: GenRepositoryImpl_1901_
) : GenUseCase_1901_<String, List<GenModel_1901_>> {
    override suspend fun invoke(params: String): List<GenModel_1901_> = repository.search(params)
}

abstract class GenMapper_1901_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1901_ : GenMapper_1901_<GenModel_1901_, String>() {
    override fun map(input: GenModel_1901_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1901_ : GenMapper_1901_<String, GenModel_1901_>() {
    override fun map(input: String): GenModel_1901_ {
        val parts = input.split(":")
        return GenModel_1901_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1901_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1901_,
    private val saveUseCase: GenSaveUseCase_1901_,
    private val deleteUseCase: GenDeleteUseCase_1901_,
    private val searchUseCase: GenSearchUseCase_1901_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1901_>(GenState_1901_.Idle)
    val state: StateFlow<GenState_1901_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1901_) {
        when (event) {
            is GenEvent_1901_.Load -> loadAll()
            is GenEvent_1901_.Update -> save(event.model)
            is GenEvent_1901_.Delete -> delete(event.id)
            is GenEvent_1901_.Refresh -> loadAll()
            is GenEvent_1901_.Search -> search(event.query)
            is GenEvent_1901_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1901_.Loading; _state.value = GenState_1901_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1901_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1901_.Success(searchUseCase(query)) } }
}
