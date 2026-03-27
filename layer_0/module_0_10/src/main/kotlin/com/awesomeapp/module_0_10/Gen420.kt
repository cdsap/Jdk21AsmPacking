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

data class GenModel_420_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_420_ {
    data class Load(val id: Long) : GenEvent_420_()
    data class Update(val model: GenModel_420_) : GenEvent_420_()
    data class Delete(val id: Long) : GenEvent_420_()
    data object Refresh : GenEvent_420_()
    data class Search(val query: String) : GenEvent_420_()
    data class Filter(val predicate: String) : GenEvent_420_()
}

sealed class GenState_420_ {
    data object Idle : GenState_420_()
    data object Loading : GenState_420_()
    data class Success(val items: List<GenModel_420_>) : GenState_420_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_420_()
    data class Partial(val items: List<GenModel_420_>, val hasMore: Boolean) : GenState_420_()
}

interface GenRepository_420_ {
    suspend fun getAll(): List<GenModel_420_>
    suspend fun getById(id: Long): GenModel_420_?
    suspend fun save(model: GenModel_420_): GenModel_420_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_420_>
}

@Singleton
class GenRepositoryImpl_420_ @Inject constructor() : GenRepository_420_ {
    private val store = mutableMapOf<Long, GenModel_420_>()
    override suspend fun getAll(): List<GenModel_420_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_420_? = store[id]
    override suspend fun save(model: GenModel_420_): GenModel_420_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_420_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_420_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_420_ @Inject constructor(
    private val repository: GenRepositoryImpl_420_
) : GenUseCase_420_<Unit, List<GenModel_420_>> {
    override suspend fun invoke(params: Unit): List<GenModel_420_> = repository.getAll()
}

class GenSaveUseCase_420_ @Inject constructor(
    private val repository: GenRepositoryImpl_420_
) : GenUseCase_420_<GenModel_420_, GenModel_420_> {
    override suspend fun invoke(params: GenModel_420_): GenModel_420_ = repository.save(params)
}

class GenDeleteUseCase_420_ @Inject constructor(
    private val repository: GenRepositoryImpl_420_
) : GenUseCase_420_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_420_ @Inject constructor(
    private val repository: GenRepositoryImpl_420_
) : GenUseCase_420_<String, List<GenModel_420_>> {
    override suspend fun invoke(params: String): List<GenModel_420_> = repository.search(params)
}

abstract class GenMapper_420_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_420_ : GenMapper_420_<GenModel_420_, String>() {
    override fun map(input: GenModel_420_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_420_ : GenMapper_420_<String, GenModel_420_>() {
    override fun map(input: String): GenModel_420_ {
        val parts = input.split(":")
        return GenModel_420_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_420_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_420_,
    private val saveUseCase: GenSaveUseCase_420_,
    private val deleteUseCase: GenDeleteUseCase_420_,
    private val searchUseCase: GenSearchUseCase_420_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_420_>(GenState_420_.Idle)
    val state: StateFlow<GenState_420_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_420_) {
        when (event) {
            is GenEvent_420_.Load -> loadAll()
            is GenEvent_420_.Update -> save(event.model)
            is GenEvent_420_.Delete -> delete(event.id)
            is GenEvent_420_.Refresh -> loadAll()
            is GenEvent_420_.Search -> search(event.query)
            is GenEvent_420_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_420_.Loading; _state.value = GenState_420_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_420_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_420_.Success(searchUseCase(query)) } }
}
