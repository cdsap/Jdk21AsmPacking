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

data class GenModel_380_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_380_ {
    data class Load(val id: Long) : GenEvent_380_()
    data class Update(val model: GenModel_380_) : GenEvent_380_()
    data class Delete(val id: Long) : GenEvent_380_()
    data object Refresh : GenEvent_380_()
    data class Search(val query: String) : GenEvent_380_()
    data class Filter(val predicate: String) : GenEvent_380_()
}

sealed class GenState_380_ {
    data object Idle : GenState_380_()
    data object Loading : GenState_380_()
    data class Success(val items: List<GenModel_380_>) : GenState_380_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_380_()
    data class Partial(val items: List<GenModel_380_>, val hasMore: Boolean) : GenState_380_()
}

interface GenRepository_380_ {
    suspend fun getAll(): List<GenModel_380_>
    suspend fun getById(id: Long): GenModel_380_?
    suspend fun save(model: GenModel_380_): GenModel_380_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_380_>
}

@Singleton
class GenRepositoryImpl_380_ @Inject constructor() : GenRepository_380_ {
    private val store = mutableMapOf<Long, GenModel_380_>()
    override suspend fun getAll(): List<GenModel_380_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_380_? = store[id]
    override suspend fun save(model: GenModel_380_): GenModel_380_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_380_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_380_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_380_ @Inject constructor(
    private val repository: GenRepositoryImpl_380_
) : GenUseCase_380_<Unit, List<GenModel_380_>> {
    override suspend fun invoke(params: Unit): List<GenModel_380_> = repository.getAll()
}

class GenSaveUseCase_380_ @Inject constructor(
    private val repository: GenRepositoryImpl_380_
) : GenUseCase_380_<GenModel_380_, GenModel_380_> {
    override suspend fun invoke(params: GenModel_380_): GenModel_380_ = repository.save(params)
}

class GenDeleteUseCase_380_ @Inject constructor(
    private val repository: GenRepositoryImpl_380_
) : GenUseCase_380_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_380_ @Inject constructor(
    private val repository: GenRepositoryImpl_380_
) : GenUseCase_380_<String, List<GenModel_380_>> {
    override suspend fun invoke(params: String): List<GenModel_380_> = repository.search(params)
}

abstract class GenMapper_380_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_380_ : GenMapper_380_<GenModel_380_, String>() {
    override fun map(input: GenModel_380_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_380_ : GenMapper_380_<String, GenModel_380_>() {
    override fun map(input: String): GenModel_380_ {
        val parts = input.split(":")
        return GenModel_380_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_380_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_380_,
    private val saveUseCase: GenSaveUseCase_380_,
    private val deleteUseCase: GenDeleteUseCase_380_,
    private val searchUseCase: GenSearchUseCase_380_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_380_>(GenState_380_.Idle)
    val state: StateFlow<GenState_380_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_380_) {
        when (event) {
            is GenEvent_380_.Load -> loadAll()
            is GenEvent_380_.Update -> save(event.model)
            is GenEvent_380_.Delete -> delete(event.id)
            is GenEvent_380_.Refresh -> loadAll()
            is GenEvent_380_.Search -> search(event.query)
            is GenEvent_380_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_380_.Loading; _state.value = GenState_380_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_380_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_380_.Success(searchUseCase(query)) } }
}
