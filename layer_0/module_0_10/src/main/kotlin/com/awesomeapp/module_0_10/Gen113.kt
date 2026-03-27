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

data class GenModel_113_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_113_ {
    data class Load(val id: Long) : GenEvent_113_()
    data class Update(val model: GenModel_113_) : GenEvent_113_()
    data class Delete(val id: Long) : GenEvent_113_()
    data object Refresh : GenEvent_113_()
    data class Search(val query: String) : GenEvent_113_()
    data class Filter(val predicate: String) : GenEvent_113_()
}

sealed class GenState_113_ {
    data object Idle : GenState_113_()
    data object Loading : GenState_113_()
    data class Success(val items: List<GenModel_113_>) : GenState_113_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_113_()
    data class Partial(val items: List<GenModel_113_>, val hasMore: Boolean) : GenState_113_()
}

interface GenRepository_113_ {
    suspend fun getAll(): List<GenModel_113_>
    suspend fun getById(id: Long): GenModel_113_?
    suspend fun save(model: GenModel_113_): GenModel_113_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_113_>
}

@Singleton
class GenRepositoryImpl_113_ @Inject constructor() : GenRepository_113_ {
    private val store = mutableMapOf<Long, GenModel_113_>()
    override suspend fun getAll(): List<GenModel_113_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_113_? = store[id]
    override suspend fun save(model: GenModel_113_): GenModel_113_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_113_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_113_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_113_ @Inject constructor(
    private val repository: GenRepositoryImpl_113_
) : GenUseCase_113_<Unit, List<GenModel_113_>> {
    override suspend fun invoke(params: Unit): List<GenModel_113_> = repository.getAll()
}

class GenSaveUseCase_113_ @Inject constructor(
    private val repository: GenRepositoryImpl_113_
) : GenUseCase_113_<GenModel_113_, GenModel_113_> {
    override suspend fun invoke(params: GenModel_113_): GenModel_113_ = repository.save(params)
}

class GenDeleteUseCase_113_ @Inject constructor(
    private val repository: GenRepositoryImpl_113_
) : GenUseCase_113_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_113_ @Inject constructor(
    private val repository: GenRepositoryImpl_113_
) : GenUseCase_113_<String, List<GenModel_113_>> {
    override suspend fun invoke(params: String): List<GenModel_113_> = repository.search(params)
}

abstract class GenMapper_113_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_113_ : GenMapper_113_<GenModel_113_, String>() {
    override fun map(input: GenModel_113_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_113_ : GenMapper_113_<String, GenModel_113_>() {
    override fun map(input: String): GenModel_113_ {
        val parts = input.split(":")
        return GenModel_113_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_113_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_113_,
    private val saveUseCase: GenSaveUseCase_113_,
    private val deleteUseCase: GenDeleteUseCase_113_,
    private val searchUseCase: GenSearchUseCase_113_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_113_>(GenState_113_.Idle)
    val state: StateFlow<GenState_113_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_113_) {
        when (event) {
            is GenEvent_113_.Load -> loadAll()
            is GenEvent_113_.Update -> save(event.model)
            is GenEvent_113_.Delete -> delete(event.id)
            is GenEvent_113_.Refresh -> loadAll()
            is GenEvent_113_.Search -> search(event.query)
            is GenEvent_113_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_113_.Loading; _state.value = GenState_113_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_113_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_113_.Success(searchUseCase(query)) } }
}
