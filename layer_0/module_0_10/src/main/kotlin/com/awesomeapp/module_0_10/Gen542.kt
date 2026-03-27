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

data class GenModel_542_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_542_ {
    data class Load(val id: Long) : GenEvent_542_()
    data class Update(val model: GenModel_542_) : GenEvent_542_()
    data class Delete(val id: Long) : GenEvent_542_()
    data object Refresh : GenEvent_542_()
    data class Search(val query: String) : GenEvent_542_()
    data class Filter(val predicate: String) : GenEvent_542_()
}

sealed class GenState_542_ {
    data object Idle : GenState_542_()
    data object Loading : GenState_542_()
    data class Success(val items: List<GenModel_542_>) : GenState_542_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_542_()
    data class Partial(val items: List<GenModel_542_>, val hasMore: Boolean) : GenState_542_()
}

interface GenRepository_542_ {
    suspend fun getAll(): List<GenModel_542_>
    suspend fun getById(id: Long): GenModel_542_?
    suspend fun save(model: GenModel_542_): GenModel_542_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_542_>
}

@Singleton
class GenRepositoryImpl_542_ @Inject constructor() : GenRepository_542_ {
    private val store = mutableMapOf<Long, GenModel_542_>()
    override suspend fun getAll(): List<GenModel_542_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_542_? = store[id]
    override suspend fun save(model: GenModel_542_): GenModel_542_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_542_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_542_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_542_ @Inject constructor(
    private val repository: GenRepositoryImpl_542_
) : GenUseCase_542_<Unit, List<GenModel_542_>> {
    override suspend fun invoke(params: Unit): List<GenModel_542_> = repository.getAll()
}

class GenSaveUseCase_542_ @Inject constructor(
    private val repository: GenRepositoryImpl_542_
) : GenUseCase_542_<GenModel_542_, GenModel_542_> {
    override suspend fun invoke(params: GenModel_542_): GenModel_542_ = repository.save(params)
}

class GenDeleteUseCase_542_ @Inject constructor(
    private val repository: GenRepositoryImpl_542_
) : GenUseCase_542_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_542_ @Inject constructor(
    private val repository: GenRepositoryImpl_542_
) : GenUseCase_542_<String, List<GenModel_542_>> {
    override suspend fun invoke(params: String): List<GenModel_542_> = repository.search(params)
}

abstract class GenMapper_542_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_542_ : GenMapper_542_<GenModel_542_, String>() {
    override fun map(input: GenModel_542_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_542_ : GenMapper_542_<String, GenModel_542_>() {
    override fun map(input: String): GenModel_542_ {
        val parts = input.split(":")
        return GenModel_542_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_542_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_542_,
    private val saveUseCase: GenSaveUseCase_542_,
    private val deleteUseCase: GenDeleteUseCase_542_,
    private val searchUseCase: GenSearchUseCase_542_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_542_>(GenState_542_.Idle)
    val state: StateFlow<GenState_542_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_542_) {
        when (event) {
            is GenEvent_542_.Load -> loadAll()
            is GenEvent_542_.Update -> save(event.model)
            is GenEvent_542_.Delete -> delete(event.id)
            is GenEvent_542_.Refresh -> loadAll()
            is GenEvent_542_.Search -> search(event.query)
            is GenEvent_542_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_542_.Loading; _state.value = GenState_542_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_542_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_542_.Success(searchUseCase(query)) } }
}
