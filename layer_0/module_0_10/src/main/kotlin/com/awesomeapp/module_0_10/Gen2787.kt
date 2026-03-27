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

data class GenModel_2787_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2787_ {
    data class Load(val id: Long) : GenEvent_2787_()
    data class Update(val model: GenModel_2787_) : GenEvent_2787_()
    data class Delete(val id: Long) : GenEvent_2787_()
    data object Refresh : GenEvent_2787_()
    data class Search(val query: String) : GenEvent_2787_()
    data class Filter(val predicate: String) : GenEvent_2787_()
}

sealed class GenState_2787_ {
    data object Idle : GenState_2787_()
    data object Loading : GenState_2787_()
    data class Success(val items: List<GenModel_2787_>) : GenState_2787_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2787_()
    data class Partial(val items: List<GenModel_2787_>, val hasMore: Boolean) : GenState_2787_()
}

interface GenRepository_2787_ {
    suspend fun getAll(): List<GenModel_2787_>
    suspend fun getById(id: Long): GenModel_2787_?
    suspend fun save(model: GenModel_2787_): GenModel_2787_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2787_>
}

@Singleton
class GenRepositoryImpl_2787_ @Inject constructor() : GenRepository_2787_ {
    private val store = mutableMapOf<Long, GenModel_2787_>()
    override suspend fun getAll(): List<GenModel_2787_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2787_? = store[id]
    override suspend fun save(model: GenModel_2787_): GenModel_2787_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2787_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2787_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2787_ @Inject constructor(
    private val repository: GenRepositoryImpl_2787_
) : GenUseCase_2787_<Unit, List<GenModel_2787_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2787_> = repository.getAll()
}

class GenSaveUseCase_2787_ @Inject constructor(
    private val repository: GenRepositoryImpl_2787_
) : GenUseCase_2787_<GenModel_2787_, GenModel_2787_> {
    override suspend fun invoke(params: GenModel_2787_): GenModel_2787_ = repository.save(params)
}

class GenDeleteUseCase_2787_ @Inject constructor(
    private val repository: GenRepositoryImpl_2787_
) : GenUseCase_2787_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2787_ @Inject constructor(
    private val repository: GenRepositoryImpl_2787_
) : GenUseCase_2787_<String, List<GenModel_2787_>> {
    override suspend fun invoke(params: String): List<GenModel_2787_> = repository.search(params)
}

abstract class GenMapper_2787_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2787_ : GenMapper_2787_<GenModel_2787_, String>() {
    override fun map(input: GenModel_2787_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2787_ : GenMapper_2787_<String, GenModel_2787_>() {
    override fun map(input: String): GenModel_2787_ {
        val parts = input.split(":")
        return GenModel_2787_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2787_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2787_,
    private val saveUseCase: GenSaveUseCase_2787_,
    private val deleteUseCase: GenDeleteUseCase_2787_,
    private val searchUseCase: GenSearchUseCase_2787_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2787_>(GenState_2787_.Idle)
    val state: StateFlow<GenState_2787_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2787_) {
        when (event) {
            is GenEvent_2787_.Load -> loadAll()
            is GenEvent_2787_.Update -> save(event.model)
            is GenEvent_2787_.Delete -> delete(event.id)
            is GenEvent_2787_.Refresh -> loadAll()
            is GenEvent_2787_.Search -> search(event.query)
            is GenEvent_2787_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2787_.Loading; _state.value = GenState_2787_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2787_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2787_.Success(searchUseCase(query)) } }
}
