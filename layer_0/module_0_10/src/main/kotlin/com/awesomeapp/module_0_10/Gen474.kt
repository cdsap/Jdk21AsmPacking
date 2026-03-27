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

data class GenModel_474_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_474_ {
    data class Load(val id: Long) : GenEvent_474_()
    data class Update(val model: GenModel_474_) : GenEvent_474_()
    data class Delete(val id: Long) : GenEvent_474_()
    data object Refresh : GenEvent_474_()
    data class Search(val query: String) : GenEvent_474_()
    data class Filter(val predicate: String) : GenEvent_474_()
}

sealed class GenState_474_ {
    data object Idle : GenState_474_()
    data object Loading : GenState_474_()
    data class Success(val items: List<GenModel_474_>) : GenState_474_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_474_()
    data class Partial(val items: List<GenModel_474_>, val hasMore: Boolean) : GenState_474_()
}

interface GenRepository_474_ {
    suspend fun getAll(): List<GenModel_474_>
    suspend fun getById(id: Long): GenModel_474_?
    suspend fun save(model: GenModel_474_): GenModel_474_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_474_>
}

@Singleton
class GenRepositoryImpl_474_ @Inject constructor() : GenRepository_474_ {
    private val store = mutableMapOf<Long, GenModel_474_>()
    override suspend fun getAll(): List<GenModel_474_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_474_? = store[id]
    override suspend fun save(model: GenModel_474_): GenModel_474_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_474_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_474_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_474_ @Inject constructor(
    private val repository: GenRepositoryImpl_474_
) : GenUseCase_474_<Unit, List<GenModel_474_>> {
    override suspend fun invoke(params: Unit): List<GenModel_474_> = repository.getAll()
}

class GenSaveUseCase_474_ @Inject constructor(
    private val repository: GenRepositoryImpl_474_
) : GenUseCase_474_<GenModel_474_, GenModel_474_> {
    override suspend fun invoke(params: GenModel_474_): GenModel_474_ = repository.save(params)
}

class GenDeleteUseCase_474_ @Inject constructor(
    private val repository: GenRepositoryImpl_474_
) : GenUseCase_474_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_474_ @Inject constructor(
    private val repository: GenRepositoryImpl_474_
) : GenUseCase_474_<String, List<GenModel_474_>> {
    override suspend fun invoke(params: String): List<GenModel_474_> = repository.search(params)
}

abstract class GenMapper_474_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_474_ : GenMapper_474_<GenModel_474_, String>() {
    override fun map(input: GenModel_474_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_474_ : GenMapper_474_<String, GenModel_474_>() {
    override fun map(input: String): GenModel_474_ {
        val parts = input.split(":")
        return GenModel_474_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_474_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_474_,
    private val saveUseCase: GenSaveUseCase_474_,
    private val deleteUseCase: GenDeleteUseCase_474_,
    private val searchUseCase: GenSearchUseCase_474_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_474_>(GenState_474_.Idle)
    val state: StateFlow<GenState_474_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_474_) {
        when (event) {
            is GenEvent_474_.Load -> loadAll()
            is GenEvent_474_.Update -> save(event.model)
            is GenEvent_474_.Delete -> delete(event.id)
            is GenEvent_474_.Refresh -> loadAll()
            is GenEvent_474_.Search -> search(event.query)
            is GenEvent_474_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_474_.Loading; _state.value = GenState_474_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_474_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_474_.Success(searchUseCase(query)) } }
}
