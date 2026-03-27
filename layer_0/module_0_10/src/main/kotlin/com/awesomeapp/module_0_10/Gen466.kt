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

data class GenModel_466_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_466_ {
    data class Load(val id: Long) : GenEvent_466_()
    data class Update(val model: GenModel_466_) : GenEvent_466_()
    data class Delete(val id: Long) : GenEvent_466_()
    data object Refresh : GenEvent_466_()
    data class Search(val query: String) : GenEvent_466_()
    data class Filter(val predicate: String) : GenEvent_466_()
}

sealed class GenState_466_ {
    data object Idle : GenState_466_()
    data object Loading : GenState_466_()
    data class Success(val items: List<GenModel_466_>) : GenState_466_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_466_()
    data class Partial(val items: List<GenModel_466_>, val hasMore: Boolean) : GenState_466_()
}

interface GenRepository_466_ {
    suspend fun getAll(): List<GenModel_466_>
    suspend fun getById(id: Long): GenModel_466_?
    suspend fun save(model: GenModel_466_): GenModel_466_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_466_>
}

@Singleton
class GenRepositoryImpl_466_ @Inject constructor() : GenRepository_466_ {
    private val store = mutableMapOf<Long, GenModel_466_>()
    override suspend fun getAll(): List<GenModel_466_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_466_? = store[id]
    override suspend fun save(model: GenModel_466_): GenModel_466_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_466_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_466_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_466_ @Inject constructor(
    private val repository: GenRepositoryImpl_466_
) : GenUseCase_466_<Unit, List<GenModel_466_>> {
    override suspend fun invoke(params: Unit): List<GenModel_466_> = repository.getAll()
}

class GenSaveUseCase_466_ @Inject constructor(
    private val repository: GenRepositoryImpl_466_
) : GenUseCase_466_<GenModel_466_, GenModel_466_> {
    override suspend fun invoke(params: GenModel_466_): GenModel_466_ = repository.save(params)
}

class GenDeleteUseCase_466_ @Inject constructor(
    private val repository: GenRepositoryImpl_466_
) : GenUseCase_466_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_466_ @Inject constructor(
    private val repository: GenRepositoryImpl_466_
) : GenUseCase_466_<String, List<GenModel_466_>> {
    override suspend fun invoke(params: String): List<GenModel_466_> = repository.search(params)
}

abstract class GenMapper_466_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_466_ : GenMapper_466_<GenModel_466_, String>() {
    override fun map(input: GenModel_466_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_466_ : GenMapper_466_<String, GenModel_466_>() {
    override fun map(input: String): GenModel_466_ {
        val parts = input.split(":")
        return GenModel_466_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_466_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_466_,
    private val saveUseCase: GenSaveUseCase_466_,
    private val deleteUseCase: GenDeleteUseCase_466_,
    private val searchUseCase: GenSearchUseCase_466_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_466_>(GenState_466_.Idle)
    val state: StateFlow<GenState_466_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_466_) {
        when (event) {
            is GenEvent_466_.Load -> loadAll()
            is GenEvent_466_.Update -> save(event.model)
            is GenEvent_466_.Delete -> delete(event.id)
            is GenEvent_466_.Refresh -> loadAll()
            is GenEvent_466_.Search -> search(event.query)
            is GenEvent_466_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_466_.Loading; _state.value = GenState_466_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_466_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_466_.Success(searchUseCase(query)) } }
}
