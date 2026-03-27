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

data class GenModel_70_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_70_ {
    data class Load(val id: Long) : GenEvent_70_()
    data class Update(val model: GenModel_70_) : GenEvent_70_()
    data class Delete(val id: Long) : GenEvent_70_()
    data object Refresh : GenEvent_70_()
    data class Search(val query: String) : GenEvent_70_()
    data class Filter(val predicate: String) : GenEvent_70_()
}

sealed class GenState_70_ {
    data object Idle : GenState_70_()
    data object Loading : GenState_70_()
    data class Success(val items: List<GenModel_70_>) : GenState_70_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_70_()
    data class Partial(val items: List<GenModel_70_>, val hasMore: Boolean) : GenState_70_()
}

interface GenRepository_70_ {
    suspend fun getAll(): List<GenModel_70_>
    suspend fun getById(id: Long): GenModel_70_?
    suspend fun save(model: GenModel_70_): GenModel_70_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_70_>
}

@Singleton
class GenRepositoryImpl_70_ @Inject constructor() : GenRepository_70_ {
    private val store = mutableMapOf<Long, GenModel_70_>()
    override suspend fun getAll(): List<GenModel_70_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_70_? = store[id]
    override suspend fun save(model: GenModel_70_): GenModel_70_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_70_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_70_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_70_ @Inject constructor(
    private val repository: GenRepositoryImpl_70_
) : GenUseCase_70_<Unit, List<GenModel_70_>> {
    override suspend fun invoke(params: Unit): List<GenModel_70_> = repository.getAll()
}

class GenSaveUseCase_70_ @Inject constructor(
    private val repository: GenRepositoryImpl_70_
) : GenUseCase_70_<GenModel_70_, GenModel_70_> {
    override suspend fun invoke(params: GenModel_70_): GenModel_70_ = repository.save(params)
}

class GenDeleteUseCase_70_ @Inject constructor(
    private val repository: GenRepositoryImpl_70_
) : GenUseCase_70_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_70_ @Inject constructor(
    private val repository: GenRepositoryImpl_70_
) : GenUseCase_70_<String, List<GenModel_70_>> {
    override suspend fun invoke(params: String): List<GenModel_70_> = repository.search(params)
}

abstract class GenMapper_70_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_70_ : GenMapper_70_<GenModel_70_, String>() {
    override fun map(input: GenModel_70_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_70_ : GenMapper_70_<String, GenModel_70_>() {
    override fun map(input: String): GenModel_70_ {
        val parts = input.split(":")
        return GenModel_70_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_70_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_70_,
    private val saveUseCase: GenSaveUseCase_70_,
    private val deleteUseCase: GenDeleteUseCase_70_,
    private val searchUseCase: GenSearchUseCase_70_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_70_>(GenState_70_.Idle)
    val state: StateFlow<GenState_70_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_70_) {
        when (event) {
            is GenEvent_70_.Load -> loadAll()
            is GenEvent_70_.Update -> save(event.model)
            is GenEvent_70_.Delete -> delete(event.id)
            is GenEvent_70_.Refresh -> loadAll()
            is GenEvent_70_.Search -> search(event.query)
            is GenEvent_70_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_70_.Loading; _state.value = GenState_70_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_70_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_70_.Success(searchUseCase(query)) } }
}
