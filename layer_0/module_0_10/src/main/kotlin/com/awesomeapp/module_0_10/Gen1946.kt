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

data class GenModel_1946_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1946_ {
    data class Load(val id: Long) : GenEvent_1946_()
    data class Update(val model: GenModel_1946_) : GenEvent_1946_()
    data class Delete(val id: Long) : GenEvent_1946_()
    data object Refresh : GenEvent_1946_()
    data class Search(val query: String) : GenEvent_1946_()
    data class Filter(val predicate: String) : GenEvent_1946_()
}

sealed class GenState_1946_ {
    data object Idle : GenState_1946_()
    data object Loading : GenState_1946_()
    data class Success(val items: List<GenModel_1946_>) : GenState_1946_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1946_()
    data class Partial(val items: List<GenModel_1946_>, val hasMore: Boolean) : GenState_1946_()
}

interface GenRepository_1946_ {
    suspend fun getAll(): List<GenModel_1946_>
    suspend fun getById(id: Long): GenModel_1946_?
    suspend fun save(model: GenModel_1946_): GenModel_1946_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1946_>
}

@Singleton
class GenRepositoryImpl_1946_ @Inject constructor() : GenRepository_1946_ {
    private val store = mutableMapOf<Long, GenModel_1946_>()
    override suspend fun getAll(): List<GenModel_1946_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1946_? = store[id]
    override suspend fun save(model: GenModel_1946_): GenModel_1946_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1946_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1946_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1946_ @Inject constructor(
    private val repository: GenRepositoryImpl_1946_
) : GenUseCase_1946_<Unit, List<GenModel_1946_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1946_> = repository.getAll()
}

class GenSaveUseCase_1946_ @Inject constructor(
    private val repository: GenRepositoryImpl_1946_
) : GenUseCase_1946_<GenModel_1946_, GenModel_1946_> {
    override suspend fun invoke(params: GenModel_1946_): GenModel_1946_ = repository.save(params)
}

class GenDeleteUseCase_1946_ @Inject constructor(
    private val repository: GenRepositoryImpl_1946_
) : GenUseCase_1946_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1946_ @Inject constructor(
    private val repository: GenRepositoryImpl_1946_
) : GenUseCase_1946_<String, List<GenModel_1946_>> {
    override suspend fun invoke(params: String): List<GenModel_1946_> = repository.search(params)
}

abstract class GenMapper_1946_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1946_ : GenMapper_1946_<GenModel_1946_, String>() {
    override fun map(input: GenModel_1946_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1946_ : GenMapper_1946_<String, GenModel_1946_>() {
    override fun map(input: String): GenModel_1946_ {
        val parts = input.split(":")
        return GenModel_1946_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1946_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1946_,
    private val saveUseCase: GenSaveUseCase_1946_,
    private val deleteUseCase: GenDeleteUseCase_1946_,
    private val searchUseCase: GenSearchUseCase_1946_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1946_>(GenState_1946_.Idle)
    val state: StateFlow<GenState_1946_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1946_) {
        when (event) {
            is GenEvent_1946_.Load -> loadAll()
            is GenEvent_1946_.Update -> save(event.model)
            is GenEvent_1946_.Delete -> delete(event.id)
            is GenEvent_1946_.Refresh -> loadAll()
            is GenEvent_1946_.Search -> search(event.query)
            is GenEvent_1946_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1946_.Loading; _state.value = GenState_1946_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1946_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1946_.Success(searchUseCase(query)) } }
}
