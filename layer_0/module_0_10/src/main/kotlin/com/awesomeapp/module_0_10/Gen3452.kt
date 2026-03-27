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

data class GenModel_3452_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3452_ {
    data class Load(val id: Long) : GenEvent_3452_()
    data class Update(val model: GenModel_3452_) : GenEvent_3452_()
    data class Delete(val id: Long) : GenEvent_3452_()
    data object Refresh : GenEvent_3452_()
    data class Search(val query: String) : GenEvent_3452_()
    data class Filter(val predicate: String) : GenEvent_3452_()
}

sealed class GenState_3452_ {
    data object Idle : GenState_3452_()
    data object Loading : GenState_3452_()
    data class Success(val items: List<GenModel_3452_>) : GenState_3452_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3452_()
    data class Partial(val items: List<GenModel_3452_>, val hasMore: Boolean) : GenState_3452_()
}

interface GenRepository_3452_ {
    suspend fun getAll(): List<GenModel_3452_>
    suspend fun getById(id: Long): GenModel_3452_?
    suspend fun save(model: GenModel_3452_): GenModel_3452_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3452_>
}

@Singleton
class GenRepositoryImpl_3452_ @Inject constructor() : GenRepository_3452_ {
    private val store = mutableMapOf<Long, GenModel_3452_>()
    override suspend fun getAll(): List<GenModel_3452_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3452_? = store[id]
    override suspend fun save(model: GenModel_3452_): GenModel_3452_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3452_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3452_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3452_ @Inject constructor(
    private val repository: GenRepositoryImpl_3452_
) : GenUseCase_3452_<Unit, List<GenModel_3452_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3452_> = repository.getAll()
}

class GenSaveUseCase_3452_ @Inject constructor(
    private val repository: GenRepositoryImpl_3452_
) : GenUseCase_3452_<GenModel_3452_, GenModel_3452_> {
    override suspend fun invoke(params: GenModel_3452_): GenModel_3452_ = repository.save(params)
}

class GenDeleteUseCase_3452_ @Inject constructor(
    private val repository: GenRepositoryImpl_3452_
) : GenUseCase_3452_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3452_ @Inject constructor(
    private val repository: GenRepositoryImpl_3452_
) : GenUseCase_3452_<String, List<GenModel_3452_>> {
    override suspend fun invoke(params: String): List<GenModel_3452_> = repository.search(params)
}

abstract class GenMapper_3452_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3452_ : GenMapper_3452_<GenModel_3452_, String>() {
    override fun map(input: GenModel_3452_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3452_ : GenMapper_3452_<String, GenModel_3452_>() {
    override fun map(input: String): GenModel_3452_ {
        val parts = input.split(":")
        return GenModel_3452_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3452_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3452_,
    private val saveUseCase: GenSaveUseCase_3452_,
    private val deleteUseCase: GenDeleteUseCase_3452_,
    private val searchUseCase: GenSearchUseCase_3452_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3452_>(GenState_3452_.Idle)
    val state: StateFlow<GenState_3452_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3452_) {
        when (event) {
            is GenEvent_3452_.Load -> loadAll()
            is GenEvent_3452_.Update -> save(event.model)
            is GenEvent_3452_.Delete -> delete(event.id)
            is GenEvent_3452_.Refresh -> loadAll()
            is GenEvent_3452_.Search -> search(event.query)
            is GenEvent_3452_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3452_.Loading; _state.value = GenState_3452_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3452_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3452_.Success(searchUseCase(query)) } }
}
