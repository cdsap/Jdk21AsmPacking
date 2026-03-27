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

data class GenModel_3052_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3052_ {
    data class Load(val id: Long) : GenEvent_3052_()
    data class Update(val model: GenModel_3052_) : GenEvent_3052_()
    data class Delete(val id: Long) : GenEvent_3052_()
    data object Refresh : GenEvent_3052_()
    data class Search(val query: String) : GenEvent_3052_()
    data class Filter(val predicate: String) : GenEvent_3052_()
}

sealed class GenState_3052_ {
    data object Idle : GenState_3052_()
    data object Loading : GenState_3052_()
    data class Success(val items: List<GenModel_3052_>) : GenState_3052_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3052_()
    data class Partial(val items: List<GenModel_3052_>, val hasMore: Boolean) : GenState_3052_()
}

interface GenRepository_3052_ {
    suspend fun getAll(): List<GenModel_3052_>
    suspend fun getById(id: Long): GenModel_3052_?
    suspend fun save(model: GenModel_3052_): GenModel_3052_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3052_>
}

@Singleton
class GenRepositoryImpl_3052_ @Inject constructor() : GenRepository_3052_ {
    private val store = mutableMapOf<Long, GenModel_3052_>()
    override suspend fun getAll(): List<GenModel_3052_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3052_? = store[id]
    override suspend fun save(model: GenModel_3052_): GenModel_3052_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3052_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3052_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3052_ @Inject constructor(
    private val repository: GenRepositoryImpl_3052_
) : GenUseCase_3052_<Unit, List<GenModel_3052_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3052_> = repository.getAll()
}

class GenSaveUseCase_3052_ @Inject constructor(
    private val repository: GenRepositoryImpl_3052_
) : GenUseCase_3052_<GenModel_3052_, GenModel_3052_> {
    override suspend fun invoke(params: GenModel_3052_): GenModel_3052_ = repository.save(params)
}

class GenDeleteUseCase_3052_ @Inject constructor(
    private val repository: GenRepositoryImpl_3052_
) : GenUseCase_3052_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3052_ @Inject constructor(
    private val repository: GenRepositoryImpl_3052_
) : GenUseCase_3052_<String, List<GenModel_3052_>> {
    override suspend fun invoke(params: String): List<GenModel_3052_> = repository.search(params)
}

abstract class GenMapper_3052_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3052_ : GenMapper_3052_<GenModel_3052_, String>() {
    override fun map(input: GenModel_3052_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3052_ : GenMapper_3052_<String, GenModel_3052_>() {
    override fun map(input: String): GenModel_3052_ {
        val parts = input.split(":")
        return GenModel_3052_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3052_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3052_,
    private val saveUseCase: GenSaveUseCase_3052_,
    private val deleteUseCase: GenDeleteUseCase_3052_,
    private val searchUseCase: GenSearchUseCase_3052_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3052_>(GenState_3052_.Idle)
    val state: StateFlow<GenState_3052_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3052_) {
        when (event) {
            is GenEvent_3052_.Load -> loadAll()
            is GenEvent_3052_.Update -> save(event.model)
            is GenEvent_3052_.Delete -> delete(event.id)
            is GenEvent_3052_.Refresh -> loadAll()
            is GenEvent_3052_.Search -> search(event.query)
            is GenEvent_3052_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3052_.Loading; _state.value = GenState_3052_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3052_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3052_.Success(searchUseCase(query)) } }
}
