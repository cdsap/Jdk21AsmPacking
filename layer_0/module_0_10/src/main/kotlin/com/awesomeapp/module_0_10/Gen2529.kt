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

data class GenModel_2529_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2529_ {
    data class Load(val id: Long) : GenEvent_2529_()
    data class Update(val model: GenModel_2529_) : GenEvent_2529_()
    data class Delete(val id: Long) : GenEvent_2529_()
    data object Refresh : GenEvent_2529_()
    data class Search(val query: String) : GenEvent_2529_()
    data class Filter(val predicate: String) : GenEvent_2529_()
}

sealed class GenState_2529_ {
    data object Idle : GenState_2529_()
    data object Loading : GenState_2529_()
    data class Success(val items: List<GenModel_2529_>) : GenState_2529_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2529_()
    data class Partial(val items: List<GenModel_2529_>, val hasMore: Boolean) : GenState_2529_()
}

interface GenRepository_2529_ {
    suspend fun getAll(): List<GenModel_2529_>
    suspend fun getById(id: Long): GenModel_2529_?
    suspend fun save(model: GenModel_2529_): GenModel_2529_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2529_>
}

@Singleton
class GenRepositoryImpl_2529_ @Inject constructor() : GenRepository_2529_ {
    private val store = mutableMapOf<Long, GenModel_2529_>()
    override suspend fun getAll(): List<GenModel_2529_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2529_? = store[id]
    override suspend fun save(model: GenModel_2529_): GenModel_2529_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2529_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2529_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2529_ @Inject constructor(
    private val repository: GenRepositoryImpl_2529_
) : GenUseCase_2529_<Unit, List<GenModel_2529_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2529_> = repository.getAll()
}

class GenSaveUseCase_2529_ @Inject constructor(
    private val repository: GenRepositoryImpl_2529_
) : GenUseCase_2529_<GenModel_2529_, GenModel_2529_> {
    override suspend fun invoke(params: GenModel_2529_): GenModel_2529_ = repository.save(params)
}

class GenDeleteUseCase_2529_ @Inject constructor(
    private val repository: GenRepositoryImpl_2529_
) : GenUseCase_2529_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2529_ @Inject constructor(
    private val repository: GenRepositoryImpl_2529_
) : GenUseCase_2529_<String, List<GenModel_2529_>> {
    override suspend fun invoke(params: String): List<GenModel_2529_> = repository.search(params)
}

abstract class GenMapper_2529_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2529_ : GenMapper_2529_<GenModel_2529_, String>() {
    override fun map(input: GenModel_2529_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2529_ : GenMapper_2529_<String, GenModel_2529_>() {
    override fun map(input: String): GenModel_2529_ {
        val parts = input.split(":")
        return GenModel_2529_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2529_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2529_,
    private val saveUseCase: GenSaveUseCase_2529_,
    private val deleteUseCase: GenDeleteUseCase_2529_,
    private val searchUseCase: GenSearchUseCase_2529_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2529_>(GenState_2529_.Idle)
    val state: StateFlow<GenState_2529_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2529_) {
        when (event) {
            is GenEvent_2529_.Load -> loadAll()
            is GenEvent_2529_.Update -> save(event.model)
            is GenEvent_2529_.Delete -> delete(event.id)
            is GenEvent_2529_.Refresh -> loadAll()
            is GenEvent_2529_.Search -> search(event.query)
            is GenEvent_2529_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2529_.Loading; _state.value = GenState_2529_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2529_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2529_.Success(searchUseCase(query)) } }
}
