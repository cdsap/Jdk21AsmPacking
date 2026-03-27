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

data class GenModel_2605_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2605_ {
    data class Load(val id: Long) : GenEvent_2605_()
    data class Update(val model: GenModel_2605_) : GenEvent_2605_()
    data class Delete(val id: Long) : GenEvent_2605_()
    data object Refresh : GenEvent_2605_()
    data class Search(val query: String) : GenEvent_2605_()
    data class Filter(val predicate: String) : GenEvent_2605_()
}

sealed class GenState_2605_ {
    data object Idle : GenState_2605_()
    data object Loading : GenState_2605_()
    data class Success(val items: List<GenModel_2605_>) : GenState_2605_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2605_()
    data class Partial(val items: List<GenModel_2605_>, val hasMore: Boolean) : GenState_2605_()
}

interface GenRepository_2605_ {
    suspend fun getAll(): List<GenModel_2605_>
    suspend fun getById(id: Long): GenModel_2605_?
    suspend fun save(model: GenModel_2605_): GenModel_2605_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2605_>
}

@Singleton
class GenRepositoryImpl_2605_ @Inject constructor() : GenRepository_2605_ {
    private val store = mutableMapOf<Long, GenModel_2605_>()
    override suspend fun getAll(): List<GenModel_2605_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2605_? = store[id]
    override suspend fun save(model: GenModel_2605_): GenModel_2605_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2605_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2605_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2605_ @Inject constructor(
    private val repository: GenRepositoryImpl_2605_
) : GenUseCase_2605_<Unit, List<GenModel_2605_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2605_> = repository.getAll()
}

class GenSaveUseCase_2605_ @Inject constructor(
    private val repository: GenRepositoryImpl_2605_
) : GenUseCase_2605_<GenModel_2605_, GenModel_2605_> {
    override suspend fun invoke(params: GenModel_2605_): GenModel_2605_ = repository.save(params)
}

class GenDeleteUseCase_2605_ @Inject constructor(
    private val repository: GenRepositoryImpl_2605_
) : GenUseCase_2605_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2605_ @Inject constructor(
    private val repository: GenRepositoryImpl_2605_
) : GenUseCase_2605_<String, List<GenModel_2605_>> {
    override suspend fun invoke(params: String): List<GenModel_2605_> = repository.search(params)
}

abstract class GenMapper_2605_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2605_ : GenMapper_2605_<GenModel_2605_, String>() {
    override fun map(input: GenModel_2605_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2605_ : GenMapper_2605_<String, GenModel_2605_>() {
    override fun map(input: String): GenModel_2605_ {
        val parts = input.split(":")
        return GenModel_2605_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2605_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2605_,
    private val saveUseCase: GenSaveUseCase_2605_,
    private val deleteUseCase: GenDeleteUseCase_2605_,
    private val searchUseCase: GenSearchUseCase_2605_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2605_>(GenState_2605_.Idle)
    val state: StateFlow<GenState_2605_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2605_) {
        when (event) {
            is GenEvent_2605_.Load -> loadAll()
            is GenEvent_2605_.Update -> save(event.model)
            is GenEvent_2605_.Delete -> delete(event.id)
            is GenEvent_2605_.Refresh -> loadAll()
            is GenEvent_2605_.Search -> search(event.query)
            is GenEvent_2605_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2605_.Loading; _state.value = GenState_2605_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2605_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2605_.Success(searchUseCase(query)) } }
}
