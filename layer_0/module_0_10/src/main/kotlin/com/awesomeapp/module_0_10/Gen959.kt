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

data class GenModel_959_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_959_ {
    data class Load(val id: Long) : GenEvent_959_()
    data class Update(val model: GenModel_959_) : GenEvent_959_()
    data class Delete(val id: Long) : GenEvent_959_()
    data object Refresh : GenEvent_959_()
    data class Search(val query: String) : GenEvent_959_()
    data class Filter(val predicate: String) : GenEvent_959_()
}

sealed class GenState_959_ {
    data object Idle : GenState_959_()
    data object Loading : GenState_959_()
    data class Success(val items: List<GenModel_959_>) : GenState_959_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_959_()
    data class Partial(val items: List<GenModel_959_>, val hasMore: Boolean) : GenState_959_()
}

interface GenRepository_959_ {
    suspend fun getAll(): List<GenModel_959_>
    suspend fun getById(id: Long): GenModel_959_?
    suspend fun save(model: GenModel_959_): GenModel_959_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_959_>
}

@Singleton
class GenRepositoryImpl_959_ @Inject constructor() : GenRepository_959_ {
    private val store = mutableMapOf<Long, GenModel_959_>()
    override suspend fun getAll(): List<GenModel_959_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_959_? = store[id]
    override suspend fun save(model: GenModel_959_): GenModel_959_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_959_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_959_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_959_ @Inject constructor(
    private val repository: GenRepositoryImpl_959_
) : GenUseCase_959_<Unit, List<GenModel_959_>> {
    override suspend fun invoke(params: Unit): List<GenModel_959_> = repository.getAll()
}

class GenSaveUseCase_959_ @Inject constructor(
    private val repository: GenRepositoryImpl_959_
) : GenUseCase_959_<GenModel_959_, GenModel_959_> {
    override suspend fun invoke(params: GenModel_959_): GenModel_959_ = repository.save(params)
}

class GenDeleteUseCase_959_ @Inject constructor(
    private val repository: GenRepositoryImpl_959_
) : GenUseCase_959_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_959_ @Inject constructor(
    private val repository: GenRepositoryImpl_959_
) : GenUseCase_959_<String, List<GenModel_959_>> {
    override suspend fun invoke(params: String): List<GenModel_959_> = repository.search(params)
}

abstract class GenMapper_959_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_959_ : GenMapper_959_<GenModel_959_, String>() {
    override fun map(input: GenModel_959_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_959_ : GenMapper_959_<String, GenModel_959_>() {
    override fun map(input: String): GenModel_959_ {
        val parts = input.split(":")
        return GenModel_959_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_959_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_959_,
    private val saveUseCase: GenSaveUseCase_959_,
    private val deleteUseCase: GenDeleteUseCase_959_,
    private val searchUseCase: GenSearchUseCase_959_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_959_>(GenState_959_.Idle)
    val state: StateFlow<GenState_959_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_959_) {
        when (event) {
            is GenEvent_959_.Load -> loadAll()
            is GenEvent_959_.Update -> save(event.model)
            is GenEvent_959_.Delete -> delete(event.id)
            is GenEvent_959_.Refresh -> loadAll()
            is GenEvent_959_.Search -> search(event.query)
            is GenEvent_959_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_959_.Loading; _state.value = GenState_959_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_959_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_959_.Success(searchUseCase(query)) } }
}
