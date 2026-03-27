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

data class GenModel_647_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_647_ {
    data class Load(val id: Long) : GenEvent_647_()
    data class Update(val model: GenModel_647_) : GenEvent_647_()
    data class Delete(val id: Long) : GenEvent_647_()
    data object Refresh : GenEvent_647_()
    data class Search(val query: String) : GenEvent_647_()
    data class Filter(val predicate: String) : GenEvent_647_()
}

sealed class GenState_647_ {
    data object Idle : GenState_647_()
    data object Loading : GenState_647_()
    data class Success(val items: List<GenModel_647_>) : GenState_647_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_647_()
    data class Partial(val items: List<GenModel_647_>, val hasMore: Boolean) : GenState_647_()
}

interface GenRepository_647_ {
    suspend fun getAll(): List<GenModel_647_>
    suspend fun getById(id: Long): GenModel_647_?
    suspend fun save(model: GenModel_647_): GenModel_647_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_647_>
}

@Singleton
class GenRepositoryImpl_647_ @Inject constructor() : GenRepository_647_ {
    private val store = mutableMapOf<Long, GenModel_647_>()
    override suspend fun getAll(): List<GenModel_647_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_647_? = store[id]
    override suspend fun save(model: GenModel_647_): GenModel_647_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_647_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_647_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_647_ @Inject constructor(
    private val repository: GenRepositoryImpl_647_
) : GenUseCase_647_<Unit, List<GenModel_647_>> {
    override suspend fun invoke(params: Unit): List<GenModel_647_> = repository.getAll()
}

class GenSaveUseCase_647_ @Inject constructor(
    private val repository: GenRepositoryImpl_647_
) : GenUseCase_647_<GenModel_647_, GenModel_647_> {
    override suspend fun invoke(params: GenModel_647_): GenModel_647_ = repository.save(params)
}

class GenDeleteUseCase_647_ @Inject constructor(
    private val repository: GenRepositoryImpl_647_
) : GenUseCase_647_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_647_ @Inject constructor(
    private val repository: GenRepositoryImpl_647_
) : GenUseCase_647_<String, List<GenModel_647_>> {
    override suspend fun invoke(params: String): List<GenModel_647_> = repository.search(params)
}

abstract class GenMapper_647_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_647_ : GenMapper_647_<GenModel_647_, String>() {
    override fun map(input: GenModel_647_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_647_ : GenMapper_647_<String, GenModel_647_>() {
    override fun map(input: String): GenModel_647_ {
        val parts = input.split(":")
        return GenModel_647_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_647_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_647_,
    private val saveUseCase: GenSaveUseCase_647_,
    private val deleteUseCase: GenDeleteUseCase_647_,
    private val searchUseCase: GenSearchUseCase_647_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_647_>(GenState_647_.Idle)
    val state: StateFlow<GenState_647_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_647_) {
        when (event) {
            is GenEvent_647_.Load -> loadAll()
            is GenEvent_647_.Update -> save(event.model)
            is GenEvent_647_.Delete -> delete(event.id)
            is GenEvent_647_.Refresh -> loadAll()
            is GenEvent_647_.Search -> search(event.query)
            is GenEvent_647_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_647_.Loading; _state.value = GenState_647_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_647_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_647_.Success(searchUseCase(query)) } }
}
