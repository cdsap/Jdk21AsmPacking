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

data class GenModel_924_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_924_ {
    data class Load(val id: Long) : GenEvent_924_()
    data class Update(val model: GenModel_924_) : GenEvent_924_()
    data class Delete(val id: Long) : GenEvent_924_()
    data object Refresh : GenEvent_924_()
    data class Search(val query: String) : GenEvent_924_()
    data class Filter(val predicate: String) : GenEvent_924_()
}

sealed class GenState_924_ {
    data object Idle : GenState_924_()
    data object Loading : GenState_924_()
    data class Success(val items: List<GenModel_924_>) : GenState_924_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_924_()
    data class Partial(val items: List<GenModel_924_>, val hasMore: Boolean) : GenState_924_()
}

interface GenRepository_924_ {
    suspend fun getAll(): List<GenModel_924_>
    suspend fun getById(id: Long): GenModel_924_?
    suspend fun save(model: GenModel_924_): GenModel_924_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_924_>
}

@Singleton
class GenRepositoryImpl_924_ @Inject constructor() : GenRepository_924_ {
    private val store = mutableMapOf<Long, GenModel_924_>()
    override suspend fun getAll(): List<GenModel_924_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_924_? = store[id]
    override suspend fun save(model: GenModel_924_): GenModel_924_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_924_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_924_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_924_ @Inject constructor(
    private val repository: GenRepositoryImpl_924_
) : GenUseCase_924_<Unit, List<GenModel_924_>> {
    override suspend fun invoke(params: Unit): List<GenModel_924_> = repository.getAll()
}

class GenSaveUseCase_924_ @Inject constructor(
    private val repository: GenRepositoryImpl_924_
) : GenUseCase_924_<GenModel_924_, GenModel_924_> {
    override suspend fun invoke(params: GenModel_924_): GenModel_924_ = repository.save(params)
}

class GenDeleteUseCase_924_ @Inject constructor(
    private val repository: GenRepositoryImpl_924_
) : GenUseCase_924_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_924_ @Inject constructor(
    private val repository: GenRepositoryImpl_924_
) : GenUseCase_924_<String, List<GenModel_924_>> {
    override suspend fun invoke(params: String): List<GenModel_924_> = repository.search(params)
}

abstract class GenMapper_924_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_924_ : GenMapper_924_<GenModel_924_, String>() {
    override fun map(input: GenModel_924_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_924_ : GenMapper_924_<String, GenModel_924_>() {
    override fun map(input: String): GenModel_924_ {
        val parts = input.split(":")
        return GenModel_924_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_924_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_924_,
    private val saveUseCase: GenSaveUseCase_924_,
    private val deleteUseCase: GenDeleteUseCase_924_,
    private val searchUseCase: GenSearchUseCase_924_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_924_>(GenState_924_.Idle)
    val state: StateFlow<GenState_924_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_924_) {
        when (event) {
            is GenEvent_924_.Load -> loadAll()
            is GenEvent_924_.Update -> save(event.model)
            is GenEvent_924_.Delete -> delete(event.id)
            is GenEvent_924_.Refresh -> loadAll()
            is GenEvent_924_.Search -> search(event.query)
            is GenEvent_924_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_924_.Loading; _state.value = GenState_924_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_924_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_924_.Success(searchUseCase(query)) } }
}
