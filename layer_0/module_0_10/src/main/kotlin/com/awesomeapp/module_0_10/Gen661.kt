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

data class GenModel_661_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_661_ {
    data class Load(val id: Long) : GenEvent_661_()
    data class Update(val model: GenModel_661_) : GenEvent_661_()
    data class Delete(val id: Long) : GenEvent_661_()
    data object Refresh : GenEvent_661_()
    data class Search(val query: String) : GenEvent_661_()
    data class Filter(val predicate: String) : GenEvent_661_()
}

sealed class GenState_661_ {
    data object Idle : GenState_661_()
    data object Loading : GenState_661_()
    data class Success(val items: List<GenModel_661_>) : GenState_661_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_661_()
    data class Partial(val items: List<GenModel_661_>, val hasMore: Boolean) : GenState_661_()
}

interface GenRepository_661_ {
    suspend fun getAll(): List<GenModel_661_>
    suspend fun getById(id: Long): GenModel_661_?
    suspend fun save(model: GenModel_661_): GenModel_661_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_661_>
}

@Singleton
class GenRepositoryImpl_661_ @Inject constructor() : GenRepository_661_ {
    private val store = mutableMapOf<Long, GenModel_661_>()
    override suspend fun getAll(): List<GenModel_661_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_661_? = store[id]
    override suspend fun save(model: GenModel_661_): GenModel_661_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_661_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_661_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_661_ @Inject constructor(
    private val repository: GenRepositoryImpl_661_
) : GenUseCase_661_<Unit, List<GenModel_661_>> {
    override suspend fun invoke(params: Unit): List<GenModel_661_> = repository.getAll()
}

class GenSaveUseCase_661_ @Inject constructor(
    private val repository: GenRepositoryImpl_661_
) : GenUseCase_661_<GenModel_661_, GenModel_661_> {
    override suspend fun invoke(params: GenModel_661_): GenModel_661_ = repository.save(params)
}

class GenDeleteUseCase_661_ @Inject constructor(
    private val repository: GenRepositoryImpl_661_
) : GenUseCase_661_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_661_ @Inject constructor(
    private val repository: GenRepositoryImpl_661_
) : GenUseCase_661_<String, List<GenModel_661_>> {
    override suspend fun invoke(params: String): List<GenModel_661_> = repository.search(params)
}

abstract class GenMapper_661_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_661_ : GenMapper_661_<GenModel_661_, String>() {
    override fun map(input: GenModel_661_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_661_ : GenMapper_661_<String, GenModel_661_>() {
    override fun map(input: String): GenModel_661_ {
        val parts = input.split(":")
        return GenModel_661_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_661_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_661_,
    private val saveUseCase: GenSaveUseCase_661_,
    private val deleteUseCase: GenDeleteUseCase_661_,
    private val searchUseCase: GenSearchUseCase_661_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_661_>(GenState_661_.Idle)
    val state: StateFlow<GenState_661_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_661_) {
        when (event) {
            is GenEvent_661_.Load -> loadAll()
            is GenEvent_661_.Update -> save(event.model)
            is GenEvent_661_.Delete -> delete(event.id)
            is GenEvent_661_.Refresh -> loadAll()
            is GenEvent_661_.Search -> search(event.query)
            is GenEvent_661_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_661_.Loading; _state.value = GenState_661_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_661_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_661_.Success(searchUseCase(query)) } }
}
