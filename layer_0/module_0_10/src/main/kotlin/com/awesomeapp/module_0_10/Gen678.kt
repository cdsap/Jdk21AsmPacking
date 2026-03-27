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

data class GenModel_678_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_678_ {
    data class Load(val id: Long) : GenEvent_678_()
    data class Update(val model: GenModel_678_) : GenEvent_678_()
    data class Delete(val id: Long) : GenEvent_678_()
    data object Refresh : GenEvent_678_()
    data class Search(val query: String) : GenEvent_678_()
    data class Filter(val predicate: String) : GenEvent_678_()
}

sealed class GenState_678_ {
    data object Idle : GenState_678_()
    data object Loading : GenState_678_()
    data class Success(val items: List<GenModel_678_>) : GenState_678_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_678_()
    data class Partial(val items: List<GenModel_678_>, val hasMore: Boolean) : GenState_678_()
}

interface GenRepository_678_ {
    suspend fun getAll(): List<GenModel_678_>
    suspend fun getById(id: Long): GenModel_678_?
    suspend fun save(model: GenModel_678_): GenModel_678_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_678_>
}

@Singleton
class GenRepositoryImpl_678_ @Inject constructor() : GenRepository_678_ {
    private val store = mutableMapOf<Long, GenModel_678_>()
    override suspend fun getAll(): List<GenModel_678_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_678_? = store[id]
    override suspend fun save(model: GenModel_678_): GenModel_678_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_678_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_678_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_678_ @Inject constructor(
    private val repository: GenRepositoryImpl_678_
) : GenUseCase_678_<Unit, List<GenModel_678_>> {
    override suspend fun invoke(params: Unit): List<GenModel_678_> = repository.getAll()
}

class GenSaveUseCase_678_ @Inject constructor(
    private val repository: GenRepositoryImpl_678_
) : GenUseCase_678_<GenModel_678_, GenModel_678_> {
    override suspend fun invoke(params: GenModel_678_): GenModel_678_ = repository.save(params)
}

class GenDeleteUseCase_678_ @Inject constructor(
    private val repository: GenRepositoryImpl_678_
) : GenUseCase_678_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_678_ @Inject constructor(
    private val repository: GenRepositoryImpl_678_
) : GenUseCase_678_<String, List<GenModel_678_>> {
    override suspend fun invoke(params: String): List<GenModel_678_> = repository.search(params)
}

abstract class GenMapper_678_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_678_ : GenMapper_678_<GenModel_678_, String>() {
    override fun map(input: GenModel_678_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_678_ : GenMapper_678_<String, GenModel_678_>() {
    override fun map(input: String): GenModel_678_ {
        val parts = input.split(":")
        return GenModel_678_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_678_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_678_,
    private val saveUseCase: GenSaveUseCase_678_,
    private val deleteUseCase: GenDeleteUseCase_678_,
    private val searchUseCase: GenSearchUseCase_678_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_678_>(GenState_678_.Idle)
    val state: StateFlow<GenState_678_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_678_) {
        when (event) {
            is GenEvent_678_.Load -> loadAll()
            is GenEvent_678_.Update -> save(event.model)
            is GenEvent_678_.Delete -> delete(event.id)
            is GenEvent_678_.Refresh -> loadAll()
            is GenEvent_678_.Search -> search(event.query)
            is GenEvent_678_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_678_.Loading; _state.value = GenState_678_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_678_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_678_.Success(searchUseCase(query)) } }
}
