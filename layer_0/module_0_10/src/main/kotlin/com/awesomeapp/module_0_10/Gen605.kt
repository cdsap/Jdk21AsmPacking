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

data class GenModel_605_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_605_ {
    data class Load(val id: Long) : GenEvent_605_()
    data class Update(val model: GenModel_605_) : GenEvent_605_()
    data class Delete(val id: Long) : GenEvent_605_()
    data object Refresh : GenEvent_605_()
    data class Search(val query: String) : GenEvent_605_()
    data class Filter(val predicate: String) : GenEvent_605_()
}

sealed class GenState_605_ {
    data object Idle : GenState_605_()
    data object Loading : GenState_605_()
    data class Success(val items: List<GenModel_605_>) : GenState_605_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_605_()
    data class Partial(val items: List<GenModel_605_>, val hasMore: Boolean) : GenState_605_()
}

interface GenRepository_605_ {
    suspend fun getAll(): List<GenModel_605_>
    suspend fun getById(id: Long): GenModel_605_?
    suspend fun save(model: GenModel_605_): GenModel_605_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_605_>
}

@Singleton
class GenRepositoryImpl_605_ @Inject constructor() : GenRepository_605_ {
    private val store = mutableMapOf<Long, GenModel_605_>()
    override suspend fun getAll(): List<GenModel_605_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_605_? = store[id]
    override suspend fun save(model: GenModel_605_): GenModel_605_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_605_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_605_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_605_ @Inject constructor(
    private val repository: GenRepositoryImpl_605_
) : GenUseCase_605_<Unit, List<GenModel_605_>> {
    override suspend fun invoke(params: Unit): List<GenModel_605_> = repository.getAll()
}

class GenSaveUseCase_605_ @Inject constructor(
    private val repository: GenRepositoryImpl_605_
) : GenUseCase_605_<GenModel_605_, GenModel_605_> {
    override suspend fun invoke(params: GenModel_605_): GenModel_605_ = repository.save(params)
}

class GenDeleteUseCase_605_ @Inject constructor(
    private val repository: GenRepositoryImpl_605_
) : GenUseCase_605_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_605_ @Inject constructor(
    private val repository: GenRepositoryImpl_605_
) : GenUseCase_605_<String, List<GenModel_605_>> {
    override suspend fun invoke(params: String): List<GenModel_605_> = repository.search(params)
}

abstract class GenMapper_605_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_605_ : GenMapper_605_<GenModel_605_, String>() {
    override fun map(input: GenModel_605_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_605_ : GenMapper_605_<String, GenModel_605_>() {
    override fun map(input: String): GenModel_605_ {
        val parts = input.split(":")
        return GenModel_605_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_605_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_605_,
    private val saveUseCase: GenSaveUseCase_605_,
    private val deleteUseCase: GenDeleteUseCase_605_,
    private val searchUseCase: GenSearchUseCase_605_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_605_>(GenState_605_.Idle)
    val state: StateFlow<GenState_605_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_605_) {
        when (event) {
            is GenEvent_605_.Load -> loadAll()
            is GenEvent_605_.Update -> save(event.model)
            is GenEvent_605_.Delete -> delete(event.id)
            is GenEvent_605_.Refresh -> loadAll()
            is GenEvent_605_.Search -> search(event.query)
            is GenEvent_605_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_605_.Loading; _state.value = GenState_605_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_605_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_605_.Success(searchUseCase(query)) } }
}
