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

data class GenModel_1297_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1297_ {
    data class Load(val id: Long) : GenEvent_1297_()
    data class Update(val model: GenModel_1297_) : GenEvent_1297_()
    data class Delete(val id: Long) : GenEvent_1297_()
    data object Refresh : GenEvent_1297_()
    data class Search(val query: String) : GenEvent_1297_()
    data class Filter(val predicate: String) : GenEvent_1297_()
}

sealed class GenState_1297_ {
    data object Idle : GenState_1297_()
    data object Loading : GenState_1297_()
    data class Success(val items: List<GenModel_1297_>) : GenState_1297_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1297_()
    data class Partial(val items: List<GenModel_1297_>, val hasMore: Boolean) : GenState_1297_()
}

interface GenRepository_1297_ {
    suspend fun getAll(): List<GenModel_1297_>
    suspend fun getById(id: Long): GenModel_1297_?
    suspend fun save(model: GenModel_1297_): GenModel_1297_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1297_>
}

@Singleton
class GenRepositoryImpl_1297_ @Inject constructor() : GenRepository_1297_ {
    private val store = mutableMapOf<Long, GenModel_1297_>()
    override suspend fun getAll(): List<GenModel_1297_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1297_? = store[id]
    override suspend fun save(model: GenModel_1297_): GenModel_1297_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1297_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1297_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1297_ @Inject constructor(
    private val repository: GenRepositoryImpl_1297_
) : GenUseCase_1297_<Unit, List<GenModel_1297_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1297_> = repository.getAll()
}

class GenSaveUseCase_1297_ @Inject constructor(
    private val repository: GenRepositoryImpl_1297_
) : GenUseCase_1297_<GenModel_1297_, GenModel_1297_> {
    override suspend fun invoke(params: GenModel_1297_): GenModel_1297_ = repository.save(params)
}

class GenDeleteUseCase_1297_ @Inject constructor(
    private val repository: GenRepositoryImpl_1297_
) : GenUseCase_1297_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1297_ @Inject constructor(
    private val repository: GenRepositoryImpl_1297_
) : GenUseCase_1297_<String, List<GenModel_1297_>> {
    override suspend fun invoke(params: String): List<GenModel_1297_> = repository.search(params)
}

abstract class GenMapper_1297_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1297_ : GenMapper_1297_<GenModel_1297_, String>() {
    override fun map(input: GenModel_1297_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1297_ : GenMapper_1297_<String, GenModel_1297_>() {
    override fun map(input: String): GenModel_1297_ {
        val parts = input.split(":")
        return GenModel_1297_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1297_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1297_,
    private val saveUseCase: GenSaveUseCase_1297_,
    private val deleteUseCase: GenDeleteUseCase_1297_,
    private val searchUseCase: GenSearchUseCase_1297_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1297_>(GenState_1297_.Idle)
    val state: StateFlow<GenState_1297_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1297_) {
        when (event) {
            is GenEvent_1297_.Load -> loadAll()
            is GenEvent_1297_.Update -> save(event.model)
            is GenEvent_1297_.Delete -> delete(event.id)
            is GenEvent_1297_.Refresh -> loadAll()
            is GenEvent_1297_.Search -> search(event.query)
            is GenEvent_1297_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1297_.Loading; _state.value = GenState_1297_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1297_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1297_.Success(searchUseCase(query)) } }
}
