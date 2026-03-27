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

data class GenModel_604_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_604_ {
    data class Load(val id: Long) : GenEvent_604_()
    data class Update(val model: GenModel_604_) : GenEvent_604_()
    data class Delete(val id: Long) : GenEvent_604_()
    data object Refresh : GenEvent_604_()
    data class Search(val query: String) : GenEvent_604_()
    data class Filter(val predicate: String) : GenEvent_604_()
}

sealed class GenState_604_ {
    data object Idle : GenState_604_()
    data object Loading : GenState_604_()
    data class Success(val items: List<GenModel_604_>) : GenState_604_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_604_()
    data class Partial(val items: List<GenModel_604_>, val hasMore: Boolean) : GenState_604_()
}

interface GenRepository_604_ {
    suspend fun getAll(): List<GenModel_604_>
    suspend fun getById(id: Long): GenModel_604_?
    suspend fun save(model: GenModel_604_): GenModel_604_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_604_>
}

@Singleton
class GenRepositoryImpl_604_ @Inject constructor() : GenRepository_604_ {
    private val store = mutableMapOf<Long, GenModel_604_>()
    override suspend fun getAll(): List<GenModel_604_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_604_? = store[id]
    override suspend fun save(model: GenModel_604_): GenModel_604_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_604_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_604_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_604_ @Inject constructor(
    private val repository: GenRepositoryImpl_604_
) : GenUseCase_604_<Unit, List<GenModel_604_>> {
    override suspend fun invoke(params: Unit): List<GenModel_604_> = repository.getAll()
}

class GenSaveUseCase_604_ @Inject constructor(
    private val repository: GenRepositoryImpl_604_
) : GenUseCase_604_<GenModel_604_, GenModel_604_> {
    override suspend fun invoke(params: GenModel_604_): GenModel_604_ = repository.save(params)
}

class GenDeleteUseCase_604_ @Inject constructor(
    private val repository: GenRepositoryImpl_604_
) : GenUseCase_604_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_604_ @Inject constructor(
    private val repository: GenRepositoryImpl_604_
) : GenUseCase_604_<String, List<GenModel_604_>> {
    override suspend fun invoke(params: String): List<GenModel_604_> = repository.search(params)
}

abstract class GenMapper_604_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_604_ : GenMapper_604_<GenModel_604_, String>() {
    override fun map(input: GenModel_604_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_604_ : GenMapper_604_<String, GenModel_604_>() {
    override fun map(input: String): GenModel_604_ {
        val parts = input.split(":")
        return GenModel_604_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_604_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_604_,
    private val saveUseCase: GenSaveUseCase_604_,
    private val deleteUseCase: GenDeleteUseCase_604_,
    private val searchUseCase: GenSearchUseCase_604_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_604_>(GenState_604_.Idle)
    val state: StateFlow<GenState_604_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_604_) {
        when (event) {
            is GenEvent_604_.Load -> loadAll()
            is GenEvent_604_.Update -> save(event.model)
            is GenEvent_604_.Delete -> delete(event.id)
            is GenEvent_604_.Refresh -> loadAll()
            is GenEvent_604_.Search -> search(event.query)
            is GenEvent_604_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_604_.Loading; _state.value = GenState_604_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_604_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_604_.Success(searchUseCase(query)) } }
}
