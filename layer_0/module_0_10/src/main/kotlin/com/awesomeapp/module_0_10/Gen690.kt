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

data class GenModel_690_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_690_ {
    data class Load(val id: Long) : GenEvent_690_()
    data class Update(val model: GenModel_690_) : GenEvent_690_()
    data class Delete(val id: Long) : GenEvent_690_()
    data object Refresh : GenEvent_690_()
    data class Search(val query: String) : GenEvent_690_()
    data class Filter(val predicate: String) : GenEvent_690_()
}

sealed class GenState_690_ {
    data object Idle : GenState_690_()
    data object Loading : GenState_690_()
    data class Success(val items: List<GenModel_690_>) : GenState_690_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_690_()
    data class Partial(val items: List<GenModel_690_>, val hasMore: Boolean) : GenState_690_()
}

interface GenRepository_690_ {
    suspend fun getAll(): List<GenModel_690_>
    suspend fun getById(id: Long): GenModel_690_?
    suspend fun save(model: GenModel_690_): GenModel_690_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_690_>
}

@Singleton
class GenRepositoryImpl_690_ @Inject constructor() : GenRepository_690_ {
    private val store = mutableMapOf<Long, GenModel_690_>()
    override suspend fun getAll(): List<GenModel_690_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_690_? = store[id]
    override suspend fun save(model: GenModel_690_): GenModel_690_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_690_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_690_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_690_ @Inject constructor(
    private val repository: GenRepositoryImpl_690_
) : GenUseCase_690_<Unit, List<GenModel_690_>> {
    override suspend fun invoke(params: Unit): List<GenModel_690_> = repository.getAll()
}

class GenSaveUseCase_690_ @Inject constructor(
    private val repository: GenRepositoryImpl_690_
) : GenUseCase_690_<GenModel_690_, GenModel_690_> {
    override suspend fun invoke(params: GenModel_690_): GenModel_690_ = repository.save(params)
}

class GenDeleteUseCase_690_ @Inject constructor(
    private val repository: GenRepositoryImpl_690_
) : GenUseCase_690_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_690_ @Inject constructor(
    private val repository: GenRepositoryImpl_690_
) : GenUseCase_690_<String, List<GenModel_690_>> {
    override suspend fun invoke(params: String): List<GenModel_690_> = repository.search(params)
}

abstract class GenMapper_690_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_690_ : GenMapper_690_<GenModel_690_, String>() {
    override fun map(input: GenModel_690_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_690_ : GenMapper_690_<String, GenModel_690_>() {
    override fun map(input: String): GenModel_690_ {
        val parts = input.split(":")
        return GenModel_690_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_690_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_690_,
    private val saveUseCase: GenSaveUseCase_690_,
    private val deleteUseCase: GenDeleteUseCase_690_,
    private val searchUseCase: GenSearchUseCase_690_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_690_>(GenState_690_.Idle)
    val state: StateFlow<GenState_690_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_690_) {
        when (event) {
            is GenEvent_690_.Load -> loadAll()
            is GenEvent_690_.Update -> save(event.model)
            is GenEvent_690_.Delete -> delete(event.id)
            is GenEvent_690_.Refresh -> loadAll()
            is GenEvent_690_.Search -> search(event.query)
            is GenEvent_690_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_690_.Loading; _state.value = GenState_690_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_690_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_690_.Success(searchUseCase(query)) } }
}
