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

data class GenModel_574_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_574_ {
    data class Load(val id: Long) : GenEvent_574_()
    data class Update(val model: GenModel_574_) : GenEvent_574_()
    data class Delete(val id: Long) : GenEvent_574_()
    data object Refresh : GenEvent_574_()
    data class Search(val query: String) : GenEvent_574_()
    data class Filter(val predicate: String) : GenEvent_574_()
}

sealed class GenState_574_ {
    data object Idle : GenState_574_()
    data object Loading : GenState_574_()
    data class Success(val items: List<GenModel_574_>) : GenState_574_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_574_()
    data class Partial(val items: List<GenModel_574_>, val hasMore: Boolean) : GenState_574_()
}

interface GenRepository_574_ {
    suspend fun getAll(): List<GenModel_574_>
    suspend fun getById(id: Long): GenModel_574_?
    suspend fun save(model: GenModel_574_): GenModel_574_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_574_>
}

@Singleton
class GenRepositoryImpl_574_ @Inject constructor() : GenRepository_574_ {
    private val store = mutableMapOf<Long, GenModel_574_>()
    override suspend fun getAll(): List<GenModel_574_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_574_? = store[id]
    override suspend fun save(model: GenModel_574_): GenModel_574_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_574_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_574_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_574_ @Inject constructor(
    private val repository: GenRepositoryImpl_574_
) : GenUseCase_574_<Unit, List<GenModel_574_>> {
    override suspend fun invoke(params: Unit): List<GenModel_574_> = repository.getAll()
}

class GenSaveUseCase_574_ @Inject constructor(
    private val repository: GenRepositoryImpl_574_
) : GenUseCase_574_<GenModel_574_, GenModel_574_> {
    override suspend fun invoke(params: GenModel_574_): GenModel_574_ = repository.save(params)
}

class GenDeleteUseCase_574_ @Inject constructor(
    private val repository: GenRepositoryImpl_574_
) : GenUseCase_574_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_574_ @Inject constructor(
    private val repository: GenRepositoryImpl_574_
) : GenUseCase_574_<String, List<GenModel_574_>> {
    override suspend fun invoke(params: String): List<GenModel_574_> = repository.search(params)
}

abstract class GenMapper_574_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_574_ : GenMapper_574_<GenModel_574_, String>() {
    override fun map(input: GenModel_574_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_574_ : GenMapper_574_<String, GenModel_574_>() {
    override fun map(input: String): GenModel_574_ {
        val parts = input.split(":")
        return GenModel_574_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_574_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_574_,
    private val saveUseCase: GenSaveUseCase_574_,
    private val deleteUseCase: GenDeleteUseCase_574_,
    private val searchUseCase: GenSearchUseCase_574_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_574_>(GenState_574_.Idle)
    val state: StateFlow<GenState_574_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_574_) {
        when (event) {
            is GenEvent_574_.Load -> loadAll()
            is GenEvent_574_.Update -> save(event.model)
            is GenEvent_574_.Delete -> delete(event.id)
            is GenEvent_574_.Refresh -> loadAll()
            is GenEvent_574_.Search -> search(event.query)
            is GenEvent_574_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_574_.Loading; _state.value = GenState_574_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_574_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_574_.Success(searchUseCase(query)) } }
}
