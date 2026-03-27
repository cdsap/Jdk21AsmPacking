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

data class GenModel_599_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_599_ {
    data class Load(val id: Long) : GenEvent_599_()
    data class Update(val model: GenModel_599_) : GenEvent_599_()
    data class Delete(val id: Long) : GenEvent_599_()
    data object Refresh : GenEvent_599_()
    data class Search(val query: String) : GenEvent_599_()
    data class Filter(val predicate: String) : GenEvent_599_()
}

sealed class GenState_599_ {
    data object Idle : GenState_599_()
    data object Loading : GenState_599_()
    data class Success(val items: List<GenModel_599_>) : GenState_599_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_599_()
    data class Partial(val items: List<GenModel_599_>, val hasMore: Boolean) : GenState_599_()
}

interface GenRepository_599_ {
    suspend fun getAll(): List<GenModel_599_>
    suspend fun getById(id: Long): GenModel_599_?
    suspend fun save(model: GenModel_599_): GenModel_599_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_599_>
}

@Singleton
class GenRepositoryImpl_599_ @Inject constructor() : GenRepository_599_ {
    private val store = mutableMapOf<Long, GenModel_599_>()
    override suspend fun getAll(): List<GenModel_599_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_599_? = store[id]
    override suspend fun save(model: GenModel_599_): GenModel_599_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_599_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_599_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_599_ @Inject constructor(
    private val repository: GenRepositoryImpl_599_
) : GenUseCase_599_<Unit, List<GenModel_599_>> {
    override suspend fun invoke(params: Unit): List<GenModel_599_> = repository.getAll()
}

class GenSaveUseCase_599_ @Inject constructor(
    private val repository: GenRepositoryImpl_599_
) : GenUseCase_599_<GenModel_599_, GenModel_599_> {
    override suspend fun invoke(params: GenModel_599_): GenModel_599_ = repository.save(params)
}

class GenDeleteUseCase_599_ @Inject constructor(
    private val repository: GenRepositoryImpl_599_
) : GenUseCase_599_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_599_ @Inject constructor(
    private val repository: GenRepositoryImpl_599_
) : GenUseCase_599_<String, List<GenModel_599_>> {
    override suspend fun invoke(params: String): List<GenModel_599_> = repository.search(params)
}

abstract class GenMapper_599_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_599_ : GenMapper_599_<GenModel_599_, String>() {
    override fun map(input: GenModel_599_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_599_ : GenMapper_599_<String, GenModel_599_>() {
    override fun map(input: String): GenModel_599_ {
        val parts = input.split(":")
        return GenModel_599_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_599_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_599_,
    private val saveUseCase: GenSaveUseCase_599_,
    private val deleteUseCase: GenDeleteUseCase_599_,
    private val searchUseCase: GenSearchUseCase_599_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_599_>(GenState_599_.Idle)
    val state: StateFlow<GenState_599_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_599_) {
        when (event) {
            is GenEvent_599_.Load -> loadAll()
            is GenEvent_599_.Update -> save(event.model)
            is GenEvent_599_.Delete -> delete(event.id)
            is GenEvent_599_.Refresh -> loadAll()
            is GenEvent_599_.Search -> search(event.query)
            is GenEvent_599_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_599_.Loading; _state.value = GenState_599_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_599_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_599_.Success(searchUseCase(query)) } }
}
