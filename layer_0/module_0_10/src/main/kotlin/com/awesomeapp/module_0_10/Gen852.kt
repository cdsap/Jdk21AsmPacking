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

data class GenModel_852_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_852_ {
    data class Load(val id: Long) : GenEvent_852_()
    data class Update(val model: GenModel_852_) : GenEvent_852_()
    data class Delete(val id: Long) : GenEvent_852_()
    data object Refresh : GenEvent_852_()
    data class Search(val query: String) : GenEvent_852_()
    data class Filter(val predicate: String) : GenEvent_852_()
}

sealed class GenState_852_ {
    data object Idle : GenState_852_()
    data object Loading : GenState_852_()
    data class Success(val items: List<GenModel_852_>) : GenState_852_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_852_()
    data class Partial(val items: List<GenModel_852_>, val hasMore: Boolean) : GenState_852_()
}

interface GenRepository_852_ {
    suspend fun getAll(): List<GenModel_852_>
    suspend fun getById(id: Long): GenModel_852_?
    suspend fun save(model: GenModel_852_): GenModel_852_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_852_>
}

@Singleton
class GenRepositoryImpl_852_ @Inject constructor() : GenRepository_852_ {
    private val store = mutableMapOf<Long, GenModel_852_>()
    override suspend fun getAll(): List<GenModel_852_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_852_? = store[id]
    override suspend fun save(model: GenModel_852_): GenModel_852_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_852_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_852_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_852_ @Inject constructor(
    private val repository: GenRepositoryImpl_852_
) : GenUseCase_852_<Unit, List<GenModel_852_>> {
    override suspend fun invoke(params: Unit): List<GenModel_852_> = repository.getAll()
}

class GenSaveUseCase_852_ @Inject constructor(
    private val repository: GenRepositoryImpl_852_
) : GenUseCase_852_<GenModel_852_, GenModel_852_> {
    override suspend fun invoke(params: GenModel_852_): GenModel_852_ = repository.save(params)
}

class GenDeleteUseCase_852_ @Inject constructor(
    private val repository: GenRepositoryImpl_852_
) : GenUseCase_852_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_852_ @Inject constructor(
    private val repository: GenRepositoryImpl_852_
) : GenUseCase_852_<String, List<GenModel_852_>> {
    override suspend fun invoke(params: String): List<GenModel_852_> = repository.search(params)
}

abstract class GenMapper_852_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_852_ : GenMapper_852_<GenModel_852_, String>() {
    override fun map(input: GenModel_852_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_852_ : GenMapper_852_<String, GenModel_852_>() {
    override fun map(input: String): GenModel_852_ {
        val parts = input.split(":")
        return GenModel_852_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_852_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_852_,
    private val saveUseCase: GenSaveUseCase_852_,
    private val deleteUseCase: GenDeleteUseCase_852_,
    private val searchUseCase: GenSearchUseCase_852_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_852_>(GenState_852_.Idle)
    val state: StateFlow<GenState_852_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_852_) {
        when (event) {
            is GenEvent_852_.Load -> loadAll()
            is GenEvent_852_.Update -> save(event.model)
            is GenEvent_852_.Delete -> delete(event.id)
            is GenEvent_852_.Refresh -> loadAll()
            is GenEvent_852_.Search -> search(event.query)
            is GenEvent_852_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_852_.Loading; _state.value = GenState_852_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_852_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_852_.Success(searchUseCase(query)) } }
}
