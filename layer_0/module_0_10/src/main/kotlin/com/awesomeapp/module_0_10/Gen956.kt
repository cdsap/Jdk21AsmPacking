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

data class GenModel_956_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_956_ {
    data class Load(val id: Long) : GenEvent_956_()
    data class Update(val model: GenModel_956_) : GenEvent_956_()
    data class Delete(val id: Long) : GenEvent_956_()
    data object Refresh : GenEvent_956_()
    data class Search(val query: String) : GenEvent_956_()
    data class Filter(val predicate: String) : GenEvent_956_()
}

sealed class GenState_956_ {
    data object Idle : GenState_956_()
    data object Loading : GenState_956_()
    data class Success(val items: List<GenModel_956_>) : GenState_956_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_956_()
    data class Partial(val items: List<GenModel_956_>, val hasMore: Boolean) : GenState_956_()
}

interface GenRepository_956_ {
    suspend fun getAll(): List<GenModel_956_>
    suspend fun getById(id: Long): GenModel_956_?
    suspend fun save(model: GenModel_956_): GenModel_956_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_956_>
}

@Singleton
class GenRepositoryImpl_956_ @Inject constructor() : GenRepository_956_ {
    private val store = mutableMapOf<Long, GenModel_956_>()
    override suspend fun getAll(): List<GenModel_956_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_956_? = store[id]
    override suspend fun save(model: GenModel_956_): GenModel_956_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_956_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_956_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_956_ @Inject constructor(
    private val repository: GenRepositoryImpl_956_
) : GenUseCase_956_<Unit, List<GenModel_956_>> {
    override suspend fun invoke(params: Unit): List<GenModel_956_> = repository.getAll()
}

class GenSaveUseCase_956_ @Inject constructor(
    private val repository: GenRepositoryImpl_956_
) : GenUseCase_956_<GenModel_956_, GenModel_956_> {
    override suspend fun invoke(params: GenModel_956_): GenModel_956_ = repository.save(params)
}

class GenDeleteUseCase_956_ @Inject constructor(
    private val repository: GenRepositoryImpl_956_
) : GenUseCase_956_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_956_ @Inject constructor(
    private val repository: GenRepositoryImpl_956_
) : GenUseCase_956_<String, List<GenModel_956_>> {
    override suspend fun invoke(params: String): List<GenModel_956_> = repository.search(params)
}

abstract class GenMapper_956_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_956_ : GenMapper_956_<GenModel_956_, String>() {
    override fun map(input: GenModel_956_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_956_ : GenMapper_956_<String, GenModel_956_>() {
    override fun map(input: String): GenModel_956_ {
        val parts = input.split(":")
        return GenModel_956_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_956_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_956_,
    private val saveUseCase: GenSaveUseCase_956_,
    private val deleteUseCase: GenDeleteUseCase_956_,
    private val searchUseCase: GenSearchUseCase_956_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_956_>(GenState_956_.Idle)
    val state: StateFlow<GenState_956_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_956_) {
        when (event) {
            is GenEvent_956_.Load -> loadAll()
            is GenEvent_956_.Update -> save(event.model)
            is GenEvent_956_.Delete -> delete(event.id)
            is GenEvent_956_.Refresh -> loadAll()
            is GenEvent_956_.Search -> search(event.query)
            is GenEvent_956_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_956_.Loading; _state.value = GenState_956_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_956_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_956_.Success(searchUseCase(query)) } }
}
