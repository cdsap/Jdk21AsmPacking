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

data class GenModel_1930_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1930_ {
    data class Load(val id: Long) : GenEvent_1930_()
    data class Update(val model: GenModel_1930_) : GenEvent_1930_()
    data class Delete(val id: Long) : GenEvent_1930_()
    data object Refresh : GenEvent_1930_()
    data class Search(val query: String) : GenEvent_1930_()
    data class Filter(val predicate: String) : GenEvent_1930_()
}

sealed class GenState_1930_ {
    data object Idle : GenState_1930_()
    data object Loading : GenState_1930_()
    data class Success(val items: List<GenModel_1930_>) : GenState_1930_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1930_()
    data class Partial(val items: List<GenModel_1930_>, val hasMore: Boolean) : GenState_1930_()
}

interface GenRepository_1930_ {
    suspend fun getAll(): List<GenModel_1930_>
    suspend fun getById(id: Long): GenModel_1930_?
    suspend fun save(model: GenModel_1930_): GenModel_1930_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1930_>
}

@Singleton
class GenRepositoryImpl_1930_ @Inject constructor() : GenRepository_1930_ {
    private val store = mutableMapOf<Long, GenModel_1930_>()
    override suspend fun getAll(): List<GenModel_1930_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1930_? = store[id]
    override suspend fun save(model: GenModel_1930_): GenModel_1930_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1930_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1930_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1930_ @Inject constructor(
    private val repository: GenRepositoryImpl_1930_
) : GenUseCase_1930_<Unit, List<GenModel_1930_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1930_> = repository.getAll()
}

class GenSaveUseCase_1930_ @Inject constructor(
    private val repository: GenRepositoryImpl_1930_
) : GenUseCase_1930_<GenModel_1930_, GenModel_1930_> {
    override suspend fun invoke(params: GenModel_1930_): GenModel_1930_ = repository.save(params)
}

class GenDeleteUseCase_1930_ @Inject constructor(
    private val repository: GenRepositoryImpl_1930_
) : GenUseCase_1930_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1930_ @Inject constructor(
    private val repository: GenRepositoryImpl_1930_
) : GenUseCase_1930_<String, List<GenModel_1930_>> {
    override suspend fun invoke(params: String): List<GenModel_1930_> = repository.search(params)
}

abstract class GenMapper_1930_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1930_ : GenMapper_1930_<GenModel_1930_, String>() {
    override fun map(input: GenModel_1930_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1930_ : GenMapper_1930_<String, GenModel_1930_>() {
    override fun map(input: String): GenModel_1930_ {
        val parts = input.split(":")
        return GenModel_1930_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1930_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1930_,
    private val saveUseCase: GenSaveUseCase_1930_,
    private val deleteUseCase: GenDeleteUseCase_1930_,
    private val searchUseCase: GenSearchUseCase_1930_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1930_>(GenState_1930_.Idle)
    val state: StateFlow<GenState_1930_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1930_) {
        when (event) {
            is GenEvent_1930_.Load -> loadAll()
            is GenEvent_1930_.Update -> save(event.model)
            is GenEvent_1930_.Delete -> delete(event.id)
            is GenEvent_1930_.Refresh -> loadAll()
            is GenEvent_1930_.Search -> search(event.query)
            is GenEvent_1930_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1930_.Loading; _state.value = GenState_1930_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1930_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1930_.Success(searchUseCase(query)) } }
}
