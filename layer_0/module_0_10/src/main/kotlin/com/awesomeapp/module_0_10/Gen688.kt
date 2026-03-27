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

data class GenModel_688_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_688_ {
    data class Load(val id: Long) : GenEvent_688_()
    data class Update(val model: GenModel_688_) : GenEvent_688_()
    data class Delete(val id: Long) : GenEvent_688_()
    data object Refresh : GenEvent_688_()
    data class Search(val query: String) : GenEvent_688_()
    data class Filter(val predicate: String) : GenEvent_688_()
}

sealed class GenState_688_ {
    data object Idle : GenState_688_()
    data object Loading : GenState_688_()
    data class Success(val items: List<GenModel_688_>) : GenState_688_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_688_()
    data class Partial(val items: List<GenModel_688_>, val hasMore: Boolean) : GenState_688_()
}

interface GenRepository_688_ {
    suspend fun getAll(): List<GenModel_688_>
    suspend fun getById(id: Long): GenModel_688_?
    suspend fun save(model: GenModel_688_): GenModel_688_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_688_>
}

@Singleton
class GenRepositoryImpl_688_ @Inject constructor() : GenRepository_688_ {
    private val store = mutableMapOf<Long, GenModel_688_>()
    override suspend fun getAll(): List<GenModel_688_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_688_? = store[id]
    override suspend fun save(model: GenModel_688_): GenModel_688_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_688_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_688_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_688_ @Inject constructor(
    private val repository: GenRepositoryImpl_688_
) : GenUseCase_688_<Unit, List<GenModel_688_>> {
    override suspend fun invoke(params: Unit): List<GenModel_688_> = repository.getAll()
}

class GenSaveUseCase_688_ @Inject constructor(
    private val repository: GenRepositoryImpl_688_
) : GenUseCase_688_<GenModel_688_, GenModel_688_> {
    override suspend fun invoke(params: GenModel_688_): GenModel_688_ = repository.save(params)
}

class GenDeleteUseCase_688_ @Inject constructor(
    private val repository: GenRepositoryImpl_688_
) : GenUseCase_688_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_688_ @Inject constructor(
    private val repository: GenRepositoryImpl_688_
) : GenUseCase_688_<String, List<GenModel_688_>> {
    override suspend fun invoke(params: String): List<GenModel_688_> = repository.search(params)
}

abstract class GenMapper_688_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_688_ : GenMapper_688_<GenModel_688_, String>() {
    override fun map(input: GenModel_688_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_688_ : GenMapper_688_<String, GenModel_688_>() {
    override fun map(input: String): GenModel_688_ {
        val parts = input.split(":")
        return GenModel_688_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_688_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_688_,
    private val saveUseCase: GenSaveUseCase_688_,
    private val deleteUseCase: GenDeleteUseCase_688_,
    private val searchUseCase: GenSearchUseCase_688_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_688_>(GenState_688_.Idle)
    val state: StateFlow<GenState_688_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_688_) {
        when (event) {
            is GenEvent_688_.Load -> loadAll()
            is GenEvent_688_.Update -> save(event.model)
            is GenEvent_688_.Delete -> delete(event.id)
            is GenEvent_688_.Refresh -> loadAll()
            is GenEvent_688_.Search -> search(event.query)
            is GenEvent_688_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_688_.Loading; _state.value = GenState_688_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_688_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_688_.Success(searchUseCase(query)) } }
}
