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

data class GenModel_452_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_452_ {
    data class Load(val id: Long) : GenEvent_452_()
    data class Update(val model: GenModel_452_) : GenEvent_452_()
    data class Delete(val id: Long) : GenEvent_452_()
    data object Refresh : GenEvent_452_()
    data class Search(val query: String) : GenEvent_452_()
    data class Filter(val predicate: String) : GenEvent_452_()
}

sealed class GenState_452_ {
    data object Idle : GenState_452_()
    data object Loading : GenState_452_()
    data class Success(val items: List<GenModel_452_>) : GenState_452_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_452_()
    data class Partial(val items: List<GenModel_452_>, val hasMore: Boolean) : GenState_452_()
}

interface GenRepository_452_ {
    suspend fun getAll(): List<GenModel_452_>
    suspend fun getById(id: Long): GenModel_452_?
    suspend fun save(model: GenModel_452_): GenModel_452_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_452_>
}

@Singleton
class GenRepositoryImpl_452_ @Inject constructor() : GenRepository_452_ {
    private val store = mutableMapOf<Long, GenModel_452_>()
    override suspend fun getAll(): List<GenModel_452_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_452_? = store[id]
    override suspend fun save(model: GenModel_452_): GenModel_452_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_452_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_452_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_452_ @Inject constructor(
    private val repository: GenRepositoryImpl_452_
) : GenUseCase_452_<Unit, List<GenModel_452_>> {
    override suspend fun invoke(params: Unit): List<GenModel_452_> = repository.getAll()
}

class GenSaveUseCase_452_ @Inject constructor(
    private val repository: GenRepositoryImpl_452_
) : GenUseCase_452_<GenModel_452_, GenModel_452_> {
    override suspend fun invoke(params: GenModel_452_): GenModel_452_ = repository.save(params)
}

class GenDeleteUseCase_452_ @Inject constructor(
    private val repository: GenRepositoryImpl_452_
) : GenUseCase_452_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_452_ @Inject constructor(
    private val repository: GenRepositoryImpl_452_
) : GenUseCase_452_<String, List<GenModel_452_>> {
    override suspend fun invoke(params: String): List<GenModel_452_> = repository.search(params)
}

abstract class GenMapper_452_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_452_ : GenMapper_452_<GenModel_452_, String>() {
    override fun map(input: GenModel_452_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_452_ : GenMapper_452_<String, GenModel_452_>() {
    override fun map(input: String): GenModel_452_ {
        val parts = input.split(":")
        return GenModel_452_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_452_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_452_,
    private val saveUseCase: GenSaveUseCase_452_,
    private val deleteUseCase: GenDeleteUseCase_452_,
    private val searchUseCase: GenSearchUseCase_452_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_452_>(GenState_452_.Idle)
    val state: StateFlow<GenState_452_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_452_) {
        when (event) {
            is GenEvent_452_.Load -> loadAll()
            is GenEvent_452_.Update -> save(event.model)
            is GenEvent_452_.Delete -> delete(event.id)
            is GenEvent_452_.Refresh -> loadAll()
            is GenEvent_452_.Search -> search(event.query)
            is GenEvent_452_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_452_.Loading; _state.value = GenState_452_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_452_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_452_.Success(searchUseCase(query)) } }
}
