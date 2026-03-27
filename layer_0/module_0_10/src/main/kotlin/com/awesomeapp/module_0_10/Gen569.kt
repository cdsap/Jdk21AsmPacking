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

data class GenModel_569_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_569_ {
    data class Load(val id: Long) : GenEvent_569_()
    data class Update(val model: GenModel_569_) : GenEvent_569_()
    data class Delete(val id: Long) : GenEvent_569_()
    data object Refresh : GenEvent_569_()
    data class Search(val query: String) : GenEvent_569_()
    data class Filter(val predicate: String) : GenEvent_569_()
}

sealed class GenState_569_ {
    data object Idle : GenState_569_()
    data object Loading : GenState_569_()
    data class Success(val items: List<GenModel_569_>) : GenState_569_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_569_()
    data class Partial(val items: List<GenModel_569_>, val hasMore: Boolean) : GenState_569_()
}

interface GenRepository_569_ {
    suspend fun getAll(): List<GenModel_569_>
    suspend fun getById(id: Long): GenModel_569_?
    suspend fun save(model: GenModel_569_): GenModel_569_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_569_>
}

@Singleton
class GenRepositoryImpl_569_ @Inject constructor() : GenRepository_569_ {
    private val store = mutableMapOf<Long, GenModel_569_>()
    override suspend fun getAll(): List<GenModel_569_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_569_? = store[id]
    override suspend fun save(model: GenModel_569_): GenModel_569_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_569_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_569_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_569_ @Inject constructor(
    private val repository: GenRepositoryImpl_569_
) : GenUseCase_569_<Unit, List<GenModel_569_>> {
    override suspend fun invoke(params: Unit): List<GenModel_569_> = repository.getAll()
}

class GenSaveUseCase_569_ @Inject constructor(
    private val repository: GenRepositoryImpl_569_
) : GenUseCase_569_<GenModel_569_, GenModel_569_> {
    override suspend fun invoke(params: GenModel_569_): GenModel_569_ = repository.save(params)
}

class GenDeleteUseCase_569_ @Inject constructor(
    private val repository: GenRepositoryImpl_569_
) : GenUseCase_569_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_569_ @Inject constructor(
    private val repository: GenRepositoryImpl_569_
) : GenUseCase_569_<String, List<GenModel_569_>> {
    override suspend fun invoke(params: String): List<GenModel_569_> = repository.search(params)
}

abstract class GenMapper_569_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_569_ : GenMapper_569_<GenModel_569_, String>() {
    override fun map(input: GenModel_569_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_569_ : GenMapper_569_<String, GenModel_569_>() {
    override fun map(input: String): GenModel_569_ {
        val parts = input.split(":")
        return GenModel_569_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_569_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_569_,
    private val saveUseCase: GenSaveUseCase_569_,
    private val deleteUseCase: GenDeleteUseCase_569_,
    private val searchUseCase: GenSearchUseCase_569_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_569_>(GenState_569_.Idle)
    val state: StateFlow<GenState_569_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_569_) {
        when (event) {
            is GenEvent_569_.Load -> loadAll()
            is GenEvent_569_.Update -> save(event.model)
            is GenEvent_569_.Delete -> delete(event.id)
            is GenEvent_569_.Refresh -> loadAll()
            is GenEvent_569_.Search -> search(event.query)
            is GenEvent_569_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_569_.Loading; _state.value = GenState_569_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_569_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_569_.Success(searchUseCase(query)) } }
}
