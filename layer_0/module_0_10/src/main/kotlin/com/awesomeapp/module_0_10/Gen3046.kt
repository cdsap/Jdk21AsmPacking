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

data class GenModel_3046_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3046_ {
    data class Load(val id: Long) : GenEvent_3046_()
    data class Update(val model: GenModel_3046_) : GenEvent_3046_()
    data class Delete(val id: Long) : GenEvent_3046_()
    data object Refresh : GenEvent_3046_()
    data class Search(val query: String) : GenEvent_3046_()
    data class Filter(val predicate: String) : GenEvent_3046_()
}

sealed class GenState_3046_ {
    data object Idle : GenState_3046_()
    data object Loading : GenState_3046_()
    data class Success(val items: List<GenModel_3046_>) : GenState_3046_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3046_()
    data class Partial(val items: List<GenModel_3046_>, val hasMore: Boolean) : GenState_3046_()
}

interface GenRepository_3046_ {
    suspend fun getAll(): List<GenModel_3046_>
    suspend fun getById(id: Long): GenModel_3046_?
    suspend fun save(model: GenModel_3046_): GenModel_3046_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3046_>
}

@Singleton
class GenRepositoryImpl_3046_ @Inject constructor() : GenRepository_3046_ {
    private val store = mutableMapOf<Long, GenModel_3046_>()
    override suspend fun getAll(): List<GenModel_3046_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3046_? = store[id]
    override suspend fun save(model: GenModel_3046_): GenModel_3046_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3046_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3046_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3046_ @Inject constructor(
    private val repository: GenRepositoryImpl_3046_
) : GenUseCase_3046_<Unit, List<GenModel_3046_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3046_> = repository.getAll()
}

class GenSaveUseCase_3046_ @Inject constructor(
    private val repository: GenRepositoryImpl_3046_
) : GenUseCase_3046_<GenModel_3046_, GenModel_3046_> {
    override suspend fun invoke(params: GenModel_3046_): GenModel_3046_ = repository.save(params)
}

class GenDeleteUseCase_3046_ @Inject constructor(
    private val repository: GenRepositoryImpl_3046_
) : GenUseCase_3046_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3046_ @Inject constructor(
    private val repository: GenRepositoryImpl_3046_
) : GenUseCase_3046_<String, List<GenModel_3046_>> {
    override suspend fun invoke(params: String): List<GenModel_3046_> = repository.search(params)
}

abstract class GenMapper_3046_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3046_ : GenMapper_3046_<GenModel_3046_, String>() {
    override fun map(input: GenModel_3046_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3046_ : GenMapper_3046_<String, GenModel_3046_>() {
    override fun map(input: String): GenModel_3046_ {
        val parts = input.split(":")
        return GenModel_3046_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3046_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3046_,
    private val saveUseCase: GenSaveUseCase_3046_,
    private val deleteUseCase: GenDeleteUseCase_3046_,
    private val searchUseCase: GenSearchUseCase_3046_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3046_>(GenState_3046_.Idle)
    val state: StateFlow<GenState_3046_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3046_) {
        when (event) {
            is GenEvent_3046_.Load -> loadAll()
            is GenEvent_3046_.Update -> save(event.model)
            is GenEvent_3046_.Delete -> delete(event.id)
            is GenEvent_3046_.Refresh -> loadAll()
            is GenEvent_3046_.Search -> search(event.query)
            is GenEvent_3046_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3046_.Loading; _state.value = GenState_3046_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3046_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3046_.Success(searchUseCase(query)) } }
}
