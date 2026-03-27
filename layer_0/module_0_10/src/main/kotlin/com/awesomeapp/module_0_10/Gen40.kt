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

data class GenModel_40_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_40_ {
    data class Load(val id: Long) : GenEvent_40_()
    data class Update(val model: GenModel_40_) : GenEvent_40_()
    data class Delete(val id: Long) : GenEvent_40_()
    data object Refresh : GenEvent_40_()
    data class Search(val query: String) : GenEvent_40_()
    data class Filter(val predicate: String) : GenEvent_40_()
}

sealed class GenState_40_ {
    data object Idle : GenState_40_()
    data object Loading : GenState_40_()
    data class Success(val items: List<GenModel_40_>) : GenState_40_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_40_()
    data class Partial(val items: List<GenModel_40_>, val hasMore: Boolean) : GenState_40_()
}

interface GenRepository_40_ {
    suspend fun getAll(): List<GenModel_40_>
    suspend fun getById(id: Long): GenModel_40_?
    suspend fun save(model: GenModel_40_): GenModel_40_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_40_>
}

@Singleton
class GenRepositoryImpl_40_ @Inject constructor() : GenRepository_40_ {
    private val store = mutableMapOf<Long, GenModel_40_>()
    override suspend fun getAll(): List<GenModel_40_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_40_? = store[id]
    override suspend fun save(model: GenModel_40_): GenModel_40_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_40_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_40_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_40_ @Inject constructor(
    private val repository: GenRepositoryImpl_40_
) : GenUseCase_40_<Unit, List<GenModel_40_>> {
    override suspend fun invoke(params: Unit): List<GenModel_40_> = repository.getAll()
}

class GenSaveUseCase_40_ @Inject constructor(
    private val repository: GenRepositoryImpl_40_
) : GenUseCase_40_<GenModel_40_, GenModel_40_> {
    override suspend fun invoke(params: GenModel_40_): GenModel_40_ = repository.save(params)
}

class GenDeleteUseCase_40_ @Inject constructor(
    private val repository: GenRepositoryImpl_40_
) : GenUseCase_40_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_40_ @Inject constructor(
    private val repository: GenRepositoryImpl_40_
) : GenUseCase_40_<String, List<GenModel_40_>> {
    override suspend fun invoke(params: String): List<GenModel_40_> = repository.search(params)
}

abstract class GenMapper_40_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_40_ : GenMapper_40_<GenModel_40_, String>() {
    override fun map(input: GenModel_40_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_40_ : GenMapper_40_<String, GenModel_40_>() {
    override fun map(input: String): GenModel_40_ {
        val parts = input.split(":")
        return GenModel_40_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_40_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_40_,
    private val saveUseCase: GenSaveUseCase_40_,
    private val deleteUseCase: GenDeleteUseCase_40_,
    private val searchUseCase: GenSearchUseCase_40_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_40_>(GenState_40_.Idle)
    val state: StateFlow<GenState_40_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_40_) {
        when (event) {
            is GenEvent_40_.Load -> loadAll()
            is GenEvent_40_.Update -> save(event.model)
            is GenEvent_40_.Delete -> delete(event.id)
            is GenEvent_40_.Refresh -> loadAll()
            is GenEvent_40_.Search -> search(event.query)
            is GenEvent_40_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_40_.Loading; _state.value = GenState_40_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_40_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_40_.Success(searchUseCase(query)) } }
}
