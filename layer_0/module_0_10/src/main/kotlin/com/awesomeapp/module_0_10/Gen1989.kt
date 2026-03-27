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

data class GenModel_1989_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1989_ {
    data class Load(val id: Long) : GenEvent_1989_()
    data class Update(val model: GenModel_1989_) : GenEvent_1989_()
    data class Delete(val id: Long) : GenEvent_1989_()
    data object Refresh : GenEvent_1989_()
    data class Search(val query: String) : GenEvent_1989_()
    data class Filter(val predicate: String) : GenEvent_1989_()
}

sealed class GenState_1989_ {
    data object Idle : GenState_1989_()
    data object Loading : GenState_1989_()
    data class Success(val items: List<GenModel_1989_>) : GenState_1989_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1989_()
    data class Partial(val items: List<GenModel_1989_>, val hasMore: Boolean) : GenState_1989_()
}

interface GenRepository_1989_ {
    suspend fun getAll(): List<GenModel_1989_>
    suspend fun getById(id: Long): GenModel_1989_?
    suspend fun save(model: GenModel_1989_): GenModel_1989_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1989_>
}

@Singleton
class GenRepositoryImpl_1989_ @Inject constructor() : GenRepository_1989_ {
    private val store = mutableMapOf<Long, GenModel_1989_>()
    override suspend fun getAll(): List<GenModel_1989_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1989_? = store[id]
    override suspend fun save(model: GenModel_1989_): GenModel_1989_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1989_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1989_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1989_ @Inject constructor(
    private val repository: GenRepositoryImpl_1989_
) : GenUseCase_1989_<Unit, List<GenModel_1989_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1989_> = repository.getAll()
}

class GenSaveUseCase_1989_ @Inject constructor(
    private val repository: GenRepositoryImpl_1989_
) : GenUseCase_1989_<GenModel_1989_, GenModel_1989_> {
    override suspend fun invoke(params: GenModel_1989_): GenModel_1989_ = repository.save(params)
}

class GenDeleteUseCase_1989_ @Inject constructor(
    private val repository: GenRepositoryImpl_1989_
) : GenUseCase_1989_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1989_ @Inject constructor(
    private val repository: GenRepositoryImpl_1989_
) : GenUseCase_1989_<String, List<GenModel_1989_>> {
    override suspend fun invoke(params: String): List<GenModel_1989_> = repository.search(params)
}

abstract class GenMapper_1989_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1989_ : GenMapper_1989_<GenModel_1989_, String>() {
    override fun map(input: GenModel_1989_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1989_ : GenMapper_1989_<String, GenModel_1989_>() {
    override fun map(input: String): GenModel_1989_ {
        val parts = input.split(":")
        return GenModel_1989_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1989_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1989_,
    private val saveUseCase: GenSaveUseCase_1989_,
    private val deleteUseCase: GenDeleteUseCase_1989_,
    private val searchUseCase: GenSearchUseCase_1989_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1989_>(GenState_1989_.Idle)
    val state: StateFlow<GenState_1989_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1989_) {
        when (event) {
            is GenEvent_1989_.Load -> loadAll()
            is GenEvent_1989_.Update -> save(event.model)
            is GenEvent_1989_.Delete -> delete(event.id)
            is GenEvent_1989_.Refresh -> loadAll()
            is GenEvent_1989_.Search -> search(event.query)
            is GenEvent_1989_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1989_.Loading; _state.value = GenState_1989_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1989_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1989_.Success(searchUseCase(query)) } }
}
