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

data class GenModel_303_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_303_ {
    data class Load(val id: Long) : GenEvent_303_()
    data class Update(val model: GenModel_303_) : GenEvent_303_()
    data class Delete(val id: Long) : GenEvent_303_()
    data object Refresh : GenEvent_303_()
    data class Search(val query: String) : GenEvent_303_()
    data class Filter(val predicate: String) : GenEvent_303_()
}

sealed class GenState_303_ {
    data object Idle : GenState_303_()
    data object Loading : GenState_303_()
    data class Success(val items: List<GenModel_303_>) : GenState_303_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_303_()
    data class Partial(val items: List<GenModel_303_>, val hasMore: Boolean) : GenState_303_()
}

interface GenRepository_303_ {
    suspend fun getAll(): List<GenModel_303_>
    suspend fun getById(id: Long): GenModel_303_?
    suspend fun save(model: GenModel_303_): GenModel_303_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_303_>
}

@Singleton
class GenRepositoryImpl_303_ @Inject constructor() : GenRepository_303_ {
    private val store = mutableMapOf<Long, GenModel_303_>()
    override suspend fun getAll(): List<GenModel_303_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_303_? = store[id]
    override suspend fun save(model: GenModel_303_): GenModel_303_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_303_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_303_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_303_ @Inject constructor(
    private val repository: GenRepositoryImpl_303_
) : GenUseCase_303_<Unit, List<GenModel_303_>> {
    override suspend fun invoke(params: Unit): List<GenModel_303_> = repository.getAll()
}

class GenSaveUseCase_303_ @Inject constructor(
    private val repository: GenRepositoryImpl_303_
) : GenUseCase_303_<GenModel_303_, GenModel_303_> {
    override suspend fun invoke(params: GenModel_303_): GenModel_303_ = repository.save(params)
}

class GenDeleteUseCase_303_ @Inject constructor(
    private val repository: GenRepositoryImpl_303_
) : GenUseCase_303_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_303_ @Inject constructor(
    private val repository: GenRepositoryImpl_303_
) : GenUseCase_303_<String, List<GenModel_303_>> {
    override suspend fun invoke(params: String): List<GenModel_303_> = repository.search(params)
}

abstract class GenMapper_303_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_303_ : GenMapper_303_<GenModel_303_, String>() {
    override fun map(input: GenModel_303_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_303_ : GenMapper_303_<String, GenModel_303_>() {
    override fun map(input: String): GenModel_303_ {
        val parts = input.split(":")
        return GenModel_303_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_303_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_303_,
    private val saveUseCase: GenSaveUseCase_303_,
    private val deleteUseCase: GenDeleteUseCase_303_,
    private val searchUseCase: GenSearchUseCase_303_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_303_>(GenState_303_.Idle)
    val state: StateFlow<GenState_303_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_303_) {
        when (event) {
            is GenEvent_303_.Load -> loadAll()
            is GenEvent_303_.Update -> save(event.model)
            is GenEvent_303_.Delete -> delete(event.id)
            is GenEvent_303_.Refresh -> loadAll()
            is GenEvent_303_.Search -> search(event.query)
            is GenEvent_303_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_303_.Loading; _state.value = GenState_303_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_303_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_303_.Success(searchUseCase(query)) } }
}
