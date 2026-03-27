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

data class GenModel_1644_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1644_ {
    data class Load(val id: Long) : GenEvent_1644_()
    data class Update(val model: GenModel_1644_) : GenEvent_1644_()
    data class Delete(val id: Long) : GenEvent_1644_()
    data object Refresh : GenEvent_1644_()
    data class Search(val query: String) : GenEvent_1644_()
    data class Filter(val predicate: String) : GenEvent_1644_()
}

sealed class GenState_1644_ {
    data object Idle : GenState_1644_()
    data object Loading : GenState_1644_()
    data class Success(val items: List<GenModel_1644_>) : GenState_1644_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1644_()
    data class Partial(val items: List<GenModel_1644_>, val hasMore: Boolean) : GenState_1644_()
}

interface GenRepository_1644_ {
    suspend fun getAll(): List<GenModel_1644_>
    suspend fun getById(id: Long): GenModel_1644_?
    suspend fun save(model: GenModel_1644_): GenModel_1644_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1644_>
}

@Singleton
class GenRepositoryImpl_1644_ @Inject constructor() : GenRepository_1644_ {
    private val store = mutableMapOf<Long, GenModel_1644_>()
    override suspend fun getAll(): List<GenModel_1644_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1644_? = store[id]
    override suspend fun save(model: GenModel_1644_): GenModel_1644_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1644_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1644_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1644_ @Inject constructor(
    private val repository: GenRepositoryImpl_1644_
) : GenUseCase_1644_<Unit, List<GenModel_1644_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1644_> = repository.getAll()
}

class GenSaveUseCase_1644_ @Inject constructor(
    private val repository: GenRepositoryImpl_1644_
) : GenUseCase_1644_<GenModel_1644_, GenModel_1644_> {
    override suspend fun invoke(params: GenModel_1644_): GenModel_1644_ = repository.save(params)
}

class GenDeleteUseCase_1644_ @Inject constructor(
    private val repository: GenRepositoryImpl_1644_
) : GenUseCase_1644_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1644_ @Inject constructor(
    private val repository: GenRepositoryImpl_1644_
) : GenUseCase_1644_<String, List<GenModel_1644_>> {
    override suspend fun invoke(params: String): List<GenModel_1644_> = repository.search(params)
}

abstract class GenMapper_1644_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1644_ : GenMapper_1644_<GenModel_1644_, String>() {
    override fun map(input: GenModel_1644_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1644_ : GenMapper_1644_<String, GenModel_1644_>() {
    override fun map(input: String): GenModel_1644_ {
        val parts = input.split(":")
        return GenModel_1644_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1644_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1644_,
    private val saveUseCase: GenSaveUseCase_1644_,
    private val deleteUseCase: GenDeleteUseCase_1644_,
    private val searchUseCase: GenSearchUseCase_1644_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1644_>(GenState_1644_.Idle)
    val state: StateFlow<GenState_1644_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1644_) {
        when (event) {
            is GenEvent_1644_.Load -> loadAll()
            is GenEvent_1644_.Update -> save(event.model)
            is GenEvent_1644_.Delete -> delete(event.id)
            is GenEvent_1644_.Refresh -> loadAll()
            is GenEvent_1644_.Search -> search(event.query)
            is GenEvent_1644_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1644_.Loading; _state.value = GenState_1644_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1644_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1644_.Success(searchUseCase(query)) } }
}
