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

data class GenModel_1840_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1840_ {
    data class Load(val id: Long) : GenEvent_1840_()
    data class Update(val model: GenModel_1840_) : GenEvent_1840_()
    data class Delete(val id: Long) : GenEvent_1840_()
    data object Refresh : GenEvent_1840_()
    data class Search(val query: String) : GenEvent_1840_()
    data class Filter(val predicate: String) : GenEvent_1840_()
}

sealed class GenState_1840_ {
    data object Idle : GenState_1840_()
    data object Loading : GenState_1840_()
    data class Success(val items: List<GenModel_1840_>) : GenState_1840_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1840_()
    data class Partial(val items: List<GenModel_1840_>, val hasMore: Boolean) : GenState_1840_()
}

interface GenRepository_1840_ {
    suspend fun getAll(): List<GenModel_1840_>
    suspend fun getById(id: Long): GenModel_1840_?
    suspend fun save(model: GenModel_1840_): GenModel_1840_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1840_>
}

@Singleton
class GenRepositoryImpl_1840_ @Inject constructor() : GenRepository_1840_ {
    private val store = mutableMapOf<Long, GenModel_1840_>()
    override suspend fun getAll(): List<GenModel_1840_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1840_? = store[id]
    override suspend fun save(model: GenModel_1840_): GenModel_1840_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1840_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1840_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1840_ @Inject constructor(
    private val repository: GenRepositoryImpl_1840_
) : GenUseCase_1840_<Unit, List<GenModel_1840_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1840_> = repository.getAll()
}

class GenSaveUseCase_1840_ @Inject constructor(
    private val repository: GenRepositoryImpl_1840_
) : GenUseCase_1840_<GenModel_1840_, GenModel_1840_> {
    override suspend fun invoke(params: GenModel_1840_): GenModel_1840_ = repository.save(params)
}

class GenDeleteUseCase_1840_ @Inject constructor(
    private val repository: GenRepositoryImpl_1840_
) : GenUseCase_1840_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1840_ @Inject constructor(
    private val repository: GenRepositoryImpl_1840_
) : GenUseCase_1840_<String, List<GenModel_1840_>> {
    override suspend fun invoke(params: String): List<GenModel_1840_> = repository.search(params)
}

abstract class GenMapper_1840_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1840_ : GenMapper_1840_<GenModel_1840_, String>() {
    override fun map(input: GenModel_1840_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1840_ : GenMapper_1840_<String, GenModel_1840_>() {
    override fun map(input: String): GenModel_1840_ {
        val parts = input.split(":")
        return GenModel_1840_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1840_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1840_,
    private val saveUseCase: GenSaveUseCase_1840_,
    private val deleteUseCase: GenDeleteUseCase_1840_,
    private val searchUseCase: GenSearchUseCase_1840_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1840_>(GenState_1840_.Idle)
    val state: StateFlow<GenState_1840_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1840_) {
        when (event) {
            is GenEvent_1840_.Load -> loadAll()
            is GenEvent_1840_.Update -> save(event.model)
            is GenEvent_1840_.Delete -> delete(event.id)
            is GenEvent_1840_.Refresh -> loadAll()
            is GenEvent_1840_.Search -> search(event.query)
            is GenEvent_1840_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1840_.Loading; _state.value = GenState_1840_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1840_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1840_.Success(searchUseCase(query)) } }
}
