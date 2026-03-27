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

data class GenModel_507_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_507_ {
    data class Load(val id: Long) : GenEvent_507_()
    data class Update(val model: GenModel_507_) : GenEvent_507_()
    data class Delete(val id: Long) : GenEvent_507_()
    data object Refresh : GenEvent_507_()
    data class Search(val query: String) : GenEvent_507_()
    data class Filter(val predicate: String) : GenEvent_507_()
}

sealed class GenState_507_ {
    data object Idle : GenState_507_()
    data object Loading : GenState_507_()
    data class Success(val items: List<GenModel_507_>) : GenState_507_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_507_()
    data class Partial(val items: List<GenModel_507_>, val hasMore: Boolean) : GenState_507_()
}

interface GenRepository_507_ {
    suspend fun getAll(): List<GenModel_507_>
    suspend fun getById(id: Long): GenModel_507_?
    suspend fun save(model: GenModel_507_): GenModel_507_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_507_>
}

@Singleton
class GenRepositoryImpl_507_ @Inject constructor() : GenRepository_507_ {
    private val store = mutableMapOf<Long, GenModel_507_>()
    override suspend fun getAll(): List<GenModel_507_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_507_? = store[id]
    override suspend fun save(model: GenModel_507_): GenModel_507_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_507_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_507_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_507_ @Inject constructor(
    private val repository: GenRepositoryImpl_507_
) : GenUseCase_507_<Unit, List<GenModel_507_>> {
    override suspend fun invoke(params: Unit): List<GenModel_507_> = repository.getAll()
}

class GenSaveUseCase_507_ @Inject constructor(
    private val repository: GenRepositoryImpl_507_
) : GenUseCase_507_<GenModel_507_, GenModel_507_> {
    override suspend fun invoke(params: GenModel_507_): GenModel_507_ = repository.save(params)
}

class GenDeleteUseCase_507_ @Inject constructor(
    private val repository: GenRepositoryImpl_507_
) : GenUseCase_507_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_507_ @Inject constructor(
    private val repository: GenRepositoryImpl_507_
) : GenUseCase_507_<String, List<GenModel_507_>> {
    override suspend fun invoke(params: String): List<GenModel_507_> = repository.search(params)
}

abstract class GenMapper_507_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_507_ : GenMapper_507_<GenModel_507_, String>() {
    override fun map(input: GenModel_507_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_507_ : GenMapper_507_<String, GenModel_507_>() {
    override fun map(input: String): GenModel_507_ {
        val parts = input.split(":")
        return GenModel_507_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_507_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_507_,
    private val saveUseCase: GenSaveUseCase_507_,
    private val deleteUseCase: GenDeleteUseCase_507_,
    private val searchUseCase: GenSearchUseCase_507_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_507_>(GenState_507_.Idle)
    val state: StateFlow<GenState_507_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_507_) {
        when (event) {
            is GenEvent_507_.Load -> loadAll()
            is GenEvent_507_.Update -> save(event.model)
            is GenEvent_507_.Delete -> delete(event.id)
            is GenEvent_507_.Refresh -> loadAll()
            is GenEvent_507_.Search -> search(event.query)
            is GenEvent_507_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_507_.Loading; _state.value = GenState_507_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_507_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_507_.Success(searchUseCase(query)) } }
}
