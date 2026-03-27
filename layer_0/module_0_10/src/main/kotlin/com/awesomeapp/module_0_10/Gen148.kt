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

data class GenModel_148_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_148_ {
    data class Load(val id: Long) : GenEvent_148_()
    data class Update(val model: GenModel_148_) : GenEvent_148_()
    data class Delete(val id: Long) : GenEvent_148_()
    data object Refresh : GenEvent_148_()
    data class Search(val query: String) : GenEvent_148_()
    data class Filter(val predicate: String) : GenEvent_148_()
}

sealed class GenState_148_ {
    data object Idle : GenState_148_()
    data object Loading : GenState_148_()
    data class Success(val items: List<GenModel_148_>) : GenState_148_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_148_()
    data class Partial(val items: List<GenModel_148_>, val hasMore: Boolean) : GenState_148_()
}

interface GenRepository_148_ {
    suspend fun getAll(): List<GenModel_148_>
    suspend fun getById(id: Long): GenModel_148_?
    suspend fun save(model: GenModel_148_): GenModel_148_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_148_>
}

@Singleton
class GenRepositoryImpl_148_ @Inject constructor() : GenRepository_148_ {
    private val store = mutableMapOf<Long, GenModel_148_>()
    override suspend fun getAll(): List<GenModel_148_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_148_? = store[id]
    override suspend fun save(model: GenModel_148_): GenModel_148_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_148_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_148_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_148_ @Inject constructor(
    private val repository: GenRepositoryImpl_148_
) : GenUseCase_148_<Unit, List<GenModel_148_>> {
    override suspend fun invoke(params: Unit): List<GenModel_148_> = repository.getAll()
}

class GenSaveUseCase_148_ @Inject constructor(
    private val repository: GenRepositoryImpl_148_
) : GenUseCase_148_<GenModel_148_, GenModel_148_> {
    override suspend fun invoke(params: GenModel_148_): GenModel_148_ = repository.save(params)
}

class GenDeleteUseCase_148_ @Inject constructor(
    private val repository: GenRepositoryImpl_148_
) : GenUseCase_148_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_148_ @Inject constructor(
    private val repository: GenRepositoryImpl_148_
) : GenUseCase_148_<String, List<GenModel_148_>> {
    override suspend fun invoke(params: String): List<GenModel_148_> = repository.search(params)
}

abstract class GenMapper_148_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_148_ : GenMapper_148_<GenModel_148_, String>() {
    override fun map(input: GenModel_148_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_148_ : GenMapper_148_<String, GenModel_148_>() {
    override fun map(input: String): GenModel_148_ {
        val parts = input.split(":")
        return GenModel_148_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_148_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_148_,
    private val saveUseCase: GenSaveUseCase_148_,
    private val deleteUseCase: GenDeleteUseCase_148_,
    private val searchUseCase: GenSearchUseCase_148_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_148_>(GenState_148_.Idle)
    val state: StateFlow<GenState_148_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_148_) {
        when (event) {
            is GenEvent_148_.Load -> loadAll()
            is GenEvent_148_.Update -> save(event.model)
            is GenEvent_148_.Delete -> delete(event.id)
            is GenEvent_148_.Refresh -> loadAll()
            is GenEvent_148_.Search -> search(event.query)
            is GenEvent_148_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_148_.Loading; _state.value = GenState_148_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_148_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_148_.Success(searchUseCase(query)) } }
}
