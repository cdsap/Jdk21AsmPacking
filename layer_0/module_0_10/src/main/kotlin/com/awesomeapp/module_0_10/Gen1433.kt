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

data class GenModel_1433_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1433_ {
    data class Load(val id: Long) : GenEvent_1433_()
    data class Update(val model: GenModel_1433_) : GenEvent_1433_()
    data class Delete(val id: Long) : GenEvent_1433_()
    data object Refresh : GenEvent_1433_()
    data class Search(val query: String) : GenEvent_1433_()
    data class Filter(val predicate: String) : GenEvent_1433_()
}

sealed class GenState_1433_ {
    data object Idle : GenState_1433_()
    data object Loading : GenState_1433_()
    data class Success(val items: List<GenModel_1433_>) : GenState_1433_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1433_()
    data class Partial(val items: List<GenModel_1433_>, val hasMore: Boolean) : GenState_1433_()
}

interface GenRepository_1433_ {
    suspend fun getAll(): List<GenModel_1433_>
    suspend fun getById(id: Long): GenModel_1433_?
    suspend fun save(model: GenModel_1433_): GenModel_1433_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1433_>
}

@Singleton
class GenRepositoryImpl_1433_ @Inject constructor() : GenRepository_1433_ {
    private val store = mutableMapOf<Long, GenModel_1433_>()
    override suspend fun getAll(): List<GenModel_1433_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1433_? = store[id]
    override suspend fun save(model: GenModel_1433_): GenModel_1433_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1433_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1433_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1433_ @Inject constructor(
    private val repository: GenRepositoryImpl_1433_
) : GenUseCase_1433_<Unit, List<GenModel_1433_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1433_> = repository.getAll()
}

class GenSaveUseCase_1433_ @Inject constructor(
    private val repository: GenRepositoryImpl_1433_
) : GenUseCase_1433_<GenModel_1433_, GenModel_1433_> {
    override suspend fun invoke(params: GenModel_1433_): GenModel_1433_ = repository.save(params)
}

class GenDeleteUseCase_1433_ @Inject constructor(
    private val repository: GenRepositoryImpl_1433_
) : GenUseCase_1433_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1433_ @Inject constructor(
    private val repository: GenRepositoryImpl_1433_
) : GenUseCase_1433_<String, List<GenModel_1433_>> {
    override suspend fun invoke(params: String): List<GenModel_1433_> = repository.search(params)
}

abstract class GenMapper_1433_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1433_ : GenMapper_1433_<GenModel_1433_, String>() {
    override fun map(input: GenModel_1433_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1433_ : GenMapper_1433_<String, GenModel_1433_>() {
    override fun map(input: String): GenModel_1433_ {
        val parts = input.split(":")
        return GenModel_1433_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1433_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1433_,
    private val saveUseCase: GenSaveUseCase_1433_,
    private val deleteUseCase: GenDeleteUseCase_1433_,
    private val searchUseCase: GenSearchUseCase_1433_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1433_>(GenState_1433_.Idle)
    val state: StateFlow<GenState_1433_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1433_) {
        when (event) {
            is GenEvent_1433_.Load -> loadAll()
            is GenEvent_1433_.Update -> save(event.model)
            is GenEvent_1433_.Delete -> delete(event.id)
            is GenEvent_1433_.Refresh -> loadAll()
            is GenEvent_1433_.Search -> search(event.query)
            is GenEvent_1433_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1433_.Loading; _state.value = GenState_1433_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1433_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1433_.Success(searchUseCase(query)) } }
}
