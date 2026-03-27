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

data class GenModel_740_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_740_ {
    data class Load(val id: Long) : GenEvent_740_()
    data class Update(val model: GenModel_740_) : GenEvent_740_()
    data class Delete(val id: Long) : GenEvent_740_()
    data object Refresh : GenEvent_740_()
    data class Search(val query: String) : GenEvent_740_()
    data class Filter(val predicate: String) : GenEvent_740_()
}

sealed class GenState_740_ {
    data object Idle : GenState_740_()
    data object Loading : GenState_740_()
    data class Success(val items: List<GenModel_740_>) : GenState_740_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_740_()
    data class Partial(val items: List<GenModel_740_>, val hasMore: Boolean) : GenState_740_()
}

interface GenRepository_740_ {
    suspend fun getAll(): List<GenModel_740_>
    suspend fun getById(id: Long): GenModel_740_?
    suspend fun save(model: GenModel_740_): GenModel_740_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_740_>
}

@Singleton
class GenRepositoryImpl_740_ @Inject constructor() : GenRepository_740_ {
    private val store = mutableMapOf<Long, GenModel_740_>()
    override suspend fun getAll(): List<GenModel_740_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_740_? = store[id]
    override suspend fun save(model: GenModel_740_): GenModel_740_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_740_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_740_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_740_ @Inject constructor(
    private val repository: GenRepositoryImpl_740_
) : GenUseCase_740_<Unit, List<GenModel_740_>> {
    override suspend fun invoke(params: Unit): List<GenModel_740_> = repository.getAll()
}

class GenSaveUseCase_740_ @Inject constructor(
    private val repository: GenRepositoryImpl_740_
) : GenUseCase_740_<GenModel_740_, GenModel_740_> {
    override suspend fun invoke(params: GenModel_740_): GenModel_740_ = repository.save(params)
}

class GenDeleteUseCase_740_ @Inject constructor(
    private val repository: GenRepositoryImpl_740_
) : GenUseCase_740_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_740_ @Inject constructor(
    private val repository: GenRepositoryImpl_740_
) : GenUseCase_740_<String, List<GenModel_740_>> {
    override suspend fun invoke(params: String): List<GenModel_740_> = repository.search(params)
}

abstract class GenMapper_740_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_740_ : GenMapper_740_<GenModel_740_, String>() {
    override fun map(input: GenModel_740_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_740_ : GenMapper_740_<String, GenModel_740_>() {
    override fun map(input: String): GenModel_740_ {
        val parts = input.split(":")
        return GenModel_740_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_740_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_740_,
    private val saveUseCase: GenSaveUseCase_740_,
    private val deleteUseCase: GenDeleteUseCase_740_,
    private val searchUseCase: GenSearchUseCase_740_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_740_>(GenState_740_.Idle)
    val state: StateFlow<GenState_740_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_740_) {
        when (event) {
            is GenEvent_740_.Load -> loadAll()
            is GenEvent_740_.Update -> save(event.model)
            is GenEvent_740_.Delete -> delete(event.id)
            is GenEvent_740_.Refresh -> loadAll()
            is GenEvent_740_.Search -> search(event.query)
            is GenEvent_740_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_740_.Loading; _state.value = GenState_740_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_740_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_740_.Success(searchUseCase(query)) } }
}
