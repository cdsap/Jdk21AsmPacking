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

data class GenModel_1356_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1356_ {
    data class Load(val id: Long) : GenEvent_1356_()
    data class Update(val model: GenModel_1356_) : GenEvent_1356_()
    data class Delete(val id: Long) : GenEvent_1356_()
    data object Refresh : GenEvent_1356_()
    data class Search(val query: String) : GenEvent_1356_()
    data class Filter(val predicate: String) : GenEvent_1356_()
}

sealed class GenState_1356_ {
    data object Idle : GenState_1356_()
    data object Loading : GenState_1356_()
    data class Success(val items: List<GenModel_1356_>) : GenState_1356_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1356_()
    data class Partial(val items: List<GenModel_1356_>, val hasMore: Boolean) : GenState_1356_()
}

interface GenRepository_1356_ {
    suspend fun getAll(): List<GenModel_1356_>
    suspend fun getById(id: Long): GenModel_1356_?
    suspend fun save(model: GenModel_1356_): GenModel_1356_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1356_>
}

@Singleton
class GenRepositoryImpl_1356_ @Inject constructor() : GenRepository_1356_ {
    private val store = mutableMapOf<Long, GenModel_1356_>()
    override suspend fun getAll(): List<GenModel_1356_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1356_? = store[id]
    override suspend fun save(model: GenModel_1356_): GenModel_1356_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1356_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1356_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1356_ @Inject constructor(
    private val repository: GenRepositoryImpl_1356_
) : GenUseCase_1356_<Unit, List<GenModel_1356_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1356_> = repository.getAll()
}

class GenSaveUseCase_1356_ @Inject constructor(
    private val repository: GenRepositoryImpl_1356_
) : GenUseCase_1356_<GenModel_1356_, GenModel_1356_> {
    override suspend fun invoke(params: GenModel_1356_): GenModel_1356_ = repository.save(params)
}

class GenDeleteUseCase_1356_ @Inject constructor(
    private val repository: GenRepositoryImpl_1356_
) : GenUseCase_1356_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1356_ @Inject constructor(
    private val repository: GenRepositoryImpl_1356_
) : GenUseCase_1356_<String, List<GenModel_1356_>> {
    override suspend fun invoke(params: String): List<GenModel_1356_> = repository.search(params)
}

abstract class GenMapper_1356_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1356_ : GenMapper_1356_<GenModel_1356_, String>() {
    override fun map(input: GenModel_1356_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1356_ : GenMapper_1356_<String, GenModel_1356_>() {
    override fun map(input: String): GenModel_1356_ {
        val parts = input.split(":")
        return GenModel_1356_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1356_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1356_,
    private val saveUseCase: GenSaveUseCase_1356_,
    private val deleteUseCase: GenDeleteUseCase_1356_,
    private val searchUseCase: GenSearchUseCase_1356_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1356_>(GenState_1356_.Idle)
    val state: StateFlow<GenState_1356_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1356_) {
        when (event) {
            is GenEvent_1356_.Load -> loadAll()
            is GenEvent_1356_.Update -> save(event.model)
            is GenEvent_1356_.Delete -> delete(event.id)
            is GenEvent_1356_.Refresh -> loadAll()
            is GenEvent_1356_.Search -> search(event.query)
            is GenEvent_1356_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1356_.Loading; _state.value = GenState_1356_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1356_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1356_.Success(searchUseCase(query)) } }
}
