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

data class GenModel_1216_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1216_ {
    data class Load(val id: Long) : GenEvent_1216_()
    data class Update(val model: GenModel_1216_) : GenEvent_1216_()
    data class Delete(val id: Long) : GenEvent_1216_()
    data object Refresh : GenEvent_1216_()
    data class Search(val query: String) : GenEvent_1216_()
    data class Filter(val predicate: String) : GenEvent_1216_()
}

sealed class GenState_1216_ {
    data object Idle : GenState_1216_()
    data object Loading : GenState_1216_()
    data class Success(val items: List<GenModel_1216_>) : GenState_1216_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1216_()
    data class Partial(val items: List<GenModel_1216_>, val hasMore: Boolean) : GenState_1216_()
}

interface GenRepository_1216_ {
    suspend fun getAll(): List<GenModel_1216_>
    suspend fun getById(id: Long): GenModel_1216_?
    suspend fun save(model: GenModel_1216_): GenModel_1216_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1216_>
}

@Singleton
class GenRepositoryImpl_1216_ @Inject constructor() : GenRepository_1216_ {
    private val store = mutableMapOf<Long, GenModel_1216_>()
    override suspend fun getAll(): List<GenModel_1216_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1216_? = store[id]
    override suspend fun save(model: GenModel_1216_): GenModel_1216_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1216_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1216_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1216_ @Inject constructor(
    private val repository: GenRepositoryImpl_1216_
) : GenUseCase_1216_<Unit, List<GenModel_1216_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1216_> = repository.getAll()
}

class GenSaveUseCase_1216_ @Inject constructor(
    private val repository: GenRepositoryImpl_1216_
) : GenUseCase_1216_<GenModel_1216_, GenModel_1216_> {
    override suspend fun invoke(params: GenModel_1216_): GenModel_1216_ = repository.save(params)
}

class GenDeleteUseCase_1216_ @Inject constructor(
    private val repository: GenRepositoryImpl_1216_
) : GenUseCase_1216_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1216_ @Inject constructor(
    private val repository: GenRepositoryImpl_1216_
) : GenUseCase_1216_<String, List<GenModel_1216_>> {
    override suspend fun invoke(params: String): List<GenModel_1216_> = repository.search(params)
}

abstract class GenMapper_1216_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1216_ : GenMapper_1216_<GenModel_1216_, String>() {
    override fun map(input: GenModel_1216_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1216_ : GenMapper_1216_<String, GenModel_1216_>() {
    override fun map(input: String): GenModel_1216_ {
        val parts = input.split(":")
        return GenModel_1216_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1216_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1216_,
    private val saveUseCase: GenSaveUseCase_1216_,
    private val deleteUseCase: GenDeleteUseCase_1216_,
    private val searchUseCase: GenSearchUseCase_1216_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1216_>(GenState_1216_.Idle)
    val state: StateFlow<GenState_1216_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1216_) {
        when (event) {
            is GenEvent_1216_.Load -> loadAll()
            is GenEvent_1216_.Update -> save(event.model)
            is GenEvent_1216_.Delete -> delete(event.id)
            is GenEvent_1216_.Refresh -> loadAll()
            is GenEvent_1216_.Search -> search(event.query)
            is GenEvent_1216_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1216_.Loading; _state.value = GenState_1216_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1216_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1216_.Success(searchUseCase(query)) } }
}
