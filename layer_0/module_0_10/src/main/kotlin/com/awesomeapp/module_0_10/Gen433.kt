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

data class GenModel_433_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_433_ {
    data class Load(val id: Long) : GenEvent_433_()
    data class Update(val model: GenModel_433_) : GenEvent_433_()
    data class Delete(val id: Long) : GenEvent_433_()
    data object Refresh : GenEvent_433_()
    data class Search(val query: String) : GenEvent_433_()
    data class Filter(val predicate: String) : GenEvent_433_()
}

sealed class GenState_433_ {
    data object Idle : GenState_433_()
    data object Loading : GenState_433_()
    data class Success(val items: List<GenModel_433_>) : GenState_433_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_433_()
    data class Partial(val items: List<GenModel_433_>, val hasMore: Boolean) : GenState_433_()
}

interface GenRepository_433_ {
    suspend fun getAll(): List<GenModel_433_>
    suspend fun getById(id: Long): GenModel_433_?
    suspend fun save(model: GenModel_433_): GenModel_433_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_433_>
}

@Singleton
class GenRepositoryImpl_433_ @Inject constructor() : GenRepository_433_ {
    private val store = mutableMapOf<Long, GenModel_433_>()
    override suspend fun getAll(): List<GenModel_433_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_433_? = store[id]
    override suspend fun save(model: GenModel_433_): GenModel_433_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_433_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_433_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_433_ @Inject constructor(
    private val repository: GenRepositoryImpl_433_
) : GenUseCase_433_<Unit, List<GenModel_433_>> {
    override suspend fun invoke(params: Unit): List<GenModel_433_> = repository.getAll()
}

class GenSaveUseCase_433_ @Inject constructor(
    private val repository: GenRepositoryImpl_433_
) : GenUseCase_433_<GenModel_433_, GenModel_433_> {
    override suspend fun invoke(params: GenModel_433_): GenModel_433_ = repository.save(params)
}

class GenDeleteUseCase_433_ @Inject constructor(
    private val repository: GenRepositoryImpl_433_
) : GenUseCase_433_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_433_ @Inject constructor(
    private val repository: GenRepositoryImpl_433_
) : GenUseCase_433_<String, List<GenModel_433_>> {
    override suspend fun invoke(params: String): List<GenModel_433_> = repository.search(params)
}

abstract class GenMapper_433_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_433_ : GenMapper_433_<GenModel_433_, String>() {
    override fun map(input: GenModel_433_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_433_ : GenMapper_433_<String, GenModel_433_>() {
    override fun map(input: String): GenModel_433_ {
        val parts = input.split(":")
        return GenModel_433_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_433_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_433_,
    private val saveUseCase: GenSaveUseCase_433_,
    private val deleteUseCase: GenDeleteUseCase_433_,
    private val searchUseCase: GenSearchUseCase_433_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_433_>(GenState_433_.Idle)
    val state: StateFlow<GenState_433_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_433_) {
        when (event) {
            is GenEvent_433_.Load -> loadAll()
            is GenEvent_433_.Update -> save(event.model)
            is GenEvent_433_.Delete -> delete(event.id)
            is GenEvent_433_.Refresh -> loadAll()
            is GenEvent_433_.Search -> search(event.query)
            is GenEvent_433_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_433_.Loading; _state.value = GenState_433_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_433_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_433_.Success(searchUseCase(query)) } }
}
