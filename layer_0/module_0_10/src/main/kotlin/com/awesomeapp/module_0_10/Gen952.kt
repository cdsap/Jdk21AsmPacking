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

data class GenModel_952_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_952_ {
    data class Load(val id: Long) : GenEvent_952_()
    data class Update(val model: GenModel_952_) : GenEvent_952_()
    data class Delete(val id: Long) : GenEvent_952_()
    data object Refresh : GenEvent_952_()
    data class Search(val query: String) : GenEvent_952_()
    data class Filter(val predicate: String) : GenEvent_952_()
}

sealed class GenState_952_ {
    data object Idle : GenState_952_()
    data object Loading : GenState_952_()
    data class Success(val items: List<GenModel_952_>) : GenState_952_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_952_()
    data class Partial(val items: List<GenModel_952_>, val hasMore: Boolean) : GenState_952_()
}

interface GenRepository_952_ {
    suspend fun getAll(): List<GenModel_952_>
    suspend fun getById(id: Long): GenModel_952_?
    suspend fun save(model: GenModel_952_): GenModel_952_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_952_>
}

@Singleton
class GenRepositoryImpl_952_ @Inject constructor() : GenRepository_952_ {
    private val store = mutableMapOf<Long, GenModel_952_>()
    override suspend fun getAll(): List<GenModel_952_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_952_? = store[id]
    override suspend fun save(model: GenModel_952_): GenModel_952_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_952_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_952_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_952_ @Inject constructor(
    private val repository: GenRepositoryImpl_952_
) : GenUseCase_952_<Unit, List<GenModel_952_>> {
    override suspend fun invoke(params: Unit): List<GenModel_952_> = repository.getAll()
}

class GenSaveUseCase_952_ @Inject constructor(
    private val repository: GenRepositoryImpl_952_
) : GenUseCase_952_<GenModel_952_, GenModel_952_> {
    override suspend fun invoke(params: GenModel_952_): GenModel_952_ = repository.save(params)
}

class GenDeleteUseCase_952_ @Inject constructor(
    private val repository: GenRepositoryImpl_952_
) : GenUseCase_952_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_952_ @Inject constructor(
    private val repository: GenRepositoryImpl_952_
) : GenUseCase_952_<String, List<GenModel_952_>> {
    override suspend fun invoke(params: String): List<GenModel_952_> = repository.search(params)
}

abstract class GenMapper_952_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_952_ : GenMapper_952_<GenModel_952_, String>() {
    override fun map(input: GenModel_952_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_952_ : GenMapper_952_<String, GenModel_952_>() {
    override fun map(input: String): GenModel_952_ {
        val parts = input.split(":")
        return GenModel_952_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_952_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_952_,
    private val saveUseCase: GenSaveUseCase_952_,
    private val deleteUseCase: GenDeleteUseCase_952_,
    private val searchUseCase: GenSearchUseCase_952_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_952_>(GenState_952_.Idle)
    val state: StateFlow<GenState_952_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_952_) {
        when (event) {
            is GenEvent_952_.Load -> loadAll()
            is GenEvent_952_.Update -> save(event.model)
            is GenEvent_952_.Delete -> delete(event.id)
            is GenEvent_952_.Refresh -> loadAll()
            is GenEvent_952_.Search -> search(event.query)
            is GenEvent_952_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_952_.Loading; _state.value = GenState_952_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_952_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_952_.Success(searchUseCase(query)) } }
}
