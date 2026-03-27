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

data class GenModel_112_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_112_ {
    data class Load(val id: Long) : GenEvent_112_()
    data class Update(val model: GenModel_112_) : GenEvent_112_()
    data class Delete(val id: Long) : GenEvent_112_()
    data object Refresh : GenEvent_112_()
    data class Search(val query: String) : GenEvent_112_()
    data class Filter(val predicate: String) : GenEvent_112_()
}

sealed class GenState_112_ {
    data object Idle : GenState_112_()
    data object Loading : GenState_112_()
    data class Success(val items: List<GenModel_112_>) : GenState_112_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_112_()
    data class Partial(val items: List<GenModel_112_>, val hasMore: Boolean) : GenState_112_()
}

interface GenRepository_112_ {
    suspend fun getAll(): List<GenModel_112_>
    suspend fun getById(id: Long): GenModel_112_?
    suspend fun save(model: GenModel_112_): GenModel_112_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_112_>
}

@Singleton
class GenRepositoryImpl_112_ @Inject constructor() : GenRepository_112_ {
    private val store = mutableMapOf<Long, GenModel_112_>()
    override suspend fun getAll(): List<GenModel_112_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_112_? = store[id]
    override suspend fun save(model: GenModel_112_): GenModel_112_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_112_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_112_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_112_ @Inject constructor(
    private val repository: GenRepositoryImpl_112_
) : GenUseCase_112_<Unit, List<GenModel_112_>> {
    override suspend fun invoke(params: Unit): List<GenModel_112_> = repository.getAll()
}

class GenSaveUseCase_112_ @Inject constructor(
    private val repository: GenRepositoryImpl_112_
) : GenUseCase_112_<GenModel_112_, GenModel_112_> {
    override suspend fun invoke(params: GenModel_112_): GenModel_112_ = repository.save(params)
}

class GenDeleteUseCase_112_ @Inject constructor(
    private val repository: GenRepositoryImpl_112_
) : GenUseCase_112_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_112_ @Inject constructor(
    private val repository: GenRepositoryImpl_112_
) : GenUseCase_112_<String, List<GenModel_112_>> {
    override suspend fun invoke(params: String): List<GenModel_112_> = repository.search(params)
}

abstract class GenMapper_112_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_112_ : GenMapper_112_<GenModel_112_, String>() {
    override fun map(input: GenModel_112_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_112_ : GenMapper_112_<String, GenModel_112_>() {
    override fun map(input: String): GenModel_112_ {
        val parts = input.split(":")
        return GenModel_112_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_112_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_112_,
    private val saveUseCase: GenSaveUseCase_112_,
    private val deleteUseCase: GenDeleteUseCase_112_,
    private val searchUseCase: GenSearchUseCase_112_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_112_>(GenState_112_.Idle)
    val state: StateFlow<GenState_112_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_112_) {
        when (event) {
            is GenEvent_112_.Load -> loadAll()
            is GenEvent_112_.Update -> save(event.model)
            is GenEvent_112_.Delete -> delete(event.id)
            is GenEvent_112_.Refresh -> loadAll()
            is GenEvent_112_.Search -> search(event.query)
            is GenEvent_112_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_112_.Loading; _state.value = GenState_112_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_112_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_112_.Success(searchUseCase(query)) } }
}
