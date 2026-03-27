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

data class GenModel_212_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_212_ {
    data class Load(val id: Long) : GenEvent_212_()
    data class Update(val model: GenModel_212_) : GenEvent_212_()
    data class Delete(val id: Long) : GenEvent_212_()
    data object Refresh : GenEvent_212_()
    data class Search(val query: String) : GenEvent_212_()
    data class Filter(val predicate: String) : GenEvent_212_()
}

sealed class GenState_212_ {
    data object Idle : GenState_212_()
    data object Loading : GenState_212_()
    data class Success(val items: List<GenModel_212_>) : GenState_212_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_212_()
    data class Partial(val items: List<GenModel_212_>, val hasMore: Boolean) : GenState_212_()
}

interface GenRepository_212_ {
    suspend fun getAll(): List<GenModel_212_>
    suspend fun getById(id: Long): GenModel_212_?
    suspend fun save(model: GenModel_212_): GenModel_212_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_212_>
}

@Singleton
class GenRepositoryImpl_212_ @Inject constructor() : GenRepository_212_ {
    private val store = mutableMapOf<Long, GenModel_212_>()
    override suspend fun getAll(): List<GenModel_212_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_212_? = store[id]
    override suspend fun save(model: GenModel_212_): GenModel_212_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_212_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_212_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_212_ @Inject constructor(
    private val repository: GenRepositoryImpl_212_
) : GenUseCase_212_<Unit, List<GenModel_212_>> {
    override suspend fun invoke(params: Unit): List<GenModel_212_> = repository.getAll()
}

class GenSaveUseCase_212_ @Inject constructor(
    private val repository: GenRepositoryImpl_212_
) : GenUseCase_212_<GenModel_212_, GenModel_212_> {
    override suspend fun invoke(params: GenModel_212_): GenModel_212_ = repository.save(params)
}

class GenDeleteUseCase_212_ @Inject constructor(
    private val repository: GenRepositoryImpl_212_
) : GenUseCase_212_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_212_ @Inject constructor(
    private val repository: GenRepositoryImpl_212_
) : GenUseCase_212_<String, List<GenModel_212_>> {
    override suspend fun invoke(params: String): List<GenModel_212_> = repository.search(params)
}

abstract class GenMapper_212_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_212_ : GenMapper_212_<GenModel_212_, String>() {
    override fun map(input: GenModel_212_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_212_ : GenMapper_212_<String, GenModel_212_>() {
    override fun map(input: String): GenModel_212_ {
        val parts = input.split(":")
        return GenModel_212_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_212_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_212_,
    private val saveUseCase: GenSaveUseCase_212_,
    private val deleteUseCase: GenDeleteUseCase_212_,
    private val searchUseCase: GenSearchUseCase_212_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_212_>(GenState_212_.Idle)
    val state: StateFlow<GenState_212_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_212_) {
        when (event) {
            is GenEvent_212_.Load -> loadAll()
            is GenEvent_212_.Update -> save(event.model)
            is GenEvent_212_.Delete -> delete(event.id)
            is GenEvent_212_.Refresh -> loadAll()
            is GenEvent_212_.Search -> search(event.query)
            is GenEvent_212_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_212_.Loading; _state.value = GenState_212_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_212_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_212_.Success(searchUseCase(query)) } }
}
