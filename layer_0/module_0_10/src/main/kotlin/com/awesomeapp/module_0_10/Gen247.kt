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

data class GenModel_247_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_247_ {
    data class Load(val id: Long) : GenEvent_247_()
    data class Update(val model: GenModel_247_) : GenEvent_247_()
    data class Delete(val id: Long) : GenEvent_247_()
    data object Refresh : GenEvent_247_()
    data class Search(val query: String) : GenEvent_247_()
    data class Filter(val predicate: String) : GenEvent_247_()
}

sealed class GenState_247_ {
    data object Idle : GenState_247_()
    data object Loading : GenState_247_()
    data class Success(val items: List<GenModel_247_>) : GenState_247_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_247_()
    data class Partial(val items: List<GenModel_247_>, val hasMore: Boolean) : GenState_247_()
}

interface GenRepository_247_ {
    suspend fun getAll(): List<GenModel_247_>
    suspend fun getById(id: Long): GenModel_247_?
    suspend fun save(model: GenModel_247_): GenModel_247_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_247_>
}

@Singleton
class GenRepositoryImpl_247_ @Inject constructor() : GenRepository_247_ {
    private val store = mutableMapOf<Long, GenModel_247_>()
    override suspend fun getAll(): List<GenModel_247_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_247_? = store[id]
    override suspend fun save(model: GenModel_247_): GenModel_247_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_247_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_247_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_247_ @Inject constructor(
    private val repository: GenRepositoryImpl_247_
) : GenUseCase_247_<Unit, List<GenModel_247_>> {
    override suspend fun invoke(params: Unit): List<GenModel_247_> = repository.getAll()
}

class GenSaveUseCase_247_ @Inject constructor(
    private val repository: GenRepositoryImpl_247_
) : GenUseCase_247_<GenModel_247_, GenModel_247_> {
    override suspend fun invoke(params: GenModel_247_): GenModel_247_ = repository.save(params)
}

class GenDeleteUseCase_247_ @Inject constructor(
    private val repository: GenRepositoryImpl_247_
) : GenUseCase_247_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_247_ @Inject constructor(
    private val repository: GenRepositoryImpl_247_
) : GenUseCase_247_<String, List<GenModel_247_>> {
    override suspend fun invoke(params: String): List<GenModel_247_> = repository.search(params)
}

abstract class GenMapper_247_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_247_ : GenMapper_247_<GenModel_247_, String>() {
    override fun map(input: GenModel_247_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_247_ : GenMapper_247_<String, GenModel_247_>() {
    override fun map(input: String): GenModel_247_ {
        val parts = input.split(":")
        return GenModel_247_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_247_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_247_,
    private val saveUseCase: GenSaveUseCase_247_,
    private val deleteUseCase: GenDeleteUseCase_247_,
    private val searchUseCase: GenSearchUseCase_247_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_247_>(GenState_247_.Idle)
    val state: StateFlow<GenState_247_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_247_) {
        when (event) {
            is GenEvent_247_.Load -> loadAll()
            is GenEvent_247_.Update -> save(event.model)
            is GenEvent_247_.Delete -> delete(event.id)
            is GenEvent_247_.Refresh -> loadAll()
            is GenEvent_247_.Search -> search(event.query)
            is GenEvent_247_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_247_.Loading; _state.value = GenState_247_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_247_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_247_.Success(searchUseCase(query)) } }
}
