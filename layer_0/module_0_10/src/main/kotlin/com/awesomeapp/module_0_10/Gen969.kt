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

data class GenModel_969_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_969_ {
    data class Load(val id: Long) : GenEvent_969_()
    data class Update(val model: GenModel_969_) : GenEvent_969_()
    data class Delete(val id: Long) : GenEvent_969_()
    data object Refresh : GenEvent_969_()
    data class Search(val query: String) : GenEvent_969_()
    data class Filter(val predicate: String) : GenEvent_969_()
}

sealed class GenState_969_ {
    data object Idle : GenState_969_()
    data object Loading : GenState_969_()
    data class Success(val items: List<GenModel_969_>) : GenState_969_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_969_()
    data class Partial(val items: List<GenModel_969_>, val hasMore: Boolean) : GenState_969_()
}

interface GenRepository_969_ {
    suspend fun getAll(): List<GenModel_969_>
    suspend fun getById(id: Long): GenModel_969_?
    suspend fun save(model: GenModel_969_): GenModel_969_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_969_>
}

@Singleton
class GenRepositoryImpl_969_ @Inject constructor() : GenRepository_969_ {
    private val store = mutableMapOf<Long, GenModel_969_>()
    override suspend fun getAll(): List<GenModel_969_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_969_? = store[id]
    override suspend fun save(model: GenModel_969_): GenModel_969_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_969_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_969_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_969_ @Inject constructor(
    private val repository: GenRepositoryImpl_969_
) : GenUseCase_969_<Unit, List<GenModel_969_>> {
    override suspend fun invoke(params: Unit): List<GenModel_969_> = repository.getAll()
}

class GenSaveUseCase_969_ @Inject constructor(
    private val repository: GenRepositoryImpl_969_
) : GenUseCase_969_<GenModel_969_, GenModel_969_> {
    override suspend fun invoke(params: GenModel_969_): GenModel_969_ = repository.save(params)
}

class GenDeleteUseCase_969_ @Inject constructor(
    private val repository: GenRepositoryImpl_969_
) : GenUseCase_969_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_969_ @Inject constructor(
    private val repository: GenRepositoryImpl_969_
) : GenUseCase_969_<String, List<GenModel_969_>> {
    override suspend fun invoke(params: String): List<GenModel_969_> = repository.search(params)
}

abstract class GenMapper_969_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_969_ : GenMapper_969_<GenModel_969_, String>() {
    override fun map(input: GenModel_969_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_969_ : GenMapper_969_<String, GenModel_969_>() {
    override fun map(input: String): GenModel_969_ {
        val parts = input.split(":")
        return GenModel_969_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_969_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_969_,
    private val saveUseCase: GenSaveUseCase_969_,
    private val deleteUseCase: GenDeleteUseCase_969_,
    private val searchUseCase: GenSearchUseCase_969_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_969_>(GenState_969_.Idle)
    val state: StateFlow<GenState_969_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_969_) {
        when (event) {
            is GenEvent_969_.Load -> loadAll()
            is GenEvent_969_.Update -> save(event.model)
            is GenEvent_969_.Delete -> delete(event.id)
            is GenEvent_969_.Refresh -> loadAll()
            is GenEvent_969_.Search -> search(event.query)
            is GenEvent_969_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_969_.Loading; _state.value = GenState_969_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_969_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_969_.Success(searchUseCase(query)) } }
}
