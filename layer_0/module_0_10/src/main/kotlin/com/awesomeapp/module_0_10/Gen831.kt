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

data class GenModel_831_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_831_ {
    data class Load(val id: Long) : GenEvent_831_()
    data class Update(val model: GenModel_831_) : GenEvent_831_()
    data class Delete(val id: Long) : GenEvent_831_()
    data object Refresh : GenEvent_831_()
    data class Search(val query: String) : GenEvent_831_()
    data class Filter(val predicate: String) : GenEvent_831_()
}

sealed class GenState_831_ {
    data object Idle : GenState_831_()
    data object Loading : GenState_831_()
    data class Success(val items: List<GenModel_831_>) : GenState_831_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_831_()
    data class Partial(val items: List<GenModel_831_>, val hasMore: Boolean) : GenState_831_()
}

interface GenRepository_831_ {
    suspend fun getAll(): List<GenModel_831_>
    suspend fun getById(id: Long): GenModel_831_?
    suspend fun save(model: GenModel_831_): GenModel_831_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_831_>
}

@Singleton
class GenRepositoryImpl_831_ @Inject constructor() : GenRepository_831_ {
    private val store = mutableMapOf<Long, GenModel_831_>()
    override suspend fun getAll(): List<GenModel_831_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_831_? = store[id]
    override suspend fun save(model: GenModel_831_): GenModel_831_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_831_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_831_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_831_ @Inject constructor(
    private val repository: GenRepositoryImpl_831_
) : GenUseCase_831_<Unit, List<GenModel_831_>> {
    override suspend fun invoke(params: Unit): List<GenModel_831_> = repository.getAll()
}

class GenSaveUseCase_831_ @Inject constructor(
    private val repository: GenRepositoryImpl_831_
) : GenUseCase_831_<GenModel_831_, GenModel_831_> {
    override suspend fun invoke(params: GenModel_831_): GenModel_831_ = repository.save(params)
}

class GenDeleteUseCase_831_ @Inject constructor(
    private val repository: GenRepositoryImpl_831_
) : GenUseCase_831_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_831_ @Inject constructor(
    private val repository: GenRepositoryImpl_831_
) : GenUseCase_831_<String, List<GenModel_831_>> {
    override suspend fun invoke(params: String): List<GenModel_831_> = repository.search(params)
}

abstract class GenMapper_831_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_831_ : GenMapper_831_<GenModel_831_, String>() {
    override fun map(input: GenModel_831_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_831_ : GenMapper_831_<String, GenModel_831_>() {
    override fun map(input: String): GenModel_831_ {
        val parts = input.split(":")
        return GenModel_831_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_831_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_831_,
    private val saveUseCase: GenSaveUseCase_831_,
    private val deleteUseCase: GenDeleteUseCase_831_,
    private val searchUseCase: GenSearchUseCase_831_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_831_>(GenState_831_.Idle)
    val state: StateFlow<GenState_831_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_831_) {
        when (event) {
            is GenEvent_831_.Load -> loadAll()
            is GenEvent_831_.Update -> save(event.model)
            is GenEvent_831_.Delete -> delete(event.id)
            is GenEvent_831_.Refresh -> loadAll()
            is GenEvent_831_.Search -> search(event.query)
            is GenEvent_831_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_831_.Loading; _state.value = GenState_831_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_831_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_831_.Success(searchUseCase(query)) } }
}
