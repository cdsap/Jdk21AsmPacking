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

data class GenModel_1620_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1620_ {
    data class Load(val id: Long) : GenEvent_1620_()
    data class Update(val model: GenModel_1620_) : GenEvent_1620_()
    data class Delete(val id: Long) : GenEvent_1620_()
    data object Refresh : GenEvent_1620_()
    data class Search(val query: String) : GenEvent_1620_()
    data class Filter(val predicate: String) : GenEvent_1620_()
}

sealed class GenState_1620_ {
    data object Idle : GenState_1620_()
    data object Loading : GenState_1620_()
    data class Success(val items: List<GenModel_1620_>) : GenState_1620_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1620_()
    data class Partial(val items: List<GenModel_1620_>, val hasMore: Boolean) : GenState_1620_()
}

interface GenRepository_1620_ {
    suspend fun getAll(): List<GenModel_1620_>
    suspend fun getById(id: Long): GenModel_1620_?
    suspend fun save(model: GenModel_1620_): GenModel_1620_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1620_>
}

@Singleton
class GenRepositoryImpl_1620_ @Inject constructor() : GenRepository_1620_ {
    private val store = mutableMapOf<Long, GenModel_1620_>()
    override suspend fun getAll(): List<GenModel_1620_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1620_? = store[id]
    override suspend fun save(model: GenModel_1620_): GenModel_1620_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1620_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1620_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1620_ @Inject constructor(
    private val repository: GenRepositoryImpl_1620_
) : GenUseCase_1620_<Unit, List<GenModel_1620_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1620_> = repository.getAll()
}

class GenSaveUseCase_1620_ @Inject constructor(
    private val repository: GenRepositoryImpl_1620_
) : GenUseCase_1620_<GenModel_1620_, GenModel_1620_> {
    override suspend fun invoke(params: GenModel_1620_): GenModel_1620_ = repository.save(params)
}

class GenDeleteUseCase_1620_ @Inject constructor(
    private val repository: GenRepositoryImpl_1620_
) : GenUseCase_1620_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1620_ @Inject constructor(
    private val repository: GenRepositoryImpl_1620_
) : GenUseCase_1620_<String, List<GenModel_1620_>> {
    override suspend fun invoke(params: String): List<GenModel_1620_> = repository.search(params)
}

abstract class GenMapper_1620_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1620_ : GenMapper_1620_<GenModel_1620_, String>() {
    override fun map(input: GenModel_1620_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1620_ : GenMapper_1620_<String, GenModel_1620_>() {
    override fun map(input: String): GenModel_1620_ {
        val parts = input.split(":")
        return GenModel_1620_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1620_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1620_,
    private val saveUseCase: GenSaveUseCase_1620_,
    private val deleteUseCase: GenDeleteUseCase_1620_,
    private val searchUseCase: GenSearchUseCase_1620_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1620_>(GenState_1620_.Idle)
    val state: StateFlow<GenState_1620_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1620_) {
        when (event) {
            is GenEvent_1620_.Load -> loadAll()
            is GenEvent_1620_.Update -> save(event.model)
            is GenEvent_1620_.Delete -> delete(event.id)
            is GenEvent_1620_.Refresh -> loadAll()
            is GenEvent_1620_.Search -> search(event.query)
            is GenEvent_1620_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1620_.Loading; _state.value = GenState_1620_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1620_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1620_.Success(searchUseCase(query)) } }
}
