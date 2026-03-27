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

data class GenModel_469_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_469_ {
    data class Load(val id: Long) : GenEvent_469_()
    data class Update(val model: GenModel_469_) : GenEvent_469_()
    data class Delete(val id: Long) : GenEvent_469_()
    data object Refresh : GenEvent_469_()
    data class Search(val query: String) : GenEvent_469_()
    data class Filter(val predicate: String) : GenEvent_469_()
}

sealed class GenState_469_ {
    data object Idle : GenState_469_()
    data object Loading : GenState_469_()
    data class Success(val items: List<GenModel_469_>) : GenState_469_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_469_()
    data class Partial(val items: List<GenModel_469_>, val hasMore: Boolean) : GenState_469_()
}

interface GenRepository_469_ {
    suspend fun getAll(): List<GenModel_469_>
    suspend fun getById(id: Long): GenModel_469_?
    suspend fun save(model: GenModel_469_): GenModel_469_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_469_>
}

@Singleton
class GenRepositoryImpl_469_ @Inject constructor() : GenRepository_469_ {
    private val store = mutableMapOf<Long, GenModel_469_>()
    override suspend fun getAll(): List<GenModel_469_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_469_? = store[id]
    override suspend fun save(model: GenModel_469_): GenModel_469_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_469_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_469_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_469_ @Inject constructor(
    private val repository: GenRepositoryImpl_469_
) : GenUseCase_469_<Unit, List<GenModel_469_>> {
    override suspend fun invoke(params: Unit): List<GenModel_469_> = repository.getAll()
}

class GenSaveUseCase_469_ @Inject constructor(
    private val repository: GenRepositoryImpl_469_
) : GenUseCase_469_<GenModel_469_, GenModel_469_> {
    override suspend fun invoke(params: GenModel_469_): GenModel_469_ = repository.save(params)
}

class GenDeleteUseCase_469_ @Inject constructor(
    private val repository: GenRepositoryImpl_469_
) : GenUseCase_469_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_469_ @Inject constructor(
    private val repository: GenRepositoryImpl_469_
) : GenUseCase_469_<String, List<GenModel_469_>> {
    override suspend fun invoke(params: String): List<GenModel_469_> = repository.search(params)
}

abstract class GenMapper_469_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_469_ : GenMapper_469_<GenModel_469_, String>() {
    override fun map(input: GenModel_469_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_469_ : GenMapper_469_<String, GenModel_469_>() {
    override fun map(input: String): GenModel_469_ {
        val parts = input.split(":")
        return GenModel_469_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_469_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_469_,
    private val saveUseCase: GenSaveUseCase_469_,
    private val deleteUseCase: GenDeleteUseCase_469_,
    private val searchUseCase: GenSearchUseCase_469_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_469_>(GenState_469_.Idle)
    val state: StateFlow<GenState_469_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_469_) {
        when (event) {
            is GenEvent_469_.Load -> loadAll()
            is GenEvent_469_.Update -> save(event.model)
            is GenEvent_469_.Delete -> delete(event.id)
            is GenEvent_469_.Refresh -> loadAll()
            is GenEvent_469_.Search -> search(event.query)
            is GenEvent_469_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_469_.Loading; _state.value = GenState_469_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_469_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_469_.Success(searchUseCase(query)) } }
}
