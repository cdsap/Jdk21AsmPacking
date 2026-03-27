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

data class GenModel_125_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_125_ {
    data class Load(val id: Long) : GenEvent_125_()
    data class Update(val model: GenModel_125_) : GenEvent_125_()
    data class Delete(val id: Long) : GenEvent_125_()
    data object Refresh : GenEvent_125_()
    data class Search(val query: String) : GenEvent_125_()
    data class Filter(val predicate: String) : GenEvent_125_()
}

sealed class GenState_125_ {
    data object Idle : GenState_125_()
    data object Loading : GenState_125_()
    data class Success(val items: List<GenModel_125_>) : GenState_125_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_125_()
    data class Partial(val items: List<GenModel_125_>, val hasMore: Boolean) : GenState_125_()
}

interface GenRepository_125_ {
    suspend fun getAll(): List<GenModel_125_>
    suspend fun getById(id: Long): GenModel_125_?
    suspend fun save(model: GenModel_125_): GenModel_125_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_125_>
}

@Singleton
class GenRepositoryImpl_125_ @Inject constructor() : GenRepository_125_ {
    private val store = mutableMapOf<Long, GenModel_125_>()
    override suspend fun getAll(): List<GenModel_125_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_125_? = store[id]
    override suspend fun save(model: GenModel_125_): GenModel_125_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_125_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_125_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_125_ @Inject constructor(
    private val repository: GenRepositoryImpl_125_
) : GenUseCase_125_<Unit, List<GenModel_125_>> {
    override suspend fun invoke(params: Unit): List<GenModel_125_> = repository.getAll()
}

class GenSaveUseCase_125_ @Inject constructor(
    private val repository: GenRepositoryImpl_125_
) : GenUseCase_125_<GenModel_125_, GenModel_125_> {
    override suspend fun invoke(params: GenModel_125_): GenModel_125_ = repository.save(params)
}

class GenDeleteUseCase_125_ @Inject constructor(
    private val repository: GenRepositoryImpl_125_
) : GenUseCase_125_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_125_ @Inject constructor(
    private val repository: GenRepositoryImpl_125_
) : GenUseCase_125_<String, List<GenModel_125_>> {
    override suspend fun invoke(params: String): List<GenModel_125_> = repository.search(params)
}

abstract class GenMapper_125_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_125_ : GenMapper_125_<GenModel_125_, String>() {
    override fun map(input: GenModel_125_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_125_ : GenMapper_125_<String, GenModel_125_>() {
    override fun map(input: String): GenModel_125_ {
        val parts = input.split(":")
        return GenModel_125_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_125_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_125_,
    private val saveUseCase: GenSaveUseCase_125_,
    private val deleteUseCase: GenDeleteUseCase_125_,
    private val searchUseCase: GenSearchUseCase_125_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_125_>(GenState_125_.Idle)
    val state: StateFlow<GenState_125_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_125_) {
        when (event) {
            is GenEvent_125_.Load -> loadAll()
            is GenEvent_125_.Update -> save(event.model)
            is GenEvent_125_.Delete -> delete(event.id)
            is GenEvent_125_.Refresh -> loadAll()
            is GenEvent_125_.Search -> search(event.query)
            is GenEvent_125_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_125_.Loading; _state.value = GenState_125_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_125_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_125_.Success(searchUseCase(query)) } }
}
