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

data class GenModel_986_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_986_ {
    data class Load(val id: Long) : GenEvent_986_()
    data class Update(val model: GenModel_986_) : GenEvent_986_()
    data class Delete(val id: Long) : GenEvent_986_()
    data object Refresh : GenEvent_986_()
    data class Search(val query: String) : GenEvent_986_()
    data class Filter(val predicate: String) : GenEvent_986_()
}

sealed class GenState_986_ {
    data object Idle : GenState_986_()
    data object Loading : GenState_986_()
    data class Success(val items: List<GenModel_986_>) : GenState_986_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_986_()
    data class Partial(val items: List<GenModel_986_>, val hasMore: Boolean) : GenState_986_()
}

interface GenRepository_986_ {
    suspend fun getAll(): List<GenModel_986_>
    suspend fun getById(id: Long): GenModel_986_?
    suspend fun save(model: GenModel_986_): GenModel_986_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_986_>
}

@Singleton
class GenRepositoryImpl_986_ @Inject constructor() : GenRepository_986_ {
    private val store = mutableMapOf<Long, GenModel_986_>()
    override suspend fun getAll(): List<GenModel_986_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_986_? = store[id]
    override suspend fun save(model: GenModel_986_): GenModel_986_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_986_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_986_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_986_ @Inject constructor(
    private val repository: GenRepositoryImpl_986_
) : GenUseCase_986_<Unit, List<GenModel_986_>> {
    override suspend fun invoke(params: Unit): List<GenModel_986_> = repository.getAll()
}

class GenSaveUseCase_986_ @Inject constructor(
    private val repository: GenRepositoryImpl_986_
) : GenUseCase_986_<GenModel_986_, GenModel_986_> {
    override suspend fun invoke(params: GenModel_986_): GenModel_986_ = repository.save(params)
}

class GenDeleteUseCase_986_ @Inject constructor(
    private val repository: GenRepositoryImpl_986_
) : GenUseCase_986_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_986_ @Inject constructor(
    private val repository: GenRepositoryImpl_986_
) : GenUseCase_986_<String, List<GenModel_986_>> {
    override suspend fun invoke(params: String): List<GenModel_986_> = repository.search(params)
}

abstract class GenMapper_986_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_986_ : GenMapper_986_<GenModel_986_, String>() {
    override fun map(input: GenModel_986_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_986_ : GenMapper_986_<String, GenModel_986_>() {
    override fun map(input: String): GenModel_986_ {
        val parts = input.split(":")
        return GenModel_986_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_986_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_986_,
    private val saveUseCase: GenSaveUseCase_986_,
    private val deleteUseCase: GenDeleteUseCase_986_,
    private val searchUseCase: GenSearchUseCase_986_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_986_>(GenState_986_.Idle)
    val state: StateFlow<GenState_986_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_986_) {
        when (event) {
            is GenEvent_986_.Load -> loadAll()
            is GenEvent_986_.Update -> save(event.model)
            is GenEvent_986_.Delete -> delete(event.id)
            is GenEvent_986_.Refresh -> loadAll()
            is GenEvent_986_.Search -> search(event.query)
            is GenEvent_986_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_986_.Loading; _state.value = GenState_986_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_986_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_986_.Success(searchUseCase(query)) } }
}
