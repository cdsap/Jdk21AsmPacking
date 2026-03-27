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

data class GenModel_820_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_820_ {
    data class Load(val id: Long) : GenEvent_820_()
    data class Update(val model: GenModel_820_) : GenEvent_820_()
    data class Delete(val id: Long) : GenEvent_820_()
    data object Refresh : GenEvent_820_()
    data class Search(val query: String) : GenEvent_820_()
    data class Filter(val predicate: String) : GenEvent_820_()
}

sealed class GenState_820_ {
    data object Idle : GenState_820_()
    data object Loading : GenState_820_()
    data class Success(val items: List<GenModel_820_>) : GenState_820_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_820_()
    data class Partial(val items: List<GenModel_820_>, val hasMore: Boolean) : GenState_820_()
}

interface GenRepository_820_ {
    suspend fun getAll(): List<GenModel_820_>
    suspend fun getById(id: Long): GenModel_820_?
    suspend fun save(model: GenModel_820_): GenModel_820_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_820_>
}

@Singleton
class GenRepositoryImpl_820_ @Inject constructor() : GenRepository_820_ {
    private val store = mutableMapOf<Long, GenModel_820_>()
    override suspend fun getAll(): List<GenModel_820_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_820_? = store[id]
    override suspend fun save(model: GenModel_820_): GenModel_820_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_820_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_820_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_820_ @Inject constructor(
    private val repository: GenRepositoryImpl_820_
) : GenUseCase_820_<Unit, List<GenModel_820_>> {
    override suspend fun invoke(params: Unit): List<GenModel_820_> = repository.getAll()
}

class GenSaveUseCase_820_ @Inject constructor(
    private val repository: GenRepositoryImpl_820_
) : GenUseCase_820_<GenModel_820_, GenModel_820_> {
    override suspend fun invoke(params: GenModel_820_): GenModel_820_ = repository.save(params)
}

class GenDeleteUseCase_820_ @Inject constructor(
    private val repository: GenRepositoryImpl_820_
) : GenUseCase_820_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_820_ @Inject constructor(
    private val repository: GenRepositoryImpl_820_
) : GenUseCase_820_<String, List<GenModel_820_>> {
    override suspend fun invoke(params: String): List<GenModel_820_> = repository.search(params)
}

abstract class GenMapper_820_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_820_ : GenMapper_820_<GenModel_820_, String>() {
    override fun map(input: GenModel_820_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_820_ : GenMapper_820_<String, GenModel_820_>() {
    override fun map(input: String): GenModel_820_ {
        val parts = input.split(":")
        return GenModel_820_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_820_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_820_,
    private val saveUseCase: GenSaveUseCase_820_,
    private val deleteUseCase: GenDeleteUseCase_820_,
    private val searchUseCase: GenSearchUseCase_820_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_820_>(GenState_820_.Idle)
    val state: StateFlow<GenState_820_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_820_) {
        when (event) {
            is GenEvent_820_.Load -> loadAll()
            is GenEvent_820_.Update -> save(event.model)
            is GenEvent_820_.Delete -> delete(event.id)
            is GenEvent_820_.Refresh -> loadAll()
            is GenEvent_820_.Search -> search(event.query)
            is GenEvent_820_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_820_.Loading; _state.value = GenState_820_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_820_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_820_.Success(searchUseCase(query)) } }
}
