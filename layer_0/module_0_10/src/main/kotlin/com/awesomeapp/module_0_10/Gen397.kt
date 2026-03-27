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

data class GenModel_397_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_397_ {
    data class Load(val id: Long) : GenEvent_397_()
    data class Update(val model: GenModel_397_) : GenEvent_397_()
    data class Delete(val id: Long) : GenEvent_397_()
    data object Refresh : GenEvent_397_()
    data class Search(val query: String) : GenEvent_397_()
    data class Filter(val predicate: String) : GenEvent_397_()
}

sealed class GenState_397_ {
    data object Idle : GenState_397_()
    data object Loading : GenState_397_()
    data class Success(val items: List<GenModel_397_>) : GenState_397_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_397_()
    data class Partial(val items: List<GenModel_397_>, val hasMore: Boolean) : GenState_397_()
}

interface GenRepository_397_ {
    suspend fun getAll(): List<GenModel_397_>
    suspend fun getById(id: Long): GenModel_397_?
    suspend fun save(model: GenModel_397_): GenModel_397_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_397_>
}

@Singleton
class GenRepositoryImpl_397_ @Inject constructor() : GenRepository_397_ {
    private val store = mutableMapOf<Long, GenModel_397_>()
    override suspend fun getAll(): List<GenModel_397_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_397_? = store[id]
    override suspend fun save(model: GenModel_397_): GenModel_397_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_397_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_397_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_397_ @Inject constructor(
    private val repository: GenRepositoryImpl_397_
) : GenUseCase_397_<Unit, List<GenModel_397_>> {
    override suspend fun invoke(params: Unit): List<GenModel_397_> = repository.getAll()
}

class GenSaveUseCase_397_ @Inject constructor(
    private val repository: GenRepositoryImpl_397_
) : GenUseCase_397_<GenModel_397_, GenModel_397_> {
    override suspend fun invoke(params: GenModel_397_): GenModel_397_ = repository.save(params)
}

class GenDeleteUseCase_397_ @Inject constructor(
    private val repository: GenRepositoryImpl_397_
) : GenUseCase_397_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_397_ @Inject constructor(
    private val repository: GenRepositoryImpl_397_
) : GenUseCase_397_<String, List<GenModel_397_>> {
    override suspend fun invoke(params: String): List<GenModel_397_> = repository.search(params)
}

abstract class GenMapper_397_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_397_ : GenMapper_397_<GenModel_397_, String>() {
    override fun map(input: GenModel_397_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_397_ : GenMapper_397_<String, GenModel_397_>() {
    override fun map(input: String): GenModel_397_ {
        val parts = input.split(":")
        return GenModel_397_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_397_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_397_,
    private val saveUseCase: GenSaveUseCase_397_,
    private val deleteUseCase: GenDeleteUseCase_397_,
    private val searchUseCase: GenSearchUseCase_397_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_397_>(GenState_397_.Idle)
    val state: StateFlow<GenState_397_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_397_) {
        when (event) {
            is GenEvent_397_.Load -> loadAll()
            is GenEvent_397_.Update -> save(event.model)
            is GenEvent_397_.Delete -> delete(event.id)
            is GenEvent_397_.Refresh -> loadAll()
            is GenEvent_397_.Search -> search(event.query)
            is GenEvent_397_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_397_.Loading; _state.value = GenState_397_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_397_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_397_.Success(searchUseCase(query)) } }
}
