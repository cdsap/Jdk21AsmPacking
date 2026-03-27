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

data class GenModel_141_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_141_ {
    data class Load(val id: Long) : GenEvent_141_()
    data class Update(val model: GenModel_141_) : GenEvent_141_()
    data class Delete(val id: Long) : GenEvent_141_()
    data object Refresh : GenEvent_141_()
    data class Search(val query: String) : GenEvent_141_()
    data class Filter(val predicate: String) : GenEvent_141_()
}

sealed class GenState_141_ {
    data object Idle : GenState_141_()
    data object Loading : GenState_141_()
    data class Success(val items: List<GenModel_141_>) : GenState_141_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_141_()
    data class Partial(val items: List<GenModel_141_>, val hasMore: Boolean) : GenState_141_()
}

interface GenRepository_141_ {
    suspend fun getAll(): List<GenModel_141_>
    suspend fun getById(id: Long): GenModel_141_?
    suspend fun save(model: GenModel_141_): GenModel_141_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_141_>
}

@Singleton
class GenRepositoryImpl_141_ @Inject constructor() : GenRepository_141_ {
    private val store = mutableMapOf<Long, GenModel_141_>()
    override suspend fun getAll(): List<GenModel_141_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_141_? = store[id]
    override suspend fun save(model: GenModel_141_): GenModel_141_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_141_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_141_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_141_ @Inject constructor(
    private val repository: GenRepositoryImpl_141_
) : GenUseCase_141_<Unit, List<GenModel_141_>> {
    override suspend fun invoke(params: Unit): List<GenModel_141_> = repository.getAll()
}

class GenSaveUseCase_141_ @Inject constructor(
    private val repository: GenRepositoryImpl_141_
) : GenUseCase_141_<GenModel_141_, GenModel_141_> {
    override suspend fun invoke(params: GenModel_141_): GenModel_141_ = repository.save(params)
}

class GenDeleteUseCase_141_ @Inject constructor(
    private val repository: GenRepositoryImpl_141_
) : GenUseCase_141_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_141_ @Inject constructor(
    private val repository: GenRepositoryImpl_141_
) : GenUseCase_141_<String, List<GenModel_141_>> {
    override suspend fun invoke(params: String): List<GenModel_141_> = repository.search(params)
}

abstract class GenMapper_141_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_141_ : GenMapper_141_<GenModel_141_, String>() {
    override fun map(input: GenModel_141_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_141_ : GenMapper_141_<String, GenModel_141_>() {
    override fun map(input: String): GenModel_141_ {
        val parts = input.split(":")
        return GenModel_141_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_141_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_141_,
    private val saveUseCase: GenSaveUseCase_141_,
    private val deleteUseCase: GenDeleteUseCase_141_,
    private val searchUseCase: GenSearchUseCase_141_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_141_>(GenState_141_.Idle)
    val state: StateFlow<GenState_141_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_141_) {
        when (event) {
            is GenEvent_141_.Load -> loadAll()
            is GenEvent_141_.Update -> save(event.model)
            is GenEvent_141_.Delete -> delete(event.id)
            is GenEvent_141_.Refresh -> loadAll()
            is GenEvent_141_.Search -> search(event.query)
            is GenEvent_141_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_141_.Loading; _state.value = GenState_141_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_141_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_141_.Success(searchUseCase(query)) } }
}
