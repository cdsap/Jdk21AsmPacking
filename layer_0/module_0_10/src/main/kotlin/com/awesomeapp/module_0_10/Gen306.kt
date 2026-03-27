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

data class GenModel_306_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_306_ {
    data class Load(val id: Long) : GenEvent_306_()
    data class Update(val model: GenModel_306_) : GenEvent_306_()
    data class Delete(val id: Long) : GenEvent_306_()
    data object Refresh : GenEvent_306_()
    data class Search(val query: String) : GenEvent_306_()
    data class Filter(val predicate: String) : GenEvent_306_()
}

sealed class GenState_306_ {
    data object Idle : GenState_306_()
    data object Loading : GenState_306_()
    data class Success(val items: List<GenModel_306_>) : GenState_306_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_306_()
    data class Partial(val items: List<GenModel_306_>, val hasMore: Boolean) : GenState_306_()
}

interface GenRepository_306_ {
    suspend fun getAll(): List<GenModel_306_>
    suspend fun getById(id: Long): GenModel_306_?
    suspend fun save(model: GenModel_306_): GenModel_306_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_306_>
}

@Singleton
class GenRepositoryImpl_306_ @Inject constructor() : GenRepository_306_ {
    private val store = mutableMapOf<Long, GenModel_306_>()
    override suspend fun getAll(): List<GenModel_306_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_306_? = store[id]
    override suspend fun save(model: GenModel_306_): GenModel_306_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_306_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_306_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_306_ @Inject constructor(
    private val repository: GenRepositoryImpl_306_
) : GenUseCase_306_<Unit, List<GenModel_306_>> {
    override suspend fun invoke(params: Unit): List<GenModel_306_> = repository.getAll()
}

class GenSaveUseCase_306_ @Inject constructor(
    private val repository: GenRepositoryImpl_306_
) : GenUseCase_306_<GenModel_306_, GenModel_306_> {
    override suspend fun invoke(params: GenModel_306_): GenModel_306_ = repository.save(params)
}

class GenDeleteUseCase_306_ @Inject constructor(
    private val repository: GenRepositoryImpl_306_
) : GenUseCase_306_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_306_ @Inject constructor(
    private val repository: GenRepositoryImpl_306_
) : GenUseCase_306_<String, List<GenModel_306_>> {
    override suspend fun invoke(params: String): List<GenModel_306_> = repository.search(params)
}

abstract class GenMapper_306_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_306_ : GenMapper_306_<GenModel_306_, String>() {
    override fun map(input: GenModel_306_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_306_ : GenMapper_306_<String, GenModel_306_>() {
    override fun map(input: String): GenModel_306_ {
        val parts = input.split(":")
        return GenModel_306_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_306_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_306_,
    private val saveUseCase: GenSaveUseCase_306_,
    private val deleteUseCase: GenDeleteUseCase_306_,
    private val searchUseCase: GenSearchUseCase_306_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_306_>(GenState_306_.Idle)
    val state: StateFlow<GenState_306_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_306_) {
        when (event) {
            is GenEvent_306_.Load -> loadAll()
            is GenEvent_306_.Update -> save(event.model)
            is GenEvent_306_.Delete -> delete(event.id)
            is GenEvent_306_.Refresh -> loadAll()
            is GenEvent_306_.Search -> search(event.query)
            is GenEvent_306_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_306_.Loading; _state.value = GenState_306_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_306_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_306_.Success(searchUseCase(query)) } }
}
