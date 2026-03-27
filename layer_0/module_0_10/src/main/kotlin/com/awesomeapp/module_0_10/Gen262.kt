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

data class GenModel_262_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_262_ {
    data class Load(val id: Long) : GenEvent_262_()
    data class Update(val model: GenModel_262_) : GenEvent_262_()
    data class Delete(val id: Long) : GenEvent_262_()
    data object Refresh : GenEvent_262_()
    data class Search(val query: String) : GenEvent_262_()
    data class Filter(val predicate: String) : GenEvent_262_()
}

sealed class GenState_262_ {
    data object Idle : GenState_262_()
    data object Loading : GenState_262_()
    data class Success(val items: List<GenModel_262_>) : GenState_262_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_262_()
    data class Partial(val items: List<GenModel_262_>, val hasMore: Boolean) : GenState_262_()
}

interface GenRepository_262_ {
    suspend fun getAll(): List<GenModel_262_>
    suspend fun getById(id: Long): GenModel_262_?
    suspend fun save(model: GenModel_262_): GenModel_262_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_262_>
}

@Singleton
class GenRepositoryImpl_262_ @Inject constructor() : GenRepository_262_ {
    private val store = mutableMapOf<Long, GenModel_262_>()
    override suspend fun getAll(): List<GenModel_262_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_262_? = store[id]
    override suspend fun save(model: GenModel_262_): GenModel_262_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_262_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_262_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_262_ @Inject constructor(
    private val repository: GenRepositoryImpl_262_
) : GenUseCase_262_<Unit, List<GenModel_262_>> {
    override suspend fun invoke(params: Unit): List<GenModel_262_> = repository.getAll()
}

class GenSaveUseCase_262_ @Inject constructor(
    private val repository: GenRepositoryImpl_262_
) : GenUseCase_262_<GenModel_262_, GenModel_262_> {
    override suspend fun invoke(params: GenModel_262_): GenModel_262_ = repository.save(params)
}

class GenDeleteUseCase_262_ @Inject constructor(
    private val repository: GenRepositoryImpl_262_
) : GenUseCase_262_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_262_ @Inject constructor(
    private val repository: GenRepositoryImpl_262_
) : GenUseCase_262_<String, List<GenModel_262_>> {
    override suspend fun invoke(params: String): List<GenModel_262_> = repository.search(params)
}

abstract class GenMapper_262_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_262_ : GenMapper_262_<GenModel_262_, String>() {
    override fun map(input: GenModel_262_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_262_ : GenMapper_262_<String, GenModel_262_>() {
    override fun map(input: String): GenModel_262_ {
        val parts = input.split(":")
        return GenModel_262_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_262_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_262_,
    private val saveUseCase: GenSaveUseCase_262_,
    private val deleteUseCase: GenDeleteUseCase_262_,
    private val searchUseCase: GenSearchUseCase_262_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_262_>(GenState_262_.Idle)
    val state: StateFlow<GenState_262_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_262_) {
        when (event) {
            is GenEvent_262_.Load -> loadAll()
            is GenEvent_262_.Update -> save(event.model)
            is GenEvent_262_.Delete -> delete(event.id)
            is GenEvent_262_.Refresh -> loadAll()
            is GenEvent_262_.Search -> search(event.query)
            is GenEvent_262_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_262_.Loading; _state.value = GenState_262_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_262_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_262_.Success(searchUseCase(query)) } }
}
