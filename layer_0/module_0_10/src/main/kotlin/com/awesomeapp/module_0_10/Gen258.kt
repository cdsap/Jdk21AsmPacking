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

data class GenModel_258_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_258_ {
    data class Load(val id: Long) : GenEvent_258_()
    data class Update(val model: GenModel_258_) : GenEvent_258_()
    data class Delete(val id: Long) : GenEvent_258_()
    data object Refresh : GenEvent_258_()
    data class Search(val query: String) : GenEvent_258_()
    data class Filter(val predicate: String) : GenEvent_258_()
}

sealed class GenState_258_ {
    data object Idle : GenState_258_()
    data object Loading : GenState_258_()
    data class Success(val items: List<GenModel_258_>) : GenState_258_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_258_()
    data class Partial(val items: List<GenModel_258_>, val hasMore: Boolean) : GenState_258_()
}

interface GenRepository_258_ {
    suspend fun getAll(): List<GenModel_258_>
    suspend fun getById(id: Long): GenModel_258_?
    suspend fun save(model: GenModel_258_): GenModel_258_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_258_>
}

@Singleton
class GenRepositoryImpl_258_ @Inject constructor() : GenRepository_258_ {
    private val store = mutableMapOf<Long, GenModel_258_>()
    override suspend fun getAll(): List<GenModel_258_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_258_? = store[id]
    override suspend fun save(model: GenModel_258_): GenModel_258_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_258_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_258_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_258_ @Inject constructor(
    private val repository: GenRepositoryImpl_258_
) : GenUseCase_258_<Unit, List<GenModel_258_>> {
    override suspend fun invoke(params: Unit): List<GenModel_258_> = repository.getAll()
}

class GenSaveUseCase_258_ @Inject constructor(
    private val repository: GenRepositoryImpl_258_
) : GenUseCase_258_<GenModel_258_, GenModel_258_> {
    override suspend fun invoke(params: GenModel_258_): GenModel_258_ = repository.save(params)
}

class GenDeleteUseCase_258_ @Inject constructor(
    private val repository: GenRepositoryImpl_258_
) : GenUseCase_258_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_258_ @Inject constructor(
    private val repository: GenRepositoryImpl_258_
) : GenUseCase_258_<String, List<GenModel_258_>> {
    override suspend fun invoke(params: String): List<GenModel_258_> = repository.search(params)
}

abstract class GenMapper_258_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_258_ : GenMapper_258_<GenModel_258_, String>() {
    override fun map(input: GenModel_258_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_258_ : GenMapper_258_<String, GenModel_258_>() {
    override fun map(input: String): GenModel_258_ {
        val parts = input.split(":")
        return GenModel_258_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_258_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_258_,
    private val saveUseCase: GenSaveUseCase_258_,
    private val deleteUseCase: GenDeleteUseCase_258_,
    private val searchUseCase: GenSearchUseCase_258_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_258_>(GenState_258_.Idle)
    val state: StateFlow<GenState_258_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_258_) {
        when (event) {
            is GenEvent_258_.Load -> loadAll()
            is GenEvent_258_.Update -> save(event.model)
            is GenEvent_258_.Delete -> delete(event.id)
            is GenEvent_258_.Refresh -> loadAll()
            is GenEvent_258_.Search -> search(event.query)
            is GenEvent_258_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_258_.Loading; _state.value = GenState_258_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_258_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_258_.Success(searchUseCase(query)) } }
}
