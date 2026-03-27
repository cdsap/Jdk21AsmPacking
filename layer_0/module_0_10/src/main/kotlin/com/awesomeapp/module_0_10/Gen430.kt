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

data class GenModel_430_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_430_ {
    data class Load(val id: Long) : GenEvent_430_()
    data class Update(val model: GenModel_430_) : GenEvent_430_()
    data class Delete(val id: Long) : GenEvent_430_()
    data object Refresh : GenEvent_430_()
    data class Search(val query: String) : GenEvent_430_()
    data class Filter(val predicate: String) : GenEvent_430_()
}

sealed class GenState_430_ {
    data object Idle : GenState_430_()
    data object Loading : GenState_430_()
    data class Success(val items: List<GenModel_430_>) : GenState_430_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_430_()
    data class Partial(val items: List<GenModel_430_>, val hasMore: Boolean) : GenState_430_()
}

interface GenRepository_430_ {
    suspend fun getAll(): List<GenModel_430_>
    suspend fun getById(id: Long): GenModel_430_?
    suspend fun save(model: GenModel_430_): GenModel_430_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_430_>
}

@Singleton
class GenRepositoryImpl_430_ @Inject constructor() : GenRepository_430_ {
    private val store = mutableMapOf<Long, GenModel_430_>()
    override suspend fun getAll(): List<GenModel_430_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_430_? = store[id]
    override suspend fun save(model: GenModel_430_): GenModel_430_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_430_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_430_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_430_ @Inject constructor(
    private val repository: GenRepositoryImpl_430_
) : GenUseCase_430_<Unit, List<GenModel_430_>> {
    override suspend fun invoke(params: Unit): List<GenModel_430_> = repository.getAll()
}

class GenSaveUseCase_430_ @Inject constructor(
    private val repository: GenRepositoryImpl_430_
) : GenUseCase_430_<GenModel_430_, GenModel_430_> {
    override suspend fun invoke(params: GenModel_430_): GenModel_430_ = repository.save(params)
}

class GenDeleteUseCase_430_ @Inject constructor(
    private val repository: GenRepositoryImpl_430_
) : GenUseCase_430_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_430_ @Inject constructor(
    private val repository: GenRepositoryImpl_430_
) : GenUseCase_430_<String, List<GenModel_430_>> {
    override suspend fun invoke(params: String): List<GenModel_430_> = repository.search(params)
}

abstract class GenMapper_430_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_430_ : GenMapper_430_<GenModel_430_, String>() {
    override fun map(input: GenModel_430_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_430_ : GenMapper_430_<String, GenModel_430_>() {
    override fun map(input: String): GenModel_430_ {
        val parts = input.split(":")
        return GenModel_430_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_430_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_430_,
    private val saveUseCase: GenSaveUseCase_430_,
    private val deleteUseCase: GenDeleteUseCase_430_,
    private val searchUseCase: GenSearchUseCase_430_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_430_>(GenState_430_.Idle)
    val state: StateFlow<GenState_430_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_430_) {
        when (event) {
            is GenEvent_430_.Load -> loadAll()
            is GenEvent_430_.Update -> save(event.model)
            is GenEvent_430_.Delete -> delete(event.id)
            is GenEvent_430_.Refresh -> loadAll()
            is GenEvent_430_.Search -> search(event.query)
            is GenEvent_430_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_430_.Loading; _state.value = GenState_430_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_430_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_430_.Success(searchUseCase(query)) } }
}
