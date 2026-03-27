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

data class GenModel_394_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_394_ {
    data class Load(val id: Long) : GenEvent_394_()
    data class Update(val model: GenModel_394_) : GenEvent_394_()
    data class Delete(val id: Long) : GenEvent_394_()
    data object Refresh : GenEvent_394_()
    data class Search(val query: String) : GenEvent_394_()
    data class Filter(val predicate: String) : GenEvent_394_()
}

sealed class GenState_394_ {
    data object Idle : GenState_394_()
    data object Loading : GenState_394_()
    data class Success(val items: List<GenModel_394_>) : GenState_394_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_394_()
    data class Partial(val items: List<GenModel_394_>, val hasMore: Boolean) : GenState_394_()
}

interface GenRepository_394_ {
    suspend fun getAll(): List<GenModel_394_>
    suspend fun getById(id: Long): GenModel_394_?
    suspend fun save(model: GenModel_394_): GenModel_394_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_394_>
}

@Singleton
class GenRepositoryImpl_394_ @Inject constructor() : GenRepository_394_ {
    private val store = mutableMapOf<Long, GenModel_394_>()
    override suspend fun getAll(): List<GenModel_394_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_394_? = store[id]
    override suspend fun save(model: GenModel_394_): GenModel_394_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_394_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_394_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_394_ @Inject constructor(
    private val repository: GenRepositoryImpl_394_
) : GenUseCase_394_<Unit, List<GenModel_394_>> {
    override suspend fun invoke(params: Unit): List<GenModel_394_> = repository.getAll()
}

class GenSaveUseCase_394_ @Inject constructor(
    private val repository: GenRepositoryImpl_394_
) : GenUseCase_394_<GenModel_394_, GenModel_394_> {
    override suspend fun invoke(params: GenModel_394_): GenModel_394_ = repository.save(params)
}

class GenDeleteUseCase_394_ @Inject constructor(
    private val repository: GenRepositoryImpl_394_
) : GenUseCase_394_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_394_ @Inject constructor(
    private val repository: GenRepositoryImpl_394_
) : GenUseCase_394_<String, List<GenModel_394_>> {
    override suspend fun invoke(params: String): List<GenModel_394_> = repository.search(params)
}

abstract class GenMapper_394_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_394_ : GenMapper_394_<GenModel_394_, String>() {
    override fun map(input: GenModel_394_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_394_ : GenMapper_394_<String, GenModel_394_>() {
    override fun map(input: String): GenModel_394_ {
        val parts = input.split(":")
        return GenModel_394_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_394_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_394_,
    private val saveUseCase: GenSaveUseCase_394_,
    private val deleteUseCase: GenDeleteUseCase_394_,
    private val searchUseCase: GenSearchUseCase_394_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_394_>(GenState_394_.Idle)
    val state: StateFlow<GenState_394_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_394_) {
        when (event) {
            is GenEvent_394_.Load -> loadAll()
            is GenEvent_394_.Update -> save(event.model)
            is GenEvent_394_.Delete -> delete(event.id)
            is GenEvent_394_.Refresh -> loadAll()
            is GenEvent_394_.Search -> search(event.query)
            is GenEvent_394_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_394_.Loading; _state.value = GenState_394_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_394_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_394_.Success(searchUseCase(query)) } }
}
