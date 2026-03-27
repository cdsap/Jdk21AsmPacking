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

data class GenModel_1690_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1690_ {
    data class Load(val id: Long) : GenEvent_1690_()
    data class Update(val model: GenModel_1690_) : GenEvent_1690_()
    data class Delete(val id: Long) : GenEvent_1690_()
    data object Refresh : GenEvent_1690_()
    data class Search(val query: String) : GenEvent_1690_()
    data class Filter(val predicate: String) : GenEvent_1690_()
}

sealed class GenState_1690_ {
    data object Idle : GenState_1690_()
    data object Loading : GenState_1690_()
    data class Success(val items: List<GenModel_1690_>) : GenState_1690_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1690_()
    data class Partial(val items: List<GenModel_1690_>, val hasMore: Boolean) : GenState_1690_()
}

interface GenRepository_1690_ {
    suspend fun getAll(): List<GenModel_1690_>
    suspend fun getById(id: Long): GenModel_1690_?
    suspend fun save(model: GenModel_1690_): GenModel_1690_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1690_>
}

@Singleton
class GenRepositoryImpl_1690_ @Inject constructor() : GenRepository_1690_ {
    private val store = mutableMapOf<Long, GenModel_1690_>()
    override suspend fun getAll(): List<GenModel_1690_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1690_? = store[id]
    override suspend fun save(model: GenModel_1690_): GenModel_1690_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1690_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1690_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1690_ @Inject constructor(
    private val repository: GenRepositoryImpl_1690_
) : GenUseCase_1690_<Unit, List<GenModel_1690_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1690_> = repository.getAll()
}

class GenSaveUseCase_1690_ @Inject constructor(
    private val repository: GenRepositoryImpl_1690_
) : GenUseCase_1690_<GenModel_1690_, GenModel_1690_> {
    override suspend fun invoke(params: GenModel_1690_): GenModel_1690_ = repository.save(params)
}

class GenDeleteUseCase_1690_ @Inject constructor(
    private val repository: GenRepositoryImpl_1690_
) : GenUseCase_1690_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1690_ @Inject constructor(
    private val repository: GenRepositoryImpl_1690_
) : GenUseCase_1690_<String, List<GenModel_1690_>> {
    override suspend fun invoke(params: String): List<GenModel_1690_> = repository.search(params)
}

abstract class GenMapper_1690_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1690_ : GenMapper_1690_<GenModel_1690_, String>() {
    override fun map(input: GenModel_1690_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1690_ : GenMapper_1690_<String, GenModel_1690_>() {
    override fun map(input: String): GenModel_1690_ {
        val parts = input.split(":")
        return GenModel_1690_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1690_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1690_,
    private val saveUseCase: GenSaveUseCase_1690_,
    private val deleteUseCase: GenDeleteUseCase_1690_,
    private val searchUseCase: GenSearchUseCase_1690_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1690_>(GenState_1690_.Idle)
    val state: StateFlow<GenState_1690_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1690_) {
        when (event) {
            is GenEvent_1690_.Load -> loadAll()
            is GenEvent_1690_.Update -> save(event.model)
            is GenEvent_1690_.Delete -> delete(event.id)
            is GenEvent_1690_.Refresh -> loadAll()
            is GenEvent_1690_.Search -> search(event.query)
            is GenEvent_1690_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1690_.Loading; _state.value = GenState_1690_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1690_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1690_.Success(searchUseCase(query)) } }
}
