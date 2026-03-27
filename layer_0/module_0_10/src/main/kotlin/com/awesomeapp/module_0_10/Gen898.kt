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

data class GenModel_898_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_898_ {
    data class Load(val id: Long) : GenEvent_898_()
    data class Update(val model: GenModel_898_) : GenEvent_898_()
    data class Delete(val id: Long) : GenEvent_898_()
    data object Refresh : GenEvent_898_()
    data class Search(val query: String) : GenEvent_898_()
    data class Filter(val predicate: String) : GenEvent_898_()
}

sealed class GenState_898_ {
    data object Idle : GenState_898_()
    data object Loading : GenState_898_()
    data class Success(val items: List<GenModel_898_>) : GenState_898_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_898_()
    data class Partial(val items: List<GenModel_898_>, val hasMore: Boolean) : GenState_898_()
}

interface GenRepository_898_ {
    suspend fun getAll(): List<GenModel_898_>
    suspend fun getById(id: Long): GenModel_898_?
    suspend fun save(model: GenModel_898_): GenModel_898_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_898_>
}

@Singleton
class GenRepositoryImpl_898_ @Inject constructor() : GenRepository_898_ {
    private val store = mutableMapOf<Long, GenModel_898_>()
    override suspend fun getAll(): List<GenModel_898_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_898_? = store[id]
    override suspend fun save(model: GenModel_898_): GenModel_898_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_898_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_898_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_898_ @Inject constructor(
    private val repository: GenRepositoryImpl_898_
) : GenUseCase_898_<Unit, List<GenModel_898_>> {
    override suspend fun invoke(params: Unit): List<GenModel_898_> = repository.getAll()
}

class GenSaveUseCase_898_ @Inject constructor(
    private val repository: GenRepositoryImpl_898_
) : GenUseCase_898_<GenModel_898_, GenModel_898_> {
    override suspend fun invoke(params: GenModel_898_): GenModel_898_ = repository.save(params)
}

class GenDeleteUseCase_898_ @Inject constructor(
    private val repository: GenRepositoryImpl_898_
) : GenUseCase_898_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_898_ @Inject constructor(
    private val repository: GenRepositoryImpl_898_
) : GenUseCase_898_<String, List<GenModel_898_>> {
    override suspend fun invoke(params: String): List<GenModel_898_> = repository.search(params)
}

abstract class GenMapper_898_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_898_ : GenMapper_898_<GenModel_898_, String>() {
    override fun map(input: GenModel_898_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_898_ : GenMapper_898_<String, GenModel_898_>() {
    override fun map(input: String): GenModel_898_ {
        val parts = input.split(":")
        return GenModel_898_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_898_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_898_,
    private val saveUseCase: GenSaveUseCase_898_,
    private val deleteUseCase: GenDeleteUseCase_898_,
    private val searchUseCase: GenSearchUseCase_898_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_898_>(GenState_898_.Idle)
    val state: StateFlow<GenState_898_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_898_) {
        when (event) {
            is GenEvent_898_.Load -> loadAll()
            is GenEvent_898_.Update -> save(event.model)
            is GenEvent_898_.Delete -> delete(event.id)
            is GenEvent_898_.Refresh -> loadAll()
            is GenEvent_898_.Search -> search(event.query)
            is GenEvent_898_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_898_.Loading; _state.value = GenState_898_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_898_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_898_.Success(searchUseCase(query)) } }
}
