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

data class GenModel_1257_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1257_ {
    data class Load(val id: Long) : GenEvent_1257_()
    data class Update(val model: GenModel_1257_) : GenEvent_1257_()
    data class Delete(val id: Long) : GenEvent_1257_()
    data object Refresh : GenEvent_1257_()
    data class Search(val query: String) : GenEvent_1257_()
    data class Filter(val predicate: String) : GenEvent_1257_()
}

sealed class GenState_1257_ {
    data object Idle : GenState_1257_()
    data object Loading : GenState_1257_()
    data class Success(val items: List<GenModel_1257_>) : GenState_1257_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1257_()
    data class Partial(val items: List<GenModel_1257_>, val hasMore: Boolean) : GenState_1257_()
}

interface GenRepository_1257_ {
    suspend fun getAll(): List<GenModel_1257_>
    suspend fun getById(id: Long): GenModel_1257_?
    suspend fun save(model: GenModel_1257_): GenModel_1257_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1257_>
}

@Singleton
class GenRepositoryImpl_1257_ @Inject constructor() : GenRepository_1257_ {
    private val store = mutableMapOf<Long, GenModel_1257_>()
    override suspend fun getAll(): List<GenModel_1257_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1257_? = store[id]
    override suspend fun save(model: GenModel_1257_): GenModel_1257_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1257_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1257_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1257_ @Inject constructor(
    private val repository: GenRepositoryImpl_1257_
) : GenUseCase_1257_<Unit, List<GenModel_1257_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1257_> = repository.getAll()
}

class GenSaveUseCase_1257_ @Inject constructor(
    private val repository: GenRepositoryImpl_1257_
) : GenUseCase_1257_<GenModel_1257_, GenModel_1257_> {
    override suspend fun invoke(params: GenModel_1257_): GenModel_1257_ = repository.save(params)
}

class GenDeleteUseCase_1257_ @Inject constructor(
    private val repository: GenRepositoryImpl_1257_
) : GenUseCase_1257_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1257_ @Inject constructor(
    private val repository: GenRepositoryImpl_1257_
) : GenUseCase_1257_<String, List<GenModel_1257_>> {
    override suspend fun invoke(params: String): List<GenModel_1257_> = repository.search(params)
}

abstract class GenMapper_1257_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1257_ : GenMapper_1257_<GenModel_1257_, String>() {
    override fun map(input: GenModel_1257_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1257_ : GenMapper_1257_<String, GenModel_1257_>() {
    override fun map(input: String): GenModel_1257_ {
        val parts = input.split(":")
        return GenModel_1257_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1257_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1257_,
    private val saveUseCase: GenSaveUseCase_1257_,
    private val deleteUseCase: GenDeleteUseCase_1257_,
    private val searchUseCase: GenSearchUseCase_1257_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1257_>(GenState_1257_.Idle)
    val state: StateFlow<GenState_1257_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1257_) {
        when (event) {
            is GenEvent_1257_.Load -> loadAll()
            is GenEvent_1257_.Update -> save(event.model)
            is GenEvent_1257_.Delete -> delete(event.id)
            is GenEvent_1257_.Refresh -> loadAll()
            is GenEvent_1257_.Search -> search(event.query)
            is GenEvent_1257_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1257_.Loading; _state.value = GenState_1257_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1257_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1257_.Success(searchUseCase(query)) } }
}
