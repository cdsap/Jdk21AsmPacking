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

data class GenModel_885_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_885_ {
    data class Load(val id: Long) : GenEvent_885_()
    data class Update(val model: GenModel_885_) : GenEvent_885_()
    data class Delete(val id: Long) : GenEvent_885_()
    data object Refresh : GenEvent_885_()
    data class Search(val query: String) : GenEvent_885_()
    data class Filter(val predicate: String) : GenEvent_885_()
}

sealed class GenState_885_ {
    data object Idle : GenState_885_()
    data object Loading : GenState_885_()
    data class Success(val items: List<GenModel_885_>) : GenState_885_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_885_()
    data class Partial(val items: List<GenModel_885_>, val hasMore: Boolean) : GenState_885_()
}

interface GenRepository_885_ {
    suspend fun getAll(): List<GenModel_885_>
    suspend fun getById(id: Long): GenModel_885_?
    suspend fun save(model: GenModel_885_): GenModel_885_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_885_>
}

@Singleton
class GenRepositoryImpl_885_ @Inject constructor() : GenRepository_885_ {
    private val store = mutableMapOf<Long, GenModel_885_>()
    override suspend fun getAll(): List<GenModel_885_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_885_? = store[id]
    override suspend fun save(model: GenModel_885_): GenModel_885_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_885_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_885_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_885_ @Inject constructor(
    private val repository: GenRepositoryImpl_885_
) : GenUseCase_885_<Unit, List<GenModel_885_>> {
    override suspend fun invoke(params: Unit): List<GenModel_885_> = repository.getAll()
}

class GenSaveUseCase_885_ @Inject constructor(
    private val repository: GenRepositoryImpl_885_
) : GenUseCase_885_<GenModel_885_, GenModel_885_> {
    override suspend fun invoke(params: GenModel_885_): GenModel_885_ = repository.save(params)
}

class GenDeleteUseCase_885_ @Inject constructor(
    private val repository: GenRepositoryImpl_885_
) : GenUseCase_885_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_885_ @Inject constructor(
    private val repository: GenRepositoryImpl_885_
) : GenUseCase_885_<String, List<GenModel_885_>> {
    override suspend fun invoke(params: String): List<GenModel_885_> = repository.search(params)
}

abstract class GenMapper_885_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_885_ : GenMapper_885_<GenModel_885_, String>() {
    override fun map(input: GenModel_885_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_885_ : GenMapper_885_<String, GenModel_885_>() {
    override fun map(input: String): GenModel_885_ {
        val parts = input.split(":")
        return GenModel_885_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_885_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_885_,
    private val saveUseCase: GenSaveUseCase_885_,
    private val deleteUseCase: GenDeleteUseCase_885_,
    private val searchUseCase: GenSearchUseCase_885_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_885_>(GenState_885_.Idle)
    val state: StateFlow<GenState_885_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_885_) {
        when (event) {
            is GenEvent_885_.Load -> loadAll()
            is GenEvent_885_.Update -> save(event.model)
            is GenEvent_885_.Delete -> delete(event.id)
            is GenEvent_885_.Refresh -> loadAll()
            is GenEvent_885_.Search -> search(event.query)
            is GenEvent_885_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_885_.Loading; _state.value = GenState_885_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_885_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_885_.Success(searchUseCase(query)) } }
}
