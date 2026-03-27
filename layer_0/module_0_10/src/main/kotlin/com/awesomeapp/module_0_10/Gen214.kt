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

data class GenModel_214_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_214_ {
    data class Load(val id: Long) : GenEvent_214_()
    data class Update(val model: GenModel_214_) : GenEvent_214_()
    data class Delete(val id: Long) : GenEvent_214_()
    data object Refresh : GenEvent_214_()
    data class Search(val query: String) : GenEvent_214_()
    data class Filter(val predicate: String) : GenEvent_214_()
}

sealed class GenState_214_ {
    data object Idle : GenState_214_()
    data object Loading : GenState_214_()
    data class Success(val items: List<GenModel_214_>) : GenState_214_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_214_()
    data class Partial(val items: List<GenModel_214_>, val hasMore: Boolean) : GenState_214_()
}

interface GenRepository_214_ {
    suspend fun getAll(): List<GenModel_214_>
    suspend fun getById(id: Long): GenModel_214_?
    suspend fun save(model: GenModel_214_): GenModel_214_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_214_>
}

@Singleton
class GenRepositoryImpl_214_ @Inject constructor() : GenRepository_214_ {
    private val store = mutableMapOf<Long, GenModel_214_>()
    override suspend fun getAll(): List<GenModel_214_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_214_? = store[id]
    override suspend fun save(model: GenModel_214_): GenModel_214_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_214_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_214_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_214_ @Inject constructor(
    private val repository: GenRepositoryImpl_214_
) : GenUseCase_214_<Unit, List<GenModel_214_>> {
    override suspend fun invoke(params: Unit): List<GenModel_214_> = repository.getAll()
}

class GenSaveUseCase_214_ @Inject constructor(
    private val repository: GenRepositoryImpl_214_
) : GenUseCase_214_<GenModel_214_, GenModel_214_> {
    override suspend fun invoke(params: GenModel_214_): GenModel_214_ = repository.save(params)
}

class GenDeleteUseCase_214_ @Inject constructor(
    private val repository: GenRepositoryImpl_214_
) : GenUseCase_214_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_214_ @Inject constructor(
    private val repository: GenRepositoryImpl_214_
) : GenUseCase_214_<String, List<GenModel_214_>> {
    override suspend fun invoke(params: String): List<GenModel_214_> = repository.search(params)
}

abstract class GenMapper_214_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_214_ : GenMapper_214_<GenModel_214_, String>() {
    override fun map(input: GenModel_214_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_214_ : GenMapper_214_<String, GenModel_214_>() {
    override fun map(input: String): GenModel_214_ {
        val parts = input.split(":")
        return GenModel_214_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_214_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_214_,
    private val saveUseCase: GenSaveUseCase_214_,
    private val deleteUseCase: GenDeleteUseCase_214_,
    private val searchUseCase: GenSearchUseCase_214_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_214_>(GenState_214_.Idle)
    val state: StateFlow<GenState_214_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_214_) {
        when (event) {
            is GenEvent_214_.Load -> loadAll()
            is GenEvent_214_.Update -> save(event.model)
            is GenEvent_214_.Delete -> delete(event.id)
            is GenEvent_214_.Refresh -> loadAll()
            is GenEvent_214_.Search -> search(event.query)
            is GenEvent_214_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_214_.Loading; _state.value = GenState_214_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_214_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_214_.Success(searchUseCase(query)) } }
}
