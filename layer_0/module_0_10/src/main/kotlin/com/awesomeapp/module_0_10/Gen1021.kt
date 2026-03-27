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

data class GenModel_1021_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1021_ {
    data class Load(val id: Long) : GenEvent_1021_()
    data class Update(val model: GenModel_1021_) : GenEvent_1021_()
    data class Delete(val id: Long) : GenEvent_1021_()
    data object Refresh : GenEvent_1021_()
    data class Search(val query: String) : GenEvent_1021_()
    data class Filter(val predicate: String) : GenEvent_1021_()
}

sealed class GenState_1021_ {
    data object Idle : GenState_1021_()
    data object Loading : GenState_1021_()
    data class Success(val items: List<GenModel_1021_>) : GenState_1021_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1021_()
    data class Partial(val items: List<GenModel_1021_>, val hasMore: Boolean) : GenState_1021_()
}

interface GenRepository_1021_ {
    suspend fun getAll(): List<GenModel_1021_>
    suspend fun getById(id: Long): GenModel_1021_?
    suspend fun save(model: GenModel_1021_): GenModel_1021_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1021_>
}

@Singleton
class GenRepositoryImpl_1021_ @Inject constructor() : GenRepository_1021_ {
    private val store = mutableMapOf<Long, GenModel_1021_>()
    override suspend fun getAll(): List<GenModel_1021_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1021_? = store[id]
    override suspend fun save(model: GenModel_1021_): GenModel_1021_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1021_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1021_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1021_ @Inject constructor(
    private val repository: GenRepositoryImpl_1021_
) : GenUseCase_1021_<Unit, List<GenModel_1021_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1021_> = repository.getAll()
}

class GenSaveUseCase_1021_ @Inject constructor(
    private val repository: GenRepositoryImpl_1021_
) : GenUseCase_1021_<GenModel_1021_, GenModel_1021_> {
    override suspend fun invoke(params: GenModel_1021_): GenModel_1021_ = repository.save(params)
}

class GenDeleteUseCase_1021_ @Inject constructor(
    private val repository: GenRepositoryImpl_1021_
) : GenUseCase_1021_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1021_ @Inject constructor(
    private val repository: GenRepositoryImpl_1021_
) : GenUseCase_1021_<String, List<GenModel_1021_>> {
    override suspend fun invoke(params: String): List<GenModel_1021_> = repository.search(params)
}

abstract class GenMapper_1021_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1021_ : GenMapper_1021_<GenModel_1021_, String>() {
    override fun map(input: GenModel_1021_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1021_ : GenMapper_1021_<String, GenModel_1021_>() {
    override fun map(input: String): GenModel_1021_ {
        val parts = input.split(":")
        return GenModel_1021_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1021_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1021_,
    private val saveUseCase: GenSaveUseCase_1021_,
    private val deleteUseCase: GenDeleteUseCase_1021_,
    private val searchUseCase: GenSearchUseCase_1021_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1021_>(GenState_1021_.Idle)
    val state: StateFlow<GenState_1021_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1021_) {
        when (event) {
            is GenEvent_1021_.Load -> loadAll()
            is GenEvent_1021_.Update -> save(event.model)
            is GenEvent_1021_.Delete -> delete(event.id)
            is GenEvent_1021_.Refresh -> loadAll()
            is GenEvent_1021_.Search -> search(event.query)
            is GenEvent_1021_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1021_.Loading; _state.value = GenState_1021_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1021_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1021_.Success(searchUseCase(query)) } }
}
