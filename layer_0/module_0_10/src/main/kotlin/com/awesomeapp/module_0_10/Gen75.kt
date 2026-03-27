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

data class GenModel_75_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_75_ {
    data class Load(val id: Long) : GenEvent_75_()
    data class Update(val model: GenModel_75_) : GenEvent_75_()
    data class Delete(val id: Long) : GenEvent_75_()
    data object Refresh : GenEvent_75_()
    data class Search(val query: String) : GenEvent_75_()
    data class Filter(val predicate: String) : GenEvent_75_()
}

sealed class GenState_75_ {
    data object Idle : GenState_75_()
    data object Loading : GenState_75_()
    data class Success(val items: List<GenModel_75_>) : GenState_75_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_75_()
    data class Partial(val items: List<GenModel_75_>, val hasMore: Boolean) : GenState_75_()
}

interface GenRepository_75_ {
    suspend fun getAll(): List<GenModel_75_>
    suspend fun getById(id: Long): GenModel_75_?
    suspend fun save(model: GenModel_75_): GenModel_75_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_75_>
}

@Singleton
class GenRepositoryImpl_75_ @Inject constructor() : GenRepository_75_ {
    private val store = mutableMapOf<Long, GenModel_75_>()
    override suspend fun getAll(): List<GenModel_75_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_75_? = store[id]
    override suspend fun save(model: GenModel_75_): GenModel_75_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_75_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_75_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_75_ @Inject constructor(
    private val repository: GenRepositoryImpl_75_
) : GenUseCase_75_<Unit, List<GenModel_75_>> {
    override suspend fun invoke(params: Unit): List<GenModel_75_> = repository.getAll()
}

class GenSaveUseCase_75_ @Inject constructor(
    private val repository: GenRepositoryImpl_75_
) : GenUseCase_75_<GenModel_75_, GenModel_75_> {
    override suspend fun invoke(params: GenModel_75_): GenModel_75_ = repository.save(params)
}

class GenDeleteUseCase_75_ @Inject constructor(
    private val repository: GenRepositoryImpl_75_
) : GenUseCase_75_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_75_ @Inject constructor(
    private val repository: GenRepositoryImpl_75_
) : GenUseCase_75_<String, List<GenModel_75_>> {
    override suspend fun invoke(params: String): List<GenModel_75_> = repository.search(params)
}

abstract class GenMapper_75_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_75_ : GenMapper_75_<GenModel_75_, String>() {
    override fun map(input: GenModel_75_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_75_ : GenMapper_75_<String, GenModel_75_>() {
    override fun map(input: String): GenModel_75_ {
        val parts = input.split(":")
        return GenModel_75_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_75_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_75_,
    private val saveUseCase: GenSaveUseCase_75_,
    private val deleteUseCase: GenDeleteUseCase_75_,
    private val searchUseCase: GenSearchUseCase_75_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_75_>(GenState_75_.Idle)
    val state: StateFlow<GenState_75_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_75_) {
        when (event) {
            is GenEvent_75_.Load -> loadAll()
            is GenEvent_75_.Update -> save(event.model)
            is GenEvent_75_.Delete -> delete(event.id)
            is GenEvent_75_.Refresh -> loadAll()
            is GenEvent_75_.Search -> search(event.query)
            is GenEvent_75_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_75_.Loading; _state.value = GenState_75_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_75_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_75_.Success(searchUseCase(query)) } }
}
