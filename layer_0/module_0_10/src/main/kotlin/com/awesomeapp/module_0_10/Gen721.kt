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

data class GenModel_721_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_721_ {
    data class Load(val id: Long) : GenEvent_721_()
    data class Update(val model: GenModel_721_) : GenEvent_721_()
    data class Delete(val id: Long) : GenEvent_721_()
    data object Refresh : GenEvent_721_()
    data class Search(val query: String) : GenEvent_721_()
    data class Filter(val predicate: String) : GenEvent_721_()
}

sealed class GenState_721_ {
    data object Idle : GenState_721_()
    data object Loading : GenState_721_()
    data class Success(val items: List<GenModel_721_>) : GenState_721_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_721_()
    data class Partial(val items: List<GenModel_721_>, val hasMore: Boolean) : GenState_721_()
}

interface GenRepository_721_ {
    suspend fun getAll(): List<GenModel_721_>
    suspend fun getById(id: Long): GenModel_721_?
    suspend fun save(model: GenModel_721_): GenModel_721_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_721_>
}

@Singleton
class GenRepositoryImpl_721_ @Inject constructor() : GenRepository_721_ {
    private val store = mutableMapOf<Long, GenModel_721_>()
    override suspend fun getAll(): List<GenModel_721_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_721_? = store[id]
    override suspend fun save(model: GenModel_721_): GenModel_721_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_721_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_721_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_721_ @Inject constructor(
    private val repository: GenRepositoryImpl_721_
) : GenUseCase_721_<Unit, List<GenModel_721_>> {
    override suspend fun invoke(params: Unit): List<GenModel_721_> = repository.getAll()
}

class GenSaveUseCase_721_ @Inject constructor(
    private val repository: GenRepositoryImpl_721_
) : GenUseCase_721_<GenModel_721_, GenModel_721_> {
    override suspend fun invoke(params: GenModel_721_): GenModel_721_ = repository.save(params)
}

class GenDeleteUseCase_721_ @Inject constructor(
    private val repository: GenRepositoryImpl_721_
) : GenUseCase_721_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_721_ @Inject constructor(
    private val repository: GenRepositoryImpl_721_
) : GenUseCase_721_<String, List<GenModel_721_>> {
    override suspend fun invoke(params: String): List<GenModel_721_> = repository.search(params)
}

abstract class GenMapper_721_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_721_ : GenMapper_721_<GenModel_721_, String>() {
    override fun map(input: GenModel_721_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_721_ : GenMapper_721_<String, GenModel_721_>() {
    override fun map(input: String): GenModel_721_ {
        val parts = input.split(":")
        return GenModel_721_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_721_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_721_,
    private val saveUseCase: GenSaveUseCase_721_,
    private val deleteUseCase: GenDeleteUseCase_721_,
    private val searchUseCase: GenSearchUseCase_721_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_721_>(GenState_721_.Idle)
    val state: StateFlow<GenState_721_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_721_) {
        when (event) {
            is GenEvent_721_.Load -> loadAll()
            is GenEvent_721_.Update -> save(event.model)
            is GenEvent_721_.Delete -> delete(event.id)
            is GenEvent_721_.Refresh -> loadAll()
            is GenEvent_721_.Search -> search(event.query)
            is GenEvent_721_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_721_.Loading; _state.value = GenState_721_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_721_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_721_.Success(searchUseCase(query)) } }
}
