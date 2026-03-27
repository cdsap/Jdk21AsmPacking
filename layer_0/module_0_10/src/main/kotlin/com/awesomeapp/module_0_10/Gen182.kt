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

data class GenModel_182_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_182_ {
    data class Load(val id: Long) : GenEvent_182_()
    data class Update(val model: GenModel_182_) : GenEvent_182_()
    data class Delete(val id: Long) : GenEvent_182_()
    data object Refresh : GenEvent_182_()
    data class Search(val query: String) : GenEvent_182_()
    data class Filter(val predicate: String) : GenEvent_182_()
}

sealed class GenState_182_ {
    data object Idle : GenState_182_()
    data object Loading : GenState_182_()
    data class Success(val items: List<GenModel_182_>) : GenState_182_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_182_()
    data class Partial(val items: List<GenModel_182_>, val hasMore: Boolean) : GenState_182_()
}

interface GenRepository_182_ {
    suspend fun getAll(): List<GenModel_182_>
    suspend fun getById(id: Long): GenModel_182_?
    suspend fun save(model: GenModel_182_): GenModel_182_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_182_>
}

@Singleton
class GenRepositoryImpl_182_ @Inject constructor() : GenRepository_182_ {
    private val store = mutableMapOf<Long, GenModel_182_>()
    override suspend fun getAll(): List<GenModel_182_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_182_? = store[id]
    override suspend fun save(model: GenModel_182_): GenModel_182_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_182_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_182_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_182_ @Inject constructor(
    private val repository: GenRepositoryImpl_182_
) : GenUseCase_182_<Unit, List<GenModel_182_>> {
    override suspend fun invoke(params: Unit): List<GenModel_182_> = repository.getAll()
}

class GenSaveUseCase_182_ @Inject constructor(
    private val repository: GenRepositoryImpl_182_
) : GenUseCase_182_<GenModel_182_, GenModel_182_> {
    override suspend fun invoke(params: GenModel_182_): GenModel_182_ = repository.save(params)
}

class GenDeleteUseCase_182_ @Inject constructor(
    private val repository: GenRepositoryImpl_182_
) : GenUseCase_182_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_182_ @Inject constructor(
    private val repository: GenRepositoryImpl_182_
) : GenUseCase_182_<String, List<GenModel_182_>> {
    override suspend fun invoke(params: String): List<GenModel_182_> = repository.search(params)
}

abstract class GenMapper_182_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_182_ : GenMapper_182_<GenModel_182_, String>() {
    override fun map(input: GenModel_182_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_182_ : GenMapper_182_<String, GenModel_182_>() {
    override fun map(input: String): GenModel_182_ {
        val parts = input.split(":")
        return GenModel_182_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_182_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_182_,
    private val saveUseCase: GenSaveUseCase_182_,
    private val deleteUseCase: GenDeleteUseCase_182_,
    private val searchUseCase: GenSearchUseCase_182_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_182_>(GenState_182_.Idle)
    val state: StateFlow<GenState_182_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_182_) {
        when (event) {
            is GenEvent_182_.Load -> loadAll()
            is GenEvent_182_.Update -> save(event.model)
            is GenEvent_182_.Delete -> delete(event.id)
            is GenEvent_182_.Refresh -> loadAll()
            is GenEvent_182_.Search -> search(event.query)
            is GenEvent_182_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_182_.Loading; _state.value = GenState_182_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_182_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_182_.Success(searchUseCase(query)) } }
}
