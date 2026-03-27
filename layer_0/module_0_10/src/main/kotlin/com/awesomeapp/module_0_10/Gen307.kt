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

data class GenModel_307_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_307_ {
    data class Load(val id: Long) : GenEvent_307_()
    data class Update(val model: GenModel_307_) : GenEvent_307_()
    data class Delete(val id: Long) : GenEvent_307_()
    data object Refresh : GenEvent_307_()
    data class Search(val query: String) : GenEvent_307_()
    data class Filter(val predicate: String) : GenEvent_307_()
}

sealed class GenState_307_ {
    data object Idle : GenState_307_()
    data object Loading : GenState_307_()
    data class Success(val items: List<GenModel_307_>) : GenState_307_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_307_()
    data class Partial(val items: List<GenModel_307_>, val hasMore: Boolean) : GenState_307_()
}

interface GenRepository_307_ {
    suspend fun getAll(): List<GenModel_307_>
    suspend fun getById(id: Long): GenModel_307_?
    suspend fun save(model: GenModel_307_): GenModel_307_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_307_>
}

@Singleton
class GenRepositoryImpl_307_ @Inject constructor() : GenRepository_307_ {
    private val store = mutableMapOf<Long, GenModel_307_>()
    override suspend fun getAll(): List<GenModel_307_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_307_? = store[id]
    override suspend fun save(model: GenModel_307_): GenModel_307_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_307_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_307_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_307_ @Inject constructor(
    private val repository: GenRepositoryImpl_307_
) : GenUseCase_307_<Unit, List<GenModel_307_>> {
    override suspend fun invoke(params: Unit): List<GenModel_307_> = repository.getAll()
}

class GenSaveUseCase_307_ @Inject constructor(
    private val repository: GenRepositoryImpl_307_
) : GenUseCase_307_<GenModel_307_, GenModel_307_> {
    override suspend fun invoke(params: GenModel_307_): GenModel_307_ = repository.save(params)
}

class GenDeleteUseCase_307_ @Inject constructor(
    private val repository: GenRepositoryImpl_307_
) : GenUseCase_307_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_307_ @Inject constructor(
    private val repository: GenRepositoryImpl_307_
) : GenUseCase_307_<String, List<GenModel_307_>> {
    override suspend fun invoke(params: String): List<GenModel_307_> = repository.search(params)
}

abstract class GenMapper_307_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_307_ : GenMapper_307_<GenModel_307_, String>() {
    override fun map(input: GenModel_307_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_307_ : GenMapper_307_<String, GenModel_307_>() {
    override fun map(input: String): GenModel_307_ {
        val parts = input.split(":")
        return GenModel_307_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_307_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_307_,
    private val saveUseCase: GenSaveUseCase_307_,
    private val deleteUseCase: GenDeleteUseCase_307_,
    private val searchUseCase: GenSearchUseCase_307_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_307_>(GenState_307_.Idle)
    val state: StateFlow<GenState_307_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_307_) {
        when (event) {
            is GenEvent_307_.Load -> loadAll()
            is GenEvent_307_.Update -> save(event.model)
            is GenEvent_307_.Delete -> delete(event.id)
            is GenEvent_307_.Refresh -> loadAll()
            is GenEvent_307_.Search -> search(event.query)
            is GenEvent_307_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_307_.Loading; _state.value = GenState_307_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_307_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_307_.Success(searchUseCase(query)) } }
}
