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

data class GenModel_129_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_129_ {
    data class Load(val id: Long) : GenEvent_129_()
    data class Update(val model: GenModel_129_) : GenEvent_129_()
    data class Delete(val id: Long) : GenEvent_129_()
    data object Refresh : GenEvent_129_()
    data class Search(val query: String) : GenEvent_129_()
    data class Filter(val predicate: String) : GenEvent_129_()
}

sealed class GenState_129_ {
    data object Idle : GenState_129_()
    data object Loading : GenState_129_()
    data class Success(val items: List<GenModel_129_>) : GenState_129_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_129_()
    data class Partial(val items: List<GenModel_129_>, val hasMore: Boolean) : GenState_129_()
}

interface GenRepository_129_ {
    suspend fun getAll(): List<GenModel_129_>
    suspend fun getById(id: Long): GenModel_129_?
    suspend fun save(model: GenModel_129_): GenModel_129_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_129_>
}

@Singleton
class GenRepositoryImpl_129_ @Inject constructor() : GenRepository_129_ {
    private val store = mutableMapOf<Long, GenModel_129_>()
    override suspend fun getAll(): List<GenModel_129_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_129_? = store[id]
    override suspend fun save(model: GenModel_129_): GenModel_129_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_129_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_129_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_129_ @Inject constructor(
    private val repository: GenRepositoryImpl_129_
) : GenUseCase_129_<Unit, List<GenModel_129_>> {
    override suspend fun invoke(params: Unit): List<GenModel_129_> = repository.getAll()
}

class GenSaveUseCase_129_ @Inject constructor(
    private val repository: GenRepositoryImpl_129_
) : GenUseCase_129_<GenModel_129_, GenModel_129_> {
    override suspend fun invoke(params: GenModel_129_): GenModel_129_ = repository.save(params)
}

class GenDeleteUseCase_129_ @Inject constructor(
    private val repository: GenRepositoryImpl_129_
) : GenUseCase_129_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_129_ @Inject constructor(
    private val repository: GenRepositoryImpl_129_
) : GenUseCase_129_<String, List<GenModel_129_>> {
    override suspend fun invoke(params: String): List<GenModel_129_> = repository.search(params)
}

abstract class GenMapper_129_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_129_ : GenMapper_129_<GenModel_129_, String>() {
    override fun map(input: GenModel_129_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_129_ : GenMapper_129_<String, GenModel_129_>() {
    override fun map(input: String): GenModel_129_ {
        val parts = input.split(":")
        return GenModel_129_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_129_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_129_,
    private val saveUseCase: GenSaveUseCase_129_,
    private val deleteUseCase: GenDeleteUseCase_129_,
    private val searchUseCase: GenSearchUseCase_129_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_129_>(GenState_129_.Idle)
    val state: StateFlow<GenState_129_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_129_) {
        when (event) {
            is GenEvent_129_.Load -> loadAll()
            is GenEvent_129_.Update -> save(event.model)
            is GenEvent_129_.Delete -> delete(event.id)
            is GenEvent_129_.Refresh -> loadAll()
            is GenEvent_129_.Search -> search(event.query)
            is GenEvent_129_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_129_.Loading; _state.value = GenState_129_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_129_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_129_.Success(searchUseCase(query)) } }
}
