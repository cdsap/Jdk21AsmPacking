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

data class GenModel_520_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_520_ {
    data class Load(val id: Long) : GenEvent_520_()
    data class Update(val model: GenModel_520_) : GenEvent_520_()
    data class Delete(val id: Long) : GenEvent_520_()
    data object Refresh : GenEvent_520_()
    data class Search(val query: String) : GenEvent_520_()
    data class Filter(val predicate: String) : GenEvent_520_()
}

sealed class GenState_520_ {
    data object Idle : GenState_520_()
    data object Loading : GenState_520_()
    data class Success(val items: List<GenModel_520_>) : GenState_520_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_520_()
    data class Partial(val items: List<GenModel_520_>, val hasMore: Boolean) : GenState_520_()
}

interface GenRepository_520_ {
    suspend fun getAll(): List<GenModel_520_>
    suspend fun getById(id: Long): GenModel_520_?
    suspend fun save(model: GenModel_520_): GenModel_520_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_520_>
}

@Singleton
class GenRepositoryImpl_520_ @Inject constructor() : GenRepository_520_ {
    private val store = mutableMapOf<Long, GenModel_520_>()
    override suspend fun getAll(): List<GenModel_520_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_520_? = store[id]
    override suspend fun save(model: GenModel_520_): GenModel_520_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_520_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_520_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_520_ @Inject constructor(
    private val repository: GenRepositoryImpl_520_
) : GenUseCase_520_<Unit, List<GenModel_520_>> {
    override suspend fun invoke(params: Unit): List<GenModel_520_> = repository.getAll()
}

class GenSaveUseCase_520_ @Inject constructor(
    private val repository: GenRepositoryImpl_520_
) : GenUseCase_520_<GenModel_520_, GenModel_520_> {
    override suspend fun invoke(params: GenModel_520_): GenModel_520_ = repository.save(params)
}

class GenDeleteUseCase_520_ @Inject constructor(
    private val repository: GenRepositoryImpl_520_
) : GenUseCase_520_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_520_ @Inject constructor(
    private val repository: GenRepositoryImpl_520_
) : GenUseCase_520_<String, List<GenModel_520_>> {
    override suspend fun invoke(params: String): List<GenModel_520_> = repository.search(params)
}

abstract class GenMapper_520_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_520_ : GenMapper_520_<GenModel_520_, String>() {
    override fun map(input: GenModel_520_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_520_ : GenMapper_520_<String, GenModel_520_>() {
    override fun map(input: String): GenModel_520_ {
        val parts = input.split(":")
        return GenModel_520_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_520_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_520_,
    private val saveUseCase: GenSaveUseCase_520_,
    private val deleteUseCase: GenDeleteUseCase_520_,
    private val searchUseCase: GenSearchUseCase_520_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_520_>(GenState_520_.Idle)
    val state: StateFlow<GenState_520_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_520_) {
        when (event) {
            is GenEvent_520_.Load -> loadAll()
            is GenEvent_520_.Update -> save(event.model)
            is GenEvent_520_.Delete -> delete(event.id)
            is GenEvent_520_.Refresh -> loadAll()
            is GenEvent_520_.Search -> search(event.query)
            is GenEvent_520_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_520_.Loading; _state.value = GenState_520_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_520_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_520_.Success(searchUseCase(query)) } }
}
