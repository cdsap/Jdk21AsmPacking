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

data class GenModel_817_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_817_ {
    data class Load(val id: Long) : GenEvent_817_()
    data class Update(val model: GenModel_817_) : GenEvent_817_()
    data class Delete(val id: Long) : GenEvent_817_()
    data object Refresh : GenEvent_817_()
    data class Search(val query: String) : GenEvent_817_()
    data class Filter(val predicate: String) : GenEvent_817_()
}

sealed class GenState_817_ {
    data object Idle : GenState_817_()
    data object Loading : GenState_817_()
    data class Success(val items: List<GenModel_817_>) : GenState_817_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_817_()
    data class Partial(val items: List<GenModel_817_>, val hasMore: Boolean) : GenState_817_()
}

interface GenRepository_817_ {
    suspend fun getAll(): List<GenModel_817_>
    suspend fun getById(id: Long): GenModel_817_?
    suspend fun save(model: GenModel_817_): GenModel_817_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_817_>
}

@Singleton
class GenRepositoryImpl_817_ @Inject constructor() : GenRepository_817_ {
    private val store = mutableMapOf<Long, GenModel_817_>()
    override suspend fun getAll(): List<GenModel_817_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_817_? = store[id]
    override suspend fun save(model: GenModel_817_): GenModel_817_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_817_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_817_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_817_ @Inject constructor(
    private val repository: GenRepositoryImpl_817_
) : GenUseCase_817_<Unit, List<GenModel_817_>> {
    override suspend fun invoke(params: Unit): List<GenModel_817_> = repository.getAll()
}

class GenSaveUseCase_817_ @Inject constructor(
    private val repository: GenRepositoryImpl_817_
) : GenUseCase_817_<GenModel_817_, GenModel_817_> {
    override suspend fun invoke(params: GenModel_817_): GenModel_817_ = repository.save(params)
}

class GenDeleteUseCase_817_ @Inject constructor(
    private val repository: GenRepositoryImpl_817_
) : GenUseCase_817_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_817_ @Inject constructor(
    private val repository: GenRepositoryImpl_817_
) : GenUseCase_817_<String, List<GenModel_817_>> {
    override suspend fun invoke(params: String): List<GenModel_817_> = repository.search(params)
}

abstract class GenMapper_817_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_817_ : GenMapper_817_<GenModel_817_, String>() {
    override fun map(input: GenModel_817_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_817_ : GenMapper_817_<String, GenModel_817_>() {
    override fun map(input: String): GenModel_817_ {
        val parts = input.split(":")
        return GenModel_817_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_817_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_817_,
    private val saveUseCase: GenSaveUseCase_817_,
    private val deleteUseCase: GenDeleteUseCase_817_,
    private val searchUseCase: GenSearchUseCase_817_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_817_>(GenState_817_.Idle)
    val state: StateFlow<GenState_817_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_817_) {
        when (event) {
            is GenEvent_817_.Load -> loadAll()
            is GenEvent_817_.Update -> save(event.model)
            is GenEvent_817_.Delete -> delete(event.id)
            is GenEvent_817_.Refresh -> loadAll()
            is GenEvent_817_.Search -> search(event.query)
            is GenEvent_817_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_817_.Loading; _state.value = GenState_817_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_817_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_817_.Success(searchUseCase(query)) } }
}
