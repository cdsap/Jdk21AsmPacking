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

data class GenModel_260_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_260_ {
    data class Load(val id: Long) : GenEvent_260_()
    data class Update(val model: GenModel_260_) : GenEvent_260_()
    data class Delete(val id: Long) : GenEvent_260_()
    data object Refresh : GenEvent_260_()
    data class Search(val query: String) : GenEvent_260_()
    data class Filter(val predicate: String) : GenEvent_260_()
}

sealed class GenState_260_ {
    data object Idle : GenState_260_()
    data object Loading : GenState_260_()
    data class Success(val items: List<GenModel_260_>) : GenState_260_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_260_()
    data class Partial(val items: List<GenModel_260_>, val hasMore: Boolean) : GenState_260_()
}

interface GenRepository_260_ {
    suspend fun getAll(): List<GenModel_260_>
    suspend fun getById(id: Long): GenModel_260_?
    suspend fun save(model: GenModel_260_): GenModel_260_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_260_>
}

@Singleton
class GenRepositoryImpl_260_ @Inject constructor() : GenRepository_260_ {
    private val store = mutableMapOf<Long, GenModel_260_>()
    override suspend fun getAll(): List<GenModel_260_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_260_? = store[id]
    override suspend fun save(model: GenModel_260_): GenModel_260_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_260_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_260_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_260_ @Inject constructor(
    private val repository: GenRepositoryImpl_260_
) : GenUseCase_260_<Unit, List<GenModel_260_>> {
    override suspend fun invoke(params: Unit): List<GenModel_260_> = repository.getAll()
}

class GenSaveUseCase_260_ @Inject constructor(
    private val repository: GenRepositoryImpl_260_
) : GenUseCase_260_<GenModel_260_, GenModel_260_> {
    override suspend fun invoke(params: GenModel_260_): GenModel_260_ = repository.save(params)
}

class GenDeleteUseCase_260_ @Inject constructor(
    private val repository: GenRepositoryImpl_260_
) : GenUseCase_260_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_260_ @Inject constructor(
    private val repository: GenRepositoryImpl_260_
) : GenUseCase_260_<String, List<GenModel_260_>> {
    override suspend fun invoke(params: String): List<GenModel_260_> = repository.search(params)
}

abstract class GenMapper_260_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_260_ : GenMapper_260_<GenModel_260_, String>() {
    override fun map(input: GenModel_260_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_260_ : GenMapper_260_<String, GenModel_260_>() {
    override fun map(input: String): GenModel_260_ {
        val parts = input.split(":")
        return GenModel_260_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_260_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_260_,
    private val saveUseCase: GenSaveUseCase_260_,
    private val deleteUseCase: GenDeleteUseCase_260_,
    private val searchUseCase: GenSearchUseCase_260_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_260_>(GenState_260_.Idle)
    val state: StateFlow<GenState_260_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_260_) {
        when (event) {
            is GenEvent_260_.Load -> loadAll()
            is GenEvent_260_.Update -> save(event.model)
            is GenEvent_260_.Delete -> delete(event.id)
            is GenEvent_260_.Refresh -> loadAll()
            is GenEvent_260_.Search -> search(event.query)
            is GenEvent_260_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_260_.Loading; _state.value = GenState_260_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_260_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_260_.Success(searchUseCase(query)) } }
}
