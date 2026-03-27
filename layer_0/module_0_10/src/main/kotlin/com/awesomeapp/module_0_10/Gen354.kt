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

data class GenModel_354_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_354_ {
    data class Load(val id: Long) : GenEvent_354_()
    data class Update(val model: GenModel_354_) : GenEvent_354_()
    data class Delete(val id: Long) : GenEvent_354_()
    data object Refresh : GenEvent_354_()
    data class Search(val query: String) : GenEvent_354_()
    data class Filter(val predicate: String) : GenEvent_354_()
}

sealed class GenState_354_ {
    data object Idle : GenState_354_()
    data object Loading : GenState_354_()
    data class Success(val items: List<GenModel_354_>) : GenState_354_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_354_()
    data class Partial(val items: List<GenModel_354_>, val hasMore: Boolean) : GenState_354_()
}

interface GenRepository_354_ {
    suspend fun getAll(): List<GenModel_354_>
    suspend fun getById(id: Long): GenModel_354_?
    suspend fun save(model: GenModel_354_): GenModel_354_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_354_>
}

@Singleton
class GenRepositoryImpl_354_ @Inject constructor() : GenRepository_354_ {
    private val store = mutableMapOf<Long, GenModel_354_>()
    override suspend fun getAll(): List<GenModel_354_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_354_? = store[id]
    override suspend fun save(model: GenModel_354_): GenModel_354_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_354_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_354_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_354_ @Inject constructor(
    private val repository: GenRepositoryImpl_354_
) : GenUseCase_354_<Unit, List<GenModel_354_>> {
    override suspend fun invoke(params: Unit): List<GenModel_354_> = repository.getAll()
}

class GenSaveUseCase_354_ @Inject constructor(
    private val repository: GenRepositoryImpl_354_
) : GenUseCase_354_<GenModel_354_, GenModel_354_> {
    override suspend fun invoke(params: GenModel_354_): GenModel_354_ = repository.save(params)
}

class GenDeleteUseCase_354_ @Inject constructor(
    private val repository: GenRepositoryImpl_354_
) : GenUseCase_354_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_354_ @Inject constructor(
    private val repository: GenRepositoryImpl_354_
) : GenUseCase_354_<String, List<GenModel_354_>> {
    override suspend fun invoke(params: String): List<GenModel_354_> = repository.search(params)
}

abstract class GenMapper_354_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_354_ : GenMapper_354_<GenModel_354_, String>() {
    override fun map(input: GenModel_354_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_354_ : GenMapper_354_<String, GenModel_354_>() {
    override fun map(input: String): GenModel_354_ {
        val parts = input.split(":")
        return GenModel_354_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_354_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_354_,
    private val saveUseCase: GenSaveUseCase_354_,
    private val deleteUseCase: GenDeleteUseCase_354_,
    private val searchUseCase: GenSearchUseCase_354_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_354_>(GenState_354_.Idle)
    val state: StateFlow<GenState_354_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_354_) {
        when (event) {
            is GenEvent_354_.Load -> loadAll()
            is GenEvent_354_.Update -> save(event.model)
            is GenEvent_354_.Delete -> delete(event.id)
            is GenEvent_354_.Refresh -> loadAll()
            is GenEvent_354_.Search -> search(event.query)
            is GenEvent_354_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_354_.Loading; _state.value = GenState_354_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_354_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_354_.Success(searchUseCase(query)) } }
}
