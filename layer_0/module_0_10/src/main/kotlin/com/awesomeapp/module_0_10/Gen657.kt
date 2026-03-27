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

data class GenModel_657_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_657_ {
    data class Load(val id: Long) : GenEvent_657_()
    data class Update(val model: GenModel_657_) : GenEvent_657_()
    data class Delete(val id: Long) : GenEvent_657_()
    data object Refresh : GenEvent_657_()
    data class Search(val query: String) : GenEvent_657_()
    data class Filter(val predicate: String) : GenEvent_657_()
}

sealed class GenState_657_ {
    data object Idle : GenState_657_()
    data object Loading : GenState_657_()
    data class Success(val items: List<GenModel_657_>) : GenState_657_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_657_()
    data class Partial(val items: List<GenModel_657_>, val hasMore: Boolean) : GenState_657_()
}

interface GenRepository_657_ {
    suspend fun getAll(): List<GenModel_657_>
    suspend fun getById(id: Long): GenModel_657_?
    suspend fun save(model: GenModel_657_): GenModel_657_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_657_>
}

@Singleton
class GenRepositoryImpl_657_ @Inject constructor() : GenRepository_657_ {
    private val store = mutableMapOf<Long, GenModel_657_>()
    override suspend fun getAll(): List<GenModel_657_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_657_? = store[id]
    override suspend fun save(model: GenModel_657_): GenModel_657_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_657_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_657_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_657_ @Inject constructor(
    private val repository: GenRepositoryImpl_657_
) : GenUseCase_657_<Unit, List<GenModel_657_>> {
    override suspend fun invoke(params: Unit): List<GenModel_657_> = repository.getAll()
}

class GenSaveUseCase_657_ @Inject constructor(
    private val repository: GenRepositoryImpl_657_
) : GenUseCase_657_<GenModel_657_, GenModel_657_> {
    override suspend fun invoke(params: GenModel_657_): GenModel_657_ = repository.save(params)
}

class GenDeleteUseCase_657_ @Inject constructor(
    private val repository: GenRepositoryImpl_657_
) : GenUseCase_657_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_657_ @Inject constructor(
    private val repository: GenRepositoryImpl_657_
) : GenUseCase_657_<String, List<GenModel_657_>> {
    override suspend fun invoke(params: String): List<GenModel_657_> = repository.search(params)
}

abstract class GenMapper_657_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_657_ : GenMapper_657_<GenModel_657_, String>() {
    override fun map(input: GenModel_657_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_657_ : GenMapper_657_<String, GenModel_657_>() {
    override fun map(input: String): GenModel_657_ {
        val parts = input.split(":")
        return GenModel_657_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_657_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_657_,
    private val saveUseCase: GenSaveUseCase_657_,
    private val deleteUseCase: GenDeleteUseCase_657_,
    private val searchUseCase: GenSearchUseCase_657_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_657_>(GenState_657_.Idle)
    val state: StateFlow<GenState_657_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_657_) {
        when (event) {
            is GenEvent_657_.Load -> loadAll()
            is GenEvent_657_.Update -> save(event.model)
            is GenEvent_657_.Delete -> delete(event.id)
            is GenEvent_657_.Refresh -> loadAll()
            is GenEvent_657_.Search -> search(event.query)
            is GenEvent_657_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_657_.Loading; _state.value = GenState_657_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_657_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_657_.Success(searchUseCase(query)) } }
}
