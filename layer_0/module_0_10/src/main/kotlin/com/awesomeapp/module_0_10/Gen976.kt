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

data class GenModel_976_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_976_ {
    data class Load(val id: Long) : GenEvent_976_()
    data class Update(val model: GenModel_976_) : GenEvent_976_()
    data class Delete(val id: Long) : GenEvent_976_()
    data object Refresh : GenEvent_976_()
    data class Search(val query: String) : GenEvent_976_()
    data class Filter(val predicate: String) : GenEvent_976_()
}

sealed class GenState_976_ {
    data object Idle : GenState_976_()
    data object Loading : GenState_976_()
    data class Success(val items: List<GenModel_976_>) : GenState_976_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_976_()
    data class Partial(val items: List<GenModel_976_>, val hasMore: Boolean) : GenState_976_()
}

interface GenRepository_976_ {
    suspend fun getAll(): List<GenModel_976_>
    suspend fun getById(id: Long): GenModel_976_?
    suspend fun save(model: GenModel_976_): GenModel_976_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_976_>
}

@Singleton
class GenRepositoryImpl_976_ @Inject constructor() : GenRepository_976_ {
    private val store = mutableMapOf<Long, GenModel_976_>()
    override suspend fun getAll(): List<GenModel_976_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_976_? = store[id]
    override suspend fun save(model: GenModel_976_): GenModel_976_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_976_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_976_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_976_ @Inject constructor(
    private val repository: GenRepositoryImpl_976_
) : GenUseCase_976_<Unit, List<GenModel_976_>> {
    override suspend fun invoke(params: Unit): List<GenModel_976_> = repository.getAll()
}

class GenSaveUseCase_976_ @Inject constructor(
    private val repository: GenRepositoryImpl_976_
) : GenUseCase_976_<GenModel_976_, GenModel_976_> {
    override suspend fun invoke(params: GenModel_976_): GenModel_976_ = repository.save(params)
}

class GenDeleteUseCase_976_ @Inject constructor(
    private val repository: GenRepositoryImpl_976_
) : GenUseCase_976_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_976_ @Inject constructor(
    private val repository: GenRepositoryImpl_976_
) : GenUseCase_976_<String, List<GenModel_976_>> {
    override suspend fun invoke(params: String): List<GenModel_976_> = repository.search(params)
}

abstract class GenMapper_976_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_976_ : GenMapper_976_<GenModel_976_, String>() {
    override fun map(input: GenModel_976_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_976_ : GenMapper_976_<String, GenModel_976_>() {
    override fun map(input: String): GenModel_976_ {
        val parts = input.split(":")
        return GenModel_976_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_976_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_976_,
    private val saveUseCase: GenSaveUseCase_976_,
    private val deleteUseCase: GenDeleteUseCase_976_,
    private val searchUseCase: GenSearchUseCase_976_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_976_>(GenState_976_.Idle)
    val state: StateFlow<GenState_976_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_976_) {
        when (event) {
            is GenEvent_976_.Load -> loadAll()
            is GenEvent_976_.Update -> save(event.model)
            is GenEvent_976_.Delete -> delete(event.id)
            is GenEvent_976_.Refresh -> loadAll()
            is GenEvent_976_.Search -> search(event.query)
            is GenEvent_976_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_976_.Loading; _state.value = GenState_976_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_976_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_976_.Success(searchUseCase(query)) } }
}
