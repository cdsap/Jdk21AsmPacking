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

data class GenModel_375_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_375_ {
    data class Load(val id: Long) : GenEvent_375_()
    data class Update(val model: GenModel_375_) : GenEvent_375_()
    data class Delete(val id: Long) : GenEvent_375_()
    data object Refresh : GenEvent_375_()
    data class Search(val query: String) : GenEvent_375_()
    data class Filter(val predicate: String) : GenEvent_375_()
}

sealed class GenState_375_ {
    data object Idle : GenState_375_()
    data object Loading : GenState_375_()
    data class Success(val items: List<GenModel_375_>) : GenState_375_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_375_()
    data class Partial(val items: List<GenModel_375_>, val hasMore: Boolean) : GenState_375_()
}

interface GenRepository_375_ {
    suspend fun getAll(): List<GenModel_375_>
    suspend fun getById(id: Long): GenModel_375_?
    suspend fun save(model: GenModel_375_): GenModel_375_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_375_>
}

@Singleton
class GenRepositoryImpl_375_ @Inject constructor() : GenRepository_375_ {
    private val store = mutableMapOf<Long, GenModel_375_>()
    override suspend fun getAll(): List<GenModel_375_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_375_? = store[id]
    override suspend fun save(model: GenModel_375_): GenModel_375_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_375_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_375_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_375_ @Inject constructor(
    private val repository: GenRepositoryImpl_375_
) : GenUseCase_375_<Unit, List<GenModel_375_>> {
    override suspend fun invoke(params: Unit): List<GenModel_375_> = repository.getAll()
}

class GenSaveUseCase_375_ @Inject constructor(
    private val repository: GenRepositoryImpl_375_
) : GenUseCase_375_<GenModel_375_, GenModel_375_> {
    override suspend fun invoke(params: GenModel_375_): GenModel_375_ = repository.save(params)
}

class GenDeleteUseCase_375_ @Inject constructor(
    private val repository: GenRepositoryImpl_375_
) : GenUseCase_375_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_375_ @Inject constructor(
    private val repository: GenRepositoryImpl_375_
) : GenUseCase_375_<String, List<GenModel_375_>> {
    override suspend fun invoke(params: String): List<GenModel_375_> = repository.search(params)
}

abstract class GenMapper_375_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_375_ : GenMapper_375_<GenModel_375_, String>() {
    override fun map(input: GenModel_375_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_375_ : GenMapper_375_<String, GenModel_375_>() {
    override fun map(input: String): GenModel_375_ {
        val parts = input.split(":")
        return GenModel_375_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_375_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_375_,
    private val saveUseCase: GenSaveUseCase_375_,
    private val deleteUseCase: GenDeleteUseCase_375_,
    private val searchUseCase: GenSearchUseCase_375_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_375_>(GenState_375_.Idle)
    val state: StateFlow<GenState_375_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_375_) {
        when (event) {
            is GenEvent_375_.Load -> loadAll()
            is GenEvent_375_.Update -> save(event.model)
            is GenEvent_375_.Delete -> delete(event.id)
            is GenEvent_375_.Refresh -> loadAll()
            is GenEvent_375_.Search -> search(event.query)
            is GenEvent_375_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_375_.Loading; _state.value = GenState_375_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_375_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_375_.Success(searchUseCase(query)) } }
}
