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

data class GenModel_589_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_589_ {
    data class Load(val id: Long) : GenEvent_589_()
    data class Update(val model: GenModel_589_) : GenEvent_589_()
    data class Delete(val id: Long) : GenEvent_589_()
    data object Refresh : GenEvent_589_()
    data class Search(val query: String) : GenEvent_589_()
    data class Filter(val predicate: String) : GenEvent_589_()
}

sealed class GenState_589_ {
    data object Idle : GenState_589_()
    data object Loading : GenState_589_()
    data class Success(val items: List<GenModel_589_>) : GenState_589_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_589_()
    data class Partial(val items: List<GenModel_589_>, val hasMore: Boolean) : GenState_589_()
}

interface GenRepository_589_ {
    suspend fun getAll(): List<GenModel_589_>
    suspend fun getById(id: Long): GenModel_589_?
    suspend fun save(model: GenModel_589_): GenModel_589_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_589_>
}

@Singleton
class GenRepositoryImpl_589_ @Inject constructor() : GenRepository_589_ {
    private val store = mutableMapOf<Long, GenModel_589_>()
    override suspend fun getAll(): List<GenModel_589_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_589_? = store[id]
    override suspend fun save(model: GenModel_589_): GenModel_589_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_589_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_589_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_589_ @Inject constructor(
    private val repository: GenRepositoryImpl_589_
) : GenUseCase_589_<Unit, List<GenModel_589_>> {
    override suspend fun invoke(params: Unit): List<GenModel_589_> = repository.getAll()
}

class GenSaveUseCase_589_ @Inject constructor(
    private val repository: GenRepositoryImpl_589_
) : GenUseCase_589_<GenModel_589_, GenModel_589_> {
    override suspend fun invoke(params: GenModel_589_): GenModel_589_ = repository.save(params)
}

class GenDeleteUseCase_589_ @Inject constructor(
    private val repository: GenRepositoryImpl_589_
) : GenUseCase_589_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_589_ @Inject constructor(
    private val repository: GenRepositoryImpl_589_
) : GenUseCase_589_<String, List<GenModel_589_>> {
    override suspend fun invoke(params: String): List<GenModel_589_> = repository.search(params)
}

abstract class GenMapper_589_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_589_ : GenMapper_589_<GenModel_589_, String>() {
    override fun map(input: GenModel_589_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_589_ : GenMapper_589_<String, GenModel_589_>() {
    override fun map(input: String): GenModel_589_ {
        val parts = input.split(":")
        return GenModel_589_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_589_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_589_,
    private val saveUseCase: GenSaveUseCase_589_,
    private val deleteUseCase: GenDeleteUseCase_589_,
    private val searchUseCase: GenSearchUseCase_589_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_589_>(GenState_589_.Idle)
    val state: StateFlow<GenState_589_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_589_) {
        when (event) {
            is GenEvent_589_.Load -> loadAll()
            is GenEvent_589_.Update -> save(event.model)
            is GenEvent_589_.Delete -> delete(event.id)
            is GenEvent_589_.Refresh -> loadAll()
            is GenEvent_589_.Search -> search(event.query)
            is GenEvent_589_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_589_.Loading; _state.value = GenState_589_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_589_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_589_.Success(searchUseCase(query)) } }
}
