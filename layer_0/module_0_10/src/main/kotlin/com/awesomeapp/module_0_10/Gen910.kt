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

data class GenModel_910_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_910_ {
    data class Load(val id: Long) : GenEvent_910_()
    data class Update(val model: GenModel_910_) : GenEvent_910_()
    data class Delete(val id: Long) : GenEvent_910_()
    data object Refresh : GenEvent_910_()
    data class Search(val query: String) : GenEvent_910_()
    data class Filter(val predicate: String) : GenEvent_910_()
}

sealed class GenState_910_ {
    data object Idle : GenState_910_()
    data object Loading : GenState_910_()
    data class Success(val items: List<GenModel_910_>) : GenState_910_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_910_()
    data class Partial(val items: List<GenModel_910_>, val hasMore: Boolean) : GenState_910_()
}

interface GenRepository_910_ {
    suspend fun getAll(): List<GenModel_910_>
    suspend fun getById(id: Long): GenModel_910_?
    suspend fun save(model: GenModel_910_): GenModel_910_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_910_>
}

@Singleton
class GenRepositoryImpl_910_ @Inject constructor() : GenRepository_910_ {
    private val store = mutableMapOf<Long, GenModel_910_>()
    override suspend fun getAll(): List<GenModel_910_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_910_? = store[id]
    override suspend fun save(model: GenModel_910_): GenModel_910_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_910_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_910_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_910_ @Inject constructor(
    private val repository: GenRepositoryImpl_910_
) : GenUseCase_910_<Unit, List<GenModel_910_>> {
    override suspend fun invoke(params: Unit): List<GenModel_910_> = repository.getAll()
}

class GenSaveUseCase_910_ @Inject constructor(
    private val repository: GenRepositoryImpl_910_
) : GenUseCase_910_<GenModel_910_, GenModel_910_> {
    override suspend fun invoke(params: GenModel_910_): GenModel_910_ = repository.save(params)
}

class GenDeleteUseCase_910_ @Inject constructor(
    private val repository: GenRepositoryImpl_910_
) : GenUseCase_910_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_910_ @Inject constructor(
    private val repository: GenRepositoryImpl_910_
) : GenUseCase_910_<String, List<GenModel_910_>> {
    override suspend fun invoke(params: String): List<GenModel_910_> = repository.search(params)
}

abstract class GenMapper_910_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_910_ : GenMapper_910_<GenModel_910_, String>() {
    override fun map(input: GenModel_910_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_910_ : GenMapper_910_<String, GenModel_910_>() {
    override fun map(input: String): GenModel_910_ {
        val parts = input.split(":")
        return GenModel_910_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_910_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_910_,
    private val saveUseCase: GenSaveUseCase_910_,
    private val deleteUseCase: GenDeleteUseCase_910_,
    private val searchUseCase: GenSearchUseCase_910_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_910_>(GenState_910_.Idle)
    val state: StateFlow<GenState_910_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_910_) {
        when (event) {
            is GenEvent_910_.Load -> loadAll()
            is GenEvent_910_.Update -> save(event.model)
            is GenEvent_910_.Delete -> delete(event.id)
            is GenEvent_910_.Refresh -> loadAll()
            is GenEvent_910_.Search -> search(event.query)
            is GenEvent_910_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_910_.Loading; _state.value = GenState_910_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_910_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_910_.Success(searchUseCase(query)) } }
}
