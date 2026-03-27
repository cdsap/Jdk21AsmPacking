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

data class GenModel_216_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_216_ {
    data class Load(val id: Long) : GenEvent_216_()
    data class Update(val model: GenModel_216_) : GenEvent_216_()
    data class Delete(val id: Long) : GenEvent_216_()
    data object Refresh : GenEvent_216_()
    data class Search(val query: String) : GenEvent_216_()
    data class Filter(val predicate: String) : GenEvent_216_()
}

sealed class GenState_216_ {
    data object Idle : GenState_216_()
    data object Loading : GenState_216_()
    data class Success(val items: List<GenModel_216_>) : GenState_216_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_216_()
    data class Partial(val items: List<GenModel_216_>, val hasMore: Boolean) : GenState_216_()
}

interface GenRepository_216_ {
    suspend fun getAll(): List<GenModel_216_>
    suspend fun getById(id: Long): GenModel_216_?
    suspend fun save(model: GenModel_216_): GenModel_216_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_216_>
}

@Singleton
class GenRepositoryImpl_216_ @Inject constructor() : GenRepository_216_ {
    private val store = mutableMapOf<Long, GenModel_216_>()
    override suspend fun getAll(): List<GenModel_216_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_216_? = store[id]
    override suspend fun save(model: GenModel_216_): GenModel_216_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_216_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_216_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_216_ @Inject constructor(
    private val repository: GenRepositoryImpl_216_
) : GenUseCase_216_<Unit, List<GenModel_216_>> {
    override suspend fun invoke(params: Unit): List<GenModel_216_> = repository.getAll()
}

class GenSaveUseCase_216_ @Inject constructor(
    private val repository: GenRepositoryImpl_216_
) : GenUseCase_216_<GenModel_216_, GenModel_216_> {
    override suspend fun invoke(params: GenModel_216_): GenModel_216_ = repository.save(params)
}

class GenDeleteUseCase_216_ @Inject constructor(
    private val repository: GenRepositoryImpl_216_
) : GenUseCase_216_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_216_ @Inject constructor(
    private val repository: GenRepositoryImpl_216_
) : GenUseCase_216_<String, List<GenModel_216_>> {
    override suspend fun invoke(params: String): List<GenModel_216_> = repository.search(params)
}

abstract class GenMapper_216_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_216_ : GenMapper_216_<GenModel_216_, String>() {
    override fun map(input: GenModel_216_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_216_ : GenMapper_216_<String, GenModel_216_>() {
    override fun map(input: String): GenModel_216_ {
        val parts = input.split(":")
        return GenModel_216_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_216_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_216_,
    private val saveUseCase: GenSaveUseCase_216_,
    private val deleteUseCase: GenDeleteUseCase_216_,
    private val searchUseCase: GenSearchUseCase_216_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_216_>(GenState_216_.Idle)
    val state: StateFlow<GenState_216_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_216_) {
        when (event) {
            is GenEvent_216_.Load -> loadAll()
            is GenEvent_216_.Update -> save(event.model)
            is GenEvent_216_.Delete -> delete(event.id)
            is GenEvent_216_.Refresh -> loadAll()
            is GenEvent_216_.Search -> search(event.query)
            is GenEvent_216_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_216_.Loading; _state.value = GenState_216_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_216_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_216_.Success(searchUseCase(query)) } }
}
