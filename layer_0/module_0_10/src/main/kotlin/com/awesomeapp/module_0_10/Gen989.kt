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

data class GenModel_989_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_989_ {
    data class Load(val id: Long) : GenEvent_989_()
    data class Update(val model: GenModel_989_) : GenEvent_989_()
    data class Delete(val id: Long) : GenEvent_989_()
    data object Refresh : GenEvent_989_()
    data class Search(val query: String) : GenEvent_989_()
    data class Filter(val predicate: String) : GenEvent_989_()
}

sealed class GenState_989_ {
    data object Idle : GenState_989_()
    data object Loading : GenState_989_()
    data class Success(val items: List<GenModel_989_>) : GenState_989_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_989_()
    data class Partial(val items: List<GenModel_989_>, val hasMore: Boolean) : GenState_989_()
}

interface GenRepository_989_ {
    suspend fun getAll(): List<GenModel_989_>
    suspend fun getById(id: Long): GenModel_989_?
    suspend fun save(model: GenModel_989_): GenModel_989_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_989_>
}

@Singleton
class GenRepositoryImpl_989_ @Inject constructor() : GenRepository_989_ {
    private val store = mutableMapOf<Long, GenModel_989_>()
    override suspend fun getAll(): List<GenModel_989_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_989_? = store[id]
    override suspend fun save(model: GenModel_989_): GenModel_989_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_989_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_989_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_989_ @Inject constructor(
    private val repository: GenRepositoryImpl_989_
) : GenUseCase_989_<Unit, List<GenModel_989_>> {
    override suspend fun invoke(params: Unit): List<GenModel_989_> = repository.getAll()
}

class GenSaveUseCase_989_ @Inject constructor(
    private val repository: GenRepositoryImpl_989_
) : GenUseCase_989_<GenModel_989_, GenModel_989_> {
    override suspend fun invoke(params: GenModel_989_): GenModel_989_ = repository.save(params)
}

class GenDeleteUseCase_989_ @Inject constructor(
    private val repository: GenRepositoryImpl_989_
) : GenUseCase_989_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_989_ @Inject constructor(
    private val repository: GenRepositoryImpl_989_
) : GenUseCase_989_<String, List<GenModel_989_>> {
    override suspend fun invoke(params: String): List<GenModel_989_> = repository.search(params)
}

abstract class GenMapper_989_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_989_ : GenMapper_989_<GenModel_989_, String>() {
    override fun map(input: GenModel_989_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_989_ : GenMapper_989_<String, GenModel_989_>() {
    override fun map(input: String): GenModel_989_ {
        val parts = input.split(":")
        return GenModel_989_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_989_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_989_,
    private val saveUseCase: GenSaveUseCase_989_,
    private val deleteUseCase: GenDeleteUseCase_989_,
    private val searchUseCase: GenSearchUseCase_989_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_989_>(GenState_989_.Idle)
    val state: StateFlow<GenState_989_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_989_) {
        when (event) {
            is GenEvent_989_.Load -> loadAll()
            is GenEvent_989_.Update -> save(event.model)
            is GenEvent_989_.Delete -> delete(event.id)
            is GenEvent_989_.Refresh -> loadAll()
            is GenEvent_989_.Search -> search(event.query)
            is GenEvent_989_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_989_.Loading; _state.value = GenState_989_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_989_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_989_.Success(searchUseCase(query)) } }
}
