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

data class GenModel_365_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_365_ {
    data class Load(val id: Long) : GenEvent_365_()
    data class Update(val model: GenModel_365_) : GenEvent_365_()
    data class Delete(val id: Long) : GenEvent_365_()
    data object Refresh : GenEvent_365_()
    data class Search(val query: String) : GenEvent_365_()
    data class Filter(val predicate: String) : GenEvent_365_()
}

sealed class GenState_365_ {
    data object Idle : GenState_365_()
    data object Loading : GenState_365_()
    data class Success(val items: List<GenModel_365_>) : GenState_365_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_365_()
    data class Partial(val items: List<GenModel_365_>, val hasMore: Boolean) : GenState_365_()
}

interface GenRepository_365_ {
    suspend fun getAll(): List<GenModel_365_>
    suspend fun getById(id: Long): GenModel_365_?
    suspend fun save(model: GenModel_365_): GenModel_365_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_365_>
}

@Singleton
class GenRepositoryImpl_365_ @Inject constructor() : GenRepository_365_ {
    private val store = mutableMapOf<Long, GenModel_365_>()
    override suspend fun getAll(): List<GenModel_365_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_365_? = store[id]
    override suspend fun save(model: GenModel_365_): GenModel_365_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_365_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_365_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_365_ @Inject constructor(
    private val repository: GenRepositoryImpl_365_
) : GenUseCase_365_<Unit, List<GenModel_365_>> {
    override suspend fun invoke(params: Unit): List<GenModel_365_> = repository.getAll()
}

class GenSaveUseCase_365_ @Inject constructor(
    private val repository: GenRepositoryImpl_365_
) : GenUseCase_365_<GenModel_365_, GenModel_365_> {
    override suspend fun invoke(params: GenModel_365_): GenModel_365_ = repository.save(params)
}

class GenDeleteUseCase_365_ @Inject constructor(
    private val repository: GenRepositoryImpl_365_
) : GenUseCase_365_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_365_ @Inject constructor(
    private val repository: GenRepositoryImpl_365_
) : GenUseCase_365_<String, List<GenModel_365_>> {
    override suspend fun invoke(params: String): List<GenModel_365_> = repository.search(params)
}

abstract class GenMapper_365_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_365_ : GenMapper_365_<GenModel_365_, String>() {
    override fun map(input: GenModel_365_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_365_ : GenMapper_365_<String, GenModel_365_>() {
    override fun map(input: String): GenModel_365_ {
        val parts = input.split(":")
        return GenModel_365_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_365_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_365_,
    private val saveUseCase: GenSaveUseCase_365_,
    private val deleteUseCase: GenDeleteUseCase_365_,
    private val searchUseCase: GenSearchUseCase_365_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_365_>(GenState_365_.Idle)
    val state: StateFlow<GenState_365_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_365_) {
        when (event) {
            is GenEvent_365_.Load -> loadAll()
            is GenEvent_365_.Update -> save(event.model)
            is GenEvent_365_.Delete -> delete(event.id)
            is GenEvent_365_.Refresh -> loadAll()
            is GenEvent_365_.Search -> search(event.query)
            is GenEvent_365_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_365_.Loading; _state.value = GenState_365_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_365_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_365_.Success(searchUseCase(query)) } }
}
