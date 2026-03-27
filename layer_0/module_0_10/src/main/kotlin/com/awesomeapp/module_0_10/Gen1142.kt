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

data class GenModel_1142_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1142_ {
    data class Load(val id: Long) : GenEvent_1142_()
    data class Update(val model: GenModel_1142_) : GenEvent_1142_()
    data class Delete(val id: Long) : GenEvent_1142_()
    data object Refresh : GenEvent_1142_()
    data class Search(val query: String) : GenEvent_1142_()
    data class Filter(val predicate: String) : GenEvent_1142_()
}

sealed class GenState_1142_ {
    data object Idle : GenState_1142_()
    data object Loading : GenState_1142_()
    data class Success(val items: List<GenModel_1142_>) : GenState_1142_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1142_()
    data class Partial(val items: List<GenModel_1142_>, val hasMore: Boolean) : GenState_1142_()
}

interface GenRepository_1142_ {
    suspend fun getAll(): List<GenModel_1142_>
    suspend fun getById(id: Long): GenModel_1142_?
    suspend fun save(model: GenModel_1142_): GenModel_1142_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1142_>
}

@Singleton
class GenRepositoryImpl_1142_ @Inject constructor() : GenRepository_1142_ {
    private val store = mutableMapOf<Long, GenModel_1142_>()
    override suspend fun getAll(): List<GenModel_1142_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1142_? = store[id]
    override suspend fun save(model: GenModel_1142_): GenModel_1142_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1142_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1142_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1142_ @Inject constructor(
    private val repository: GenRepositoryImpl_1142_
) : GenUseCase_1142_<Unit, List<GenModel_1142_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1142_> = repository.getAll()
}

class GenSaveUseCase_1142_ @Inject constructor(
    private val repository: GenRepositoryImpl_1142_
) : GenUseCase_1142_<GenModel_1142_, GenModel_1142_> {
    override suspend fun invoke(params: GenModel_1142_): GenModel_1142_ = repository.save(params)
}

class GenDeleteUseCase_1142_ @Inject constructor(
    private val repository: GenRepositoryImpl_1142_
) : GenUseCase_1142_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1142_ @Inject constructor(
    private val repository: GenRepositoryImpl_1142_
) : GenUseCase_1142_<String, List<GenModel_1142_>> {
    override suspend fun invoke(params: String): List<GenModel_1142_> = repository.search(params)
}

abstract class GenMapper_1142_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1142_ : GenMapper_1142_<GenModel_1142_, String>() {
    override fun map(input: GenModel_1142_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1142_ : GenMapper_1142_<String, GenModel_1142_>() {
    override fun map(input: String): GenModel_1142_ {
        val parts = input.split(":")
        return GenModel_1142_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1142_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1142_,
    private val saveUseCase: GenSaveUseCase_1142_,
    private val deleteUseCase: GenDeleteUseCase_1142_,
    private val searchUseCase: GenSearchUseCase_1142_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1142_>(GenState_1142_.Idle)
    val state: StateFlow<GenState_1142_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1142_) {
        when (event) {
            is GenEvent_1142_.Load -> loadAll()
            is GenEvent_1142_.Update -> save(event.model)
            is GenEvent_1142_.Delete -> delete(event.id)
            is GenEvent_1142_.Refresh -> loadAll()
            is GenEvent_1142_.Search -> search(event.query)
            is GenEvent_1142_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1142_.Loading; _state.value = GenState_1142_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1142_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1142_.Success(searchUseCase(query)) } }
}
