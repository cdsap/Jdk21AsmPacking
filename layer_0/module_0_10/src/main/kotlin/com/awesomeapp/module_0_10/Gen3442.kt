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

data class GenModel_3442_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3442_ {
    data class Load(val id: Long) : GenEvent_3442_()
    data class Update(val model: GenModel_3442_) : GenEvent_3442_()
    data class Delete(val id: Long) : GenEvent_3442_()
    data object Refresh : GenEvent_3442_()
    data class Search(val query: String) : GenEvent_3442_()
    data class Filter(val predicate: String) : GenEvent_3442_()
}

sealed class GenState_3442_ {
    data object Idle : GenState_3442_()
    data object Loading : GenState_3442_()
    data class Success(val items: List<GenModel_3442_>) : GenState_3442_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3442_()
    data class Partial(val items: List<GenModel_3442_>, val hasMore: Boolean) : GenState_3442_()
}

interface GenRepository_3442_ {
    suspend fun getAll(): List<GenModel_3442_>
    suspend fun getById(id: Long): GenModel_3442_?
    suspend fun save(model: GenModel_3442_): GenModel_3442_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3442_>
}

@Singleton
class GenRepositoryImpl_3442_ @Inject constructor() : GenRepository_3442_ {
    private val store = mutableMapOf<Long, GenModel_3442_>()
    override suspend fun getAll(): List<GenModel_3442_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3442_? = store[id]
    override suspend fun save(model: GenModel_3442_): GenModel_3442_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3442_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3442_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3442_ @Inject constructor(
    private val repository: GenRepositoryImpl_3442_
) : GenUseCase_3442_<Unit, List<GenModel_3442_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3442_> = repository.getAll()
}

class GenSaveUseCase_3442_ @Inject constructor(
    private val repository: GenRepositoryImpl_3442_
) : GenUseCase_3442_<GenModel_3442_, GenModel_3442_> {
    override suspend fun invoke(params: GenModel_3442_): GenModel_3442_ = repository.save(params)
}

class GenDeleteUseCase_3442_ @Inject constructor(
    private val repository: GenRepositoryImpl_3442_
) : GenUseCase_3442_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3442_ @Inject constructor(
    private val repository: GenRepositoryImpl_3442_
) : GenUseCase_3442_<String, List<GenModel_3442_>> {
    override suspend fun invoke(params: String): List<GenModel_3442_> = repository.search(params)
}

abstract class GenMapper_3442_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3442_ : GenMapper_3442_<GenModel_3442_, String>() {
    override fun map(input: GenModel_3442_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3442_ : GenMapper_3442_<String, GenModel_3442_>() {
    override fun map(input: String): GenModel_3442_ {
        val parts = input.split(":")
        return GenModel_3442_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3442_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3442_,
    private val saveUseCase: GenSaveUseCase_3442_,
    private val deleteUseCase: GenDeleteUseCase_3442_,
    private val searchUseCase: GenSearchUseCase_3442_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3442_>(GenState_3442_.Idle)
    val state: StateFlow<GenState_3442_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3442_) {
        when (event) {
            is GenEvent_3442_.Load -> loadAll()
            is GenEvent_3442_.Update -> save(event.model)
            is GenEvent_3442_.Delete -> delete(event.id)
            is GenEvent_3442_.Refresh -> loadAll()
            is GenEvent_3442_.Search -> search(event.query)
            is GenEvent_3442_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3442_.Loading; _state.value = GenState_3442_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3442_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3442_.Success(searchUseCase(query)) } }
}
