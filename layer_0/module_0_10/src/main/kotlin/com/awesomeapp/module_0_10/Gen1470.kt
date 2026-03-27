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

data class GenModel_1470_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1470_ {
    data class Load(val id: Long) : GenEvent_1470_()
    data class Update(val model: GenModel_1470_) : GenEvent_1470_()
    data class Delete(val id: Long) : GenEvent_1470_()
    data object Refresh : GenEvent_1470_()
    data class Search(val query: String) : GenEvent_1470_()
    data class Filter(val predicate: String) : GenEvent_1470_()
}

sealed class GenState_1470_ {
    data object Idle : GenState_1470_()
    data object Loading : GenState_1470_()
    data class Success(val items: List<GenModel_1470_>) : GenState_1470_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1470_()
    data class Partial(val items: List<GenModel_1470_>, val hasMore: Boolean) : GenState_1470_()
}

interface GenRepository_1470_ {
    suspend fun getAll(): List<GenModel_1470_>
    suspend fun getById(id: Long): GenModel_1470_?
    suspend fun save(model: GenModel_1470_): GenModel_1470_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1470_>
}

@Singleton
class GenRepositoryImpl_1470_ @Inject constructor() : GenRepository_1470_ {
    private val store = mutableMapOf<Long, GenModel_1470_>()
    override suspend fun getAll(): List<GenModel_1470_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1470_? = store[id]
    override suspend fun save(model: GenModel_1470_): GenModel_1470_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1470_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1470_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1470_ @Inject constructor(
    private val repository: GenRepositoryImpl_1470_
) : GenUseCase_1470_<Unit, List<GenModel_1470_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1470_> = repository.getAll()
}

class GenSaveUseCase_1470_ @Inject constructor(
    private val repository: GenRepositoryImpl_1470_
) : GenUseCase_1470_<GenModel_1470_, GenModel_1470_> {
    override suspend fun invoke(params: GenModel_1470_): GenModel_1470_ = repository.save(params)
}

class GenDeleteUseCase_1470_ @Inject constructor(
    private val repository: GenRepositoryImpl_1470_
) : GenUseCase_1470_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1470_ @Inject constructor(
    private val repository: GenRepositoryImpl_1470_
) : GenUseCase_1470_<String, List<GenModel_1470_>> {
    override suspend fun invoke(params: String): List<GenModel_1470_> = repository.search(params)
}

abstract class GenMapper_1470_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1470_ : GenMapper_1470_<GenModel_1470_, String>() {
    override fun map(input: GenModel_1470_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1470_ : GenMapper_1470_<String, GenModel_1470_>() {
    override fun map(input: String): GenModel_1470_ {
        val parts = input.split(":")
        return GenModel_1470_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1470_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1470_,
    private val saveUseCase: GenSaveUseCase_1470_,
    private val deleteUseCase: GenDeleteUseCase_1470_,
    private val searchUseCase: GenSearchUseCase_1470_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1470_>(GenState_1470_.Idle)
    val state: StateFlow<GenState_1470_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1470_) {
        when (event) {
            is GenEvent_1470_.Load -> loadAll()
            is GenEvent_1470_.Update -> save(event.model)
            is GenEvent_1470_.Delete -> delete(event.id)
            is GenEvent_1470_.Refresh -> loadAll()
            is GenEvent_1470_.Search -> search(event.query)
            is GenEvent_1470_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1470_.Loading; _state.value = GenState_1470_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1470_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1470_.Success(searchUseCase(query)) } }
}
