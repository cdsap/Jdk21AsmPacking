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

data class GenModel_1452_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1452_ {
    data class Load(val id: Long) : GenEvent_1452_()
    data class Update(val model: GenModel_1452_) : GenEvent_1452_()
    data class Delete(val id: Long) : GenEvent_1452_()
    data object Refresh : GenEvent_1452_()
    data class Search(val query: String) : GenEvent_1452_()
    data class Filter(val predicate: String) : GenEvent_1452_()
}

sealed class GenState_1452_ {
    data object Idle : GenState_1452_()
    data object Loading : GenState_1452_()
    data class Success(val items: List<GenModel_1452_>) : GenState_1452_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1452_()
    data class Partial(val items: List<GenModel_1452_>, val hasMore: Boolean) : GenState_1452_()
}

interface GenRepository_1452_ {
    suspend fun getAll(): List<GenModel_1452_>
    suspend fun getById(id: Long): GenModel_1452_?
    suspend fun save(model: GenModel_1452_): GenModel_1452_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1452_>
}

@Singleton
class GenRepositoryImpl_1452_ @Inject constructor() : GenRepository_1452_ {
    private val store = mutableMapOf<Long, GenModel_1452_>()
    override suspend fun getAll(): List<GenModel_1452_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1452_? = store[id]
    override suspend fun save(model: GenModel_1452_): GenModel_1452_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1452_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1452_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1452_ @Inject constructor(
    private val repository: GenRepositoryImpl_1452_
) : GenUseCase_1452_<Unit, List<GenModel_1452_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1452_> = repository.getAll()
}

class GenSaveUseCase_1452_ @Inject constructor(
    private val repository: GenRepositoryImpl_1452_
) : GenUseCase_1452_<GenModel_1452_, GenModel_1452_> {
    override suspend fun invoke(params: GenModel_1452_): GenModel_1452_ = repository.save(params)
}

class GenDeleteUseCase_1452_ @Inject constructor(
    private val repository: GenRepositoryImpl_1452_
) : GenUseCase_1452_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1452_ @Inject constructor(
    private val repository: GenRepositoryImpl_1452_
) : GenUseCase_1452_<String, List<GenModel_1452_>> {
    override suspend fun invoke(params: String): List<GenModel_1452_> = repository.search(params)
}

abstract class GenMapper_1452_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1452_ : GenMapper_1452_<GenModel_1452_, String>() {
    override fun map(input: GenModel_1452_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1452_ : GenMapper_1452_<String, GenModel_1452_>() {
    override fun map(input: String): GenModel_1452_ {
        val parts = input.split(":")
        return GenModel_1452_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1452_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1452_,
    private val saveUseCase: GenSaveUseCase_1452_,
    private val deleteUseCase: GenDeleteUseCase_1452_,
    private val searchUseCase: GenSearchUseCase_1452_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1452_>(GenState_1452_.Idle)
    val state: StateFlow<GenState_1452_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1452_) {
        when (event) {
            is GenEvent_1452_.Load -> loadAll()
            is GenEvent_1452_.Update -> save(event.model)
            is GenEvent_1452_.Delete -> delete(event.id)
            is GenEvent_1452_.Refresh -> loadAll()
            is GenEvent_1452_.Search -> search(event.query)
            is GenEvent_1452_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1452_.Loading; _state.value = GenState_1452_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1452_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1452_.Success(searchUseCase(query)) } }
}
