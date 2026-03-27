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

data class GenModel_758_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_758_ {
    data class Load(val id: Long) : GenEvent_758_()
    data class Update(val model: GenModel_758_) : GenEvent_758_()
    data class Delete(val id: Long) : GenEvent_758_()
    data object Refresh : GenEvent_758_()
    data class Search(val query: String) : GenEvent_758_()
    data class Filter(val predicate: String) : GenEvent_758_()
}

sealed class GenState_758_ {
    data object Idle : GenState_758_()
    data object Loading : GenState_758_()
    data class Success(val items: List<GenModel_758_>) : GenState_758_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_758_()
    data class Partial(val items: List<GenModel_758_>, val hasMore: Boolean) : GenState_758_()
}

interface GenRepository_758_ {
    suspend fun getAll(): List<GenModel_758_>
    suspend fun getById(id: Long): GenModel_758_?
    suspend fun save(model: GenModel_758_): GenModel_758_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_758_>
}

@Singleton
class GenRepositoryImpl_758_ @Inject constructor() : GenRepository_758_ {
    private val store = mutableMapOf<Long, GenModel_758_>()
    override suspend fun getAll(): List<GenModel_758_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_758_? = store[id]
    override suspend fun save(model: GenModel_758_): GenModel_758_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_758_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_758_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_758_ @Inject constructor(
    private val repository: GenRepositoryImpl_758_
) : GenUseCase_758_<Unit, List<GenModel_758_>> {
    override suspend fun invoke(params: Unit): List<GenModel_758_> = repository.getAll()
}

class GenSaveUseCase_758_ @Inject constructor(
    private val repository: GenRepositoryImpl_758_
) : GenUseCase_758_<GenModel_758_, GenModel_758_> {
    override suspend fun invoke(params: GenModel_758_): GenModel_758_ = repository.save(params)
}

class GenDeleteUseCase_758_ @Inject constructor(
    private val repository: GenRepositoryImpl_758_
) : GenUseCase_758_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_758_ @Inject constructor(
    private val repository: GenRepositoryImpl_758_
) : GenUseCase_758_<String, List<GenModel_758_>> {
    override suspend fun invoke(params: String): List<GenModel_758_> = repository.search(params)
}

abstract class GenMapper_758_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_758_ : GenMapper_758_<GenModel_758_, String>() {
    override fun map(input: GenModel_758_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_758_ : GenMapper_758_<String, GenModel_758_>() {
    override fun map(input: String): GenModel_758_ {
        val parts = input.split(":")
        return GenModel_758_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_758_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_758_,
    private val saveUseCase: GenSaveUseCase_758_,
    private val deleteUseCase: GenDeleteUseCase_758_,
    private val searchUseCase: GenSearchUseCase_758_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_758_>(GenState_758_.Idle)
    val state: StateFlow<GenState_758_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_758_) {
        when (event) {
            is GenEvent_758_.Load -> loadAll()
            is GenEvent_758_.Update -> save(event.model)
            is GenEvent_758_.Delete -> delete(event.id)
            is GenEvent_758_.Refresh -> loadAll()
            is GenEvent_758_.Search -> search(event.query)
            is GenEvent_758_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_758_.Loading; _state.value = GenState_758_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_758_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_758_.Success(searchUseCase(query)) } }
}
