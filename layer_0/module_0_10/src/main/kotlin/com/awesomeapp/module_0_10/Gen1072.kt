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

data class GenModel_1072_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1072_ {
    data class Load(val id: Long) : GenEvent_1072_()
    data class Update(val model: GenModel_1072_) : GenEvent_1072_()
    data class Delete(val id: Long) : GenEvent_1072_()
    data object Refresh : GenEvent_1072_()
    data class Search(val query: String) : GenEvent_1072_()
    data class Filter(val predicate: String) : GenEvent_1072_()
}

sealed class GenState_1072_ {
    data object Idle : GenState_1072_()
    data object Loading : GenState_1072_()
    data class Success(val items: List<GenModel_1072_>) : GenState_1072_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1072_()
    data class Partial(val items: List<GenModel_1072_>, val hasMore: Boolean) : GenState_1072_()
}

interface GenRepository_1072_ {
    suspend fun getAll(): List<GenModel_1072_>
    suspend fun getById(id: Long): GenModel_1072_?
    suspend fun save(model: GenModel_1072_): GenModel_1072_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1072_>
}

@Singleton
class GenRepositoryImpl_1072_ @Inject constructor() : GenRepository_1072_ {
    private val store = mutableMapOf<Long, GenModel_1072_>()
    override suspend fun getAll(): List<GenModel_1072_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1072_? = store[id]
    override suspend fun save(model: GenModel_1072_): GenModel_1072_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1072_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1072_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1072_ @Inject constructor(
    private val repository: GenRepositoryImpl_1072_
) : GenUseCase_1072_<Unit, List<GenModel_1072_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1072_> = repository.getAll()
}

class GenSaveUseCase_1072_ @Inject constructor(
    private val repository: GenRepositoryImpl_1072_
) : GenUseCase_1072_<GenModel_1072_, GenModel_1072_> {
    override suspend fun invoke(params: GenModel_1072_): GenModel_1072_ = repository.save(params)
}

class GenDeleteUseCase_1072_ @Inject constructor(
    private val repository: GenRepositoryImpl_1072_
) : GenUseCase_1072_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1072_ @Inject constructor(
    private val repository: GenRepositoryImpl_1072_
) : GenUseCase_1072_<String, List<GenModel_1072_>> {
    override suspend fun invoke(params: String): List<GenModel_1072_> = repository.search(params)
}

abstract class GenMapper_1072_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1072_ : GenMapper_1072_<GenModel_1072_, String>() {
    override fun map(input: GenModel_1072_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1072_ : GenMapper_1072_<String, GenModel_1072_>() {
    override fun map(input: String): GenModel_1072_ {
        val parts = input.split(":")
        return GenModel_1072_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1072_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1072_,
    private val saveUseCase: GenSaveUseCase_1072_,
    private val deleteUseCase: GenDeleteUseCase_1072_,
    private val searchUseCase: GenSearchUseCase_1072_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1072_>(GenState_1072_.Idle)
    val state: StateFlow<GenState_1072_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1072_) {
        when (event) {
            is GenEvent_1072_.Load -> loadAll()
            is GenEvent_1072_.Update -> save(event.model)
            is GenEvent_1072_.Delete -> delete(event.id)
            is GenEvent_1072_.Refresh -> loadAll()
            is GenEvent_1072_.Search -> search(event.query)
            is GenEvent_1072_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1072_.Loading; _state.value = GenState_1072_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1072_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1072_.Success(searchUseCase(query)) } }
}
