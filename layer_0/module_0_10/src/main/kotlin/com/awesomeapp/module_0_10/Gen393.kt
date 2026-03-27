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

data class GenModel_393_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_393_ {
    data class Load(val id: Long) : GenEvent_393_()
    data class Update(val model: GenModel_393_) : GenEvent_393_()
    data class Delete(val id: Long) : GenEvent_393_()
    data object Refresh : GenEvent_393_()
    data class Search(val query: String) : GenEvent_393_()
    data class Filter(val predicate: String) : GenEvent_393_()
}

sealed class GenState_393_ {
    data object Idle : GenState_393_()
    data object Loading : GenState_393_()
    data class Success(val items: List<GenModel_393_>) : GenState_393_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_393_()
    data class Partial(val items: List<GenModel_393_>, val hasMore: Boolean) : GenState_393_()
}

interface GenRepository_393_ {
    suspend fun getAll(): List<GenModel_393_>
    suspend fun getById(id: Long): GenModel_393_?
    suspend fun save(model: GenModel_393_): GenModel_393_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_393_>
}

@Singleton
class GenRepositoryImpl_393_ @Inject constructor() : GenRepository_393_ {
    private val store = mutableMapOf<Long, GenModel_393_>()
    override suspend fun getAll(): List<GenModel_393_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_393_? = store[id]
    override suspend fun save(model: GenModel_393_): GenModel_393_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_393_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_393_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_393_ @Inject constructor(
    private val repository: GenRepositoryImpl_393_
) : GenUseCase_393_<Unit, List<GenModel_393_>> {
    override suspend fun invoke(params: Unit): List<GenModel_393_> = repository.getAll()
}

class GenSaveUseCase_393_ @Inject constructor(
    private val repository: GenRepositoryImpl_393_
) : GenUseCase_393_<GenModel_393_, GenModel_393_> {
    override suspend fun invoke(params: GenModel_393_): GenModel_393_ = repository.save(params)
}

class GenDeleteUseCase_393_ @Inject constructor(
    private val repository: GenRepositoryImpl_393_
) : GenUseCase_393_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_393_ @Inject constructor(
    private val repository: GenRepositoryImpl_393_
) : GenUseCase_393_<String, List<GenModel_393_>> {
    override suspend fun invoke(params: String): List<GenModel_393_> = repository.search(params)
}

abstract class GenMapper_393_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_393_ : GenMapper_393_<GenModel_393_, String>() {
    override fun map(input: GenModel_393_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_393_ : GenMapper_393_<String, GenModel_393_>() {
    override fun map(input: String): GenModel_393_ {
        val parts = input.split(":")
        return GenModel_393_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_393_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_393_,
    private val saveUseCase: GenSaveUseCase_393_,
    private val deleteUseCase: GenDeleteUseCase_393_,
    private val searchUseCase: GenSearchUseCase_393_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_393_>(GenState_393_.Idle)
    val state: StateFlow<GenState_393_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_393_) {
        when (event) {
            is GenEvent_393_.Load -> loadAll()
            is GenEvent_393_.Update -> save(event.model)
            is GenEvent_393_.Delete -> delete(event.id)
            is GenEvent_393_.Refresh -> loadAll()
            is GenEvent_393_.Search -> search(event.query)
            is GenEvent_393_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_393_.Loading; _state.value = GenState_393_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_393_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_393_.Success(searchUseCase(query)) } }
}
