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

data class GenModel_634_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_634_ {
    data class Load(val id: Long) : GenEvent_634_()
    data class Update(val model: GenModel_634_) : GenEvent_634_()
    data class Delete(val id: Long) : GenEvent_634_()
    data object Refresh : GenEvent_634_()
    data class Search(val query: String) : GenEvent_634_()
    data class Filter(val predicate: String) : GenEvent_634_()
}

sealed class GenState_634_ {
    data object Idle : GenState_634_()
    data object Loading : GenState_634_()
    data class Success(val items: List<GenModel_634_>) : GenState_634_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_634_()
    data class Partial(val items: List<GenModel_634_>, val hasMore: Boolean) : GenState_634_()
}

interface GenRepository_634_ {
    suspend fun getAll(): List<GenModel_634_>
    suspend fun getById(id: Long): GenModel_634_?
    suspend fun save(model: GenModel_634_): GenModel_634_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_634_>
}

@Singleton
class GenRepositoryImpl_634_ @Inject constructor() : GenRepository_634_ {
    private val store = mutableMapOf<Long, GenModel_634_>()
    override suspend fun getAll(): List<GenModel_634_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_634_? = store[id]
    override suspend fun save(model: GenModel_634_): GenModel_634_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_634_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_634_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_634_ @Inject constructor(
    private val repository: GenRepositoryImpl_634_
) : GenUseCase_634_<Unit, List<GenModel_634_>> {
    override suspend fun invoke(params: Unit): List<GenModel_634_> = repository.getAll()
}

class GenSaveUseCase_634_ @Inject constructor(
    private val repository: GenRepositoryImpl_634_
) : GenUseCase_634_<GenModel_634_, GenModel_634_> {
    override suspend fun invoke(params: GenModel_634_): GenModel_634_ = repository.save(params)
}

class GenDeleteUseCase_634_ @Inject constructor(
    private val repository: GenRepositoryImpl_634_
) : GenUseCase_634_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_634_ @Inject constructor(
    private val repository: GenRepositoryImpl_634_
) : GenUseCase_634_<String, List<GenModel_634_>> {
    override suspend fun invoke(params: String): List<GenModel_634_> = repository.search(params)
}

abstract class GenMapper_634_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_634_ : GenMapper_634_<GenModel_634_, String>() {
    override fun map(input: GenModel_634_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_634_ : GenMapper_634_<String, GenModel_634_>() {
    override fun map(input: String): GenModel_634_ {
        val parts = input.split(":")
        return GenModel_634_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_634_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_634_,
    private val saveUseCase: GenSaveUseCase_634_,
    private val deleteUseCase: GenDeleteUseCase_634_,
    private val searchUseCase: GenSearchUseCase_634_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_634_>(GenState_634_.Idle)
    val state: StateFlow<GenState_634_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_634_) {
        when (event) {
            is GenEvent_634_.Load -> loadAll()
            is GenEvent_634_.Update -> save(event.model)
            is GenEvent_634_.Delete -> delete(event.id)
            is GenEvent_634_.Refresh -> loadAll()
            is GenEvent_634_.Search -> search(event.query)
            is GenEvent_634_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_634_.Loading; _state.value = GenState_634_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_634_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_634_.Success(searchUseCase(query)) } }
}
