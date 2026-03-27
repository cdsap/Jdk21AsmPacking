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

data class GenModel_901_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_901_ {
    data class Load(val id: Long) : GenEvent_901_()
    data class Update(val model: GenModel_901_) : GenEvent_901_()
    data class Delete(val id: Long) : GenEvent_901_()
    data object Refresh : GenEvent_901_()
    data class Search(val query: String) : GenEvent_901_()
    data class Filter(val predicate: String) : GenEvent_901_()
}

sealed class GenState_901_ {
    data object Idle : GenState_901_()
    data object Loading : GenState_901_()
    data class Success(val items: List<GenModel_901_>) : GenState_901_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_901_()
    data class Partial(val items: List<GenModel_901_>, val hasMore: Boolean) : GenState_901_()
}

interface GenRepository_901_ {
    suspend fun getAll(): List<GenModel_901_>
    suspend fun getById(id: Long): GenModel_901_?
    suspend fun save(model: GenModel_901_): GenModel_901_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_901_>
}

@Singleton
class GenRepositoryImpl_901_ @Inject constructor() : GenRepository_901_ {
    private val store = mutableMapOf<Long, GenModel_901_>()
    override suspend fun getAll(): List<GenModel_901_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_901_? = store[id]
    override suspend fun save(model: GenModel_901_): GenModel_901_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_901_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_901_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_901_ @Inject constructor(
    private val repository: GenRepositoryImpl_901_
) : GenUseCase_901_<Unit, List<GenModel_901_>> {
    override suspend fun invoke(params: Unit): List<GenModel_901_> = repository.getAll()
}

class GenSaveUseCase_901_ @Inject constructor(
    private val repository: GenRepositoryImpl_901_
) : GenUseCase_901_<GenModel_901_, GenModel_901_> {
    override suspend fun invoke(params: GenModel_901_): GenModel_901_ = repository.save(params)
}

class GenDeleteUseCase_901_ @Inject constructor(
    private val repository: GenRepositoryImpl_901_
) : GenUseCase_901_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_901_ @Inject constructor(
    private val repository: GenRepositoryImpl_901_
) : GenUseCase_901_<String, List<GenModel_901_>> {
    override suspend fun invoke(params: String): List<GenModel_901_> = repository.search(params)
}

abstract class GenMapper_901_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_901_ : GenMapper_901_<GenModel_901_, String>() {
    override fun map(input: GenModel_901_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_901_ : GenMapper_901_<String, GenModel_901_>() {
    override fun map(input: String): GenModel_901_ {
        val parts = input.split(":")
        return GenModel_901_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_901_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_901_,
    private val saveUseCase: GenSaveUseCase_901_,
    private val deleteUseCase: GenDeleteUseCase_901_,
    private val searchUseCase: GenSearchUseCase_901_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_901_>(GenState_901_.Idle)
    val state: StateFlow<GenState_901_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_901_) {
        when (event) {
            is GenEvent_901_.Load -> loadAll()
            is GenEvent_901_.Update -> save(event.model)
            is GenEvent_901_.Delete -> delete(event.id)
            is GenEvent_901_.Refresh -> loadAll()
            is GenEvent_901_.Search -> search(event.query)
            is GenEvent_901_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_901_.Loading; _state.value = GenState_901_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_901_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_901_.Success(searchUseCase(query)) } }
}
