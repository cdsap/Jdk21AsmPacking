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

data class GenModel_2502_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2502_ {
    data class Load(val id: Long) : GenEvent_2502_()
    data class Update(val model: GenModel_2502_) : GenEvent_2502_()
    data class Delete(val id: Long) : GenEvent_2502_()
    data object Refresh : GenEvent_2502_()
    data class Search(val query: String) : GenEvent_2502_()
    data class Filter(val predicate: String) : GenEvent_2502_()
}

sealed class GenState_2502_ {
    data object Idle : GenState_2502_()
    data object Loading : GenState_2502_()
    data class Success(val items: List<GenModel_2502_>) : GenState_2502_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2502_()
    data class Partial(val items: List<GenModel_2502_>, val hasMore: Boolean) : GenState_2502_()
}

interface GenRepository_2502_ {
    suspend fun getAll(): List<GenModel_2502_>
    suspend fun getById(id: Long): GenModel_2502_?
    suspend fun save(model: GenModel_2502_): GenModel_2502_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2502_>
}

@Singleton
class GenRepositoryImpl_2502_ @Inject constructor() : GenRepository_2502_ {
    private val store = mutableMapOf<Long, GenModel_2502_>()
    override suspend fun getAll(): List<GenModel_2502_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2502_? = store[id]
    override suspend fun save(model: GenModel_2502_): GenModel_2502_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2502_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2502_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2502_ @Inject constructor(
    private val repository: GenRepositoryImpl_2502_
) : GenUseCase_2502_<Unit, List<GenModel_2502_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2502_> = repository.getAll()
}

class GenSaveUseCase_2502_ @Inject constructor(
    private val repository: GenRepositoryImpl_2502_
) : GenUseCase_2502_<GenModel_2502_, GenModel_2502_> {
    override suspend fun invoke(params: GenModel_2502_): GenModel_2502_ = repository.save(params)
}

class GenDeleteUseCase_2502_ @Inject constructor(
    private val repository: GenRepositoryImpl_2502_
) : GenUseCase_2502_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2502_ @Inject constructor(
    private val repository: GenRepositoryImpl_2502_
) : GenUseCase_2502_<String, List<GenModel_2502_>> {
    override suspend fun invoke(params: String): List<GenModel_2502_> = repository.search(params)
}

abstract class GenMapper_2502_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2502_ : GenMapper_2502_<GenModel_2502_, String>() {
    override fun map(input: GenModel_2502_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2502_ : GenMapper_2502_<String, GenModel_2502_>() {
    override fun map(input: String): GenModel_2502_ {
        val parts = input.split(":")
        return GenModel_2502_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2502_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2502_,
    private val saveUseCase: GenSaveUseCase_2502_,
    private val deleteUseCase: GenDeleteUseCase_2502_,
    private val searchUseCase: GenSearchUseCase_2502_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2502_>(GenState_2502_.Idle)
    val state: StateFlow<GenState_2502_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2502_) {
        when (event) {
            is GenEvent_2502_.Load -> loadAll()
            is GenEvent_2502_.Update -> save(event.model)
            is GenEvent_2502_.Delete -> delete(event.id)
            is GenEvent_2502_.Refresh -> loadAll()
            is GenEvent_2502_.Search -> search(event.query)
            is GenEvent_2502_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2502_.Loading; _state.value = GenState_2502_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2502_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2502_.Success(searchUseCase(query)) } }
}
