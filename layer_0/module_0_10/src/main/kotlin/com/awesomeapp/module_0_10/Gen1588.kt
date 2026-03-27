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

data class GenModel_1588_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1588_ {
    data class Load(val id: Long) : GenEvent_1588_()
    data class Update(val model: GenModel_1588_) : GenEvent_1588_()
    data class Delete(val id: Long) : GenEvent_1588_()
    data object Refresh : GenEvent_1588_()
    data class Search(val query: String) : GenEvent_1588_()
    data class Filter(val predicate: String) : GenEvent_1588_()
}

sealed class GenState_1588_ {
    data object Idle : GenState_1588_()
    data object Loading : GenState_1588_()
    data class Success(val items: List<GenModel_1588_>) : GenState_1588_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1588_()
    data class Partial(val items: List<GenModel_1588_>, val hasMore: Boolean) : GenState_1588_()
}

interface GenRepository_1588_ {
    suspend fun getAll(): List<GenModel_1588_>
    suspend fun getById(id: Long): GenModel_1588_?
    suspend fun save(model: GenModel_1588_): GenModel_1588_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1588_>
}

@Singleton
class GenRepositoryImpl_1588_ @Inject constructor() : GenRepository_1588_ {
    private val store = mutableMapOf<Long, GenModel_1588_>()
    override suspend fun getAll(): List<GenModel_1588_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1588_? = store[id]
    override suspend fun save(model: GenModel_1588_): GenModel_1588_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1588_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1588_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1588_ @Inject constructor(
    private val repository: GenRepositoryImpl_1588_
) : GenUseCase_1588_<Unit, List<GenModel_1588_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1588_> = repository.getAll()
}

class GenSaveUseCase_1588_ @Inject constructor(
    private val repository: GenRepositoryImpl_1588_
) : GenUseCase_1588_<GenModel_1588_, GenModel_1588_> {
    override suspend fun invoke(params: GenModel_1588_): GenModel_1588_ = repository.save(params)
}

class GenDeleteUseCase_1588_ @Inject constructor(
    private val repository: GenRepositoryImpl_1588_
) : GenUseCase_1588_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1588_ @Inject constructor(
    private val repository: GenRepositoryImpl_1588_
) : GenUseCase_1588_<String, List<GenModel_1588_>> {
    override suspend fun invoke(params: String): List<GenModel_1588_> = repository.search(params)
}

abstract class GenMapper_1588_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1588_ : GenMapper_1588_<GenModel_1588_, String>() {
    override fun map(input: GenModel_1588_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1588_ : GenMapper_1588_<String, GenModel_1588_>() {
    override fun map(input: String): GenModel_1588_ {
        val parts = input.split(":")
        return GenModel_1588_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1588_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1588_,
    private val saveUseCase: GenSaveUseCase_1588_,
    private val deleteUseCase: GenDeleteUseCase_1588_,
    private val searchUseCase: GenSearchUseCase_1588_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1588_>(GenState_1588_.Idle)
    val state: StateFlow<GenState_1588_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1588_) {
        when (event) {
            is GenEvent_1588_.Load -> loadAll()
            is GenEvent_1588_.Update -> save(event.model)
            is GenEvent_1588_.Delete -> delete(event.id)
            is GenEvent_1588_.Refresh -> loadAll()
            is GenEvent_1588_.Search -> search(event.query)
            is GenEvent_1588_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1588_.Loading; _state.value = GenState_1588_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1588_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1588_.Success(searchUseCase(query)) } }
}
