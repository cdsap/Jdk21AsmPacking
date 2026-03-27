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

data class GenModel_455_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_455_ {
    data class Load(val id: Long) : GenEvent_455_()
    data class Update(val model: GenModel_455_) : GenEvent_455_()
    data class Delete(val id: Long) : GenEvent_455_()
    data object Refresh : GenEvent_455_()
    data class Search(val query: String) : GenEvent_455_()
    data class Filter(val predicate: String) : GenEvent_455_()
}

sealed class GenState_455_ {
    data object Idle : GenState_455_()
    data object Loading : GenState_455_()
    data class Success(val items: List<GenModel_455_>) : GenState_455_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_455_()
    data class Partial(val items: List<GenModel_455_>, val hasMore: Boolean) : GenState_455_()
}

interface GenRepository_455_ {
    suspend fun getAll(): List<GenModel_455_>
    suspend fun getById(id: Long): GenModel_455_?
    suspend fun save(model: GenModel_455_): GenModel_455_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_455_>
}

@Singleton
class GenRepositoryImpl_455_ @Inject constructor() : GenRepository_455_ {
    private val store = mutableMapOf<Long, GenModel_455_>()
    override suspend fun getAll(): List<GenModel_455_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_455_? = store[id]
    override suspend fun save(model: GenModel_455_): GenModel_455_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_455_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_455_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_455_ @Inject constructor(
    private val repository: GenRepositoryImpl_455_
) : GenUseCase_455_<Unit, List<GenModel_455_>> {
    override suspend fun invoke(params: Unit): List<GenModel_455_> = repository.getAll()
}

class GenSaveUseCase_455_ @Inject constructor(
    private val repository: GenRepositoryImpl_455_
) : GenUseCase_455_<GenModel_455_, GenModel_455_> {
    override suspend fun invoke(params: GenModel_455_): GenModel_455_ = repository.save(params)
}

class GenDeleteUseCase_455_ @Inject constructor(
    private val repository: GenRepositoryImpl_455_
) : GenUseCase_455_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_455_ @Inject constructor(
    private val repository: GenRepositoryImpl_455_
) : GenUseCase_455_<String, List<GenModel_455_>> {
    override suspend fun invoke(params: String): List<GenModel_455_> = repository.search(params)
}

abstract class GenMapper_455_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_455_ : GenMapper_455_<GenModel_455_, String>() {
    override fun map(input: GenModel_455_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_455_ : GenMapper_455_<String, GenModel_455_>() {
    override fun map(input: String): GenModel_455_ {
        val parts = input.split(":")
        return GenModel_455_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_455_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_455_,
    private val saveUseCase: GenSaveUseCase_455_,
    private val deleteUseCase: GenDeleteUseCase_455_,
    private val searchUseCase: GenSearchUseCase_455_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_455_>(GenState_455_.Idle)
    val state: StateFlow<GenState_455_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_455_) {
        when (event) {
            is GenEvent_455_.Load -> loadAll()
            is GenEvent_455_.Update -> save(event.model)
            is GenEvent_455_.Delete -> delete(event.id)
            is GenEvent_455_.Refresh -> loadAll()
            is GenEvent_455_.Search -> search(event.query)
            is GenEvent_455_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_455_.Loading; _state.value = GenState_455_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_455_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_455_.Success(searchUseCase(query)) } }
}
