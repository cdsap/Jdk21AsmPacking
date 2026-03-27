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

data class GenModel_1386_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1386_ {
    data class Load(val id: Long) : GenEvent_1386_()
    data class Update(val model: GenModel_1386_) : GenEvent_1386_()
    data class Delete(val id: Long) : GenEvent_1386_()
    data object Refresh : GenEvent_1386_()
    data class Search(val query: String) : GenEvent_1386_()
    data class Filter(val predicate: String) : GenEvent_1386_()
}

sealed class GenState_1386_ {
    data object Idle : GenState_1386_()
    data object Loading : GenState_1386_()
    data class Success(val items: List<GenModel_1386_>) : GenState_1386_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1386_()
    data class Partial(val items: List<GenModel_1386_>, val hasMore: Boolean) : GenState_1386_()
}

interface GenRepository_1386_ {
    suspend fun getAll(): List<GenModel_1386_>
    suspend fun getById(id: Long): GenModel_1386_?
    suspend fun save(model: GenModel_1386_): GenModel_1386_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1386_>
}

@Singleton
class GenRepositoryImpl_1386_ @Inject constructor() : GenRepository_1386_ {
    private val store = mutableMapOf<Long, GenModel_1386_>()
    override suspend fun getAll(): List<GenModel_1386_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1386_? = store[id]
    override suspend fun save(model: GenModel_1386_): GenModel_1386_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1386_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1386_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1386_ @Inject constructor(
    private val repository: GenRepositoryImpl_1386_
) : GenUseCase_1386_<Unit, List<GenModel_1386_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1386_> = repository.getAll()
}

class GenSaveUseCase_1386_ @Inject constructor(
    private val repository: GenRepositoryImpl_1386_
) : GenUseCase_1386_<GenModel_1386_, GenModel_1386_> {
    override suspend fun invoke(params: GenModel_1386_): GenModel_1386_ = repository.save(params)
}

class GenDeleteUseCase_1386_ @Inject constructor(
    private val repository: GenRepositoryImpl_1386_
) : GenUseCase_1386_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1386_ @Inject constructor(
    private val repository: GenRepositoryImpl_1386_
) : GenUseCase_1386_<String, List<GenModel_1386_>> {
    override suspend fun invoke(params: String): List<GenModel_1386_> = repository.search(params)
}

abstract class GenMapper_1386_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1386_ : GenMapper_1386_<GenModel_1386_, String>() {
    override fun map(input: GenModel_1386_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1386_ : GenMapper_1386_<String, GenModel_1386_>() {
    override fun map(input: String): GenModel_1386_ {
        val parts = input.split(":")
        return GenModel_1386_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1386_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1386_,
    private val saveUseCase: GenSaveUseCase_1386_,
    private val deleteUseCase: GenDeleteUseCase_1386_,
    private val searchUseCase: GenSearchUseCase_1386_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1386_>(GenState_1386_.Idle)
    val state: StateFlow<GenState_1386_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1386_) {
        when (event) {
            is GenEvent_1386_.Load -> loadAll()
            is GenEvent_1386_.Update -> save(event.model)
            is GenEvent_1386_.Delete -> delete(event.id)
            is GenEvent_1386_.Refresh -> loadAll()
            is GenEvent_1386_.Search -> search(event.query)
            is GenEvent_1386_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1386_.Loading; _state.value = GenState_1386_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1386_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1386_.Success(searchUseCase(query)) } }
}
