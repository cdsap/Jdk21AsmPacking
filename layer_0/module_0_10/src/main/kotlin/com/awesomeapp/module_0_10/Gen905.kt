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

data class GenModel_905_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_905_ {
    data class Load(val id: Long) : GenEvent_905_()
    data class Update(val model: GenModel_905_) : GenEvent_905_()
    data class Delete(val id: Long) : GenEvent_905_()
    data object Refresh : GenEvent_905_()
    data class Search(val query: String) : GenEvent_905_()
    data class Filter(val predicate: String) : GenEvent_905_()
}

sealed class GenState_905_ {
    data object Idle : GenState_905_()
    data object Loading : GenState_905_()
    data class Success(val items: List<GenModel_905_>) : GenState_905_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_905_()
    data class Partial(val items: List<GenModel_905_>, val hasMore: Boolean) : GenState_905_()
}

interface GenRepository_905_ {
    suspend fun getAll(): List<GenModel_905_>
    suspend fun getById(id: Long): GenModel_905_?
    suspend fun save(model: GenModel_905_): GenModel_905_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_905_>
}

@Singleton
class GenRepositoryImpl_905_ @Inject constructor() : GenRepository_905_ {
    private val store = mutableMapOf<Long, GenModel_905_>()
    override suspend fun getAll(): List<GenModel_905_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_905_? = store[id]
    override suspend fun save(model: GenModel_905_): GenModel_905_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_905_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_905_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_905_ @Inject constructor(
    private val repository: GenRepositoryImpl_905_
) : GenUseCase_905_<Unit, List<GenModel_905_>> {
    override suspend fun invoke(params: Unit): List<GenModel_905_> = repository.getAll()
}

class GenSaveUseCase_905_ @Inject constructor(
    private val repository: GenRepositoryImpl_905_
) : GenUseCase_905_<GenModel_905_, GenModel_905_> {
    override suspend fun invoke(params: GenModel_905_): GenModel_905_ = repository.save(params)
}

class GenDeleteUseCase_905_ @Inject constructor(
    private val repository: GenRepositoryImpl_905_
) : GenUseCase_905_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_905_ @Inject constructor(
    private val repository: GenRepositoryImpl_905_
) : GenUseCase_905_<String, List<GenModel_905_>> {
    override suspend fun invoke(params: String): List<GenModel_905_> = repository.search(params)
}

abstract class GenMapper_905_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_905_ : GenMapper_905_<GenModel_905_, String>() {
    override fun map(input: GenModel_905_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_905_ : GenMapper_905_<String, GenModel_905_>() {
    override fun map(input: String): GenModel_905_ {
        val parts = input.split(":")
        return GenModel_905_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_905_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_905_,
    private val saveUseCase: GenSaveUseCase_905_,
    private val deleteUseCase: GenDeleteUseCase_905_,
    private val searchUseCase: GenSearchUseCase_905_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_905_>(GenState_905_.Idle)
    val state: StateFlow<GenState_905_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_905_) {
        when (event) {
            is GenEvent_905_.Load -> loadAll()
            is GenEvent_905_.Update -> save(event.model)
            is GenEvent_905_.Delete -> delete(event.id)
            is GenEvent_905_.Refresh -> loadAll()
            is GenEvent_905_.Search -> search(event.query)
            is GenEvent_905_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_905_.Loading; _state.value = GenState_905_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_905_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_905_.Success(searchUseCase(query)) } }
}
