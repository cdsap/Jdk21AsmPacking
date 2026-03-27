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

data class GenModel_301_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_301_ {
    data class Load(val id: Long) : GenEvent_301_()
    data class Update(val model: GenModel_301_) : GenEvent_301_()
    data class Delete(val id: Long) : GenEvent_301_()
    data object Refresh : GenEvent_301_()
    data class Search(val query: String) : GenEvent_301_()
    data class Filter(val predicate: String) : GenEvent_301_()
}

sealed class GenState_301_ {
    data object Idle : GenState_301_()
    data object Loading : GenState_301_()
    data class Success(val items: List<GenModel_301_>) : GenState_301_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_301_()
    data class Partial(val items: List<GenModel_301_>, val hasMore: Boolean) : GenState_301_()
}

interface GenRepository_301_ {
    suspend fun getAll(): List<GenModel_301_>
    suspend fun getById(id: Long): GenModel_301_?
    suspend fun save(model: GenModel_301_): GenModel_301_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_301_>
}

@Singleton
class GenRepositoryImpl_301_ @Inject constructor() : GenRepository_301_ {
    private val store = mutableMapOf<Long, GenModel_301_>()
    override suspend fun getAll(): List<GenModel_301_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_301_? = store[id]
    override suspend fun save(model: GenModel_301_): GenModel_301_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_301_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_301_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_301_ @Inject constructor(
    private val repository: GenRepositoryImpl_301_
) : GenUseCase_301_<Unit, List<GenModel_301_>> {
    override suspend fun invoke(params: Unit): List<GenModel_301_> = repository.getAll()
}

class GenSaveUseCase_301_ @Inject constructor(
    private val repository: GenRepositoryImpl_301_
) : GenUseCase_301_<GenModel_301_, GenModel_301_> {
    override suspend fun invoke(params: GenModel_301_): GenModel_301_ = repository.save(params)
}

class GenDeleteUseCase_301_ @Inject constructor(
    private val repository: GenRepositoryImpl_301_
) : GenUseCase_301_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_301_ @Inject constructor(
    private val repository: GenRepositoryImpl_301_
) : GenUseCase_301_<String, List<GenModel_301_>> {
    override suspend fun invoke(params: String): List<GenModel_301_> = repository.search(params)
}

abstract class GenMapper_301_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_301_ : GenMapper_301_<GenModel_301_, String>() {
    override fun map(input: GenModel_301_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_301_ : GenMapper_301_<String, GenModel_301_>() {
    override fun map(input: String): GenModel_301_ {
        val parts = input.split(":")
        return GenModel_301_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_301_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_301_,
    private val saveUseCase: GenSaveUseCase_301_,
    private val deleteUseCase: GenDeleteUseCase_301_,
    private val searchUseCase: GenSearchUseCase_301_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_301_>(GenState_301_.Idle)
    val state: StateFlow<GenState_301_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_301_) {
        when (event) {
            is GenEvent_301_.Load -> loadAll()
            is GenEvent_301_.Update -> save(event.model)
            is GenEvent_301_.Delete -> delete(event.id)
            is GenEvent_301_.Refresh -> loadAll()
            is GenEvent_301_.Search -> search(event.query)
            is GenEvent_301_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_301_.Loading; _state.value = GenState_301_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_301_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_301_.Success(searchUseCase(query)) } }
}
