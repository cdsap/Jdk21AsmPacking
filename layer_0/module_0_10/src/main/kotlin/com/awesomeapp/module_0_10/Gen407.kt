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

data class GenModel_407_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_407_ {
    data class Load(val id: Long) : GenEvent_407_()
    data class Update(val model: GenModel_407_) : GenEvent_407_()
    data class Delete(val id: Long) : GenEvent_407_()
    data object Refresh : GenEvent_407_()
    data class Search(val query: String) : GenEvent_407_()
    data class Filter(val predicate: String) : GenEvent_407_()
}

sealed class GenState_407_ {
    data object Idle : GenState_407_()
    data object Loading : GenState_407_()
    data class Success(val items: List<GenModel_407_>) : GenState_407_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_407_()
    data class Partial(val items: List<GenModel_407_>, val hasMore: Boolean) : GenState_407_()
}

interface GenRepository_407_ {
    suspend fun getAll(): List<GenModel_407_>
    suspend fun getById(id: Long): GenModel_407_?
    suspend fun save(model: GenModel_407_): GenModel_407_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_407_>
}

@Singleton
class GenRepositoryImpl_407_ @Inject constructor() : GenRepository_407_ {
    private val store = mutableMapOf<Long, GenModel_407_>()
    override suspend fun getAll(): List<GenModel_407_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_407_? = store[id]
    override suspend fun save(model: GenModel_407_): GenModel_407_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_407_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_407_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_407_ @Inject constructor(
    private val repository: GenRepositoryImpl_407_
) : GenUseCase_407_<Unit, List<GenModel_407_>> {
    override suspend fun invoke(params: Unit): List<GenModel_407_> = repository.getAll()
}

class GenSaveUseCase_407_ @Inject constructor(
    private val repository: GenRepositoryImpl_407_
) : GenUseCase_407_<GenModel_407_, GenModel_407_> {
    override suspend fun invoke(params: GenModel_407_): GenModel_407_ = repository.save(params)
}

class GenDeleteUseCase_407_ @Inject constructor(
    private val repository: GenRepositoryImpl_407_
) : GenUseCase_407_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_407_ @Inject constructor(
    private val repository: GenRepositoryImpl_407_
) : GenUseCase_407_<String, List<GenModel_407_>> {
    override suspend fun invoke(params: String): List<GenModel_407_> = repository.search(params)
}

abstract class GenMapper_407_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_407_ : GenMapper_407_<GenModel_407_, String>() {
    override fun map(input: GenModel_407_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_407_ : GenMapper_407_<String, GenModel_407_>() {
    override fun map(input: String): GenModel_407_ {
        val parts = input.split(":")
        return GenModel_407_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_407_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_407_,
    private val saveUseCase: GenSaveUseCase_407_,
    private val deleteUseCase: GenDeleteUseCase_407_,
    private val searchUseCase: GenSearchUseCase_407_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_407_>(GenState_407_.Idle)
    val state: StateFlow<GenState_407_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_407_) {
        when (event) {
            is GenEvent_407_.Load -> loadAll()
            is GenEvent_407_.Update -> save(event.model)
            is GenEvent_407_.Delete -> delete(event.id)
            is GenEvent_407_.Refresh -> loadAll()
            is GenEvent_407_.Search -> search(event.query)
            is GenEvent_407_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_407_.Loading; _state.value = GenState_407_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_407_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_407_.Success(searchUseCase(query)) } }
}
