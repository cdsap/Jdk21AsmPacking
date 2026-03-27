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

data class GenModel_930_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_930_ {
    data class Load(val id: Long) : GenEvent_930_()
    data class Update(val model: GenModel_930_) : GenEvent_930_()
    data class Delete(val id: Long) : GenEvent_930_()
    data object Refresh : GenEvent_930_()
    data class Search(val query: String) : GenEvent_930_()
    data class Filter(val predicate: String) : GenEvent_930_()
}

sealed class GenState_930_ {
    data object Idle : GenState_930_()
    data object Loading : GenState_930_()
    data class Success(val items: List<GenModel_930_>) : GenState_930_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_930_()
    data class Partial(val items: List<GenModel_930_>, val hasMore: Boolean) : GenState_930_()
}

interface GenRepository_930_ {
    suspend fun getAll(): List<GenModel_930_>
    suspend fun getById(id: Long): GenModel_930_?
    suspend fun save(model: GenModel_930_): GenModel_930_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_930_>
}

@Singleton
class GenRepositoryImpl_930_ @Inject constructor() : GenRepository_930_ {
    private val store = mutableMapOf<Long, GenModel_930_>()
    override suspend fun getAll(): List<GenModel_930_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_930_? = store[id]
    override suspend fun save(model: GenModel_930_): GenModel_930_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_930_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_930_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_930_ @Inject constructor(
    private val repository: GenRepositoryImpl_930_
) : GenUseCase_930_<Unit, List<GenModel_930_>> {
    override suspend fun invoke(params: Unit): List<GenModel_930_> = repository.getAll()
}

class GenSaveUseCase_930_ @Inject constructor(
    private val repository: GenRepositoryImpl_930_
) : GenUseCase_930_<GenModel_930_, GenModel_930_> {
    override suspend fun invoke(params: GenModel_930_): GenModel_930_ = repository.save(params)
}

class GenDeleteUseCase_930_ @Inject constructor(
    private val repository: GenRepositoryImpl_930_
) : GenUseCase_930_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_930_ @Inject constructor(
    private val repository: GenRepositoryImpl_930_
) : GenUseCase_930_<String, List<GenModel_930_>> {
    override suspend fun invoke(params: String): List<GenModel_930_> = repository.search(params)
}

abstract class GenMapper_930_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_930_ : GenMapper_930_<GenModel_930_, String>() {
    override fun map(input: GenModel_930_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_930_ : GenMapper_930_<String, GenModel_930_>() {
    override fun map(input: String): GenModel_930_ {
        val parts = input.split(":")
        return GenModel_930_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_930_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_930_,
    private val saveUseCase: GenSaveUseCase_930_,
    private val deleteUseCase: GenDeleteUseCase_930_,
    private val searchUseCase: GenSearchUseCase_930_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_930_>(GenState_930_.Idle)
    val state: StateFlow<GenState_930_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_930_) {
        when (event) {
            is GenEvent_930_.Load -> loadAll()
            is GenEvent_930_.Update -> save(event.model)
            is GenEvent_930_.Delete -> delete(event.id)
            is GenEvent_930_.Refresh -> loadAll()
            is GenEvent_930_.Search -> search(event.query)
            is GenEvent_930_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_930_.Loading; _state.value = GenState_930_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_930_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_930_.Success(searchUseCase(query)) } }
}
