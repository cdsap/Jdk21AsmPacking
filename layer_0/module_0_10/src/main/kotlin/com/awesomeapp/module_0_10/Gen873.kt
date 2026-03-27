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

data class GenModel_873_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_873_ {
    data class Load(val id: Long) : GenEvent_873_()
    data class Update(val model: GenModel_873_) : GenEvent_873_()
    data class Delete(val id: Long) : GenEvent_873_()
    data object Refresh : GenEvent_873_()
    data class Search(val query: String) : GenEvent_873_()
    data class Filter(val predicate: String) : GenEvent_873_()
}

sealed class GenState_873_ {
    data object Idle : GenState_873_()
    data object Loading : GenState_873_()
    data class Success(val items: List<GenModel_873_>) : GenState_873_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_873_()
    data class Partial(val items: List<GenModel_873_>, val hasMore: Boolean) : GenState_873_()
}

interface GenRepository_873_ {
    suspend fun getAll(): List<GenModel_873_>
    suspend fun getById(id: Long): GenModel_873_?
    suspend fun save(model: GenModel_873_): GenModel_873_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_873_>
}

@Singleton
class GenRepositoryImpl_873_ @Inject constructor() : GenRepository_873_ {
    private val store = mutableMapOf<Long, GenModel_873_>()
    override suspend fun getAll(): List<GenModel_873_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_873_? = store[id]
    override suspend fun save(model: GenModel_873_): GenModel_873_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_873_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_873_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_873_ @Inject constructor(
    private val repository: GenRepositoryImpl_873_
) : GenUseCase_873_<Unit, List<GenModel_873_>> {
    override suspend fun invoke(params: Unit): List<GenModel_873_> = repository.getAll()
}

class GenSaveUseCase_873_ @Inject constructor(
    private val repository: GenRepositoryImpl_873_
) : GenUseCase_873_<GenModel_873_, GenModel_873_> {
    override suspend fun invoke(params: GenModel_873_): GenModel_873_ = repository.save(params)
}

class GenDeleteUseCase_873_ @Inject constructor(
    private val repository: GenRepositoryImpl_873_
) : GenUseCase_873_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_873_ @Inject constructor(
    private val repository: GenRepositoryImpl_873_
) : GenUseCase_873_<String, List<GenModel_873_>> {
    override suspend fun invoke(params: String): List<GenModel_873_> = repository.search(params)
}

abstract class GenMapper_873_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_873_ : GenMapper_873_<GenModel_873_, String>() {
    override fun map(input: GenModel_873_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_873_ : GenMapper_873_<String, GenModel_873_>() {
    override fun map(input: String): GenModel_873_ {
        val parts = input.split(":")
        return GenModel_873_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_873_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_873_,
    private val saveUseCase: GenSaveUseCase_873_,
    private val deleteUseCase: GenDeleteUseCase_873_,
    private val searchUseCase: GenSearchUseCase_873_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_873_>(GenState_873_.Idle)
    val state: StateFlow<GenState_873_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_873_) {
        when (event) {
            is GenEvent_873_.Load -> loadAll()
            is GenEvent_873_.Update -> save(event.model)
            is GenEvent_873_.Delete -> delete(event.id)
            is GenEvent_873_.Refresh -> loadAll()
            is GenEvent_873_.Search -> search(event.query)
            is GenEvent_873_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_873_.Loading; _state.value = GenState_873_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_873_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_873_.Success(searchUseCase(query)) } }
}
