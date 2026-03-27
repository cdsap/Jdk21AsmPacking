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

data class GenModel_1520_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1520_ {
    data class Load(val id: Long) : GenEvent_1520_()
    data class Update(val model: GenModel_1520_) : GenEvent_1520_()
    data class Delete(val id: Long) : GenEvent_1520_()
    data object Refresh : GenEvent_1520_()
    data class Search(val query: String) : GenEvent_1520_()
    data class Filter(val predicate: String) : GenEvent_1520_()
}

sealed class GenState_1520_ {
    data object Idle : GenState_1520_()
    data object Loading : GenState_1520_()
    data class Success(val items: List<GenModel_1520_>) : GenState_1520_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1520_()
    data class Partial(val items: List<GenModel_1520_>, val hasMore: Boolean) : GenState_1520_()
}

interface GenRepository_1520_ {
    suspend fun getAll(): List<GenModel_1520_>
    suspend fun getById(id: Long): GenModel_1520_?
    suspend fun save(model: GenModel_1520_): GenModel_1520_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1520_>
}

@Singleton
class GenRepositoryImpl_1520_ @Inject constructor() : GenRepository_1520_ {
    private val store = mutableMapOf<Long, GenModel_1520_>()
    override suspend fun getAll(): List<GenModel_1520_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1520_? = store[id]
    override suspend fun save(model: GenModel_1520_): GenModel_1520_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1520_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1520_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1520_ @Inject constructor(
    private val repository: GenRepositoryImpl_1520_
) : GenUseCase_1520_<Unit, List<GenModel_1520_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1520_> = repository.getAll()
}

class GenSaveUseCase_1520_ @Inject constructor(
    private val repository: GenRepositoryImpl_1520_
) : GenUseCase_1520_<GenModel_1520_, GenModel_1520_> {
    override suspend fun invoke(params: GenModel_1520_): GenModel_1520_ = repository.save(params)
}

class GenDeleteUseCase_1520_ @Inject constructor(
    private val repository: GenRepositoryImpl_1520_
) : GenUseCase_1520_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1520_ @Inject constructor(
    private val repository: GenRepositoryImpl_1520_
) : GenUseCase_1520_<String, List<GenModel_1520_>> {
    override suspend fun invoke(params: String): List<GenModel_1520_> = repository.search(params)
}

abstract class GenMapper_1520_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1520_ : GenMapper_1520_<GenModel_1520_, String>() {
    override fun map(input: GenModel_1520_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1520_ : GenMapper_1520_<String, GenModel_1520_>() {
    override fun map(input: String): GenModel_1520_ {
        val parts = input.split(":")
        return GenModel_1520_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1520_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1520_,
    private val saveUseCase: GenSaveUseCase_1520_,
    private val deleteUseCase: GenDeleteUseCase_1520_,
    private val searchUseCase: GenSearchUseCase_1520_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1520_>(GenState_1520_.Idle)
    val state: StateFlow<GenState_1520_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1520_) {
        when (event) {
            is GenEvent_1520_.Load -> loadAll()
            is GenEvent_1520_.Update -> save(event.model)
            is GenEvent_1520_.Delete -> delete(event.id)
            is GenEvent_1520_.Refresh -> loadAll()
            is GenEvent_1520_.Search -> search(event.query)
            is GenEvent_1520_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1520_.Loading; _state.value = GenState_1520_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1520_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1520_.Success(searchUseCase(query)) } }
}
