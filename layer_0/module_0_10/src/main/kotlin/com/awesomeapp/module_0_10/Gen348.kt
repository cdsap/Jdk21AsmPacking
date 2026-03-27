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

data class GenModel_348_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_348_ {
    data class Load(val id: Long) : GenEvent_348_()
    data class Update(val model: GenModel_348_) : GenEvent_348_()
    data class Delete(val id: Long) : GenEvent_348_()
    data object Refresh : GenEvent_348_()
    data class Search(val query: String) : GenEvent_348_()
    data class Filter(val predicate: String) : GenEvent_348_()
}

sealed class GenState_348_ {
    data object Idle : GenState_348_()
    data object Loading : GenState_348_()
    data class Success(val items: List<GenModel_348_>) : GenState_348_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_348_()
    data class Partial(val items: List<GenModel_348_>, val hasMore: Boolean) : GenState_348_()
}

interface GenRepository_348_ {
    suspend fun getAll(): List<GenModel_348_>
    suspend fun getById(id: Long): GenModel_348_?
    suspend fun save(model: GenModel_348_): GenModel_348_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_348_>
}

@Singleton
class GenRepositoryImpl_348_ @Inject constructor() : GenRepository_348_ {
    private val store = mutableMapOf<Long, GenModel_348_>()
    override suspend fun getAll(): List<GenModel_348_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_348_? = store[id]
    override suspend fun save(model: GenModel_348_): GenModel_348_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_348_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_348_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_348_ @Inject constructor(
    private val repository: GenRepositoryImpl_348_
) : GenUseCase_348_<Unit, List<GenModel_348_>> {
    override suspend fun invoke(params: Unit): List<GenModel_348_> = repository.getAll()
}

class GenSaveUseCase_348_ @Inject constructor(
    private val repository: GenRepositoryImpl_348_
) : GenUseCase_348_<GenModel_348_, GenModel_348_> {
    override suspend fun invoke(params: GenModel_348_): GenModel_348_ = repository.save(params)
}

class GenDeleteUseCase_348_ @Inject constructor(
    private val repository: GenRepositoryImpl_348_
) : GenUseCase_348_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_348_ @Inject constructor(
    private val repository: GenRepositoryImpl_348_
) : GenUseCase_348_<String, List<GenModel_348_>> {
    override suspend fun invoke(params: String): List<GenModel_348_> = repository.search(params)
}

abstract class GenMapper_348_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_348_ : GenMapper_348_<GenModel_348_, String>() {
    override fun map(input: GenModel_348_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_348_ : GenMapper_348_<String, GenModel_348_>() {
    override fun map(input: String): GenModel_348_ {
        val parts = input.split(":")
        return GenModel_348_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_348_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_348_,
    private val saveUseCase: GenSaveUseCase_348_,
    private val deleteUseCase: GenDeleteUseCase_348_,
    private val searchUseCase: GenSearchUseCase_348_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_348_>(GenState_348_.Idle)
    val state: StateFlow<GenState_348_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_348_) {
        when (event) {
            is GenEvent_348_.Load -> loadAll()
            is GenEvent_348_.Update -> save(event.model)
            is GenEvent_348_.Delete -> delete(event.id)
            is GenEvent_348_.Refresh -> loadAll()
            is GenEvent_348_.Search -> search(event.query)
            is GenEvent_348_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_348_.Loading; _state.value = GenState_348_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_348_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_348_.Success(searchUseCase(query)) } }
}
