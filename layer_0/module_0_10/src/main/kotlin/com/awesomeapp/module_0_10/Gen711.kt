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

data class GenModel_711_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_711_ {
    data class Load(val id: Long) : GenEvent_711_()
    data class Update(val model: GenModel_711_) : GenEvent_711_()
    data class Delete(val id: Long) : GenEvent_711_()
    data object Refresh : GenEvent_711_()
    data class Search(val query: String) : GenEvent_711_()
    data class Filter(val predicate: String) : GenEvent_711_()
}

sealed class GenState_711_ {
    data object Idle : GenState_711_()
    data object Loading : GenState_711_()
    data class Success(val items: List<GenModel_711_>) : GenState_711_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_711_()
    data class Partial(val items: List<GenModel_711_>, val hasMore: Boolean) : GenState_711_()
}

interface GenRepository_711_ {
    suspend fun getAll(): List<GenModel_711_>
    suspend fun getById(id: Long): GenModel_711_?
    suspend fun save(model: GenModel_711_): GenModel_711_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_711_>
}

@Singleton
class GenRepositoryImpl_711_ @Inject constructor() : GenRepository_711_ {
    private val store = mutableMapOf<Long, GenModel_711_>()
    override suspend fun getAll(): List<GenModel_711_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_711_? = store[id]
    override suspend fun save(model: GenModel_711_): GenModel_711_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_711_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_711_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_711_ @Inject constructor(
    private val repository: GenRepositoryImpl_711_
) : GenUseCase_711_<Unit, List<GenModel_711_>> {
    override suspend fun invoke(params: Unit): List<GenModel_711_> = repository.getAll()
}

class GenSaveUseCase_711_ @Inject constructor(
    private val repository: GenRepositoryImpl_711_
) : GenUseCase_711_<GenModel_711_, GenModel_711_> {
    override suspend fun invoke(params: GenModel_711_): GenModel_711_ = repository.save(params)
}

class GenDeleteUseCase_711_ @Inject constructor(
    private val repository: GenRepositoryImpl_711_
) : GenUseCase_711_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_711_ @Inject constructor(
    private val repository: GenRepositoryImpl_711_
) : GenUseCase_711_<String, List<GenModel_711_>> {
    override suspend fun invoke(params: String): List<GenModel_711_> = repository.search(params)
}

abstract class GenMapper_711_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_711_ : GenMapper_711_<GenModel_711_, String>() {
    override fun map(input: GenModel_711_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_711_ : GenMapper_711_<String, GenModel_711_>() {
    override fun map(input: String): GenModel_711_ {
        val parts = input.split(":")
        return GenModel_711_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_711_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_711_,
    private val saveUseCase: GenSaveUseCase_711_,
    private val deleteUseCase: GenDeleteUseCase_711_,
    private val searchUseCase: GenSearchUseCase_711_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_711_>(GenState_711_.Idle)
    val state: StateFlow<GenState_711_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_711_) {
        when (event) {
            is GenEvent_711_.Load -> loadAll()
            is GenEvent_711_.Update -> save(event.model)
            is GenEvent_711_.Delete -> delete(event.id)
            is GenEvent_711_.Refresh -> loadAll()
            is GenEvent_711_.Search -> search(event.query)
            is GenEvent_711_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_711_.Loading; _state.value = GenState_711_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_711_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_711_.Success(searchUseCase(query)) } }
}
