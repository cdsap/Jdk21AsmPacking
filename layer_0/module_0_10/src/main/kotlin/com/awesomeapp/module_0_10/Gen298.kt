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

data class GenModel_298_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_298_ {
    data class Load(val id: Long) : GenEvent_298_()
    data class Update(val model: GenModel_298_) : GenEvent_298_()
    data class Delete(val id: Long) : GenEvent_298_()
    data object Refresh : GenEvent_298_()
    data class Search(val query: String) : GenEvent_298_()
    data class Filter(val predicate: String) : GenEvent_298_()
}

sealed class GenState_298_ {
    data object Idle : GenState_298_()
    data object Loading : GenState_298_()
    data class Success(val items: List<GenModel_298_>) : GenState_298_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_298_()
    data class Partial(val items: List<GenModel_298_>, val hasMore: Boolean) : GenState_298_()
}

interface GenRepository_298_ {
    suspend fun getAll(): List<GenModel_298_>
    suspend fun getById(id: Long): GenModel_298_?
    suspend fun save(model: GenModel_298_): GenModel_298_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_298_>
}

@Singleton
class GenRepositoryImpl_298_ @Inject constructor() : GenRepository_298_ {
    private val store = mutableMapOf<Long, GenModel_298_>()
    override suspend fun getAll(): List<GenModel_298_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_298_? = store[id]
    override suspend fun save(model: GenModel_298_): GenModel_298_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_298_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_298_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_298_ @Inject constructor(
    private val repository: GenRepositoryImpl_298_
) : GenUseCase_298_<Unit, List<GenModel_298_>> {
    override suspend fun invoke(params: Unit): List<GenModel_298_> = repository.getAll()
}

class GenSaveUseCase_298_ @Inject constructor(
    private val repository: GenRepositoryImpl_298_
) : GenUseCase_298_<GenModel_298_, GenModel_298_> {
    override suspend fun invoke(params: GenModel_298_): GenModel_298_ = repository.save(params)
}

class GenDeleteUseCase_298_ @Inject constructor(
    private val repository: GenRepositoryImpl_298_
) : GenUseCase_298_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_298_ @Inject constructor(
    private val repository: GenRepositoryImpl_298_
) : GenUseCase_298_<String, List<GenModel_298_>> {
    override suspend fun invoke(params: String): List<GenModel_298_> = repository.search(params)
}

abstract class GenMapper_298_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_298_ : GenMapper_298_<GenModel_298_, String>() {
    override fun map(input: GenModel_298_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_298_ : GenMapper_298_<String, GenModel_298_>() {
    override fun map(input: String): GenModel_298_ {
        val parts = input.split(":")
        return GenModel_298_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_298_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_298_,
    private val saveUseCase: GenSaveUseCase_298_,
    private val deleteUseCase: GenDeleteUseCase_298_,
    private val searchUseCase: GenSearchUseCase_298_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_298_>(GenState_298_.Idle)
    val state: StateFlow<GenState_298_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_298_) {
        when (event) {
            is GenEvent_298_.Load -> loadAll()
            is GenEvent_298_.Update -> save(event.model)
            is GenEvent_298_.Delete -> delete(event.id)
            is GenEvent_298_.Refresh -> loadAll()
            is GenEvent_298_.Search -> search(event.query)
            is GenEvent_298_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_298_.Loading; _state.value = GenState_298_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_298_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_298_.Success(searchUseCase(query)) } }
}
