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

data class GenModel_944_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_944_ {
    data class Load(val id: Long) : GenEvent_944_()
    data class Update(val model: GenModel_944_) : GenEvent_944_()
    data class Delete(val id: Long) : GenEvent_944_()
    data object Refresh : GenEvent_944_()
    data class Search(val query: String) : GenEvent_944_()
    data class Filter(val predicate: String) : GenEvent_944_()
}

sealed class GenState_944_ {
    data object Idle : GenState_944_()
    data object Loading : GenState_944_()
    data class Success(val items: List<GenModel_944_>) : GenState_944_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_944_()
    data class Partial(val items: List<GenModel_944_>, val hasMore: Boolean) : GenState_944_()
}

interface GenRepository_944_ {
    suspend fun getAll(): List<GenModel_944_>
    suspend fun getById(id: Long): GenModel_944_?
    suspend fun save(model: GenModel_944_): GenModel_944_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_944_>
}

@Singleton
class GenRepositoryImpl_944_ @Inject constructor() : GenRepository_944_ {
    private val store = mutableMapOf<Long, GenModel_944_>()
    override suspend fun getAll(): List<GenModel_944_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_944_? = store[id]
    override suspend fun save(model: GenModel_944_): GenModel_944_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_944_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_944_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_944_ @Inject constructor(
    private val repository: GenRepositoryImpl_944_
) : GenUseCase_944_<Unit, List<GenModel_944_>> {
    override suspend fun invoke(params: Unit): List<GenModel_944_> = repository.getAll()
}

class GenSaveUseCase_944_ @Inject constructor(
    private val repository: GenRepositoryImpl_944_
) : GenUseCase_944_<GenModel_944_, GenModel_944_> {
    override suspend fun invoke(params: GenModel_944_): GenModel_944_ = repository.save(params)
}

class GenDeleteUseCase_944_ @Inject constructor(
    private val repository: GenRepositoryImpl_944_
) : GenUseCase_944_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_944_ @Inject constructor(
    private val repository: GenRepositoryImpl_944_
) : GenUseCase_944_<String, List<GenModel_944_>> {
    override suspend fun invoke(params: String): List<GenModel_944_> = repository.search(params)
}

abstract class GenMapper_944_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_944_ : GenMapper_944_<GenModel_944_, String>() {
    override fun map(input: GenModel_944_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_944_ : GenMapper_944_<String, GenModel_944_>() {
    override fun map(input: String): GenModel_944_ {
        val parts = input.split(":")
        return GenModel_944_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_944_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_944_,
    private val saveUseCase: GenSaveUseCase_944_,
    private val deleteUseCase: GenDeleteUseCase_944_,
    private val searchUseCase: GenSearchUseCase_944_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_944_>(GenState_944_.Idle)
    val state: StateFlow<GenState_944_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_944_) {
        when (event) {
            is GenEvent_944_.Load -> loadAll()
            is GenEvent_944_.Update -> save(event.model)
            is GenEvent_944_.Delete -> delete(event.id)
            is GenEvent_944_.Refresh -> loadAll()
            is GenEvent_944_.Search -> search(event.query)
            is GenEvent_944_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_944_.Loading; _state.value = GenState_944_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_944_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_944_.Success(searchUseCase(query)) } }
}
