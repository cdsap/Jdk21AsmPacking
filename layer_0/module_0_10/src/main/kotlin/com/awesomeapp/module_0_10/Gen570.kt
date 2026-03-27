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

data class GenModel_570_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_570_ {
    data class Load(val id: Long) : GenEvent_570_()
    data class Update(val model: GenModel_570_) : GenEvent_570_()
    data class Delete(val id: Long) : GenEvent_570_()
    data object Refresh : GenEvent_570_()
    data class Search(val query: String) : GenEvent_570_()
    data class Filter(val predicate: String) : GenEvent_570_()
}

sealed class GenState_570_ {
    data object Idle : GenState_570_()
    data object Loading : GenState_570_()
    data class Success(val items: List<GenModel_570_>) : GenState_570_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_570_()
    data class Partial(val items: List<GenModel_570_>, val hasMore: Boolean) : GenState_570_()
}

interface GenRepository_570_ {
    suspend fun getAll(): List<GenModel_570_>
    suspend fun getById(id: Long): GenModel_570_?
    suspend fun save(model: GenModel_570_): GenModel_570_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_570_>
}

@Singleton
class GenRepositoryImpl_570_ @Inject constructor() : GenRepository_570_ {
    private val store = mutableMapOf<Long, GenModel_570_>()
    override suspend fun getAll(): List<GenModel_570_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_570_? = store[id]
    override suspend fun save(model: GenModel_570_): GenModel_570_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_570_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_570_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_570_ @Inject constructor(
    private val repository: GenRepositoryImpl_570_
) : GenUseCase_570_<Unit, List<GenModel_570_>> {
    override suspend fun invoke(params: Unit): List<GenModel_570_> = repository.getAll()
}

class GenSaveUseCase_570_ @Inject constructor(
    private val repository: GenRepositoryImpl_570_
) : GenUseCase_570_<GenModel_570_, GenModel_570_> {
    override suspend fun invoke(params: GenModel_570_): GenModel_570_ = repository.save(params)
}

class GenDeleteUseCase_570_ @Inject constructor(
    private val repository: GenRepositoryImpl_570_
) : GenUseCase_570_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_570_ @Inject constructor(
    private val repository: GenRepositoryImpl_570_
) : GenUseCase_570_<String, List<GenModel_570_>> {
    override suspend fun invoke(params: String): List<GenModel_570_> = repository.search(params)
}

abstract class GenMapper_570_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_570_ : GenMapper_570_<GenModel_570_, String>() {
    override fun map(input: GenModel_570_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_570_ : GenMapper_570_<String, GenModel_570_>() {
    override fun map(input: String): GenModel_570_ {
        val parts = input.split(":")
        return GenModel_570_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_570_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_570_,
    private val saveUseCase: GenSaveUseCase_570_,
    private val deleteUseCase: GenDeleteUseCase_570_,
    private val searchUseCase: GenSearchUseCase_570_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_570_>(GenState_570_.Idle)
    val state: StateFlow<GenState_570_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_570_) {
        when (event) {
            is GenEvent_570_.Load -> loadAll()
            is GenEvent_570_.Update -> save(event.model)
            is GenEvent_570_.Delete -> delete(event.id)
            is GenEvent_570_.Refresh -> loadAll()
            is GenEvent_570_.Search -> search(event.query)
            is GenEvent_570_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_570_.Loading; _state.value = GenState_570_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_570_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_570_.Success(searchUseCase(query)) } }
}
