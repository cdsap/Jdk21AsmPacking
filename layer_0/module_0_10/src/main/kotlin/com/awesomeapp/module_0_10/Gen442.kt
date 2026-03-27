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

data class GenModel_442_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_442_ {
    data class Load(val id: Long) : GenEvent_442_()
    data class Update(val model: GenModel_442_) : GenEvent_442_()
    data class Delete(val id: Long) : GenEvent_442_()
    data object Refresh : GenEvent_442_()
    data class Search(val query: String) : GenEvent_442_()
    data class Filter(val predicate: String) : GenEvent_442_()
}

sealed class GenState_442_ {
    data object Idle : GenState_442_()
    data object Loading : GenState_442_()
    data class Success(val items: List<GenModel_442_>) : GenState_442_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_442_()
    data class Partial(val items: List<GenModel_442_>, val hasMore: Boolean) : GenState_442_()
}

interface GenRepository_442_ {
    suspend fun getAll(): List<GenModel_442_>
    suspend fun getById(id: Long): GenModel_442_?
    suspend fun save(model: GenModel_442_): GenModel_442_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_442_>
}

@Singleton
class GenRepositoryImpl_442_ @Inject constructor() : GenRepository_442_ {
    private val store = mutableMapOf<Long, GenModel_442_>()
    override suspend fun getAll(): List<GenModel_442_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_442_? = store[id]
    override suspend fun save(model: GenModel_442_): GenModel_442_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_442_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_442_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_442_ @Inject constructor(
    private val repository: GenRepositoryImpl_442_
) : GenUseCase_442_<Unit, List<GenModel_442_>> {
    override suspend fun invoke(params: Unit): List<GenModel_442_> = repository.getAll()
}

class GenSaveUseCase_442_ @Inject constructor(
    private val repository: GenRepositoryImpl_442_
) : GenUseCase_442_<GenModel_442_, GenModel_442_> {
    override suspend fun invoke(params: GenModel_442_): GenModel_442_ = repository.save(params)
}

class GenDeleteUseCase_442_ @Inject constructor(
    private val repository: GenRepositoryImpl_442_
) : GenUseCase_442_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_442_ @Inject constructor(
    private val repository: GenRepositoryImpl_442_
) : GenUseCase_442_<String, List<GenModel_442_>> {
    override suspend fun invoke(params: String): List<GenModel_442_> = repository.search(params)
}

abstract class GenMapper_442_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_442_ : GenMapper_442_<GenModel_442_, String>() {
    override fun map(input: GenModel_442_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_442_ : GenMapper_442_<String, GenModel_442_>() {
    override fun map(input: String): GenModel_442_ {
        val parts = input.split(":")
        return GenModel_442_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_442_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_442_,
    private val saveUseCase: GenSaveUseCase_442_,
    private val deleteUseCase: GenDeleteUseCase_442_,
    private val searchUseCase: GenSearchUseCase_442_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_442_>(GenState_442_.Idle)
    val state: StateFlow<GenState_442_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_442_) {
        when (event) {
            is GenEvent_442_.Load -> loadAll()
            is GenEvent_442_.Update -> save(event.model)
            is GenEvent_442_.Delete -> delete(event.id)
            is GenEvent_442_.Refresh -> loadAll()
            is GenEvent_442_.Search -> search(event.query)
            is GenEvent_442_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_442_.Loading; _state.value = GenState_442_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_442_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_442_.Success(searchUseCase(query)) } }
}
