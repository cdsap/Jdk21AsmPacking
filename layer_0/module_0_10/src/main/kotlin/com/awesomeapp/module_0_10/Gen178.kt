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

data class GenModel_178_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_178_ {
    data class Load(val id: Long) : GenEvent_178_()
    data class Update(val model: GenModel_178_) : GenEvent_178_()
    data class Delete(val id: Long) : GenEvent_178_()
    data object Refresh : GenEvent_178_()
    data class Search(val query: String) : GenEvent_178_()
    data class Filter(val predicate: String) : GenEvent_178_()
}

sealed class GenState_178_ {
    data object Idle : GenState_178_()
    data object Loading : GenState_178_()
    data class Success(val items: List<GenModel_178_>) : GenState_178_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_178_()
    data class Partial(val items: List<GenModel_178_>, val hasMore: Boolean) : GenState_178_()
}

interface GenRepository_178_ {
    suspend fun getAll(): List<GenModel_178_>
    suspend fun getById(id: Long): GenModel_178_?
    suspend fun save(model: GenModel_178_): GenModel_178_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_178_>
}

@Singleton
class GenRepositoryImpl_178_ @Inject constructor() : GenRepository_178_ {
    private val store = mutableMapOf<Long, GenModel_178_>()
    override suspend fun getAll(): List<GenModel_178_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_178_? = store[id]
    override suspend fun save(model: GenModel_178_): GenModel_178_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_178_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_178_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_178_ @Inject constructor(
    private val repository: GenRepositoryImpl_178_
) : GenUseCase_178_<Unit, List<GenModel_178_>> {
    override suspend fun invoke(params: Unit): List<GenModel_178_> = repository.getAll()
}

class GenSaveUseCase_178_ @Inject constructor(
    private val repository: GenRepositoryImpl_178_
) : GenUseCase_178_<GenModel_178_, GenModel_178_> {
    override suspend fun invoke(params: GenModel_178_): GenModel_178_ = repository.save(params)
}

class GenDeleteUseCase_178_ @Inject constructor(
    private val repository: GenRepositoryImpl_178_
) : GenUseCase_178_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_178_ @Inject constructor(
    private val repository: GenRepositoryImpl_178_
) : GenUseCase_178_<String, List<GenModel_178_>> {
    override suspend fun invoke(params: String): List<GenModel_178_> = repository.search(params)
}

abstract class GenMapper_178_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_178_ : GenMapper_178_<GenModel_178_, String>() {
    override fun map(input: GenModel_178_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_178_ : GenMapper_178_<String, GenModel_178_>() {
    override fun map(input: String): GenModel_178_ {
        val parts = input.split(":")
        return GenModel_178_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_178_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_178_,
    private val saveUseCase: GenSaveUseCase_178_,
    private val deleteUseCase: GenDeleteUseCase_178_,
    private val searchUseCase: GenSearchUseCase_178_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_178_>(GenState_178_.Idle)
    val state: StateFlow<GenState_178_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_178_) {
        when (event) {
            is GenEvent_178_.Load -> loadAll()
            is GenEvent_178_.Update -> save(event.model)
            is GenEvent_178_.Delete -> delete(event.id)
            is GenEvent_178_.Refresh -> loadAll()
            is GenEvent_178_.Search -> search(event.query)
            is GenEvent_178_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_178_.Loading; _state.value = GenState_178_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_178_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_178_.Success(searchUseCase(query)) } }
}
