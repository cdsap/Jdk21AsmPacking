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

data class GenModel_1340_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1340_ {
    data class Load(val id: Long) : GenEvent_1340_()
    data class Update(val model: GenModel_1340_) : GenEvent_1340_()
    data class Delete(val id: Long) : GenEvent_1340_()
    data object Refresh : GenEvent_1340_()
    data class Search(val query: String) : GenEvent_1340_()
    data class Filter(val predicate: String) : GenEvent_1340_()
}

sealed class GenState_1340_ {
    data object Idle : GenState_1340_()
    data object Loading : GenState_1340_()
    data class Success(val items: List<GenModel_1340_>) : GenState_1340_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1340_()
    data class Partial(val items: List<GenModel_1340_>, val hasMore: Boolean) : GenState_1340_()
}

interface GenRepository_1340_ {
    suspend fun getAll(): List<GenModel_1340_>
    suspend fun getById(id: Long): GenModel_1340_?
    suspend fun save(model: GenModel_1340_): GenModel_1340_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1340_>
}

@Singleton
class GenRepositoryImpl_1340_ @Inject constructor() : GenRepository_1340_ {
    private val store = mutableMapOf<Long, GenModel_1340_>()
    override suspend fun getAll(): List<GenModel_1340_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1340_? = store[id]
    override suspend fun save(model: GenModel_1340_): GenModel_1340_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1340_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1340_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1340_ @Inject constructor(
    private val repository: GenRepositoryImpl_1340_
) : GenUseCase_1340_<Unit, List<GenModel_1340_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1340_> = repository.getAll()
}

class GenSaveUseCase_1340_ @Inject constructor(
    private val repository: GenRepositoryImpl_1340_
) : GenUseCase_1340_<GenModel_1340_, GenModel_1340_> {
    override suspend fun invoke(params: GenModel_1340_): GenModel_1340_ = repository.save(params)
}

class GenDeleteUseCase_1340_ @Inject constructor(
    private val repository: GenRepositoryImpl_1340_
) : GenUseCase_1340_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1340_ @Inject constructor(
    private val repository: GenRepositoryImpl_1340_
) : GenUseCase_1340_<String, List<GenModel_1340_>> {
    override suspend fun invoke(params: String): List<GenModel_1340_> = repository.search(params)
}

abstract class GenMapper_1340_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1340_ : GenMapper_1340_<GenModel_1340_, String>() {
    override fun map(input: GenModel_1340_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1340_ : GenMapper_1340_<String, GenModel_1340_>() {
    override fun map(input: String): GenModel_1340_ {
        val parts = input.split(":")
        return GenModel_1340_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1340_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1340_,
    private val saveUseCase: GenSaveUseCase_1340_,
    private val deleteUseCase: GenDeleteUseCase_1340_,
    private val searchUseCase: GenSearchUseCase_1340_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1340_>(GenState_1340_.Idle)
    val state: StateFlow<GenState_1340_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1340_) {
        when (event) {
            is GenEvent_1340_.Load -> loadAll()
            is GenEvent_1340_.Update -> save(event.model)
            is GenEvent_1340_.Delete -> delete(event.id)
            is GenEvent_1340_.Refresh -> loadAll()
            is GenEvent_1340_.Search -> search(event.query)
            is GenEvent_1340_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1340_.Loading; _state.value = GenState_1340_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1340_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1340_.Success(searchUseCase(query)) } }
}
