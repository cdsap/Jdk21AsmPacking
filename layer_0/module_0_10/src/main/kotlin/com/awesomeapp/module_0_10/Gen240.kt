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

data class GenModel_240_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_240_ {
    data class Load(val id: Long) : GenEvent_240_()
    data class Update(val model: GenModel_240_) : GenEvent_240_()
    data class Delete(val id: Long) : GenEvent_240_()
    data object Refresh : GenEvent_240_()
    data class Search(val query: String) : GenEvent_240_()
    data class Filter(val predicate: String) : GenEvent_240_()
}

sealed class GenState_240_ {
    data object Idle : GenState_240_()
    data object Loading : GenState_240_()
    data class Success(val items: List<GenModel_240_>) : GenState_240_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_240_()
    data class Partial(val items: List<GenModel_240_>, val hasMore: Boolean) : GenState_240_()
}

interface GenRepository_240_ {
    suspend fun getAll(): List<GenModel_240_>
    suspend fun getById(id: Long): GenModel_240_?
    suspend fun save(model: GenModel_240_): GenModel_240_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_240_>
}

@Singleton
class GenRepositoryImpl_240_ @Inject constructor() : GenRepository_240_ {
    private val store = mutableMapOf<Long, GenModel_240_>()
    override suspend fun getAll(): List<GenModel_240_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_240_? = store[id]
    override suspend fun save(model: GenModel_240_): GenModel_240_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_240_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_240_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_240_ @Inject constructor(
    private val repository: GenRepositoryImpl_240_
) : GenUseCase_240_<Unit, List<GenModel_240_>> {
    override suspend fun invoke(params: Unit): List<GenModel_240_> = repository.getAll()
}

class GenSaveUseCase_240_ @Inject constructor(
    private val repository: GenRepositoryImpl_240_
) : GenUseCase_240_<GenModel_240_, GenModel_240_> {
    override suspend fun invoke(params: GenModel_240_): GenModel_240_ = repository.save(params)
}

class GenDeleteUseCase_240_ @Inject constructor(
    private val repository: GenRepositoryImpl_240_
) : GenUseCase_240_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_240_ @Inject constructor(
    private val repository: GenRepositoryImpl_240_
) : GenUseCase_240_<String, List<GenModel_240_>> {
    override suspend fun invoke(params: String): List<GenModel_240_> = repository.search(params)
}

abstract class GenMapper_240_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_240_ : GenMapper_240_<GenModel_240_, String>() {
    override fun map(input: GenModel_240_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_240_ : GenMapper_240_<String, GenModel_240_>() {
    override fun map(input: String): GenModel_240_ {
        val parts = input.split(":")
        return GenModel_240_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_240_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_240_,
    private val saveUseCase: GenSaveUseCase_240_,
    private val deleteUseCase: GenDeleteUseCase_240_,
    private val searchUseCase: GenSearchUseCase_240_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_240_>(GenState_240_.Idle)
    val state: StateFlow<GenState_240_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_240_) {
        when (event) {
            is GenEvent_240_.Load -> loadAll()
            is GenEvent_240_.Update -> save(event.model)
            is GenEvent_240_.Delete -> delete(event.id)
            is GenEvent_240_.Refresh -> loadAll()
            is GenEvent_240_.Search -> search(event.query)
            is GenEvent_240_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_240_.Loading; _state.value = GenState_240_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_240_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_240_.Success(searchUseCase(query)) } }
}
