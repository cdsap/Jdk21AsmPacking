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

data class GenModel_881_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_881_ {
    data class Load(val id: Long) : GenEvent_881_()
    data class Update(val model: GenModel_881_) : GenEvent_881_()
    data class Delete(val id: Long) : GenEvent_881_()
    data object Refresh : GenEvent_881_()
    data class Search(val query: String) : GenEvent_881_()
    data class Filter(val predicate: String) : GenEvent_881_()
}

sealed class GenState_881_ {
    data object Idle : GenState_881_()
    data object Loading : GenState_881_()
    data class Success(val items: List<GenModel_881_>) : GenState_881_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_881_()
    data class Partial(val items: List<GenModel_881_>, val hasMore: Boolean) : GenState_881_()
}

interface GenRepository_881_ {
    suspend fun getAll(): List<GenModel_881_>
    suspend fun getById(id: Long): GenModel_881_?
    suspend fun save(model: GenModel_881_): GenModel_881_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_881_>
}

@Singleton
class GenRepositoryImpl_881_ @Inject constructor() : GenRepository_881_ {
    private val store = mutableMapOf<Long, GenModel_881_>()
    override suspend fun getAll(): List<GenModel_881_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_881_? = store[id]
    override suspend fun save(model: GenModel_881_): GenModel_881_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_881_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_881_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_881_ @Inject constructor(
    private val repository: GenRepositoryImpl_881_
) : GenUseCase_881_<Unit, List<GenModel_881_>> {
    override suspend fun invoke(params: Unit): List<GenModel_881_> = repository.getAll()
}

class GenSaveUseCase_881_ @Inject constructor(
    private val repository: GenRepositoryImpl_881_
) : GenUseCase_881_<GenModel_881_, GenModel_881_> {
    override suspend fun invoke(params: GenModel_881_): GenModel_881_ = repository.save(params)
}

class GenDeleteUseCase_881_ @Inject constructor(
    private val repository: GenRepositoryImpl_881_
) : GenUseCase_881_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_881_ @Inject constructor(
    private val repository: GenRepositoryImpl_881_
) : GenUseCase_881_<String, List<GenModel_881_>> {
    override suspend fun invoke(params: String): List<GenModel_881_> = repository.search(params)
}

abstract class GenMapper_881_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_881_ : GenMapper_881_<GenModel_881_, String>() {
    override fun map(input: GenModel_881_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_881_ : GenMapper_881_<String, GenModel_881_>() {
    override fun map(input: String): GenModel_881_ {
        val parts = input.split(":")
        return GenModel_881_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_881_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_881_,
    private val saveUseCase: GenSaveUseCase_881_,
    private val deleteUseCase: GenDeleteUseCase_881_,
    private val searchUseCase: GenSearchUseCase_881_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_881_>(GenState_881_.Idle)
    val state: StateFlow<GenState_881_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_881_) {
        when (event) {
            is GenEvent_881_.Load -> loadAll()
            is GenEvent_881_.Update -> save(event.model)
            is GenEvent_881_.Delete -> delete(event.id)
            is GenEvent_881_.Refresh -> loadAll()
            is GenEvent_881_.Search -> search(event.query)
            is GenEvent_881_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_881_.Loading; _state.value = GenState_881_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_881_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_881_.Success(searchUseCase(query)) } }
}
