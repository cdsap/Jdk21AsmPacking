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

data class GenModel_632_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_632_ {
    data class Load(val id: Long) : GenEvent_632_()
    data class Update(val model: GenModel_632_) : GenEvent_632_()
    data class Delete(val id: Long) : GenEvent_632_()
    data object Refresh : GenEvent_632_()
    data class Search(val query: String) : GenEvent_632_()
    data class Filter(val predicate: String) : GenEvent_632_()
}

sealed class GenState_632_ {
    data object Idle : GenState_632_()
    data object Loading : GenState_632_()
    data class Success(val items: List<GenModel_632_>) : GenState_632_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_632_()
    data class Partial(val items: List<GenModel_632_>, val hasMore: Boolean) : GenState_632_()
}

interface GenRepository_632_ {
    suspend fun getAll(): List<GenModel_632_>
    suspend fun getById(id: Long): GenModel_632_?
    suspend fun save(model: GenModel_632_): GenModel_632_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_632_>
}

@Singleton
class GenRepositoryImpl_632_ @Inject constructor() : GenRepository_632_ {
    private val store = mutableMapOf<Long, GenModel_632_>()
    override suspend fun getAll(): List<GenModel_632_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_632_? = store[id]
    override suspend fun save(model: GenModel_632_): GenModel_632_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_632_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_632_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_632_ @Inject constructor(
    private val repository: GenRepositoryImpl_632_
) : GenUseCase_632_<Unit, List<GenModel_632_>> {
    override suspend fun invoke(params: Unit): List<GenModel_632_> = repository.getAll()
}

class GenSaveUseCase_632_ @Inject constructor(
    private val repository: GenRepositoryImpl_632_
) : GenUseCase_632_<GenModel_632_, GenModel_632_> {
    override suspend fun invoke(params: GenModel_632_): GenModel_632_ = repository.save(params)
}

class GenDeleteUseCase_632_ @Inject constructor(
    private val repository: GenRepositoryImpl_632_
) : GenUseCase_632_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_632_ @Inject constructor(
    private val repository: GenRepositoryImpl_632_
) : GenUseCase_632_<String, List<GenModel_632_>> {
    override suspend fun invoke(params: String): List<GenModel_632_> = repository.search(params)
}

abstract class GenMapper_632_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_632_ : GenMapper_632_<GenModel_632_, String>() {
    override fun map(input: GenModel_632_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_632_ : GenMapper_632_<String, GenModel_632_>() {
    override fun map(input: String): GenModel_632_ {
        val parts = input.split(":")
        return GenModel_632_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_632_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_632_,
    private val saveUseCase: GenSaveUseCase_632_,
    private val deleteUseCase: GenDeleteUseCase_632_,
    private val searchUseCase: GenSearchUseCase_632_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_632_>(GenState_632_.Idle)
    val state: StateFlow<GenState_632_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_632_) {
        when (event) {
            is GenEvent_632_.Load -> loadAll()
            is GenEvent_632_.Update -> save(event.model)
            is GenEvent_632_.Delete -> delete(event.id)
            is GenEvent_632_.Refresh -> loadAll()
            is GenEvent_632_.Search -> search(event.query)
            is GenEvent_632_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_632_.Loading; _state.value = GenState_632_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_632_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_632_.Success(searchUseCase(query)) } }
}
