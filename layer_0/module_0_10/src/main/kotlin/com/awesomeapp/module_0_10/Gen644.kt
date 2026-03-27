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

data class GenModel_644_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_644_ {
    data class Load(val id: Long) : GenEvent_644_()
    data class Update(val model: GenModel_644_) : GenEvent_644_()
    data class Delete(val id: Long) : GenEvent_644_()
    data object Refresh : GenEvent_644_()
    data class Search(val query: String) : GenEvent_644_()
    data class Filter(val predicate: String) : GenEvent_644_()
}

sealed class GenState_644_ {
    data object Idle : GenState_644_()
    data object Loading : GenState_644_()
    data class Success(val items: List<GenModel_644_>) : GenState_644_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_644_()
    data class Partial(val items: List<GenModel_644_>, val hasMore: Boolean) : GenState_644_()
}

interface GenRepository_644_ {
    suspend fun getAll(): List<GenModel_644_>
    suspend fun getById(id: Long): GenModel_644_?
    suspend fun save(model: GenModel_644_): GenModel_644_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_644_>
}

@Singleton
class GenRepositoryImpl_644_ @Inject constructor() : GenRepository_644_ {
    private val store = mutableMapOf<Long, GenModel_644_>()
    override suspend fun getAll(): List<GenModel_644_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_644_? = store[id]
    override suspend fun save(model: GenModel_644_): GenModel_644_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_644_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_644_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_644_ @Inject constructor(
    private val repository: GenRepositoryImpl_644_
) : GenUseCase_644_<Unit, List<GenModel_644_>> {
    override suspend fun invoke(params: Unit): List<GenModel_644_> = repository.getAll()
}

class GenSaveUseCase_644_ @Inject constructor(
    private val repository: GenRepositoryImpl_644_
) : GenUseCase_644_<GenModel_644_, GenModel_644_> {
    override suspend fun invoke(params: GenModel_644_): GenModel_644_ = repository.save(params)
}

class GenDeleteUseCase_644_ @Inject constructor(
    private val repository: GenRepositoryImpl_644_
) : GenUseCase_644_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_644_ @Inject constructor(
    private val repository: GenRepositoryImpl_644_
) : GenUseCase_644_<String, List<GenModel_644_>> {
    override suspend fun invoke(params: String): List<GenModel_644_> = repository.search(params)
}

abstract class GenMapper_644_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_644_ : GenMapper_644_<GenModel_644_, String>() {
    override fun map(input: GenModel_644_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_644_ : GenMapper_644_<String, GenModel_644_>() {
    override fun map(input: String): GenModel_644_ {
        val parts = input.split(":")
        return GenModel_644_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_644_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_644_,
    private val saveUseCase: GenSaveUseCase_644_,
    private val deleteUseCase: GenDeleteUseCase_644_,
    private val searchUseCase: GenSearchUseCase_644_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_644_>(GenState_644_.Idle)
    val state: StateFlow<GenState_644_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_644_) {
        when (event) {
            is GenEvent_644_.Load -> loadAll()
            is GenEvent_644_.Update -> save(event.model)
            is GenEvent_644_.Delete -> delete(event.id)
            is GenEvent_644_.Refresh -> loadAll()
            is GenEvent_644_.Search -> search(event.query)
            is GenEvent_644_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_644_.Loading; _state.value = GenState_644_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_644_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_644_.Success(searchUseCase(query)) } }
}
