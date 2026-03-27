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

data class GenModel_738_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_738_ {
    data class Load(val id: Long) : GenEvent_738_()
    data class Update(val model: GenModel_738_) : GenEvent_738_()
    data class Delete(val id: Long) : GenEvent_738_()
    data object Refresh : GenEvent_738_()
    data class Search(val query: String) : GenEvent_738_()
    data class Filter(val predicate: String) : GenEvent_738_()
}

sealed class GenState_738_ {
    data object Idle : GenState_738_()
    data object Loading : GenState_738_()
    data class Success(val items: List<GenModel_738_>) : GenState_738_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_738_()
    data class Partial(val items: List<GenModel_738_>, val hasMore: Boolean) : GenState_738_()
}

interface GenRepository_738_ {
    suspend fun getAll(): List<GenModel_738_>
    suspend fun getById(id: Long): GenModel_738_?
    suspend fun save(model: GenModel_738_): GenModel_738_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_738_>
}

@Singleton
class GenRepositoryImpl_738_ @Inject constructor() : GenRepository_738_ {
    private val store = mutableMapOf<Long, GenModel_738_>()
    override suspend fun getAll(): List<GenModel_738_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_738_? = store[id]
    override suspend fun save(model: GenModel_738_): GenModel_738_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_738_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_738_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_738_ @Inject constructor(
    private val repository: GenRepositoryImpl_738_
) : GenUseCase_738_<Unit, List<GenModel_738_>> {
    override suspend fun invoke(params: Unit): List<GenModel_738_> = repository.getAll()
}

class GenSaveUseCase_738_ @Inject constructor(
    private val repository: GenRepositoryImpl_738_
) : GenUseCase_738_<GenModel_738_, GenModel_738_> {
    override suspend fun invoke(params: GenModel_738_): GenModel_738_ = repository.save(params)
}

class GenDeleteUseCase_738_ @Inject constructor(
    private val repository: GenRepositoryImpl_738_
) : GenUseCase_738_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_738_ @Inject constructor(
    private val repository: GenRepositoryImpl_738_
) : GenUseCase_738_<String, List<GenModel_738_>> {
    override suspend fun invoke(params: String): List<GenModel_738_> = repository.search(params)
}

abstract class GenMapper_738_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_738_ : GenMapper_738_<GenModel_738_, String>() {
    override fun map(input: GenModel_738_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_738_ : GenMapper_738_<String, GenModel_738_>() {
    override fun map(input: String): GenModel_738_ {
        val parts = input.split(":")
        return GenModel_738_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_738_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_738_,
    private val saveUseCase: GenSaveUseCase_738_,
    private val deleteUseCase: GenDeleteUseCase_738_,
    private val searchUseCase: GenSearchUseCase_738_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_738_>(GenState_738_.Idle)
    val state: StateFlow<GenState_738_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_738_) {
        when (event) {
            is GenEvent_738_.Load -> loadAll()
            is GenEvent_738_.Update -> save(event.model)
            is GenEvent_738_.Delete -> delete(event.id)
            is GenEvent_738_.Refresh -> loadAll()
            is GenEvent_738_.Search -> search(event.query)
            is GenEvent_738_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_738_.Loading; _state.value = GenState_738_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_738_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_738_.Success(searchUseCase(query)) } }
}
