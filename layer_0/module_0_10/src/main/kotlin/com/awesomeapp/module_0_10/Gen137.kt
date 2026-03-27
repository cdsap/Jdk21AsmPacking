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

data class GenModel_137_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_137_ {
    data class Load(val id: Long) : GenEvent_137_()
    data class Update(val model: GenModel_137_) : GenEvent_137_()
    data class Delete(val id: Long) : GenEvent_137_()
    data object Refresh : GenEvent_137_()
    data class Search(val query: String) : GenEvent_137_()
    data class Filter(val predicate: String) : GenEvent_137_()
}

sealed class GenState_137_ {
    data object Idle : GenState_137_()
    data object Loading : GenState_137_()
    data class Success(val items: List<GenModel_137_>) : GenState_137_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_137_()
    data class Partial(val items: List<GenModel_137_>, val hasMore: Boolean) : GenState_137_()
}

interface GenRepository_137_ {
    suspend fun getAll(): List<GenModel_137_>
    suspend fun getById(id: Long): GenModel_137_?
    suspend fun save(model: GenModel_137_): GenModel_137_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_137_>
}

@Singleton
class GenRepositoryImpl_137_ @Inject constructor() : GenRepository_137_ {
    private val store = mutableMapOf<Long, GenModel_137_>()
    override suspend fun getAll(): List<GenModel_137_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_137_? = store[id]
    override suspend fun save(model: GenModel_137_): GenModel_137_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_137_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_137_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_137_ @Inject constructor(
    private val repository: GenRepositoryImpl_137_
) : GenUseCase_137_<Unit, List<GenModel_137_>> {
    override suspend fun invoke(params: Unit): List<GenModel_137_> = repository.getAll()
}

class GenSaveUseCase_137_ @Inject constructor(
    private val repository: GenRepositoryImpl_137_
) : GenUseCase_137_<GenModel_137_, GenModel_137_> {
    override suspend fun invoke(params: GenModel_137_): GenModel_137_ = repository.save(params)
}

class GenDeleteUseCase_137_ @Inject constructor(
    private val repository: GenRepositoryImpl_137_
) : GenUseCase_137_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_137_ @Inject constructor(
    private val repository: GenRepositoryImpl_137_
) : GenUseCase_137_<String, List<GenModel_137_>> {
    override suspend fun invoke(params: String): List<GenModel_137_> = repository.search(params)
}

abstract class GenMapper_137_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_137_ : GenMapper_137_<GenModel_137_, String>() {
    override fun map(input: GenModel_137_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_137_ : GenMapper_137_<String, GenModel_137_>() {
    override fun map(input: String): GenModel_137_ {
        val parts = input.split(":")
        return GenModel_137_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_137_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_137_,
    private val saveUseCase: GenSaveUseCase_137_,
    private val deleteUseCase: GenDeleteUseCase_137_,
    private val searchUseCase: GenSearchUseCase_137_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_137_>(GenState_137_.Idle)
    val state: StateFlow<GenState_137_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_137_) {
        when (event) {
            is GenEvent_137_.Load -> loadAll()
            is GenEvent_137_.Update -> save(event.model)
            is GenEvent_137_.Delete -> delete(event.id)
            is GenEvent_137_.Refresh -> loadAll()
            is GenEvent_137_.Search -> search(event.query)
            is GenEvent_137_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_137_.Loading; _state.value = GenState_137_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_137_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_137_.Success(searchUseCase(query)) } }
}
