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

data class GenModel_441_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_441_ {
    data class Load(val id: Long) : GenEvent_441_()
    data class Update(val model: GenModel_441_) : GenEvent_441_()
    data class Delete(val id: Long) : GenEvent_441_()
    data object Refresh : GenEvent_441_()
    data class Search(val query: String) : GenEvent_441_()
    data class Filter(val predicate: String) : GenEvent_441_()
}

sealed class GenState_441_ {
    data object Idle : GenState_441_()
    data object Loading : GenState_441_()
    data class Success(val items: List<GenModel_441_>) : GenState_441_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_441_()
    data class Partial(val items: List<GenModel_441_>, val hasMore: Boolean) : GenState_441_()
}

interface GenRepository_441_ {
    suspend fun getAll(): List<GenModel_441_>
    suspend fun getById(id: Long): GenModel_441_?
    suspend fun save(model: GenModel_441_): GenModel_441_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_441_>
}

@Singleton
class GenRepositoryImpl_441_ @Inject constructor() : GenRepository_441_ {
    private val store = mutableMapOf<Long, GenModel_441_>()
    override suspend fun getAll(): List<GenModel_441_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_441_? = store[id]
    override suspend fun save(model: GenModel_441_): GenModel_441_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_441_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_441_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_441_ @Inject constructor(
    private val repository: GenRepositoryImpl_441_
) : GenUseCase_441_<Unit, List<GenModel_441_>> {
    override suspend fun invoke(params: Unit): List<GenModel_441_> = repository.getAll()
}

class GenSaveUseCase_441_ @Inject constructor(
    private val repository: GenRepositoryImpl_441_
) : GenUseCase_441_<GenModel_441_, GenModel_441_> {
    override suspend fun invoke(params: GenModel_441_): GenModel_441_ = repository.save(params)
}

class GenDeleteUseCase_441_ @Inject constructor(
    private val repository: GenRepositoryImpl_441_
) : GenUseCase_441_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_441_ @Inject constructor(
    private val repository: GenRepositoryImpl_441_
) : GenUseCase_441_<String, List<GenModel_441_>> {
    override suspend fun invoke(params: String): List<GenModel_441_> = repository.search(params)
}

abstract class GenMapper_441_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_441_ : GenMapper_441_<GenModel_441_, String>() {
    override fun map(input: GenModel_441_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_441_ : GenMapper_441_<String, GenModel_441_>() {
    override fun map(input: String): GenModel_441_ {
        val parts = input.split(":")
        return GenModel_441_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_441_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_441_,
    private val saveUseCase: GenSaveUseCase_441_,
    private val deleteUseCase: GenDeleteUseCase_441_,
    private val searchUseCase: GenSearchUseCase_441_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_441_>(GenState_441_.Idle)
    val state: StateFlow<GenState_441_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_441_) {
        when (event) {
            is GenEvent_441_.Load -> loadAll()
            is GenEvent_441_.Update -> save(event.model)
            is GenEvent_441_.Delete -> delete(event.id)
            is GenEvent_441_.Refresh -> loadAll()
            is GenEvent_441_.Search -> search(event.query)
            is GenEvent_441_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_441_.Loading; _state.value = GenState_441_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_441_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_441_.Success(searchUseCase(query)) } }
}
