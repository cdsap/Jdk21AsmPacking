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

data class GenModel_450_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_450_ {
    data class Load(val id: Long) : GenEvent_450_()
    data class Update(val model: GenModel_450_) : GenEvent_450_()
    data class Delete(val id: Long) : GenEvent_450_()
    data object Refresh : GenEvent_450_()
    data class Search(val query: String) : GenEvent_450_()
    data class Filter(val predicate: String) : GenEvent_450_()
}

sealed class GenState_450_ {
    data object Idle : GenState_450_()
    data object Loading : GenState_450_()
    data class Success(val items: List<GenModel_450_>) : GenState_450_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_450_()
    data class Partial(val items: List<GenModel_450_>, val hasMore: Boolean) : GenState_450_()
}

interface GenRepository_450_ {
    suspend fun getAll(): List<GenModel_450_>
    suspend fun getById(id: Long): GenModel_450_?
    suspend fun save(model: GenModel_450_): GenModel_450_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_450_>
}

@Singleton
class GenRepositoryImpl_450_ @Inject constructor() : GenRepository_450_ {
    private val store = mutableMapOf<Long, GenModel_450_>()
    override suspend fun getAll(): List<GenModel_450_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_450_? = store[id]
    override suspend fun save(model: GenModel_450_): GenModel_450_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_450_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_450_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_450_ @Inject constructor(
    private val repository: GenRepositoryImpl_450_
) : GenUseCase_450_<Unit, List<GenModel_450_>> {
    override suspend fun invoke(params: Unit): List<GenModel_450_> = repository.getAll()
}

class GenSaveUseCase_450_ @Inject constructor(
    private val repository: GenRepositoryImpl_450_
) : GenUseCase_450_<GenModel_450_, GenModel_450_> {
    override suspend fun invoke(params: GenModel_450_): GenModel_450_ = repository.save(params)
}

class GenDeleteUseCase_450_ @Inject constructor(
    private val repository: GenRepositoryImpl_450_
) : GenUseCase_450_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_450_ @Inject constructor(
    private val repository: GenRepositoryImpl_450_
) : GenUseCase_450_<String, List<GenModel_450_>> {
    override suspend fun invoke(params: String): List<GenModel_450_> = repository.search(params)
}

abstract class GenMapper_450_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_450_ : GenMapper_450_<GenModel_450_, String>() {
    override fun map(input: GenModel_450_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_450_ : GenMapper_450_<String, GenModel_450_>() {
    override fun map(input: String): GenModel_450_ {
        val parts = input.split(":")
        return GenModel_450_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_450_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_450_,
    private val saveUseCase: GenSaveUseCase_450_,
    private val deleteUseCase: GenDeleteUseCase_450_,
    private val searchUseCase: GenSearchUseCase_450_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_450_>(GenState_450_.Idle)
    val state: StateFlow<GenState_450_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_450_) {
        when (event) {
            is GenEvent_450_.Load -> loadAll()
            is GenEvent_450_.Update -> save(event.model)
            is GenEvent_450_.Delete -> delete(event.id)
            is GenEvent_450_.Refresh -> loadAll()
            is GenEvent_450_.Search -> search(event.query)
            is GenEvent_450_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_450_.Loading; _state.value = GenState_450_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_450_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_450_.Success(searchUseCase(query)) } }
}
