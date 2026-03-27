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

data class GenModel_946_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_946_ {
    data class Load(val id: Long) : GenEvent_946_()
    data class Update(val model: GenModel_946_) : GenEvent_946_()
    data class Delete(val id: Long) : GenEvent_946_()
    data object Refresh : GenEvent_946_()
    data class Search(val query: String) : GenEvent_946_()
    data class Filter(val predicate: String) : GenEvent_946_()
}

sealed class GenState_946_ {
    data object Idle : GenState_946_()
    data object Loading : GenState_946_()
    data class Success(val items: List<GenModel_946_>) : GenState_946_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_946_()
    data class Partial(val items: List<GenModel_946_>, val hasMore: Boolean) : GenState_946_()
}

interface GenRepository_946_ {
    suspend fun getAll(): List<GenModel_946_>
    suspend fun getById(id: Long): GenModel_946_?
    suspend fun save(model: GenModel_946_): GenModel_946_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_946_>
}

@Singleton
class GenRepositoryImpl_946_ @Inject constructor() : GenRepository_946_ {
    private val store = mutableMapOf<Long, GenModel_946_>()
    override suspend fun getAll(): List<GenModel_946_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_946_? = store[id]
    override suspend fun save(model: GenModel_946_): GenModel_946_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_946_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_946_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_946_ @Inject constructor(
    private val repository: GenRepositoryImpl_946_
) : GenUseCase_946_<Unit, List<GenModel_946_>> {
    override suspend fun invoke(params: Unit): List<GenModel_946_> = repository.getAll()
}

class GenSaveUseCase_946_ @Inject constructor(
    private val repository: GenRepositoryImpl_946_
) : GenUseCase_946_<GenModel_946_, GenModel_946_> {
    override suspend fun invoke(params: GenModel_946_): GenModel_946_ = repository.save(params)
}

class GenDeleteUseCase_946_ @Inject constructor(
    private val repository: GenRepositoryImpl_946_
) : GenUseCase_946_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_946_ @Inject constructor(
    private val repository: GenRepositoryImpl_946_
) : GenUseCase_946_<String, List<GenModel_946_>> {
    override suspend fun invoke(params: String): List<GenModel_946_> = repository.search(params)
}

abstract class GenMapper_946_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_946_ : GenMapper_946_<GenModel_946_, String>() {
    override fun map(input: GenModel_946_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_946_ : GenMapper_946_<String, GenModel_946_>() {
    override fun map(input: String): GenModel_946_ {
        val parts = input.split(":")
        return GenModel_946_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_946_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_946_,
    private val saveUseCase: GenSaveUseCase_946_,
    private val deleteUseCase: GenDeleteUseCase_946_,
    private val searchUseCase: GenSearchUseCase_946_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_946_>(GenState_946_.Idle)
    val state: StateFlow<GenState_946_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_946_) {
        when (event) {
            is GenEvent_946_.Load -> loadAll()
            is GenEvent_946_.Update -> save(event.model)
            is GenEvent_946_.Delete -> delete(event.id)
            is GenEvent_946_.Refresh -> loadAll()
            is GenEvent_946_.Search -> search(event.query)
            is GenEvent_946_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_946_.Loading; _state.value = GenState_946_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_946_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_946_.Success(searchUseCase(query)) } }
}
