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

data class GenModel_395_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_395_ {
    data class Load(val id: Long) : GenEvent_395_()
    data class Update(val model: GenModel_395_) : GenEvent_395_()
    data class Delete(val id: Long) : GenEvent_395_()
    data object Refresh : GenEvent_395_()
    data class Search(val query: String) : GenEvent_395_()
    data class Filter(val predicate: String) : GenEvent_395_()
}

sealed class GenState_395_ {
    data object Idle : GenState_395_()
    data object Loading : GenState_395_()
    data class Success(val items: List<GenModel_395_>) : GenState_395_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_395_()
    data class Partial(val items: List<GenModel_395_>, val hasMore: Boolean) : GenState_395_()
}

interface GenRepository_395_ {
    suspend fun getAll(): List<GenModel_395_>
    suspend fun getById(id: Long): GenModel_395_?
    suspend fun save(model: GenModel_395_): GenModel_395_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_395_>
}

@Singleton
class GenRepositoryImpl_395_ @Inject constructor() : GenRepository_395_ {
    private val store = mutableMapOf<Long, GenModel_395_>()
    override suspend fun getAll(): List<GenModel_395_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_395_? = store[id]
    override suspend fun save(model: GenModel_395_): GenModel_395_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_395_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_395_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_395_ @Inject constructor(
    private val repository: GenRepositoryImpl_395_
) : GenUseCase_395_<Unit, List<GenModel_395_>> {
    override suspend fun invoke(params: Unit): List<GenModel_395_> = repository.getAll()
}

class GenSaveUseCase_395_ @Inject constructor(
    private val repository: GenRepositoryImpl_395_
) : GenUseCase_395_<GenModel_395_, GenModel_395_> {
    override suspend fun invoke(params: GenModel_395_): GenModel_395_ = repository.save(params)
}

class GenDeleteUseCase_395_ @Inject constructor(
    private val repository: GenRepositoryImpl_395_
) : GenUseCase_395_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_395_ @Inject constructor(
    private val repository: GenRepositoryImpl_395_
) : GenUseCase_395_<String, List<GenModel_395_>> {
    override suspend fun invoke(params: String): List<GenModel_395_> = repository.search(params)
}

abstract class GenMapper_395_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_395_ : GenMapper_395_<GenModel_395_, String>() {
    override fun map(input: GenModel_395_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_395_ : GenMapper_395_<String, GenModel_395_>() {
    override fun map(input: String): GenModel_395_ {
        val parts = input.split(":")
        return GenModel_395_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_395_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_395_,
    private val saveUseCase: GenSaveUseCase_395_,
    private val deleteUseCase: GenDeleteUseCase_395_,
    private val searchUseCase: GenSearchUseCase_395_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_395_>(GenState_395_.Idle)
    val state: StateFlow<GenState_395_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_395_) {
        when (event) {
            is GenEvent_395_.Load -> loadAll()
            is GenEvent_395_.Update -> save(event.model)
            is GenEvent_395_.Delete -> delete(event.id)
            is GenEvent_395_.Refresh -> loadAll()
            is GenEvent_395_.Search -> search(event.query)
            is GenEvent_395_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_395_.Loading; _state.value = GenState_395_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_395_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_395_.Success(searchUseCase(query)) } }
}
