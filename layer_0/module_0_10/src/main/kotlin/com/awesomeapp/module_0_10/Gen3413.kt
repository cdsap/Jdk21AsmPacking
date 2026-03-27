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

data class GenModel_3413_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3413_ {
    data class Load(val id: Long) : GenEvent_3413_()
    data class Update(val model: GenModel_3413_) : GenEvent_3413_()
    data class Delete(val id: Long) : GenEvent_3413_()
    data object Refresh : GenEvent_3413_()
    data class Search(val query: String) : GenEvent_3413_()
    data class Filter(val predicate: String) : GenEvent_3413_()
}

sealed class GenState_3413_ {
    data object Idle : GenState_3413_()
    data object Loading : GenState_3413_()
    data class Success(val items: List<GenModel_3413_>) : GenState_3413_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3413_()
    data class Partial(val items: List<GenModel_3413_>, val hasMore: Boolean) : GenState_3413_()
}

interface GenRepository_3413_ {
    suspend fun getAll(): List<GenModel_3413_>
    suspend fun getById(id: Long): GenModel_3413_?
    suspend fun save(model: GenModel_3413_): GenModel_3413_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3413_>
}

@Singleton
class GenRepositoryImpl_3413_ @Inject constructor() : GenRepository_3413_ {
    private val store = mutableMapOf<Long, GenModel_3413_>()
    override suspend fun getAll(): List<GenModel_3413_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3413_? = store[id]
    override suspend fun save(model: GenModel_3413_): GenModel_3413_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3413_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3413_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3413_ @Inject constructor(
    private val repository: GenRepositoryImpl_3413_
) : GenUseCase_3413_<Unit, List<GenModel_3413_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3413_> = repository.getAll()
}

class GenSaveUseCase_3413_ @Inject constructor(
    private val repository: GenRepositoryImpl_3413_
) : GenUseCase_3413_<GenModel_3413_, GenModel_3413_> {
    override suspend fun invoke(params: GenModel_3413_): GenModel_3413_ = repository.save(params)
}

class GenDeleteUseCase_3413_ @Inject constructor(
    private val repository: GenRepositoryImpl_3413_
) : GenUseCase_3413_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3413_ @Inject constructor(
    private val repository: GenRepositoryImpl_3413_
) : GenUseCase_3413_<String, List<GenModel_3413_>> {
    override suspend fun invoke(params: String): List<GenModel_3413_> = repository.search(params)
}

abstract class GenMapper_3413_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3413_ : GenMapper_3413_<GenModel_3413_, String>() {
    override fun map(input: GenModel_3413_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3413_ : GenMapper_3413_<String, GenModel_3413_>() {
    override fun map(input: String): GenModel_3413_ {
        val parts = input.split(":")
        return GenModel_3413_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3413_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3413_,
    private val saveUseCase: GenSaveUseCase_3413_,
    private val deleteUseCase: GenDeleteUseCase_3413_,
    private val searchUseCase: GenSearchUseCase_3413_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3413_>(GenState_3413_.Idle)
    val state: StateFlow<GenState_3413_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3413_) {
        when (event) {
            is GenEvent_3413_.Load -> loadAll()
            is GenEvent_3413_.Update -> save(event.model)
            is GenEvent_3413_.Delete -> delete(event.id)
            is GenEvent_3413_.Refresh -> loadAll()
            is GenEvent_3413_.Search -> search(event.query)
            is GenEvent_3413_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3413_.Loading; _state.value = GenState_3413_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3413_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3413_.Success(searchUseCase(query)) } }
}
