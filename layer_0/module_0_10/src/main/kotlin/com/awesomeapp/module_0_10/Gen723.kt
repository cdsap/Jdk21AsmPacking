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

data class GenModel_723_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_723_ {
    data class Load(val id: Long) : GenEvent_723_()
    data class Update(val model: GenModel_723_) : GenEvent_723_()
    data class Delete(val id: Long) : GenEvent_723_()
    data object Refresh : GenEvent_723_()
    data class Search(val query: String) : GenEvent_723_()
    data class Filter(val predicate: String) : GenEvent_723_()
}

sealed class GenState_723_ {
    data object Idle : GenState_723_()
    data object Loading : GenState_723_()
    data class Success(val items: List<GenModel_723_>) : GenState_723_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_723_()
    data class Partial(val items: List<GenModel_723_>, val hasMore: Boolean) : GenState_723_()
}

interface GenRepository_723_ {
    suspend fun getAll(): List<GenModel_723_>
    suspend fun getById(id: Long): GenModel_723_?
    suspend fun save(model: GenModel_723_): GenModel_723_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_723_>
}

@Singleton
class GenRepositoryImpl_723_ @Inject constructor() : GenRepository_723_ {
    private val store = mutableMapOf<Long, GenModel_723_>()
    override suspend fun getAll(): List<GenModel_723_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_723_? = store[id]
    override suspend fun save(model: GenModel_723_): GenModel_723_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_723_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_723_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_723_ @Inject constructor(
    private val repository: GenRepositoryImpl_723_
) : GenUseCase_723_<Unit, List<GenModel_723_>> {
    override suspend fun invoke(params: Unit): List<GenModel_723_> = repository.getAll()
}

class GenSaveUseCase_723_ @Inject constructor(
    private val repository: GenRepositoryImpl_723_
) : GenUseCase_723_<GenModel_723_, GenModel_723_> {
    override suspend fun invoke(params: GenModel_723_): GenModel_723_ = repository.save(params)
}

class GenDeleteUseCase_723_ @Inject constructor(
    private val repository: GenRepositoryImpl_723_
) : GenUseCase_723_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_723_ @Inject constructor(
    private val repository: GenRepositoryImpl_723_
) : GenUseCase_723_<String, List<GenModel_723_>> {
    override suspend fun invoke(params: String): List<GenModel_723_> = repository.search(params)
}

abstract class GenMapper_723_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_723_ : GenMapper_723_<GenModel_723_, String>() {
    override fun map(input: GenModel_723_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_723_ : GenMapper_723_<String, GenModel_723_>() {
    override fun map(input: String): GenModel_723_ {
        val parts = input.split(":")
        return GenModel_723_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_723_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_723_,
    private val saveUseCase: GenSaveUseCase_723_,
    private val deleteUseCase: GenDeleteUseCase_723_,
    private val searchUseCase: GenSearchUseCase_723_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_723_>(GenState_723_.Idle)
    val state: StateFlow<GenState_723_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_723_) {
        when (event) {
            is GenEvent_723_.Load -> loadAll()
            is GenEvent_723_.Update -> save(event.model)
            is GenEvent_723_.Delete -> delete(event.id)
            is GenEvent_723_.Refresh -> loadAll()
            is GenEvent_723_.Search -> search(event.query)
            is GenEvent_723_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_723_.Loading; _state.value = GenState_723_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_723_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_723_.Success(searchUseCase(query)) } }
}
