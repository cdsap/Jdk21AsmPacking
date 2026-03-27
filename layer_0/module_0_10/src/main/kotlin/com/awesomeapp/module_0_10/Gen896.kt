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

data class GenModel_896_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_896_ {
    data class Load(val id: Long) : GenEvent_896_()
    data class Update(val model: GenModel_896_) : GenEvent_896_()
    data class Delete(val id: Long) : GenEvent_896_()
    data object Refresh : GenEvent_896_()
    data class Search(val query: String) : GenEvent_896_()
    data class Filter(val predicate: String) : GenEvent_896_()
}

sealed class GenState_896_ {
    data object Idle : GenState_896_()
    data object Loading : GenState_896_()
    data class Success(val items: List<GenModel_896_>) : GenState_896_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_896_()
    data class Partial(val items: List<GenModel_896_>, val hasMore: Boolean) : GenState_896_()
}

interface GenRepository_896_ {
    suspend fun getAll(): List<GenModel_896_>
    suspend fun getById(id: Long): GenModel_896_?
    suspend fun save(model: GenModel_896_): GenModel_896_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_896_>
}

@Singleton
class GenRepositoryImpl_896_ @Inject constructor() : GenRepository_896_ {
    private val store = mutableMapOf<Long, GenModel_896_>()
    override suspend fun getAll(): List<GenModel_896_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_896_? = store[id]
    override suspend fun save(model: GenModel_896_): GenModel_896_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_896_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_896_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_896_ @Inject constructor(
    private val repository: GenRepositoryImpl_896_
) : GenUseCase_896_<Unit, List<GenModel_896_>> {
    override suspend fun invoke(params: Unit): List<GenModel_896_> = repository.getAll()
}

class GenSaveUseCase_896_ @Inject constructor(
    private val repository: GenRepositoryImpl_896_
) : GenUseCase_896_<GenModel_896_, GenModel_896_> {
    override suspend fun invoke(params: GenModel_896_): GenModel_896_ = repository.save(params)
}

class GenDeleteUseCase_896_ @Inject constructor(
    private val repository: GenRepositoryImpl_896_
) : GenUseCase_896_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_896_ @Inject constructor(
    private val repository: GenRepositoryImpl_896_
) : GenUseCase_896_<String, List<GenModel_896_>> {
    override suspend fun invoke(params: String): List<GenModel_896_> = repository.search(params)
}

abstract class GenMapper_896_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_896_ : GenMapper_896_<GenModel_896_, String>() {
    override fun map(input: GenModel_896_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_896_ : GenMapper_896_<String, GenModel_896_>() {
    override fun map(input: String): GenModel_896_ {
        val parts = input.split(":")
        return GenModel_896_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_896_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_896_,
    private val saveUseCase: GenSaveUseCase_896_,
    private val deleteUseCase: GenDeleteUseCase_896_,
    private val searchUseCase: GenSearchUseCase_896_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_896_>(GenState_896_.Idle)
    val state: StateFlow<GenState_896_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_896_) {
        when (event) {
            is GenEvent_896_.Load -> loadAll()
            is GenEvent_896_.Update -> save(event.model)
            is GenEvent_896_.Delete -> delete(event.id)
            is GenEvent_896_.Refresh -> loadAll()
            is GenEvent_896_.Search -> search(event.query)
            is GenEvent_896_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_896_.Loading; _state.value = GenState_896_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_896_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_896_.Success(searchUseCase(query)) } }
}
