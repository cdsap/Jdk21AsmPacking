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

data class GenModel_840_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_840_ {
    data class Load(val id: Long) : GenEvent_840_()
    data class Update(val model: GenModel_840_) : GenEvent_840_()
    data class Delete(val id: Long) : GenEvent_840_()
    data object Refresh : GenEvent_840_()
    data class Search(val query: String) : GenEvent_840_()
    data class Filter(val predicate: String) : GenEvent_840_()
}

sealed class GenState_840_ {
    data object Idle : GenState_840_()
    data object Loading : GenState_840_()
    data class Success(val items: List<GenModel_840_>) : GenState_840_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_840_()
    data class Partial(val items: List<GenModel_840_>, val hasMore: Boolean) : GenState_840_()
}

interface GenRepository_840_ {
    suspend fun getAll(): List<GenModel_840_>
    suspend fun getById(id: Long): GenModel_840_?
    suspend fun save(model: GenModel_840_): GenModel_840_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_840_>
}

@Singleton
class GenRepositoryImpl_840_ @Inject constructor() : GenRepository_840_ {
    private val store = mutableMapOf<Long, GenModel_840_>()
    override suspend fun getAll(): List<GenModel_840_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_840_? = store[id]
    override suspend fun save(model: GenModel_840_): GenModel_840_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_840_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_840_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_840_ @Inject constructor(
    private val repository: GenRepositoryImpl_840_
) : GenUseCase_840_<Unit, List<GenModel_840_>> {
    override suspend fun invoke(params: Unit): List<GenModel_840_> = repository.getAll()
}

class GenSaveUseCase_840_ @Inject constructor(
    private val repository: GenRepositoryImpl_840_
) : GenUseCase_840_<GenModel_840_, GenModel_840_> {
    override suspend fun invoke(params: GenModel_840_): GenModel_840_ = repository.save(params)
}

class GenDeleteUseCase_840_ @Inject constructor(
    private val repository: GenRepositoryImpl_840_
) : GenUseCase_840_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_840_ @Inject constructor(
    private val repository: GenRepositoryImpl_840_
) : GenUseCase_840_<String, List<GenModel_840_>> {
    override suspend fun invoke(params: String): List<GenModel_840_> = repository.search(params)
}

abstract class GenMapper_840_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_840_ : GenMapper_840_<GenModel_840_, String>() {
    override fun map(input: GenModel_840_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_840_ : GenMapper_840_<String, GenModel_840_>() {
    override fun map(input: String): GenModel_840_ {
        val parts = input.split(":")
        return GenModel_840_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_840_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_840_,
    private val saveUseCase: GenSaveUseCase_840_,
    private val deleteUseCase: GenDeleteUseCase_840_,
    private val searchUseCase: GenSearchUseCase_840_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_840_>(GenState_840_.Idle)
    val state: StateFlow<GenState_840_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_840_) {
        when (event) {
            is GenEvent_840_.Load -> loadAll()
            is GenEvent_840_.Update -> save(event.model)
            is GenEvent_840_.Delete -> delete(event.id)
            is GenEvent_840_.Refresh -> loadAll()
            is GenEvent_840_.Search -> search(event.query)
            is GenEvent_840_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_840_.Loading; _state.value = GenState_840_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_840_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_840_.Success(searchUseCase(query)) } }
}
