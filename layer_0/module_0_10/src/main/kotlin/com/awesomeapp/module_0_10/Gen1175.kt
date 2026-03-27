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

data class GenModel_1175_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1175_ {
    data class Load(val id: Long) : GenEvent_1175_()
    data class Update(val model: GenModel_1175_) : GenEvent_1175_()
    data class Delete(val id: Long) : GenEvent_1175_()
    data object Refresh : GenEvent_1175_()
    data class Search(val query: String) : GenEvent_1175_()
    data class Filter(val predicate: String) : GenEvent_1175_()
}

sealed class GenState_1175_ {
    data object Idle : GenState_1175_()
    data object Loading : GenState_1175_()
    data class Success(val items: List<GenModel_1175_>) : GenState_1175_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1175_()
    data class Partial(val items: List<GenModel_1175_>, val hasMore: Boolean) : GenState_1175_()
}

interface GenRepository_1175_ {
    suspend fun getAll(): List<GenModel_1175_>
    suspend fun getById(id: Long): GenModel_1175_?
    suspend fun save(model: GenModel_1175_): GenModel_1175_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1175_>
}

@Singleton
class GenRepositoryImpl_1175_ @Inject constructor() : GenRepository_1175_ {
    private val store = mutableMapOf<Long, GenModel_1175_>()
    override suspend fun getAll(): List<GenModel_1175_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1175_? = store[id]
    override suspend fun save(model: GenModel_1175_): GenModel_1175_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1175_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1175_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1175_ @Inject constructor(
    private val repository: GenRepositoryImpl_1175_
) : GenUseCase_1175_<Unit, List<GenModel_1175_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1175_> = repository.getAll()
}

class GenSaveUseCase_1175_ @Inject constructor(
    private val repository: GenRepositoryImpl_1175_
) : GenUseCase_1175_<GenModel_1175_, GenModel_1175_> {
    override suspend fun invoke(params: GenModel_1175_): GenModel_1175_ = repository.save(params)
}

class GenDeleteUseCase_1175_ @Inject constructor(
    private val repository: GenRepositoryImpl_1175_
) : GenUseCase_1175_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1175_ @Inject constructor(
    private val repository: GenRepositoryImpl_1175_
) : GenUseCase_1175_<String, List<GenModel_1175_>> {
    override suspend fun invoke(params: String): List<GenModel_1175_> = repository.search(params)
}

abstract class GenMapper_1175_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1175_ : GenMapper_1175_<GenModel_1175_, String>() {
    override fun map(input: GenModel_1175_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1175_ : GenMapper_1175_<String, GenModel_1175_>() {
    override fun map(input: String): GenModel_1175_ {
        val parts = input.split(":")
        return GenModel_1175_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1175_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1175_,
    private val saveUseCase: GenSaveUseCase_1175_,
    private val deleteUseCase: GenDeleteUseCase_1175_,
    private val searchUseCase: GenSearchUseCase_1175_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1175_>(GenState_1175_.Idle)
    val state: StateFlow<GenState_1175_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1175_) {
        when (event) {
            is GenEvent_1175_.Load -> loadAll()
            is GenEvent_1175_.Update -> save(event.model)
            is GenEvent_1175_.Delete -> delete(event.id)
            is GenEvent_1175_.Refresh -> loadAll()
            is GenEvent_1175_.Search -> search(event.query)
            is GenEvent_1175_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1175_.Loading; _state.value = GenState_1175_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1175_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1175_.Success(searchUseCase(query)) } }
}
