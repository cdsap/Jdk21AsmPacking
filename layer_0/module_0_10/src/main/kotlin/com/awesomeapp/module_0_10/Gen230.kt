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

data class GenModel_230_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_230_ {
    data class Load(val id: Long) : GenEvent_230_()
    data class Update(val model: GenModel_230_) : GenEvent_230_()
    data class Delete(val id: Long) : GenEvent_230_()
    data object Refresh : GenEvent_230_()
    data class Search(val query: String) : GenEvent_230_()
    data class Filter(val predicate: String) : GenEvent_230_()
}

sealed class GenState_230_ {
    data object Idle : GenState_230_()
    data object Loading : GenState_230_()
    data class Success(val items: List<GenModel_230_>) : GenState_230_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_230_()
    data class Partial(val items: List<GenModel_230_>, val hasMore: Boolean) : GenState_230_()
}

interface GenRepository_230_ {
    suspend fun getAll(): List<GenModel_230_>
    suspend fun getById(id: Long): GenModel_230_?
    suspend fun save(model: GenModel_230_): GenModel_230_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_230_>
}

@Singleton
class GenRepositoryImpl_230_ @Inject constructor() : GenRepository_230_ {
    private val store = mutableMapOf<Long, GenModel_230_>()
    override suspend fun getAll(): List<GenModel_230_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_230_? = store[id]
    override suspend fun save(model: GenModel_230_): GenModel_230_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_230_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_230_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_230_ @Inject constructor(
    private val repository: GenRepositoryImpl_230_
) : GenUseCase_230_<Unit, List<GenModel_230_>> {
    override suspend fun invoke(params: Unit): List<GenModel_230_> = repository.getAll()
}

class GenSaveUseCase_230_ @Inject constructor(
    private val repository: GenRepositoryImpl_230_
) : GenUseCase_230_<GenModel_230_, GenModel_230_> {
    override suspend fun invoke(params: GenModel_230_): GenModel_230_ = repository.save(params)
}

class GenDeleteUseCase_230_ @Inject constructor(
    private val repository: GenRepositoryImpl_230_
) : GenUseCase_230_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_230_ @Inject constructor(
    private val repository: GenRepositoryImpl_230_
) : GenUseCase_230_<String, List<GenModel_230_>> {
    override suspend fun invoke(params: String): List<GenModel_230_> = repository.search(params)
}

abstract class GenMapper_230_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_230_ : GenMapper_230_<GenModel_230_, String>() {
    override fun map(input: GenModel_230_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_230_ : GenMapper_230_<String, GenModel_230_>() {
    override fun map(input: String): GenModel_230_ {
        val parts = input.split(":")
        return GenModel_230_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_230_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_230_,
    private val saveUseCase: GenSaveUseCase_230_,
    private val deleteUseCase: GenDeleteUseCase_230_,
    private val searchUseCase: GenSearchUseCase_230_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_230_>(GenState_230_.Idle)
    val state: StateFlow<GenState_230_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_230_) {
        when (event) {
            is GenEvent_230_.Load -> loadAll()
            is GenEvent_230_.Update -> save(event.model)
            is GenEvent_230_.Delete -> delete(event.id)
            is GenEvent_230_.Refresh -> loadAll()
            is GenEvent_230_.Search -> search(event.query)
            is GenEvent_230_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_230_.Loading; _state.value = GenState_230_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_230_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_230_.Success(searchUseCase(query)) } }
}
