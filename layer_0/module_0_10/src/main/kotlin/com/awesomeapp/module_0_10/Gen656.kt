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

data class GenModel_656_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_656_ {
    data class Load(val id: Long) : GenEvent_656_()
    data class Update(val model: GenModel_656_) : GenEvent_656_()
    data class Delete(val id: Long) : GenEvent_656_()
    data object Refresh : GenEvent_656_()
    data class Search(val query: String) : GenEvent_656_()
    data class Filter(val predicate: String) : GenEvent_656_()
}

sealed class GenState_656_ {
    data object Idle : GenState_656_()
    data object Loading : GenState_656_()
    data class Success(val items: List<GenModel_656_>) : GenState_656_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_656_()
    data class Partial(val items: List<GenModel_656_>, val hasMore: Boolean) : GenState_656_()
}

interface GenRepository_656_ {
    suspend fun getAll(): List<GenModel_656_>
    suspend fun getById(id: Long): GenModel_656_?
    suspend fun save(model: GenModel_656_): GenModel_656_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_656_>
}

@Singleton
class GenRepositoryImpl_656_ @Inject constructor() : GenRepository_656_ {
    private val store = mutableMapOf<Long, GenModel_656_>()
    override suspend fun getAll(): List<GenModel_656_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_656_? = store[id]
    override suspend fun save(model: GenModel_656_): GenModel_656_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_656_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_656_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_656_ @Inject constructor(
    private val repository: GenRepositoryImpl_656_
) : GenUseCase_656_<Unit, List<GenModel_656_>> {
    override suspend fun invoke(params: Unit): List<GenModel_656_> = repository.getAll()
}

class GenSaveUseCase_656_ @Inject constructor(
    private val repository: GenRepositoryImpl_656_
) : GenUseCase_656_<GenModel_656_, GenModel_656_> {
    override suspend fun invoke(params: GenModel_656_): GenModel_656_ = repository.save(params)
}

class GenDeleteUseCase_656_ @Inject constructor(
    private val repository: GenRepositoryImpl_656_
) : GenUseCase_656_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_656_ @Inject constructor(
    private val repository: GenRepositoryImpl_656_
) : GenUseCase_656_<String, List<GenModel_656_>> {
    override suspend fun invoke(params: String): List<GenModel_656_> = repository.search(params)
}

abstract class GenMapper_656_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_656_ : GenMapper_656_<GenModel_656_, String>() {
    override fun map(input: GenModel_656_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_656_ : GenMapper_656_<String, GenModel_656_>() {
    override fun map(input: String): GenModel_656_ {
        val parts = input.split(":")
        return GenModel_656_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_656_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_656_,
    private val saveUseCase: GenSaveUseCase_656_,
    private val deleteUseCase: GenDeleteUseCase_656_,
    private val searchUseCase: GenSearchUseCase_656_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_656_>(GenState_656_.Idle)
    val state: StateFlow<GenState_656_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_656_) {
        when (event) {
            is GenEvent_656_.Load -> loadAll()
            is GenEvent_656_.Update -> save(event.model)
            is GenEvent_656_.Delete -> delete(event.id)
            is GenEvent_656_.Refresh -> loadAll()
            is GenEvent_656_.Search -> search(event.query)
            is GenEvent_656_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_656_.Loading; _state.value = GenState_656_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_656_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_656_.Success(searchUseCase(query)) } }
}
