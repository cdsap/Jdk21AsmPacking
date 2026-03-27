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

data class GenModel_199_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_199_ {
    data class Load(val id: Long) : GenEvent_199_()
    data class Update(val model: GenModel_199_) : GenEvent_199_()
    data class Delete(val id: Long) : GenEvent_199_()
    data object Refresh : GenEvent_199_()
    data class Search(val query: String) : GenEvent_199_()
    data class Filter(val predicate: String) : GenEvent_199_()
}

sealed class GenState_199_ {
    data object Idle : GenState_199_()
    data object Loading : GenState_199_()
    data class Success(val items: List<GenModel_199_>) : GenState_199_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_199_()
    data class Partial(val items: List<GenModel_199_>, val hasMore: Boolean) : GenState_199_()
}

interface GenRepository_199_ {
    suspend fun getAll(): List<GenModel_199_>
    suspend fun getById(id: Long): GenModel_199_?
    suspend fun save(model: GenModel_199_): GenModel_199_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_199_>
}

@Singleton
class GenRepositoryImpl_199_ @Inject constructor() : GenRepository_199_ {
    private val store = mutableMapOf<Long, GenModel_199_>()
    override suspend fun getAll(): List<GenModel_199_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_199_? = store[id]
    override suspend fun save(model: GenModel_199_): GenModel_199_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_199_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_199_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_199_ @Inject constructor(
    private val repository: GenRepositoryImpl_199_
) : GenUseCase_199_<Unit, List<GenModel_199_>> {
    override suspend fun invoke(params: Unit): List<GenModel_199_> = repository.getAll()
}

class GenSaveUseCase_199_ @Inject constructor(
    private val repository: GenRepositoryImpl_199_
) : GenUseCase_199_<GenModel_199_, GenModel_199_> {
    override suspend fun invoke(params: GenModel_199_): GenModel_199_ = repository.save(params)
}

class GenDeleteUseCase_199_ @Inject constructor(
    private val repository: GenRepositoryImpl_199_
) : GenUseCase_199_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_199_ @Inject constructor(
    private val repository: GenRepositoryImpl_199_
) : GenUseCase_199_<String, List<GenModel_199_>> {
    override suspend fun invoke(params: String): List<GenModel_199_> = repository.search(params)
}

abstract class GenMapper_199_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_199_ : GenMapper_199_<GenModel_199_, String>() {
    override fun map(input: GenModel_199_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_199_ : GenMapper_199_<String, GenModel_199_>() {
    override fun map(input: String): GenModel_199_ {
        val parts = input.split(":")
        return GenModel_199_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_199_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_199_,
    private val saveUseCase: GenSaveUseCase_199_,
    private val deleteUseCase: GenDeleteUseCase_199_,
    private val searchUseCase: GenSearchUseCase_199_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_199_>(GenState_199_.Idle)
    val state: StateFlow<GenState_199_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_199_) {
        when (event) {
            is GenEvent_199_.Load -> loadAll()
            is GenEvent_199_.Update -> save(event.model)
            is GenEvent_199_.Delete -> delete(event.id)
            is GenEvent_199_.Refresh -> loadAll()
            is GenEvent_199_.Search -> search(event.query)
            is GenEvent_199_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_199_.Loading; _state.value = GenState_199_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_199_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_199_.Success(searchUseCase(query)) } }
}
