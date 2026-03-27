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

data class GenModel_309_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_309_ {
    data class Load(val id: Long) : GenEvent_309_()
    data class Update(val model: GenModel_309_) : GenEvent_309_()
    data class Delete(val id: Long) : GenEvent_309_()
    data object Refresh : GenEvent_309_()
    data class Search(val query: String) : GenEvent_309_()
    data class Filter(val predicate: String) : GenEvent_309_()
}

sealed class GenState_309_ {
    data object Idle : GenState_309_()
    data object Loading : GenState_309_()
    data class Success(val items: List<GenModel_309_>) : GenState_309_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_309_()
    data class Partial(val items: List<GenModel_309_>, val hasMore: Boolean) : GenState_309_()
}

interface GenRepository_309_ {
    suspend fun getAll(): List<GenModel_309_>
    suspend fun getById(id: Long): GenModel_309_?
    suspend fun save(model: GenModel_309_): GenModel_309_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_309_>
}

@Singleton
class GenRepositoryImpl_309_ @Inject constructor() : GenRepository_309_ {
    private val store = mutableMapOf<Long, GenModel_309_>()
    override suspend fun getAll(): List<GenModel_309_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_309_? = store[id]
    override suspend fun save(model: GenModel_309_): GenModel_309_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_309_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_309_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_309_ @Inject constructor(
    private val repository: GenRepositoryImpl_309_
) : GenUseCase_309_<Unit, List<GenModel_309_>> {
    override suspend fun invoke(params: Unit): List<GenModel_309_> = repository.getAll()
}

class GenSaveUseCase_309_ @Inject constructor(
    private val repository: GenRepositoryImpl_309_
) : GenUseCase_309_<GenModel_309_, GenModel_309_> {
    override suspend fun invoke(params: GenModel_309_): GenModel_309_ = repository.save(params)
}

class GenDeleteUseCase_309_ @Inject constructor(
    private val repository: GenRepositoryImpl_309_
) : GenUseCase_309_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_309_ @Inject constructor(
    private val repository: GenRepositoryImpl_309_
) : GenUseCase_309_<String, List<GenModel_309_>> {
    override suspend fun invoke(params: String): List<GenModel_309_> = repository.search(params)
}

abstract class GenMapper_309_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_309_ : GenMapper_309_<GenModel_309_, String>() {
    override fun map(input: GenModel_309_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_309_ : GenMapper_309_<String, GenModel_309_>() {
    override fun map(input: String): GenModel_309_ {
        val parts = input.split(":")
        return GenModel_309_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_309_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_309_,
    private val saveUseCase: GenSaveUseCase_309_,
    private val deleteUseCase: GenDeleteUseCase_309_,
    private val searchUseCase: GenSearchUseCase_309_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_309_>(GenState_309_.Idle)
    val state: StateFlow<GenState_309_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_309_) {
        when (event) {
            is GenEvent_309_.Load -> loadAll()
            is GenEvent_309_.Update -> save(event.model)
            is GenEvent_309_.Delete -> delete(event.id)
            is GenEvent_309_.Refresh -> loadAll()
            is GenEvent_309_.Search -> search(event.query)
            is GenEvent_309_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_309_.Loading; _state.value = GenState_309_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_309_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_309_.Success(searchUseCase(query)) } }
}
