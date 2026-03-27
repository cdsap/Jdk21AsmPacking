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

data class GenModel_768_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_768_ {
    data class Load(val id: Long) : GenEvent_768_()
    data class Update(val model: GenModel_768_) : GenEvent_768_()
    data class Delete(val id: Long) : GenEvent_768_()
    data object Refresh : GenEvent_768_()
    data class Search(val query: String) : GenEvent_768_()
    data class Filter(val predicate: String) : GenEvent_768_()
}

sealed class GenState_768_ {
    data object Idle : GenState_768_()
    data object Loading : GenState_768_()
    data class Success(val items: List<GenModel_768_>) : GenState_768_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_768_()
    data class Partial(val items: List<GenModel_768_>, val hasMore: Boolean) : GenState_768_()
}

interface GenRepository_768_ {
    suspend fun getAll(): List<GenModel_768_>
    suspend fun getById(id: Long): GenModel_768_?
    suspend fun save(model: GenModel_768_): GenModel_768_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_768_>
}

@Singleton
class GenRepositoryImpl_768_ @Inject constructor() : GenRepository_768_ {
    private val store = mutableMapOf<Long, GenModel_768_>()
    override suspend fun getAll(): List<GenModel_768_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_768_? = store[id]
    override suspend fun save(model: GenModel_768_): GenModel_768_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_768_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_768_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_768_ @Inject constructor(
    private val repository: GenRepositoryImpl_768_
) : GenUseCase_768_<Unit, List<GenModel_768_>> {
    override suspend fun invoke(params: Unit): List<GenModel_768_> = repository.getAll()
}

class GenSaveUseCase_768_ @Inject constructor(
    private val repository: GenRepositoryImpl_768_
) : GenUseCase_768_<GenModel_768_, GenModel_768_> {
    override suspend fun invoke(params: GenModel_768_): GenModel_768_ = repository.save(params)
}

class GenDeleteUseCase_768_ @Inject constructor(
    private val repository: GenRepositoryImpl_768_
) : GenUseCase_768_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_768_ @Inject constructor(
    private val repository: GenRepositoryImpl_768_
) : GenUseCase_768_<String, List<GenModel_768_>> {
    override suspend fun invoke(params: String): List<GenModel_768_> = repository.search(params)
}

abstract class GenMapper_768_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_768_ : GenMapper_768_<GenModel_768_, String>() {
    override fun map(input: GenModel_768_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_768_ : GenMapper_768_<String, GenModel_768_>() {
    override fun map(input: String): GenModel_768_ {
        val parts = input.split(":")
        return GenModel_768_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_768_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_768_,
    private val saveUseCase: GenSaveUseCase_768_,
    private val deleteUseCase: GenDeleteUseCase_768_,
    private val searchUseCase: GenSearchUseCase_768_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_768_>(GenState_768_.Idle)
    val state: StateFlow<GenState_768_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_768_) {
        when (event) {
            is GenEvent_768_.Load -> loadAll()
            is GenEvent_768_.Update -> save(event.model)
            is GenEvent_768_.Delete -> delete(event.id)
            is GenEvent_768_.Refresh -> loadAll()
            is GenEvent_768_.Search -> search(event.query)
            is GenEvent_768_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_768_.Loading; _state.value = GenState_768_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_768_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_768_.Success(searchUseCase(query)) } }
}
