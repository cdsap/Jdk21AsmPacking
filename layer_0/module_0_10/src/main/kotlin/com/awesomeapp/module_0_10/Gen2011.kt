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

data class GenModel_2011_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2011_ {
    data class Load(val id: Long) : GenEvent_2011_()
    data class Update(val model: GenModel_2011_) : GenEvent_2011_()
    data class Delete(val id: Long) : GenEvent_2011_()
    data object Refresh : GenEvent_2011_()
    data class Search(val query: String) : GenEvent_2011_()
    data class Filter(val predicate: String) : GenEvent_2011_()
}

sealed class GenState_2011_ {
    data object Idle : GenState_2011_()
    data object Loading : GenState_2011_()
    data class Success(val items: List<GenModel_2011_>) : GenState_2011_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2011_()
    data class Partial(val items: List<GenModel_2011_>, val hasMore: Boolean) : GenState_2011_()
}

interface GenRepository_2011_ {
    suspend fun getAll(): List<GenModel_2011_>
    suspend fun getById(id: Long): GenModel_2011_?
    suspend fun save(model: GenModel_2011_): GenModel_2011_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2011_>
}

@Singleton
class GenRepositoryImpl_2011_ @Inject constructor() : GenRepository_2011_ {
    private val store = mutableMapOf<Long, GenModel_2011_>()
    override suspend fun getAll(): List<GenModel_2011_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2011_? = store[id]
    override suspend fun save(model: GenModel_2011_): GenModel_2011_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2011_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2011_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2011_ @Inject constructor(
    private val repository: GenRepositoryImpl_2011_
) : GenUseCase_2011_<Unit, List<GenModel_2011_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2011_> = repository.getAll()
}

class GenSaveUseCase_2011_ @Inject constructor(
    private val repository: GenRepositoryImpl_2011_
) : GenUseCase_2011_<GenModel_2011_, GenModel_2011_> {
    override suspend fun invoke(params: GenModel_2011_): GenModel_2011_ = repository.save(params)
}

class GenDeleteUseCase_2011_ @Inject constructor(
    private val repository: GenRepositoryImpl_2011_
) : GenUseCase_2011_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2011_ @Inject constructor(
    private val repository: GenRepositoryImpl_2011_
) : GenUseCase_2011_<String, List<GenModel_2011_>> {
    override suspend fun invoke(params: String): List<GenModel_2011_> = repository.search(params)
}

abstract class GenMapper_2011_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2011_ : GenMapper_2011_<GenModel_2011_, String>() {
    override fun map(input: GenModel_2011_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2011_ : GenMapper_2011_<String, GenModel_2011_>() {
    override fun map(input: String): GenModel_2011_ {
        val parts = input.split(":")
        return GenModel_2011_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2011_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2011_,
    private val saveUseCase: GenSaveUseCase_2011_,
    private val deleteUseCase: GenDeleteUseCase_2011_,
    private val searchUseCase: GenSearchUseCase_2011_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2011_>(GenState_2011_.Idle)
    val state: StateFlow<GenState_2011_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2011_) {
        when (event) {
            is GenEvent_2011_.Load -> loadAll()
            is GenEvent_2011_.Update -> save(event.model)
            is GenEvent_2011_.Delete -> delete(event.id)
            is GenEvent_2011_.Refresh -> loadAll()
            is GenEvent_2011_.Search -> search(event.query)
            is GenEvent_2011_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2011_.Loading; _state.value = GenState_2011_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2011_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2011_.Success(searchUseCase(query)) } }
}
