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

data class GenModel_213_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_213_ {
    data class Load(val id: Long) : GenEvent_213_()
    data class Update(val model: GenModel_213_) : GenEvent_213_()
    data class Delete(val id: Long) : GenEvent_213_()
    data object Refresh : GenEvent_213_()
    data class Search(val query: String) : GenEvent_213_()
    data class Filter(val predicate: String) : GenEvent_213_()
}

sealed class GenState_213_ {
    data object Idle : GenState_213_()
    data object Loading : GenState_213_()
    data class Success(val items: List<GenModel_213_>) : GenState_213_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_213_()
    data class Partial(val items: List<GenModel_213_>, val hasMore: Boolean) : GenState_213_()
}

interface GenRepository_213_ {
    suspend fun getAll(): List<GenModel_213_>
    suspend fun getById(id: Long): GenModel_213_?
    suspend fun save(model: GenModel_213_): GenModel_213_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_213_>
}

@Singleton
class GenRepositoryImpl_213_ @Inject constructor() : GenRepository_213_ {
    private val store = mutableMapOf<Long, GenModel_213_>()
    override suspend fun getAll(): List<GenModel_213_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_213_? = store[id]
    override suspend fun save(model: GenModel_213_): GenModel_213_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_213_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_213_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_213_ @Inject constructor(
    private val repository: GenRepositoryImpl_213_
) : GenUseCase_213_<Unit, List<GenModel_213_>> {
    override suspend fun invoke(params: Unit): List<GenModel_213_> = repository.getAll()
}

class GenSaveUseCase_213_ @Inject constructor(
    private val repository: GenRepositoryImpl_213_
) : GenUseCase_213_<GenModel_213_, GenModel_213_> {
    override suspend fun invoke(params: GenModel_213_): GenModel_213_ = repository.save(params)
}

class GenDeleteUseCase_213_ @Inject constructor(
    private val repository: GenRepositoryImpl_213_
) : GenUseCase_213_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_213_ @Inject constructor(
    private val repository: GenRepositoryImpl_213_
) : GenUseCase_213_<String, List<GenModel_213_>> {
    override suspend fun invoke(params: String): List<GenModel_213_> = repository.search(params)
}

abstract class GenMapper_213_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_213_ : GenMapper_213_<GenModel_213_, String>() {
    override fun map(input: GenModel_213_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_213_ : GenMapper_213_<String, GenModel_213_>() {
    override fun map(input: String): GenModel_213_ {
        val parts = input.split(":")
        return GenModel_213_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_213_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_213_,
    private val saveUseCase: GenSaveUseCase_213_,
    private val deleteUseCase: GenDeleteUseCase_213_,
    private val searchUseCase: GenSearchUseCase_213_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_213_>(GenState_213_.Idle)
    val state: StateFlow<GenState_213_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_213_) {
        when (event) {
            is GenEvent_213_.Load -> loadAll()
            is GenEvent_213_.Update -> save(event.model)
            is GenEvent_213_.Delete -> delete(event.id)
            is GenEvent_213_.Refresh -> loadAll()
            is GenEvent_213_.Search -> search(event.query)
            is GenEvent_213_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_213_.Loading; _state.value = GenState_213_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_213_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_213_.Success(searchUseCase(query)) } }
}
