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

data class GenModel_672_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_672_ {
    data class Load(val id: Long) : GenEvent_672_()
    data class Update(val model: GenModel_672_) : GenEvent_672_()
    data class Delete(val id: Long) : GenEvent_672_()
    data object Refresh : GenEvent_672_()
    data class Search(val query: String) : GenEvent_672_()
    data class Filter(val predicate: String) : GenEvent_672_()
}

sealed class GenState_672_ {
    data object Idle : GenState_672_()
    data object Loading : GenState_672_()
    data class Success(val items: List<GenModel_672_>) : GenState_672_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_672_()
    data class Partial(val items: List<GenModel_672_>, val hasMore: Boolean) : GenState_672_()
}

interface GenRepository_672_ {
    suspend fun getAll(): List<GenModel_672_>
    suspend fun getById(id: Long): GenModel_672_?
    suspend fun save(model: GenModel_672_): GenModel_672_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_672_>
}

@Singleton
class GenRepositoryImpl_672_ @Inject constructor() : GenRepository_672_ {
    private val store = mutableMapOf<Long, GenModel_672_>()
    override suspend fun getAll(): List<GenModel_672_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_672_? = store[id]
    override suspend fun save(model: GenModel_672_): GenModel_672_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_672_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_672_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_672_ @Inject constructor(
    private val repository: GenRepositoryImpl_672_
) : GenUseCase_672_<Unit, List<GenModel_672_>> {
    override suspend fun invoke(params: Unit): List<GenModel_672_> = repository.getAll()
}

class GenSaveUseCase_672_ @Inject constructor(
    private val repository: GenRepositoryImpl_672_
) : GenUseCase_672_<GenModel_672_, GenModel_672_> {
    override suspend fun invoke(params: GenModel_672_): GenModel_672_ = repository.save(params)
}

class GenDeleteUseCase_672_ @Inject constructor(
    private val repository: GenRepositoryImpl_672_
) : GenUseCase_672_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_672_ @Inject constructor(
    private val repository: GenRepositoryImpl_672_
) : GenUseCase_672_<String, List<GenModel_672_>> {
    override suspend fun invoke(params: String): List<GenModel_672_> = repository.search(params)
}

abstract class GenMapper_672_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_672_ : GenMapper_672_<GenModel_672_, String>() {
    override fun map(input: GenModel_672_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_672_ : GenMapper_672_<String, GenModel_672_>() {
    override fun map(input: String): GenModel_672_ {
        val parts = input.split(":")
        return GenModel_672_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_672_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_672_,
    private val saveUseCase: GenSaveUseCase_672_,
    private val deleteUseCase: GenDeleteUseCase_672_,
    private val searchUseCase: GenSearchUseCase_672_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_672_>(GenState_672_.Idle)
    val state: StateFlow<GenState_672_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_672_) {
        when (event) {
            is GenEvent_672_.Load -> loadAll()
            is GenEvent_672_.Update -> save(event.model)
            is GenEvent_672_.Delete -> delete(event.id)
            is GenEvent_672_.Refresh -> loadAll()
            is GenEvent_672_.Search -> search(event.query)
            is GenEvent_672_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_672_.Loading; _state.value = GenState_672_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_672_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_672_.Success(searchUseCase(query)) } }
}
