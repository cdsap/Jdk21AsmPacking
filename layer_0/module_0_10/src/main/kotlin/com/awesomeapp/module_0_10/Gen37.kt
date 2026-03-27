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

data class GenModel_37_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_37_ {
    data class Load(val id: Long) : GenEvent_37_()
    data class Update(val model: GenModel_37_) : GenEvent_37_()
    data class Delete(val id: Long) : GenEvent_37_()
    data object Refresh : GenEvent_37_()
    data class Search(val query: String) : GenEvent_37_()
    data class Filter(val predicate: String) : GenEvent_37_()
}

sealed class GenState_37_ {
    data object Idle : GenState_37_()
    data object Loading : GenState_37_()
    data class Success(val items: List<GenModel_37_>) : GenState_37_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_37_()
    data class Partial(val items: List<GenModel_37_>, val hasMore: Boolean) : GenState_37_()
}

interface GenRepository_37_ {
    suspend fun getAll(): List<GenModel_37_>
    suspend fun getById(id: Long): GenModel_37_?
    suspend fun save(model: GenModel_37_): GenModel_37_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_37_>
}

@Singleton
class GenRepositoryImpl_37_ @Inject constructor() : GenRepository_37_ {
    private val store = mutableMapOf<Long, GenModel_37_>()
    override suspend fun getAll(): List<GenModel_37_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_37_? = store[id]
    override suspend fun save(model: GenModel_37_): GenModel_37_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_37_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_37_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_37_ @Inject constructor(
    private val repository: GenRepositoryImpl_37_
) : GenUseCase_37_<Unit, List<GenModel_37_>> {
    override suspend fun invoke(params: Unit): List<GenModel_37_> = repository.getAll()
}

class GenSaveUseCase_37_ @Inject constructor(
    private val repository: GenRepositoryImpl_37_
) : GenUseCase_37_<GenModel_37_, GenModel_37_> {
    override suspend fun invoke(params: GenModel_37_): GenModel_37_ = repository.save(params)
}

class GenDeleteUseCase_37_ @Inject constructor(
    private val repository: GenRepositoryImpl_37_
) : GenUseCase_37_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_37_ @Inject constructor(
    private val repository: GenRepositoryImpl_37_
) : GenUseCase_37_<String, List<GenModel_37_>> {
    override suspend fun invoke(params: String): List<GenModel_37_> = repository.search(params)
}

abstract class GenMapper_37_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_37_ : GenMapper_37_<GenModel_37_, String>() {
    override fun map(input: GenModel_37_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_37_ : GenMapper_37_<String, GenModel_37_>() {
    override fun map(input: String): GenModel_37_ {
        val parts = input.split(":")
        return GenModel_37_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_37_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_37_,
    private val saveUseCase: GenSaveUseCase_37_,
    private val deleteUseCase: GenDeleteUseCase_37_,
    private val searchUseCase: GenSearchUseCase_37_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_37_>(GenState_37_.Idle)
    val state: StateFlow<GenState_37_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_37_) {
        when (event) {
            is GenEvent_37_.Load -> loadAll()
            is GenEvent_37_.Update -> save(event.model)
            is GenEvent_37_.Delete -> delete(event.id)
            is GenEvent_37_.Refresh -> loadAll()
            is GenEvent_37_.Search -> search(event.query)
            is GenEvent_37_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_37_.Loading; _state.value = GenState_37_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_37_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_37_.Success(searchUseCase(query)) } }
}
