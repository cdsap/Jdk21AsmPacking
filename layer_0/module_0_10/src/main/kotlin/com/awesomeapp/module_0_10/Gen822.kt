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

data class GenModel_822_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_822_ {
    data class Load(val id: Long) : GenEvent_822_()
    data class Update(val model: GenModel_822_) : GenEvent_822_()
    data class Delete(val id: Long) : GenEvent_822_()
    data object Refresh : GenEvent_822_()
    data class Search(val query: String) : GenEvent_822_()
    data class Filter(val predicate: String) : GenEvent_822_()
}

sealed class GenState_822_ {
    data object Idle : GenState_822_()
    data object Loading : GenState_822_()
    data class Success(val items: List<GenModel_822_>) : GenState_822_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_822_()
    data class Partial(val items: List<GenModel_822_>, val hasMore: Boolean) : GenState_822_()
}

interface GenRepository_822_ {
    suspend fun getAll(): List<GenModel_822_>
    suspend fun getById(id: Long): GenModel_822_?
    suspend fun save(model: GenModel_822_): GenModel_822_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_822_>
}

@Singleton
class GenRepositoryImpl_822_ @Inject constructor() : GenRepository_822_ {
    private val store = mutableMapOf<Long, GenModel_822_>()
    override suspend fun getAll(): List<GenModel_822_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_822_? = store[id]
    override suspend fun save(model: GenModel_822_): GenModel_822_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_822_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_822_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_822_ @Inject constructor(
    private val repository: GenRepositoryImpl_822_
) : GenUseCase_822_<Unit, List<GenModel_822_>> {
    override suspend fun invoke(params: Unit): List<GenModel_822_> = repository.getAll()
}

class GenSaveUseCase_822_ @Inject constructor(
    private val repository: GenRepositoryImpl_822_
) : GenUseCase_822_<GenModel_822_, GenModel_822_> {
    override suspend fun invoke(params: GenModel_822_): GenModel_822_ = repository.save(params)
}

class GenDeleteUseCase_822_ @Inject constructor(
    private val repository: GenRepositoryImpl_822_
) : GenUseCase_822_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_822_ @Inject constructor(
    private val repository: GenRepositoryImpl_822_
) : GenUseCase_822_<String, List<GenModel_822_>> {
    override suspend fun invoke(params: String): List<GenModel_822_> = repository.search(params)
}

abstract class GenMapper_822_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_822_ : GenMapper_822_<GenModel_822_, String>() {
    override fun map(input: GenModel_822_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_822_ : GenMapper_822_<String, GenModel_822_>() {
    override fun map(input: String): GenModel_822_ {
        val parts = input.split(":")
        return GenModel_822_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_822_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_822_,
    private val saveUseCase: GenSaveUseCase_822_,
    private val deleteUseCase: GenDeleteUseCase_822_,
    private val searchUseCase: GenSearchUseCase_822_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_822_>(GenState_822_.Idle)
    val state: StateFlow<GenState_822_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_822_) {
        when (event) {
            is GenEvent_822_.Load -> loadAll()
            is GenEvent_822_.Update -> save(event.model)
            is GenEvent_822_.Delete -> delete(event.id)
            is GenEvent_822_.Refresh -> loadAll()
            is GenEvent_822_.Search -> search(event.query)
            is GenEvent_822_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_822_.Loading; _state.value = GenState_822_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_822_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_822_.Success(searchUseCase(query)) } }
}
