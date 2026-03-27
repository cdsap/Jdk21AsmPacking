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

data class GenModel_398_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_398_ {
    data class Load(val id: Long) : GenEvent_398_()
    data class Update(val model: GenModel_398_) : GenEvent_398_()
    data class Delete(val id: Long) : GenEvent_398_()
    data object Refresh : GenEvent_398_()
    data class Search(val query: String) : GenEvent_398_()
    data class Filter(val predicate: String) : GenEvent_398_()
}

sealed class GenState_398_ {
    data object Idle : GenState_398_()
    data object Loading : GenState_398_()
    data class Success(val items: List<GenModel_398_>) : GenState_398_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_398_()
    data class Partial(val items: List<GenModel_398_>, val hasMore: Boolean) : GenState_398_()
}

interface GenRepository_398_ {
    suspend fun getAll(): List<GenModel_398_>
    suspend fun getById(id: Long): GenModel_398_?
    suspend fun save(model: GenModel_398_): GenModel_398_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_398_>
}

@Singleton
class GenRepositoryImpl_398_ @Inject constructor() : GenRepository_398_ {
    private val store = mutableMapOf<Long, GenModel_398_>()
    override suspend fun getAll(): List<GenModel_398_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_398_? = store[id]
    override suspend fun save(model: GenModel_398_): GenModel_398_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_398_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_398_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_398_ @Inject constructor(
    private val repository: GenRepositoryImpl_398_
) : GenUseCase_398_<Unit, List<GenModel_398_>> {
    override suspend fun invoke(params: Unit): List<GenModel_398_> = repository.getAll()
}

class GenSaveUseCase_398_ @Inject constructor(
    private val repository: GenRepositoryImpl_398_
) : GenUseCase_398_<GenModel_398_, GenModel_398_> {
    override suspend fun invoke(params: GenModel_398_): GenModel_398_ = repository.save(params)
}

class GenDeleteUseCase_398_ @Inject constructor(
    private val repository: GenRepositoryImpl_398_
) : GenUseCase_398_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_398_ @Inject constructor(
    private val repository: GenRepositoryImpl_398_
) : GenUseCase_398_<String, List<GenModel_398_>> {
    override suspend fun invoke(params: String): List<GenModel_398_> = repository.search(params)
}

abstract class GenMapper_398_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_398_ : GenMapper_398_<GenModel_398_, String>() {
    override fun map(input: GenModel_398_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_398_ : GenMapper_398_<String, GenModel_398_>() {
    override fun map(input: String): GenModel_398_ {
        val parts = input.split(":")
        return GenModel_398_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_398_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_398_,
    private val saveUseCase: GenSaveUseCase_398_,
    private val deleteUseCase: GenDeleteUseCase_398_,
    private val searchUseCase: GenSearchUseCase_398_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_398_>(GenState_398_.Idle)
    val state: StateFlow<GenState_398_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_398_) {
        when (event) {
            is GenEvent_398_.Load -> loadAll()
            is GenEvent_398_.Update -> save(event.model)
            is GenEvent_398_.Delete -> delete(event.id)
            is GenEvent_398_.Refresh -> loadAll()
            is GenEvent_398_.Search -> search(event.query)
            is GenEvent_398_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_398_.Loading; _state.value = GenState_398_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_398_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_398_.Success(searchUseCase(query)) } }
}
