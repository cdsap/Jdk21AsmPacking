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

data class GenModel_1787_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1787_ {
    data class Load(val id: Long) : GenEvent_1787_()
    data class Update(val model: GenModel_1787_) : GenEvent_1787_()
    data class Delete(val id: Long) : GenEvent_1787_()
    data object Refresh : GenEvent_1787_()
    data class Search(val query: String) : GenEvent_1787_()
    data class Filter(val predicate: String) : GenEvent_1787_()
}

sealed class GenState_1787_ {
    data object Idle : GenState_1787_()
    data object Loading : GenState_1787_()
    data class Success(val items: List<GenModel_1787_>) : GenState_1787_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1787_()
    data class Partial(val items: List<GenModel_1787_>, val hasMore: Boolean) : GenState_1787_()
}

interface GenRepository_1787_ {
    suspend fun getAll(): List<GenModel_1787_>
    suspend fun getById(id: Long): GenModel_1787_?
    suspend fun save(model: GenModel_1787_): GenModel_1787_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1787_>
}

@Singleton
class GenRepositoryImpl_1787_ @Inject constructor() : GenRepository_1787_ {
    private val store = mutableMapOf<Long, GenModel_1787_>()
    override suspend fun getAll(): List<GenModel_1787_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1787_? = store[id]
    override suspend fun save(model: GenModel_1787_): GenModel_1787_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1787_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1787_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1787_ @Inject constructor(
    private val repository: GenRepositoryImpl_1787_
) : GenUseCase_1787_<Unit, List<GenModel_1787_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1787_> = repository.getAll()
}

class GenSaveUseCase_1787_ @Inject constructor(
    private val repository: GenRepositoryImpl_1787_
) : GenUseCase_1787_<GenModel_1787_, GenModel_1787_> {
    override suspend fun invoke(params: GenModel_1787_): GenModel_1787_ = repository.save(params)
}

class GenDeleteUseCase_1787_ @Inject constructor(
    private val repository: GenRepositoryImpl_1787_
) : GenUseCase_1787_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1787_ @Inject constructor(
    private val repository: GenRepositoryImpl_1787_
) : GenUseCase_1787_<String, List<GenModel_1787_>> {
    override suspend fun invoke(params: String): List<GenModel_1787_> = repository.search(params)
}

abstract class GenMapper_1787_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1787_ : GenMapper_1787_<GenModel_1787_, String>() {
    override fun map(input: GenModel_1787_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1787_ : GenMapper_1787_<String, GenModel_1787_>() {
    override fun map(input: String): GenModel_1787_ {
        val parts = input.split(":")
        return GenModel_1787_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1787_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1787_,
    private val saveUseCase: GenSaveUseCase_1787_,
    private val deleteUseCase: GenDeleteUseCase_1787_,
    private val searchUseCase: GenSearchUseCase_1787_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1787_>(GenState_1787_.Idle)
    val state: StateFlow<GenState_1787_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1787_) {
        when (event) {
            is GenEvent_1787_.Load -> loadAll()
            is GenEvent_1787_.Update -> save(event.model)
            is GenEvent_1787_.Delete -> delete(event.id)
            is GenEvent_1787_.Refresh -> loadAll()
            is GenEvent_1787_.Search -> search(event.query)
            is GenEvent_1787_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1787_.Loading; _state.value = GenState_1787_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1787_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1787_.Success(searchUseCase(query)) } }
}
