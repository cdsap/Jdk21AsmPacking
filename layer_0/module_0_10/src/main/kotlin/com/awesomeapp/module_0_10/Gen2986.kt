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

data class GenModel_2986_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2986_ {
    data class Load(val id: Long) : GenEvent_2986_()
    data class Update(val model: GenModel_2986_) : GenEvent_2986_()
    data class Delete(val id: Long) : GenEvent_2986_()
    data object Refresh : GenEvent_2986_()
    data class Search(val query: String) : GenEvent_2986_()
    data class Filter(val predicate: String) : GenEvent_2986_()
}

sealed class GenState_2986_ {
    data object Idle : GenState_2986_()
    data object Loading : GenState_2986_()
    data class Success(val items: List<GenModel_2986_>) : GenState_2986_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2986_()
    data class Partial(val items: List<GenModel_2986_>, val hasMore: Boolean) : GenState_2986_()
}

interface GenRepository_2986_ {
    suspend fun getAll(): List<GenModel_2986_>
    suspend fun getById(id: Long): GenModel_2986_?
    suspend fun save(model: GenModel_2986_): GenModel_2986_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2986_>
}

@Singleton
class GenRepositoryImpl_2986_ @Inject constructor() : GenRepository_2986_ {
    private val store = mutableMapOf<Long, GenModel_2986_>()
    override suspend fun getAll(): List<GenModel_2986_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2986_? = store[id]
    override suspend fun save(model: GenModel_2986_): GenModel_2986_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2986_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2986_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2986_ @Inject constructor(
    private val repository: GenRepositoryImpl_2986_
) : GenUseCase_2986_<Unit, List<GenModel_2986_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2986_> = repository.getAll()
}

class GenSaveUseCase_2986_ @Inject constructor(
    private val repository: GenRepositoryImpl_2986_
) : GenUseCase_2986_<GenModel_2986_, GenModel_2986_> {
    override suspend fun invoke(params: GenModel_2986_): GenModel_2986_ = repository.save(params)
}

class GenDeleteUseCase_2986_ @Inject constructor(
    private val repository: GenRepositoryImpl_2986_
) : GenUseCase_2986_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2986_ @Inject constructor(
    private val repository: GenRepositoryImpl_2986_
) : GenUseCase_2986_<String, List<GenModel_2986_>> {
    override suspend fun invoke(params: String): List<GenModel_2986_> = repository.search(params)
}

abstract class GenMapper_2986_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2986_ : GenMapper_2986_<GenModel_2986_, String>() {
    override fun map(input: GenModel_2986_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2986_ : GenMapper_2986_<String, GenModel_2986_>() {
    override fun map(input: String): GenModel_2986_ {
        val parts = input.split(":")
        return GenModel_2986_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2986_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2986_,
    private val saveUseCase: GenSaveUseCase_2986_,
    private val deleteUseCase: GenDeleteUseCase_2986_,
    private val searchUseCase: GenSearchUseCase_2986_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2986_>(GenState_2986_.Idle)
    val state: StateFlow<GenState_2986_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2986_) {
        when (event) {
            is GenEvent_2986_.Load -> loadAll()
            is GenEvent_2986_.Update -> save(event.model)
            is GenEvent_2986_.Delete -> delete(event.id)
            is GenEvent_2986_.Refresh -> loadAll()
            is GenEvent_2986_.Search -> search(event.query)
            is GenEvent_2986_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2986_.Loading; _state.value = GenState_2986_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2986_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2986_.Success(searchUseCase(query)) } }
}
