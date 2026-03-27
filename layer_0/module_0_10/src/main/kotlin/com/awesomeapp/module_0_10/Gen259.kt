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

data class GenModel_259_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_259_ {
    data class Load(val id: Long) : GenEvent_259_()
    data class Update(val model: GenModel_259_) : GenEvent_259_()
    data class Delete(val id: Long) : GenEvent_259_()
    data object Refresh : GenEvent_259_()
    data class Search(val query: String) : GenEvent_259_()
    data class Filter(val predicate: String) : GenEvent_259_()
}

sealed class GenState_259_ {
    data object Idle : GenState_259_()
    data object Loading : GenState_259_()
    data class Success(val items: List<GenModel_259_>) : GenState_259_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_259_()
    data class Partial(val items: List<GenModel_259_>, val hasMore: Boolean) : GenState_259_()
}

interface GenRepository_259_ {
    suspend fun getAll(): List<GenModel_259_>
    suspend fun getById(id: Long): GenModel_259_?
    suspend fun save(model: GenModel_259_): GenModel_259_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_259_>
}

@Singleton
class GenRepositoryImpl_259_ @Inject constructor() : GenRepository_259_ {
    private val store = mutableMapOf<Long, GenModel_259_>()
    override suspend fun getAll(): List<GenModel_259_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_259_? = store[id]
    override suspend fun save(model: GenModel_259_): GenModel_259_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_259_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_259_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_259_ @Inject constructor(
    private val repository: GenRepositoryImpl_259_
) : GenUseCase_259_<Unit, List<GenModel_259_>> {
    override suspend fun invoke(params: Unit): List<GenModel_259_> = repository.getAll()
}

class GenSaveUseCase_259_ @Inject constructor(
    private val repository: GenRepositoryImpl_259_
) : GenUseCase_259_<GenModel_259_, GenModel_259_> {
    override suspend fun invoke(params: GenModel_259_): GenModel_259_ = repository.save(params)
}

class GenDeleteUseCase_259_ @Inject constructor(
    private val repository: GenRepositoryImpl_259_
) : GenUseCase_259_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_259_ @Inject constructor(
    private val repository: GenRepositoryImpl_259_
) : GenUseCase_259_<String, List<GenModel_259_>> {
    override suspend fun invoke(params: String): List<GenModel_259_> = repository.search(params)
}

abstract class GenMapper_259_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_259_ : GenMapper_259_<GenModel_259_, String>() {
    override fun map(input: GenModel_259_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_259_ : GenMapper_259_<String, GenModel_259_>() {
    override fun map(input: String): GenModel_259_ {
        val parts = input.split(":")
        return GenModel_259_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_259_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_259_,
    private val saveUseCase: GenSaveUseCase_259_,
    private val deleteUseCase: GenDeleteUseCase_259_,
    private val searchUseCase: GenSearchUseCase_259_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_259_>(GenState_259_.Idle)
    val state: StateFlow<GenState_259_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_259_) {
        when (event) {
            is GenEvent_259_.Load -> loadAll()
            is GenEvent_259_.Update -> save(event.model)
            is GenEvent_259_.Delete -> delete(event.id)
            is GenEvent_259_.Refresh -> loadAll()
            is GenEvent_259_.Search -> search(event.query)
            is GenEvent_259_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_259_.Loading; _state.value = GenState_259_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_259_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_259_.Success(searchUseCase(query)) } }
}
