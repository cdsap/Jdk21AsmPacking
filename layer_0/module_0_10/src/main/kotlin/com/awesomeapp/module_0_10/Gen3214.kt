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

data class GenModel_3214_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3214_ {
    data class Load(val id: Long) : GenEvent_3214_()
    data class Update(val model: GenModel_3214_) : GenEvent_3214_()
    data class Delete(val id: Long) : GenEvent_3214_()
    data object Refresh : GenEvent_3214_()
    data class Search(val query: String) : GenEvent_3214_()
    data class Filter(val predicate: String) : GenEvent_3214_()
}

sealed class GenState_3214_ {
    data object Idle : GenState_3214_()
    data object Loading : GenState_3214_()
    data class Success(val items: List<GenModel_3214_>) : GenState_3214_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3214_()
    data class Partial(val items: List<GenModel_3214_>, val hasMore: Boolean) : GenState_3214_()
}

interface GenRepository_3214_ {
    suspend fun getAll(): List<GenModel_3214_>
    suspend fun getById(id: Long): GenModel_3214_?
    suspend fun save(model: GenModel_3214_): GenModel_3214_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3214_>
}

@Singleton
class GenRepositoryImpl_3214_ @Inject constructor() : GenRepository_3214_ {
    private val store = mutableMapOf<Long, GenModel_3214_>()
    override suspend fun getAll(): List<GenModel_3214_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3214_? = store[id]
    override suspend fun save(model: GenModel_3214_): GenModel_3214_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3214_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3214_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3214_ @Inject constructor(
    private val repository: GenRepositoryImpl_3214_
) : GenUseCase_3214_<Unit, List<GenModel_3214_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3214_> = repository.getAll()
}

class GenSaveUseCase_3214_ @Inject constructor(
    private val repository: GenRepositoryImpl_3214_
) : GenUseCase_3214_<GenModel_3214_, GenModel_3214_> {
    override suspend fun invoke(params: GenModel_3214_): GenModel_3214_ = repository.save(params)
}

class GenDeleteUseCase_3214_ @Inject constructor(
    private val repository: GenRepositoryImpl_3214_
) : GenUseCase_3214_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3214_ @Inject constructor(
    private val repository: GenRepositoryImpl_3214_
) : GenUseCase_3214_<String, List<GenModel_3214_>> {
    override suspend fun invoke(params: String): List<GenModel_3214_> = repository.search(params)
}

abstract class GenMapper_3214_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3214_ : GenMapper_3214_<GenModel_3214_, String>() {
    override fun map(input: GenModel_3214_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3214_ : GenMapper_3214_<String, GenModel_3214_>() {
    override fun map(input: String): GenModel_3214_ {
        val parts = input.split(":")
        return GenModel_3214_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3214_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3214_,
    private val saveUseCase: GenSaveUseCase_3214_,
    private val deleteUseCase: GenDeleteUseCase_3214_,
    private val searchUseCase: GenSearchUseCase_3214_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3214_>(GenState_3214_.Idle)
    val state: StateFlow<GenState_3214_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3214_) {
        when (event) {
            is GenEvent_3214_.Load -> loadAll()
            is GenEvent_3214_.Update -> save(event.model)
            is GenEvent_3214_.Delete -> delete(event.id)
            is GenEvent_3214_.Refresh -> loadAll()
            is GenEvent_3214_.Search -> search(event.query)
            is GenEvent_3214_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3214_.Loading; _state.value = GenState_3214_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3214_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3214_.Success(searchUseCase(query)) } }
}
