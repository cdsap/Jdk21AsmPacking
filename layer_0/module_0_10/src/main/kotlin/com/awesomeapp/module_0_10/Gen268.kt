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

data class GenModel_268_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_268_ {
    data class Load(val id: Long) : GenEvent_268_()
    data class Update(val model: GenModel_268_) : GenEvent_268_()
    data class Delete(val id: Long) : GenEvent_268_()
    data object Refresh : GenEvent_268_()
    data class Search(val query: String) : GenEvent_268_()
    data class Filter(val predicate: String) : GenEvent_268_()
}

sealed class GenState_268_ {
    data object Idle : GenState_268_()
    data object Loading : GenState_268_()
    data class Success(val items: List<GenModel_268_>) : GenState_268_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_268_()
    data class Partial(val items: List<GenModel_268_>, val hasMore: Boolean) : GenState_268_()
}

interface GenRepository_268_ {
    suspend fun getAll(): List<GenModel_268_>
    suspend fun getById(id: Long): GenModel_268_?
    suspend fun save(model: GenModel_268_): GenModel_268_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_268_>
}

@Singleton
class GenRepositoryImpl_268_ @Inject constructor() : GenRepository_268_ {
    private val store = mutableMapOf<Long, GenModel_268_>()
    override suspend fun getAll(): List<GenModel_268_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_268_? = store[id]
    override suspend fun save(model: GenModel_268_): GenModel_268_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_268_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_268_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_268_ @Inject constructor(
    private val repository: GenRepositoryImpl_268_
) : GenUseCase_268_<Unit, List<GenModel_268_>> {
    override suspend fun invoke(params: Unit): List<GenModel_268_> = repository.getAll()
}

class GenSaveUseCase_268_ @Inject constructor(
    private val repository: GenRepositoryImpl_268_
) : GenUseCase_268_<GenModel_268_, GenModel_268_> {
    override suspend fun invoke(params: GenModel_268_): GenModel_268_ = repository.save(params)
}

class GenDeleteUseCase_268_ @Inject constructor(
    private val repository: GenRepositoryImpl_268_
) : GenUseCase_268_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_268_ @Inject constructor(
    private val repository: GenRepositoryImpl_268_
) : GenUseCase_268_<String, List<GenModel_268_>> {
    override suspend fun invoke(params: String): List<GenModel_268_> = repository.search(params)
}

abstract class GenMapper_268_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_268_ : GenMapper_268_<GenModel_268_, String>() {
    override fun map(input: GenModel_268_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_268_ : GenMapper_268_<String, GenModel_268_>() {
    override fun map(input: String): GenModel_268_ {
        val parts = input.split(":")
        return GenModel_268_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_268_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_268_,
    private val saveUseCase: GenSaveUseCase_268_,
    private val deleteUseCase: GenDeleteUseCase_268_,
    private val searchUseCase: GenSearchUseCase_268_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_268_>(GenState_268_.Idle)
    val state: StateFlow<GenState_268_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_268_) {
        when (event) {
            is GenEvent_268_.Load -> loadAll()
            is GenEvent_268_.Update -> save(event.model)
            is GenEvent_268_.Delete -> delete(event.id)
            is GenEvent_268_.Refresh -> loadAll()
            is GenEvent_268_.Search -> search(event.query)
            is GenEvent_268_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_268_.Loading; _state.value = GenState_268_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_268_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_268_.Success(searchUseCase(query)) } }
}
