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

data class GenModel_895_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_895_ {
    data class Load(val id: Long) : GenEvent_895_()
    data class Update(val model: GenModel_895_) : GenEvent_895_()
    data class Delete(val id: Long) : GenEvent_895_()
    data object Refresh : GenEvent_895_()
    data class Search(val query: String) : GenEvent_895_()
    data class Filter(val predicate: String) : GenEvent_895_()
}

sealed class GenState_895_ {
    data object Idle : GenState_895_()
    data object Loading : GenState_895_()
    data class Success(val items: List<GenModel_895_>) : GenState_895_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_895_()
    data class Partial(val items: List<GenModel_895_>, val hasMore: Boolean) : GenState_895_()
}

interface GenRepository_895_ {
    suspend fun getAll(): List<GenModel_895_>
    suspend fun getById(id: Long): GenModel_895_?
    suspend fun save(model: GenModel_895_): GenModel_895_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_895_>
}

@Singleton
class GenRepositoryImpl_895_ @Inject constructor() : GenRepository_895_ {
    private val store = mutableMapOf<Long, GenModel_895_>()
    override suspend fun getAll(): List<GenModel_895_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_895_? = store[id]
    override suspend fun save(model: GenModel_895_): GenModel_895_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_895_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_895_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_895_ @Inject constructor(
    private val repository: GenRepositoryImpl_895_
) : GenUseCase_895_<Unit, List<GenModel_895_>> {
    override suspend fun invoke(params: Unit): List<GenModel_895_> = repository.getAll()
}

class GenSaveUseCase_895_ @Inject constructor(
    private val repository: GenRepositoryImpl_895_
) : GenUseCase_895_<GenModel_895_, GenModel_895_> {
    override suspend fun invoke(params: GenModel_895_): GenModel_895_ = repository.save(params)
}

class GenDeleteUseCase_895_ @Inject constructor(
    private val repository: GenRepositoryImpl_895_
) : GenUseCase_895_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_895_ @Inject constructor(
    private val repository: GenRepositoryImpl_895_
) : GenUseCase_895_<String, List<GenModel_895_>> {
    override suspend fun invoke(params: String): List<GenModel_895_> = repository.search(params)
}

abstract class GenMapper_895_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_895_ : GenMapper_895_<GenModel_895_, String>() {
    override fun map(input: GenModel_895_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_895_ : GenMapper_895_<String, GenModel_895_>() {
    override fun map(input: String): GenModel_895_ {
        val parts = input.split(":")
        return GenModel_895_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_895_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_895_,
    private val saveUseCase: GenSaveUseCase_895_,
    private val deleteUseCase: GenDeleteUseCase_895_,
    private val searchUseCase: GenSearchUseCase_895_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_895_>(GenState_895_.Idle)
    val state: StateFlow<GenState_895_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_895_) {
        when (event) {
            is GenEvent_895_.Load -> loadAll()
            is GenEvent_895_.Update -> save(event.model)
            is GenEvent_895_.Delete -> delete(event.id)
            is GenEvent_895_.Refresh -> loadAll()
            is GenEvent_895_.Search -> search(event.query)
            is GenEvent_895_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_895_.Loading; _state.value = GenState_895_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_895_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_895_.Success(searchUseCase(query)) } }
}
