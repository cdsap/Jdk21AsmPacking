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

data class GenModel_1594_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1594_ {
    data class Load(val id: Long) : GenEvent_1594_()
    data class Update(val model: GenModel_1594_) : GenEvent_1594_()
    data class Delete(val id: Long) : GenEvent_1594_()
    data object Refresh : GenEvent_1594_()
    data class Search(val query: String) : GenEvent_1594_()
    data class Filter(val predicate: String) : GenEvent_1594_()
}

sealed class GenState_1594_ {
    data object Idle : GenState_1594_()
    data object Loading : GenState_1594_()
    data class Success(val items: List<GenModel_1594_>) : GenState_1594_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1594_()
    data class Partial(val items: List<GenModel_1594_>, val hasMore: Boolean) : GenState_1594_()
}

interface GenRepository_1594_ {
    suspend fun getAll(): List<GenModel_1594_>
    suspend fun getById(id: Long): GenModel_1594_?
    suspend fun save(model: GenModel_1594_): GenModel_1594_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1594_>
}

@Singleton
class GenRepositoryImpl_1594_ @Inject constructor() : GenRepository_1594_ {
    private val store = mutableMapOf<Long, GenModel_1594_>()
    override suspend fun getAll(): List<GenModel_1594_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1594_? = store[id]
    override suspend fun save(model: GenModel_1594_): GenModel_1594_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1594_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1594_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1594_ @Inject constructor(
    private val repository: GenRepositoryImpl_1594_
) : GenUseCase_1594_<Unit, List<GenModel_1594_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1594_> = repository.getAll()
}

class GenSaveUseCase_1594_ @Inject constructor(
    private val repository: GenRepositoryImpl_1594_
) : GenUseCase_1594_<GenModel_1594_, GenModel_1594_> {
    override suspend fun invoke(params: GenModel_1594_): GenModel_1594_ = repository.save(params)
}

class GenDeleteUseCase_1594_ @Inject constructor(
    private val repository: GenRepositoryImpl_1594_
) : GenUseCase_1594_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1594_ @Inject constructor(
    private val repository: GenRepositoryImpl_1594_
) : GenUseCase_1594_<String, List<GenModel_1594_>> {
    override suspend fun invoke(params: String): List<GenModel_1594_> = repository.search(params)
}

abstract class GenMapper_1594_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1594_ : GenMapper_1594_<GenModel_1594_, String>() {
    override fun map(input: GenModel_1594_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1594_ : GenMapper_1594_<String, GenModel_1594_>() {
    override fun map(input: String): GenModel_1594_ {
        val parts = input.split(":")
        return GenModel_1594_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1594_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1594_,
    private val saveUseCase: GenSaveUseCase_1594_,
    private val deleteUseCase: GenDeleteUseCase_1594_,
    private val searchUseCase: GenSearchUseCase_1594_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1594_>(GenState_1594_.Idle)
    val state: StateFlow<GenState_1594_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1594_) {
        when (event) {
            is GenEvent_1594_.Load -> loadAll()
            is GenEvent_1594_.Update -> save(event.model)
            is GenEvent_1594_.Delete -> delete(event.id)
            is GenEvent_1594_.Refresh -> loadAll()
            is GenEvent_1594_.Search -> search(event.query)
            is GenEvent_1594_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1594_.Loading; _state.value = GenState_1594_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1594_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1594_.Success(searchUseCase(query)) } }
}
