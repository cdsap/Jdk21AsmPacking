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

data class GenModel_413_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_413_ {
    data class Load(val id: Long) : GenEvent_413_()
    data class Update(val model: GenModel_413_) : GenEvent_413_()
    data class Delete(val id: Long) : GenEvent_413_()
    data object Refresh : GenEvent_413_()
    data class Search(val query: String) : GenEvent_413_()
    data class Filter(val predicate: String) : GenEvent_413_()
}

sealed class GenState_413_ {
    data object Idle : GenState_413_()
    data object Loading : GenState_413_()
    data class Success(val items: List<GenModel_413_>) : GenState_413_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_413_()
    data class Partial(val items: List<GenModel_413_>, val hasMore: Boolean) : GenState_413_()
}

interface GenRepository_413_ {
    suspend fun getAll(): List<GenModel_413_>
    suspend fun getById(id: Long): GenModel_413_?
    suspend fun save(model: GenModel_413_): GenModel_413_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_413_>
}

@Singleton
class GenRepositoryImpl_413_ @Inject constructor() : GenRepository_413_ {
    private val store = mutableMapOf<Long, GenModel_413_>()
    override suspend fun getAll(): List<GenModel_413_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_413_? = store[id]
    override suspend fun save(model: GenModel_413_): GenModel_413_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_413_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_413_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_413_ @Inject constructor(
    private val repository: GenRepositoryImpl_413_
) : GenUseCase_413_<Unit, List<GenModel_413_>> {
    override suspend fun invoke(params: Unit): List<GenModel_413_> = repository.getAll()
}

class GenSaveUseCase_413_ @Inject constructor(
    private val repository: GenRepositoryImpl_413_
) : GenUseCase_413_<GenModel_413_, GenModel_413_> {
    override suspend fun invoke(params: GenModel_413_): GenModel_413_ = repository.save(params)
}

class GenDeleteUseCase_413_ @Inject constructor(
    private val repository: GenRepositoryImpl_413_
) : GenUseCase_413_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_413_ @Inject constructor(
    private val repository: GenRepositoryImpl_413_
) : GenUseCase_413_<String, List<GenModel_413_>> {
    override suspend fun invoke(params: String): List<GenModel_413_> = repository.search(params)
}

abstract class GenMapper_413_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_413_ : GenMapper_413_<GenModel_413_, String>() {
    override fun map(input: GenModel_413_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_413_ : GenMapper_413_<String, GenModel_413_>() {
    override fun map(input: String): GenModel_413_ {
        val parts = input.split(":")
        return GenModel_413_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_413_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_413_,
    private val saveUseCase: GenSaveUseCase_413_,
    private val deleteUseCase: GenDeleteUseCase_413_,
    private val searchUseCase: GenSearchUseCase_413_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_413_>(GenState_413_.Idle)
    val state: StateFlow<GenState_413_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_413_) {
        when (event) {
            is GenEvent_413_.Load -> loadAll()
            is GenEvent_413_.Update -> save(event.model)
            is GenEvent_413_.Delete -> delete(event.id)
            is GenEvent_413_.Refresh -> loadAll()
            is GenEvent_413_.Search -> search(event.query)
            is GenEvent_413_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_413_.Loading; _state.value = GenState_413_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_413_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_413_.Success(searchUseCase(query)) } }
}
