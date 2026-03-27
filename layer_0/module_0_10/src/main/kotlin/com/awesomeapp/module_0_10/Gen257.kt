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

data class GenModel_257_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_257_ {
    data class Load(val id: Long) : GenEvent_257_()
    data class Update(val model: GenModel_257_) : GenEvent_257_()
    data class Delete(val id: Long) : GenEvent_257_()
    data object Refresh : GenEvent_257_()
    data class Search(val query: String) : GenEvent_257_()
    data class Filter(val predicate: String) : GenEvent_257_()
}

sealed class GenState_257_ {
    data object Idle : GenState_257_()
    data object Loading : GenState_257_()
    data class Success(val items: List<GenModel_257_>) : GenState_257_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_257_()
    data class Partial(val items: List<GenModel_257_>, val hasMore: Boolean) : GenState_257_()
}

interface GenRepository_257_ {
    suspend fun getAll(): List<GenModel_257_>
    suspend fun getById(id: Long): GenModel_257_?
    suspend fun save(model: GenModel_257_): GenModel_257_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_257_>
}

@Singleton
class GenRepositoryImpl_257_ @Inject constructor() : GenRepository_257_ {
    private val store = mutableMapOf<Long, GenModel_257_>()
    override suspend fun getAll(): List<GenModel_257_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_257_? = store[id]
    override suspend fun save(model: GenModel_257_): GenModel_257_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_257_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_257_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_257_ @Inject constructor(
    private val repository: GenRepositoryImpl_257_
) : GenUseCase_257_<Unit, List<GenModel_257_>> {
    override suspend fun invoke(params: Unit): List<GenModel_257_> = repository.getAll()
}

class GenSaveUseCase_257_ @Inject constructor(
    private val repository: GenRepositoryImpl_257_
) : GenUseCase_257_<GenModel_257_, GenModel_257_> {
    override suspend fun invoke(params: GenModel_257_): GenModel_257_ = repository.save(params)
}

class GenDeleteUseCase_257_ @Inject constructor(
    private val repository: GenRepositoryImpl_257_
) : GenUseCase_257_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_257_ @Inject constructor(
    private val repository: GenRepositoryImpl_257_
) : GenUseCase_257_<String, List<GenModel_257_>> {
    override suspend fun invoke(params: String): List<GenModel_257_> = repository.search(params)
}

abstract class GenMapper_257_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_257_ : GenMapper_257_<GenModel_257_, String>() {
    override fun map(input: GenModel_257_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_257_ : GenMapper_257_<String, GenModel_257_>() {
    override fun map(input: String): GenModel_257_ {
        val parts = input.split(":")
        return GenModel_257_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_257_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_257_,
    private val saveUseCase: GenSaveUseCase_257_,
    private val deleteUseCase: GenDeleteUseCase_257_,
    private val searchUseCase: GenSearchUseCase_257_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_257_>(GenState_257_.Idle)
    val state: StateFlow<GenState_257_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_257_) {
        when (event) {
            is GenEvent_257_.Load -> loadAll()
            is GenEvent_257_.Update -> save(event.model)
            is GenEvent_257_.Delete -> delete(event.id)
            is GenEvent_257_.Refresh -> loadAll()
            is GenEvent_257_.Search -> search(event.query)
            is GenEvent_257_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_257_.Loading; _state.value = GenState_257_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_257_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_257_.Success(searchUseCase(query)) } }
}
