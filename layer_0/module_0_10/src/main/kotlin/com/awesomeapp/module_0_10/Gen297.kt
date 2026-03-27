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

data class GenModel_297_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_297_ {
    data class Load(val id: Long) : GenEvent_297_()
    data class Update(val model: GenModel_297_) : GenEvent_297_()
    data class Delete(val id: Long) : GenEvent_297_()
    data object Refresh : GenEvent_297_()
    data class Search(val query: String) : GenEvent_297_()
    data class Filter(val predicate: String) : GenEvent_297_()
}

sealed class GenState_297_ {
    data object Idle : GenState_297_()
    data object Loading : GenState_297_()
    data class Success(val items: List<GenModel_297_>) : GenState_297_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_297_()
    data class Partial(val items: List<GenModel_297_>, val hasMore: Boolean) : GenState_297_()
}

interface GenRepository_297_ {
    suspend fun getAll(): List<GenModel_297_>
    suspend fun getById(id: Long): GenModel_297_?
    suspend fun save(model: GenModel_297_): GenModel_297_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_297_>
}

@Singleton
class GenRepositoryImpl_297_ @Inject constructor() : GenRepository_297_ {
    private val store = mutableMapOf<Long, GenModel_297_>()
    override suspend fun getAll(): List<GenModel_297_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_297_? = store[id]
    override suspend fun save(model: GenModel_297_): GenModel_297_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_297_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_297_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_297_ @Inject constructor(
    private val repository: GenRepositoryImpl_297_
) : GenUseCase_297_<Unit, List<GenModel_297_>> {
    override suspend fun invoke(params: Unit): List<GenModel_297_> = repository.getAll()
}

class GenSaveUseCase_297_ @Inject constructor(
    private val repository: GenRepositoryImpl_297_
) : GenUseCase_297_<GenModel_297_, GenModel_297_> {
    override suspend fun invoke(params: GenModel_297_): GenModel_297_ = repository.save(params)
}

class GenDeleteUseCase_297_ @Inject constructor(
    private val repository: GenRepositoryImpl_297_
) : GenUseCase_297_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_297_ @Inject constructor(
    private val repository: GenRepositoryImpl_297_
) : GenUseCase_297_<String, List<GenModel_297_>> {
    override suspend fun invoke(params: String): List<GenModel_297_> = repository.search(params)
}

abstract class GenMapper_297_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_297_ : GenMapper_297_<GenModel_297_, String>() {
    override fun map(input: GenModel_297_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_297_ : GenMapper_297_<String, GenModel_297_>() {
    override fun map(input: String): GenModel_297_ {
        val parts = input.split(":")
        return GenModel_297_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_297_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_297_,
    private val saveUseCase: GenSaveUseCase_297_,
    private val deleteUseCase: GenDeleteUseCase_297_,
    private val searchUseCase: GenSearchUseCase_297_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_297_>(GenState_297_.Idle)
    val state: StateFlow<GenState_297_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_297_) {
        when (event) {
            is GenEvent_297_.Load -> loadAll()
            is GenEvent_297_.Update -> save(event.model)
            is GenEvent_297_.Delete -> delete(event.id)
            is GenEvent_297_.Refresh -> loadAll()
            is GenEvent_297_.Search -> search(event.query)
            is GenEvent_297_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_297_.Loading; _state.value = GenState_297_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_297_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_297_.Success(searchUseCase(query)) } }
}
