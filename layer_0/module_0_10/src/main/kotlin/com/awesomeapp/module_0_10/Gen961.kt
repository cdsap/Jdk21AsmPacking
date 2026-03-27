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

data class GenModel_961_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_961_ {
    data class Load(val id: Long) : GenEvent_961_()
    data class Update(val model: GenModel_961_) : GenEvent_961_()
    data class Delete(val id: Long) : GenEvent_961_()
    data object Refresh : GenEvent_961_()
    data class Search(val query: String) : GenEvent_961_()
    data class Filter(val predicate: String) : GenEvent_961_()
}

sealed class GenState_961_ {
    data object Idle : GenState_961_()
    data object Loading : GenState_961_()
    data class Success(val items: List<GenModel_961_>) : GenState_961_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_961_()
    data class Partial(val items: List<GenModel_961_>, val hasMore: Boolean) : GenState_961_()
}

interface GenRepository_961_ {
    suspend fun getAll(): List<GenModel_961_>
    suspend fun getById(id: Long): GenModel_961_?
    suspend fun save(model: GenModel_961_): GenModel_961_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_961_>
}

@Singleton
class GenRepositoryImpl_961_ @Inject constructor() : GenRepository_961_ {
    private val store = mutableMapOf<Long, GenModel_961_>()
    override suspend fun getAll(): List<GenModel_961_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_961_? = store[id]
    override suspend fun save(model: GenModel_961_): GenModel_961_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_961_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_961_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_961_ @Inject constructor(
    private val repository: GenRepositoryImpl_961_
) : GenUseCase_961_<Unit, List<GenModel_961_>> {
    override suspend fun invoke(params: Unit): List<GenModel_961_> = repository.getAll()
}

class GenSaveUseCase_961_ @Inject constructor(
    private val repository: GenRepositoryImpl_961_
) : GenUseCase_961_<GenModel_961_, GenModel_961_> {
    override suspend fun invoke(params: GenModel_961_): GenModel_961_ = repository.save(params)
}

class GenDeleteUseCase_961_ @Inject constructor(
    private val repository: GenRepositoryImpl_961_
) : GenUseCase_961_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_961_ @Inject constructor(
    private val repository: GenRepositoryImpl_961_
) : GenUseCase_961_<String, List<GenModel_961_>> {
    override suspend fun invoke(params: String): List<GenModel_961_> = repository.search(params)
}

abstract class GenMapper_961_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_961_ : GenMapper_961_<GenModel_961_, String>() {
    override fun map(input: GenModel_961_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_961_ : GenMapper_961_<String, GenModel_961_>() {
    override fun map(input: String): GenModel_961_ {
        val parts = input.split(":")
        return GenModel_961_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_961_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_961_,
    private val saveUseCase: GenSaveUseCase_961_,
    private val deleteUseCase: GenDeleteUseCase_961_,
    private val searchUseCase: GenSearchUseCase_961_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_961_>(GenState_961_.Idle)
    val state: StateFlow<GenState_961_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_961_) {
        when (event) {
            is GenEvent_961_.Load -> loadAll()
            is GenEvent_961_.Update -> save(event.model)
            is GenEvent_961_.Delete -> delete(event.id)
            is GenEvent_961_.Refresh -> loadAll()
            is GenEvent_961_.Search -> search(event.query)
            is GenEvent_961_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_961_.Loading; _state.value = GenState_961_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_961_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_961_.Success(searchUseCase(query)) } }
}
