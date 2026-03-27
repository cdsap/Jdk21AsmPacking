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

data class GenModel_1923_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1923_ {
    data class Load(val id: Long) : GenEvent_1923_()
    data class Update(val model: GenModel_1923_) : GenEvent_1923_()
    data class Delete(val id: Long) : GenEvent_1923_()
    data object Refresh : GenEvent_1923_()
    data class Search(val query: String) : GenEvent_1923_()
    data class Filter(val predicate: String) : GenEvent_1923_()
}

sealed class GenState_1923_ {
    data object Idle : GenState_1923_()
    data object Loading : GenState_1923_()
    data class Success(val items: List<GenModel_1923_>) : GenState_1923_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1923_()
    data class Partial(val items: List<GenModel_1923_>, val hasMore: Boolean) : GenState_1923_()
}

interface GenRepository_1923_ {
    suspend fun getAll(): List<GenModel_1923_>
    suspend fun getById(id: Long): GenModel_1923_?
    suspend fun save(model: GenModel_1923_): GenModel_1923_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1923_>
}

@Singleton
class GenRepositoryImpl_1923_ @Inject constructor() : GenRepository_1923_ {
    private val store = mutableMapOf<Long, GenModel_1923_>()
    override suspend fun getAll(): List<GenModel_1923_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1923_? = store[id]
    override suspend fun save(model: GenModel_1923_): GenModel_1923_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1923_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1923_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1923_ @Inject constructor(
    private val repository: GenRepositoryImpl_1923_
) : GenUseCase_1923_<Unit, List<GenModel_1923_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1923_> = repository.getAll()
}

class GenSaveUseCase_1923_ @Inject constructor(
    private val repository: GenRepositoryImpl_1923_
) : GenUseCase_1923_<GenModel_1923_, GenModel_1923_> {
    override suspend fun invoke(params: GenModel_1923_): GenModel_1923_ = repository.save(params)
}

class GenDeleteUseCase_1923_ @Inject constructor(
    private val repository: GenRepositoryImpl_1923_
) : GenUseCase_1923_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1923_ @Inject constructor(
    private val repository: GenRepositoryImpl_1923_
) : GenUseCase_1923_<String, List<GenModel_1923_>> {
    override suspend fun invoke(params: String): List<GenModel_1923_> = repository.search(params)
}

abstract class GenMapper_1923_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1923_ : GenMapper_1923_<GenModel_1923_, String>() {
    override fun map(input: GenModel_1923_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1923_ : GenMapper_1923_<String, GenModel_1923_>() {
    override fun map(input: String): GenModel_1923_ {
        val parts = input.split(":")
        return GenModel_1923_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1923_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1923_,
    private val saveUseCase: GenSaveUseCase_1923_,
    private val deleteUseCase: GenDeleteUseCase_1923_,
    private val searchUseCase: GenSearchUseCase_1923_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1923_>(GenState_1923_.Idle)
    val state: StateFlow<GenState_1923_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1923_) {
        when (event) {
            is GenEvent_1923_.Load -> loadAll()
            is GenEvent_1923_.Update -> save(event.model)
            is GenEvent_1923_.Delete -> delete(event.id)
            is GenEvent_1923_.Refresh -> loadAll()
            is GenEvent_1923_.Search -> search(event.query)
            is GenEvent_1923_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1923_.Loading; _state.value = GenState_1923_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1923_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1923_.Success(searchUseCase(query)) } }
}
