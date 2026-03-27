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

data class GenModel_473_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_473_ {
    data class Load(val id: Long) : GenEvent_473_()
    data class Update(val model: GenModel_473_) : GenEvent_473_()
    data class Delete(val id: Long) : GenEvent_473_()
    data object Refresh : GenEvent_473_()
    data class Search(val query: String) : GenEvent_473_()
    data class Filter(val predicate: String) : GenEvent_473_()
}

sealed class GenState_473_ {
    data object Idle : GenState_473_()
    data object Loading : GenState_473_()
    data class Success(val items: List<GenModel_473_>) : GenState_473_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_473_()
    data class Partial(val items: List<GenModel_473_>, val hasMore: Boolean) : GenState_473_()
}

interface GenRepository_473_ {
    suspend fun getAll(): List<GenModel_473_>
    suspend fun getById(id: Long): GenModel_473_?
    suspend fun save(model: GenModel_473_): GenModel_473_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_473_>
}

@Singleton
class GenRepositoryImpl_473_ @Inject constructor() : GenRepository_473_ {
    private val store = mutableMapOf<Long, GenModel_473_>()
    override suspend fun getAll(): List<GenModel_473_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_473_? = store[id]
    override suspend fun save(model: GenModel_473_): GenModel_473_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_473_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_473_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_473_ @Inject constructor(
    private val repository: GenRepositoryImpl_473_
) : GenUseCase_473_<Unit, List<GenModel_473_>> {
    override suspend fun invoke(params: Unit): List<GenModel_473_> = repository.getAll()
}

class GenSaveUseCase_473_ @Inject constructor(
    private val repository: GenRepositoryImpl_473_
) : GenUseCase_473_<GenModel_473_, GenModel_473_> {
    override suspend fun invoke(params: GenModel_473_): GenModel_473_ = repository.save(params)
}

class GenDeleteUseCase_473_ @Inject constructor(
    private val repository: GenRepositoryImpl_473_
) : GenUseCase_473_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_473_ @Inject constructor(
    private val repository: GenRepositoryImpl_473_
) : GenUseCase_473_<String, List<GenModel_473_>> {
    override suspend fun invoke(params: String): List<GenModel_473_> = repository.search(params)
}

abstract class GenMapper_473_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_473_ : GenMapper_473_<GenModel_473_, String>() {
    override fun map(input: GenModel_473_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_473_ : GenMapper_473_<String, GenModel_473_>() {
    override fun map(input: String): GenModel_473_ {
        val parts = input.split(":")
        return GenModel_473_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_473_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_473_,
    private val saveUseCase: GenSaveUseCase_473_,
    private val deleteUseCase: GenDeleteUseCase_473_,
    private val searchUseCase: GenSearchUseCase_473_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_473_>(GenState_473_.Idle)
    val state: StateFlow<GenState_473_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_473_) {
        when (event) {
            is GenEvent_473_.Load -> loadAll()
            is GenEvent_473_.Update -> save(event.model)
            is GenEvent_473_.Delete -> delete(event.id)
            is GenEvent_473_.Refresh -> loadAll()
            is GenEvent_473_.Search -> search(event.query)
            is GenEvent_473_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_473_.Loading; _state.value = GenState_473_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_473_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_473_.Success(searchUseCase(query)) } }
}
