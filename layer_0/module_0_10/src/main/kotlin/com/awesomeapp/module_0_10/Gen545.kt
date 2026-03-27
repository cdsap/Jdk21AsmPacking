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

data class GenModel_545_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_545_ {
    data class Load(val id: Long) : GenEvent_545_()
    data class Update(val model: GenModel_545_) : GenEvent_545_()
    data class Delete(val id: Long) : GenEvent_545_()
    data object Refresh : GenEvent_545_()
    data class Search(val query: String) : GenEvent_545_()
    data class Filter(val predicate: String) : GenEvent_545_()
}

sealed class GenState_545_ {
    data object Idle : GenState_545_()
    data object Loading : GenState_545_()
    data class Success(val items: List<GenModel_545_>) : GenState_545_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_545_()
    data class Partial(val items: List<GenModel_545_>, val hasMore: Boolean) : GenState_545_()
}

interface GenRepository_545_ {
    suspend fun getAll(): List<GenModel_545_>
    suspend fun getById(id: Long): GenModel_545_?
    suspend fun save(model: GenModel_545_): GenModel_545_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_545_>
}

@Singleton
class GenRepositoryImpl_545_ @Inject constructor() : GenRepository_545_ {
    private val store = mutableMapOf<Long, GenModel_545_>()
    override suspend fun getAll(): List<GenModel_545_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_545_? = store[id]
    override suspend fun save(model: GenModel_545_): GenModel_545_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_545_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_545_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_545_ @Inject constructor(
    private val repository: GenRepositoryImpl_545_
) : GenUseCase_545_<Unit, List<GenModel_545_>> {
    override suspend fun invoke(params: Unit): List<GenModel_545_> = repository.getAll()
}

class GenSaveUseCase_545_ @Inject constructor(
    private val repository: GenRepositoryImpl_545_
) : GenUseCase_545_<GenModel_545_, GenModel_545_> {
    override suspend fun invoke(params: GenModel_545_): GenModel_545_ = repository.save(params)
}

class GenDeleteUseCase_545_ @Inject constructor(
    private val repository: GenRepositoryImpl_545_
) : GenUseCase_545_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_545_ @Inject constructor(
    private val repository: GenRepositoryImpl_545_
) : GenUseCase_545_<String, List<GenModel_545_>> {
    override suspend fun invoke(params: String): List<GenModel_545_> = repository.search(params)
}

abstract class GenMapper_545_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_545_ : GenMapper_545_<GenModel_545_, String>() {
    override fun map(input: GenModel_545_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_545_ : GenMapper_545_<String, GenModel_545_>() {
    override fun map(input: String): GenModel_545_ {
        val parts = input.split(":")
        return GenModel_545_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_545_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_545_,
    private val saveUseCase: GenSaveUseCase_545_,
    private val deleteUseCase: GenDeleteUseCase_545_,
    private val searchUseCase: GenSearchUseCase_545_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_545_>(GenState_545_.Idle)
    val state: StateFlow<GenState_545_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_545_) {
        when (event) {
            is GenEvent_545_.Load -> loadAll()
            is GenEvent_545_.Update -> save(event.model)
            is GenEvent_545_.Delete -> delete(event.id)
            is GenEvent_545_.Refresh -> loadAll()
            is GenEvent_545_.Search -> search(event.query)
            is GenEvent_545_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_545_.Loading; _state.value = GenState_545_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_545_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_545_.Success(searchUseCase(query)) } }
}
