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

data class GenModel_179_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_179_ {
    data class Load(val id: Long) : GenEvent_179_()
    data class Update(val model: GenModel_179_) : GenEvent_179_()
    data class Delete(val id: Long) : GenEvent_179_()
    data object Refresh : GenEvent_179_()
    data class Search(val query: String) : GenEvent_179_()
    data class Filter(val predicate: String) : GenEvent_179_()
}

sealed class GenState_179_ {
    data object Idle : GenState_179_()
    data object Loading : GenState_179_()
    data class Success(val items: List<GenModel_179_>) : GenState_179_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_179_()
    data class Partial(val items: List<GenModel_179_>, val hasMore: Boolean) : GenState_179_()
}

interface GenRepository_179_ {
    suspend fun getAll(): List<GenModel_179_>
    suspend fun getById(id: Long): GenModel_179_?
    suspend fun save(model: GenModel_179_): GenModel_179_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_179_>
}

@Singleton
class GenRepositoryImpl_179_ @Inject constructor() : GenRepository_179_ {
    private val store = mutableMapOf<Long, GenModel_179_>()
    override suspend fun getAll(): List<GenModel_179_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_179_? = store[id]
    override suspend fun save(model: GenModel_179_): GenModel_179_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_179_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_179_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_179_ @Inject constructor(
    private val repository: GenRepositoryImpl_179_
) : GenUseCase_179_<Unit, List<GenModel_179_>> {
    override suspend fun invoke(params: Unit): List<GenModel_179_> = repository.getAll()
}

class GenSaveUseCase_179_ @Inject constructor(
    private val repository: GenRepositoryImpl_179_
) : GenUseCase_179_<GenModel_179_, GenModel_179_> {
    override suspend fun invoke(params: GenModel_179_): GenModel_179_ = repository.save(params)
}

class GenDeleteUseCase_179_ @Inject constructor(
    private val repository: GenRepositoryImpl_179_
) : GenUseCase_179_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_179_ @Inject constructor(
    private val repository: GenRepositoryImpl_179_
) : GenUseCase_179_<String, List<GenModel_179_>> {
    override suspend fun invoke(params: String): List<GenModel_179_> = repository.search(params)
}

abstract class GenMapper_179_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_179_ : GenMapper_179_<GenModel_179_, String>() {
    override fun map(input: GenModel_179_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_179_ : GenMapper_179_<String, GenModel_179_>() {
    override fun map(input: String): GenModel_179_ {
        val parts = input.split(":")
        return GenModel_179_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_179_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_179_,
    private val saveUseCase: GenSaveUseCase_179_,
    private val deleteUseCase: GenDeleteUseCase_179_,
    private val searchUseCase: GenSearchUseCase_179_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_179_>(GenState_179_.Idle)
    val state: StateFlow<GenState_179_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_179_) {
        when (event) {
            is GenEvent_179_.Load -> loadAll()
            is GenEvent_179_.Update -> save(event.model)
            is GenEvent_179_.Delete -> delete(event.id)
            is GenEvent_179_.Refresh -> loadAll()
            is GenEvent_179_.Search -> search(event.query)
            is GenEvent_179_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_179_.Loading; _state.value = GenState_179_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_179_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_179_.Success(searchUseCase(query)) } }
}
