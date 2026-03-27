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

data class GenModel_1011_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1011_ {
    data class Load(val id: Long) : GenEvent_1011_()
    data class Update(val model: GenModel_1011_) : GenEvent_1011_()
    data class Delete(val id: Long) : GenEvent_1011_()
    data object Refresh : GenEvent_1011_()
    data class Search(val query: String) : GenEvent_1011_()
    data class Filter(val predicate: String) : GenEvent_1011_()
}

sealed class GenState_1011_ {
    data object Idle : GenState_1011_()
    data object Loading : GenState_1011_()
    data class Success(val items: List<GenModel_1011_>) : GenState_1011_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1011_()
    data class Partial(val items: List<GenModel_1011_>, val hasMore: Boolean) : GenState_1011_()
}

interface GenRepository_1011_ {
    suspend fun getAll(): List<GenModel_1011_>
    suspend fun getById(id: Long): GenModel_1011_?
    suspend fun save(model: GenModel_1011_): GenModel_1011_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1011_>
}

@Singleton
class GenRepositoryImpl_1011_ @Inject constructor() : GenRepository_1011_ {
    private val store = mutableMapOf<Long, GenModel_1011_>()
    override suspend fun getAll(): List<GenModel_1011_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1011_? = store[id]
    override suspend fun save(model: GenModel_1011_): GenModel_1011_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1011_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1011_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1011_ @Inject constructor(
    private val repository: GenRepositoryImpl_1011_
) : GenUseCase_1011_<Unit, List<GenModel_1011_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1011_> = repository.getAll()
}

class GenSaveUseCase_1011_ @Inject constructor(
    private val repository: GenRepositoryImpl_1011_
) : GenUseCase_1011_<GenModel_1011_, GenModel_1011_> {
    override suspend fun invoke(params: GenModel_1011_): GenModel_1011_ = repository.save(params)
}

class GenDeleteUseCase_1011_ @Inject constructor(
    private val repository: GenRepositoryImpl_1011_
) : GenUseCase_1011_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1011_ @Inject constructor(
    private val repository: GenRepositoryImpl_1011_
) : GenUseCase_1011_<String, List<GenModel_1011_>> {
    override suspend fun invoke(params: String): List<GenModel_1011_> = repository.search(params)
}

abstract class GenMapper_1011_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1011_ : GenMapper_1011_<GenModel_1011_, String>() {
    override fun map(input: GenModel_1011_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1011_ : GenMapper_1011_<String, GenModel_1011_>() {
    override fun map(input: String): GenModel_1011_ {
        val parts = input.split(":")
        return GenModel_1011_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1011_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1011_,
    private val saveUseCase: GenSaveUseCase_1011_,
    private val deleteUseCase: GenDeleteUseCase_1011_,
    private val searchUseCase: GenSearchUseCase_1011_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1011_>(GenState_1011_.Idle)
    val state: StateFlow<GenState_1011_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1011_) {
        when (event) {
            is GenEvent_1011_.Load -> loadAll()
            is GenEvent_1011_.Update -> save(event.model)
            is GenEvent_1011_.Delete -> delete(event.id)
            is GenEvent_1011_.Refresh -> loadAll()
            is GenEvent_1011_.Search -> search(event.query)
            is GenEvent_1011_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1011_.Loading; _state.value = GenState_1011_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1011_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1011_.Success(searchUseCase(query)) } }
}
