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

data class GenModel_663_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_663_ {
    data class Load(val id: Long) : GenEvent_663_()
    data class Update(val model: GenModel_663_) : GenEvent_663_()
    data class Delete(val id: Long) : GenEvent_663_()
    data object Refresh : GenEvent_663_()
    data class Search(val query: String) : GenEvent_663_()
    data class Filter(val predicate: String) : GenEvent_663_()
}

sealed class GenState_663_ {
    data object Idle : GenState_663_()
    data object Loading : GenState_663_()
    data class Success(val items: List<GenModel_663_>) : GenState_663_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_663_()
    data class Partial(val items: List<GenModel_663_>, val hasMore: Boolean) : GenState_663_()
}

interface GenRepository_663_ {
    suspend fun getAll(): List<GenModel_663_>
    suspend fun getById(id: Long): GenModel_663_?
    suspend fun save(model: GenModel_663_): GenModel_663_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_663_>
}

@Singleton
class GenRepositoryImpl_663_ @Inject constructor() : GenRepository_663_ {
    private val store = mutableMapOf<Long, GenModel_663_>()
    override suspend fun getAll(): List<GenModel_663_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_663_? = store[id]
    override suspend fun save(model: GenModel_663_): GenModel_663_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_663_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_663_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_663_ @Inject constructor(
    private val repository: GenRepositoryImpl_663_
) : GenUseCase_663_<Unit, List<GenModel_663_>> {
    override suspend fun invoke(params: Unit): List<GenModel_663_> = repository.getAll()
}

class GenSaveUseCase_663_ @Inject constructor(
    private val repository: GenRepositoryImpl_663_
) : GenUseCase_663_<GenModel_663_, GenModel_663_> {
    override suspend fun invoke(params: GenModel_663_): GenModel_663_ = repository.save(params)
}

class GenDeleteUseCase_663_ @Inject constructor(
    private val repository: GenRepositoryImpl_663_
) : GenUseCase_663_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_663_ @Inject constructor(
    private val repository: GenRepositoryImpl_663_
) : GenUseCase_663_<String, List<GenModel_663_>> {
    override suspend fun invoke(params: String): List<GenModel_663_> = repository.search(params)
}

abstract class GenMapper_663_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_663_ : GenMapper_663_<GenModel_663_, String>() {
    override fun map(input: GenModel_663_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_663_ : GenMapper_663_<String, GenModel_663_>() {
    override fun map(input: String): GenModel_663_ {
        val parts = input.split(":")
        return GenModel_663_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_663_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_663_,
    private val saveUseCase: GenSaveUseCase_663_,
    private val deleteUseCase: GenDeleteUseCase_663_,
    private val searchUseCase: GenSearchUseCase_663_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_663_>(GenState_663_.Idle)
    val state: StateFlow<GenState_663_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_663_) {
        when (event) {
            is GenEvent_663_.Load -> loadAll()
            is GenEvent_663_.Update -> save(event.model)
            is GenEvent_663_.Delete -> delete(event.id)
            is GenEvent_663_.Refresh -> loadAll()
            is GenEvent_663_.Search -> search(event.query)
            is GenEvent_663_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_663_.Loading; _state.value = GenState_663_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_663_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_663_.Success(searchUseCase(query)) } }
}
