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

data class GenModel_15_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_15_ {
    data class Load(val id: Long) : GenEvent_15_()
    data class Update(val model: GenModel_15_) : GenEvent_15_()
    data class Delete(val id: Long) : GenEvent_15_()
    data object Refresh : GenEvent_15_()
    data class Search(val query: String) : GenEvent_15_()
    data class Filter(val predicate: String) : GenEvent_15_()
}

sealed class GenState_15_ {
    data object Idle : GenState_15_()
    data object Loading : GenState_15_()
    data class Success(val items: List<GenModel_15_>) : GenState_15_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_15_()
    data class Partial(val items: List<GenModel_15_>, val hasMore: Boolean) : GenState_15_()
}

interface GenRepository_15_ {
    suspend fun getAll(): List<GenModel_15_>
    suspend fun getById(id: Long): GenModel_15_?
    suspend fun save(model: GenModel_15_): GenModel_15_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_15_>
}

@Singleton
class GenRepositoryImpl_15_ @Inject constructor() : GenRepository_15_ {
    private val store = mutableMapOf<Long, GenModel_15_>()
    override suspend fun getAll(): List<GenModel_15_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_15_? = store[id]
    override suspend fun save(model: GenModel_15_): GenModel_15_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_15_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_15_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_15_ @Inject constructor(
    private val repository: GenRepositoryImpl_15_
) : GenUseCase_15_<Unit, List<GenModel_15_>> {
    override suspend fun invoke(params: Unit): List<GenModel_15_> = repository.getAll()
}

class GenSaveUseCase_15_ @Inject constructor(
    private val repository: GenRepositoryImpl_15_
) : GenUseCase_15_<GenModel_15_, GenModel_15_> {
    override suspend fun invoke(params: GenModel_15_): GenModel_15_ = repository.save(params)
}

class GenDeleteUseCase_15_ @Inject constructor(
    private val repository: GenRepositoryImpl_15_
) : GenUseCase_15_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_15_ @Inject constructor(
    private val repository: GenRepositoryImpl_15_
) : GenUseCase_15_<String, List<GenModel_15_>> {
    override suspend fun invoke(params: String): List<GenModel_15_> = repository.search(params)
}

abstract class GenMapper_15_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_15_ : GenMapper_15_<GenModel_15_, String>() {
    override fun map(input: GenModel_15_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_15_ : GenMapper_15_<String, GenModel_15_>() {
    override fun map(input: String): GenModel_15_ {
        val parts = input.split(":")
        return GenModel_15_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_15_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_15_,
    private val saveUseCase: GenSaveUseCase_15_,
    private val deleteUseCase: GenDeleteUseCase_15_,
    private val searchUseCase: GenSearchUseCase_15_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_15_>(GenState_15_.Idle)
    val state: StateFlow<GenState_15_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_15_) {
        when (event) {
            is GenEvent_15_.Load -> loadAll()
            is GenEvent_15_.Update -> save(event.model)
            is GenEvent_15_.Delete -> delete(event.id)
            is GenEvent_15_.Refresh -> loadAll()
            is GenEvent_15_.Search -> search(event.query)
            is GenEvent_15_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_15_.Loading; _state.value = GenState_15_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_15_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_15_.Success(searchUseCase(query)) } }
}
