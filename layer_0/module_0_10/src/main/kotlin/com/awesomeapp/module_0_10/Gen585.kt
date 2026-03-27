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

data class GenModel_585_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_585_ {
    data class Load(val id: Long) : GenEvent_585_()
    data class Update(val model: GenModel_585_) : GenEvent_585_()
    data class Delete(val id: Long) : GenEvent_585_()
    data object Refresh : GenEvent_585_()
    data class Search(val query: String) : GenEvent_585_()
    data class Filter(val predicate: String) : GenEvent_585_()
}

sealed class GenState_585_ {
    data object Idle : GenState_585_()
    data object Loading : GenState_585_()
    data class Success(val items: List<GenModel_585_>) : GenState_585_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_585_()
    data class Partial(val items: List<GenModel_585_>, val hasMore: Boolean) : GenState_585_()
}

interface GenRepository_585_ {
    suspend fun getAll(): List<GenModel_585_>
    suspend fun getById(id: Long): GenModel_585_?
    suspend fun save(model: GenModel_585_): GenModel_585_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_585_>
}

@Singleton
class GenRepositoryImpl_585_ @Inject constructor() : GenRepository_585_ {
    private val store = mutableMapOf<Long, GenModel_585_>()
    override suspend fun getAll(): List<GenModel_585_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_585_? = store[id]
    override suspend fun save(model: GenModel_585_): GenModel_585_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_585_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_585_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_585_ @Inject constructor(
    private val repository: GenRepositoryImpl_585_
) : GenUseCase_585_<Unit, List<GenModel_585_>> {
    override suspend fun invoke(params: Unit): List<GenModel_585_> = repository.getAll()
}

class GenSaveUseCase_585_ @Inject constructor(
    private val repository: GenRepositoryImpl_585_
) : GenUseCase_585_<GenModel_585_, GenModel_585_> {
    override suspend fun invoke(params: GenModel_585_): GenModel_585_ = repository.save(params)
}

class GenDeleteUseCase_585_ @Inject constructor(
    private val repository: GenRepositoryImpl_585_
) : GenUseCase_585_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_585_ @Inject constructor(
    private val repository: GenRepositoryImpl_585_
) : GenUseCase_585_<String, List<GenModel_585_>> {
    override suspend fun invoke(params: String): List<GenModel_585_> = repository.search(params)
}

abstract class GenMapper_585_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_585_ : GenMapper_585_<GenModel_585_, String>() {
    override fun map(input: GenModel_585_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_585_ : GenMapper_585_<String, GenModel_585_>() {
    override fun map(input: String): GenModel_585_ {
        val parts = input.split(":")
        return GenModel_585_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_585_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_585_,
    private val saveUseCase: GenSaveUseCase_585_,
    private val deleteUseCase: GenDeleteUseCase_585_,
    private val searchUseCase: GenSearchUseCase_585_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_585_>(GenState_585_.Idle)
    val state: StateFlow<GenState_585_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_585_) {
        when (event) {
            is GenEvent_585_.Load -> loadAll()
            is GenEvent_585_.Update -> save(event.model)
            is GenEvent_585_.Delete -> delete(event.id)
            is GenEvent_585_.Refresh -> loadAll()
            is GenEvent_585_.Search -> search(event.query)
            is GenEvent_585_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_585_.Loading; _state.value = GenState_585_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_585_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_585_.Success(searchUseCase(query)) } }
}
