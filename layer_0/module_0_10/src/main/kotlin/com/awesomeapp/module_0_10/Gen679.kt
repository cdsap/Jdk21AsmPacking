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

data class GenModel_679_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_679_ {
    data class Load(val id: Long) : GenEvent_679_()
    data class Update(val model: GenModel_679_) : GenEvent_679_()
    data class Delete(val id: Long) : GenEvent_679_()
    data object Refresh : GenEvent_679_()
    data class Search(val query: String) : GenEvent_679_()
    data class Filter(val predicate: String) : GenEvent_679_()
}

sealed class GenState_679_ {
    data object Idle : GenState_679_()
    data object Loading : GenState_679_()
    data class Success(val items: List<GenModel_679_>) : GenState_679_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_679_()
    data class Partial(val items: List<GenModel_679_>, val hasMore: Boolean) : GenState_679_()
}

interface GenRepository_679_ {
    suspend fun getAll(): List<GenModel_679_>
    suspend fun getById(id: Long): GenModel_679_?
    suspend fun save(model: GenModel_679_): GenModel_679_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_679_>
}

@Singleton
class GenRepositoryImpl_679_ @Inject constructor() : GenRepository_679_ {
    private val store = mutableMapOf<Long, GenModel_679_>()
    override suspend fun getAll(): List<GenModel_679_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_679_? = store[id]
    override suspend fun save(model: GenModel_679_): GenModel_679_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_679_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_679_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_679_ @Inject constructor(
    private val repository: GenRepositoryImpl_679_
) : GenUseCase_679_<Unit, List<GenModel_679_>> {
    override suspend fun invoke(params: Unit): List<GenModel_679_> = repository.getAll()
}

class GenSaveUseCase_679_ @Inject constructor(
    private val repository: GenRepositoryImpl_679_
) : GenUseCase_679_<GenModel_679_, GenModel_679_> {
    override suspend fun invoke(params: GenModel_679_): GenModel_679_ = repository.save(params)
}

class GenDeleteUseCase_679_ @Inject constructor(
    private val repository: GenRepositoryImpl_679_
) : GenUseCase_679_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_679_ @Inject constructor(
    private val repository: GenRepositoryImpl_679_
) : GenUseCase_679_<String, List<GenModel_679_>> {
    override suspend fun invoke(params: String): List<GenModel_679_> = repository.search(params)
}

abstract class GenMapper_679_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_679_ : GenMapper_679_<GenModel_679_, String>() {
    override fun map(input: GenModel_679_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_679_ : GenMapper_679_<String, GenModel_679_>() {
    override fun map(input: String): GenModel_679_ {
        val parts = input.split(":")
        return GenModel_679_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_679_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_679_,
    private val saveUseCase: GenSaveUseCase_679_,
    private val deleteUseCase: GenDeleteUseCase_679_,
    private val searchUseCase: GenSearchUseCase_679_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_679_>(GenState_679_.Idle)
    val state: StateFlow<GenState_679_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_679_) {
        when (event) {
            is GenEvent_679_.Load -> loadAll()
            is GenEvent_679_.Update -> save(event.model)
            is GenEvent_679_.Delete -> delete(event.id)
            is GenEvent_679_.Refresh -> loadAll()
            is GenEvent_679_.Search -> search(event.query)
            is GenEvent_679_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_679_.Loading; _state.value = GenState_679_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_679_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_679_.Success(searchUseCase(query)) } }
}
