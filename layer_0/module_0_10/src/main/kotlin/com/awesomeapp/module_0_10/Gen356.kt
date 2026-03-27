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

data class GenModel_356_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_356_ {
    data class Load(val id: Long) : GenEvent_356_()
    data class Update(val model: GenModel_356_) : GenEvent_356_()
    data class Delete(val id: Long) : GenEvent_356_()
    data object Refresh : GenEvent_356_()
    data class Search(val query: String) : GenEvent_356_()
    data class Filter(val predicate: String) : GenEvent_356_()
}

sealed class GenState_356_ {
    data object Idle : GenState_356_()
    data object Loading : GenState_356_()
    data class Success(val items: List<GenModel_356_>) : GenState_356_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_356_()
    data class Partial(val items: List<GenModel_356_>, val hasMore: Boolean) : GenState_356_()
}

interface GenRepository_356_ {
    suspend fun getAll(): List<GenModel_356_>
    suspend fun getById(id: Long): GenModel_356_?
    suspend fun save(model: GenModel_356_): GenModel_356_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_356_>
}

@Singleton
class GenRepositoryImpl_356_ @Inject constructor() : GenRepository_356_ {
    private val store = mutableMapOf<Long, GenModel_356_>()
    override suspend fun getAll(): List<GenModel_356_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_356_? = store[id]
    override suspend fun save(model: GenModel_356_): GenModel_356_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_356_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_356_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_356_ @Inject constructor(
    private val repository: GenRepositoryImpl_356_
) : GenUseCase_356_<Unit, List<GenModel_356_>> {
    override suspend fun invoke(params: Unit): List<GenModel_356_> = repository.getAll()
}

class GenSaveUseCase_356_ @Inject constructor(
    private val repository: GenRepositoryImpl_356_
) : GenUseCase_356_<GenModel_356_, GenModel_356_> {
    override suspend fun invoke(params: GenModel_356_): GenModel_356_ = repository.save(params)
}

class GenDeleteUseCase_356_ @Inject constructor(
    private val repository: GenRepositoryImpl_356_
) : GenUseCase_356_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_356_ @Inject constructor(
    private val repository: GenRepositoryImpl_356_
) : GenUseCase_356_<String, List<GenModel_356_>> {
    override suspend fun invoke(params: String): List<GenModel_356_> = repository.search(params)
}

abstract class GenMapper_356_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_356_ : GenMapper_356_<GenModel_356_, String>() {
    override fun map(input: GenModel_356_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_356_ : GenMapper_356_<String, GenModel_356_>() {
    override fun map(input: String): GenModel_356_ {
        val parts = input.split(":")
        return GenModel_356_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_356_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_356_,
    private val saveUseCase: GenSaveUseCase_356_,
    private val deleteUseCase: GenDeleteUseCase_356_,
    private val searchUseCase: GenSearchUseCase_356_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_356_>(GenState_356_.Idle)
    val state: StateFlow<GenState_356_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_356_) {
        when (event) {
            is GenEvent_356_.Load -> loadAll()
            is GenEvent_356_.Update -> save(event.model)
            is GenEvent_356_.Delete -> delete(event.id)
            is GenEvent_356_.Refresh -> loadAll()
            is GenEvent_356_.Search -> search(event.query)
            is GenEvent_356_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_356_.Loading; _state.value = GenState_356_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_356_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_356_.Success(searchUseCase(query)) } }
}
