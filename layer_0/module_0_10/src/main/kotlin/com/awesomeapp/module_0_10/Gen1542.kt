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

data class GenModel_1542_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1542_ {
    data class Load(val id: Long) : GenEvent_1542_()
    data class Update(val model: GenModel_1542_) : GenEvent_1542_()
    data class Delete(val id: Long) : GenEvent_1542_()
    data object Refresh : GenEvent_1542_()
    data class Search(val query: String) : GenEvent_1542_()
    data class Filter(val predicate: String) : GenEvent_1542_()
}

sealed class GenState_1542_ {
    data object Idle : GenState_1542_()
    data object Loading : GenState_1542_()
    data class Success(val items: List<GenModel_1542_>) : GenState_1542_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1542_()
    data class Partial(val items: List<GenModel_1542_>, val hasMore: Boolean) : GenState_1542_()
}

interface GenRepository_1542_ {
    suspend fun getAll(): List<GenModel_1542_>
    suspend fun getById(id: Long): GenModel_1542_?
    suspend fun save(model: GenModel_1542_): GenModel_1542_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1542_>
}

@Singleton
class GenRepositoryImpl_1542_ @Inject constructor() : GenRepository_1542_ {
    private val store = mutableMapOf<Long, GenModel_1542_>()
    override suspend fun getAll(): List<GenModel_1542_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1542_? = store[id]
    override suspend fun save(model: GenModel_1542_): GenModel_1542_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1542_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1542_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1542_ @Inject constructor(
    private val repository: GenRepositoryImpl_1542_
) : GenUseCase_1542_<Unit, List<GenModel_1542_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1542_> = repository.getAll()
}

class GenSaveUseCase_1542_ @Inject constructor(
    private val repository: GenRepositoryImpl_1542_
) : GenUseCase_1542_<GenModel_1542_, GenModel_1542_> {
    override suspend fun invoke(params: GenModel_1542_): GenModel_1542_ = repository.save(params)
}

class GenDeleteUseCase_1542_ @Inject constructor(
    private val repository: GenRepositoryImpl_1542_
) : GenUseCase_1542_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1542_ @Inject constructor(
    private val repository: GenRepositoryImpl_1542_
) : GenUseCase_1542_<String, List<GenModel_1542_>> {
    override suspend fun invoke(params: String): List<GenModel_1542_> = repository.search(params)
}

abstract class GenMapper_1542_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1542_ : GenMapper_1542_<GenModel_1542_, String>() {
    override fun map(input: GenModel_1542_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1542_ : GenMapper_1542_<String, GenModel_1542_>() {
    override fun map(input: String): GenModel_1542_ {
        val parts = input.split(":")
        return GenModel_1542_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1542_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1542_,
    private val saveUseCase: GenSaveUseCase_1542_,
    private val deleteUseCase: GenDeleteUseCase_1542_,
    private val searchUseCase: GenSearchUseCase_1542_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1542_>(GenState_1542_.Idle)
    val state: StateFlow<GenState_1542_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1542_) {
        when (event) {
            is GenEvent_1542_.Load -> loadAll()
            is GenEvent_1542_.Update -> save(event.model)
            is GenEvent_1542_.Delete -> delete(event.id)
            is GenEvent_1542_.Refresh -> loadAll()
            is GenEvent_1542_.Search -> search(event.query)
            is GenEvent_1542_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1542_.Loading; _state.value = GenState_1542_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1542_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1542_.Success(searchUseCase(query)) } }
}
