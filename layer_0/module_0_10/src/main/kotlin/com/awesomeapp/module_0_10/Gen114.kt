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

data class GenModel_114_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_114_ {
    data class Load(val id: Long) : GenEvent_114_()
    data class Update(val model: GenModel_114_) : GenEvent_114_()
    data class Delete(val id: Long) : GenEvent_114_()
    data object Refresh : GenEvent_114_()
    data class Search(val query: String) : GenEvent_114_()
    data class Filter(val predicate: String) : GenEvent_114_()
}

sealed class GenState_114_ {
    data object Idle : GenState_114_()
    data object Loading : GenState_114_()
    data class Success(val items: List<GenModel_114_>) : GenState_114_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_114_()
    data class Partial(val items: List<GenModel_114_>, val hasMore: Boolean) : GenState_114_()
}

interface GenRepository_114_ {
    suspend fun getAll(): List<GenModel_114_>
    suspend fun getById(id: Long): GenModel_114_?
    suspend fun save(model: GenModel_114_): GenModel_114_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_114_>
}

@Singleton
class GenRepositoryImpl_114_ @Inject constructor() : GenRepository_114_ {
    private val store = mutableMapOf<Long, GenModel_114_>()
    override suspend fun getAll(): List<GenModel_114_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_114_? = store[id]
    override suspend fun save(model: GenModel_114_): GenModel_114_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_114_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_114_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_114_ @Inject constructor(
    private val repository: GenRepositoryImpl_114_
) : GenUseCase_114_<Unit, List<GenModel_114_>> {
    override suspend fun invoke(params: Unit): List<GenModel_114_> = repository.getAll()
}

class GenSaveUseCase_114_ @Inject constructor(
    private val repository: GenRepositoryImpl_114_
) : GenUseCase_114_<GenModel_114_, GenModel_114_> {
    override suspend fun invoke(params: GenModel_114_): GenModel_114_ = repository.save(params)
}

class GenDeleteUseCase_114_ @Inject constructor(
    private val repository: GenRepositoryImpl_114_
) : GenUseCase_114_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_114_ @Inject constructor(
    private val repository: GenRepositoryImpl_114_
) : GenUseCase_114_<String, List<GenModel_114_>> {
    override suspend fun invoke(params: String): List<GenModel_114_> = repository.search(params)
}

abstract class GenMapper_114_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_114_ : GenMapper_114_<GenModel_114_, String>() {
    override fun map(input: GenModel_114_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_114_ : GenMapper_114_<String, GenModel_114_>() {
    override fun map(input: String): GenModel_114_ {
        val parts = input.split(":")
        return GenModel_114_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_114_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_114_,
    private val saveUseCase: GenSaveUseCase_114_,
    private val deleteUseCase: GenDeleteUseCase_114_,
    private val searchUseCase: GenSearchUseCase_114_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_114_>(GenState_114_.Idle)
    val state: StateFlow<GenState_114_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_114_) {
        when (event) {
            is GenEvent_114_.Load -> loadAll()
            is GenEvent_114_.Update -> save(event.model)
            is GenEvent_114_.Delete -> delete(event.id)
            is GenEvent_114_.Refresh -> loadAll()
            is GenEvent_114_.Search -> search(event.query)
            is GenEvent_114_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_114_.Loading; _state.value = GenState_114_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_114_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_114_.Success(searchUseCase(query)) } }
}
