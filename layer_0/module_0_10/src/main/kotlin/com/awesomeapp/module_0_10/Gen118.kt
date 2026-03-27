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

data class GenModel_118_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_118_ {
    data class Load(val id: Long) : GenEvent_118_()
    data class Update(val model: GenModel_118_) : GenEvent_118_()
    data class Delete(val id: Long) : GenEvent_118_()
    data object Refresh : GenEvent_118_()
    data class Search(val query: String) : GenEvent_118_()
    data class Filter(val predicate: String) : GenEvent_118_()
}

sealed class GenState_118_ {
    data object Idle : GenState_118_()
    data object Loading : GenState_118_()
    data class Success(val items: List<GenModel_118_>) : GenState_118_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_118_()
    data class Partial(val items: List<GenModel_118_>, val hasMore: Boolean) : GenState_118_()
}

interface GenRepository_118_ {
    suspend fun getAll(): List<GenModel_118_>
    suspend fun getById(id: Long): GenModel_118_?
    suspend fun save(model: GenModel_118_): GenModel_118_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_118_>
}

@Singleton
class GenRepositoryImpl_118_ @Inject constructor() : GenRepository_118_ {
    private val store = mutableMapOf<Long, GenModel_118_>()
    override suspend fun getAll(): List<GenModel_118_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_118_? = store[id]
    override suspend fun save(model: GenModel_118_): GenModel_118_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_118_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_118_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_118_ @Inject constructor(
    private val repository: GenRepositoryImpl_118_
) : GenUseCase_118_<Unit, List<GenModel_118_>> {
    override suspend fun invoke(params: Unit): List<GenModel_118_> = repository.getAll()
}

class GenSaveUseCase_118_ @Inject constructor(
    private val repository: GenRepositoryImpl_118_
) : GenUseCase_118_<GenModel_118_, GenModel_118_> {
    override suspend fun invoke(params: GenModel_118_): GenModel_118_ = repository.save(params)
}

class GenDeleteUseCase_118_ @Inject constructor(
    private val repository: GenRepositoryImpl_118_
) : GenUseCase_118_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_118_ @Inject constructor(
    private val repository: GenRepositoryImpl_118_
) : GenUseCase_118_<String, List<GenModel_118_>> {
    override suspend fun invoke(params: String): List<GenModel_118_> = repository.search(params)
}

abstract class GenMapper_118_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_118_ : GenMapper_118_<GenModel_118_, String>() {
    override fun map(input: GenModel_118_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_118_ : GenMapper_118_<String, GenModel_118_>() {
    override fun map(input: String): GenModel_118_ {
        val parts = input.split(":")
        return GenModel_118_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_118_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_118_,
    private val saveUseCase: GenSaveUseCase_118_,
    private val deleteUseCase: GenDeleteUseCase_118_,
    private val searchUseCase: GenSearchUseCase_118_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_118_>(GenState_118_.Idle)
    val state: StateFlow<GenState_118_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_118_) {
        when (event) {
            is GenEvent_118_.Load -> loadAll()
            is GenEvent_118_.Update -> save(event.model)
            is GenEvent_118_.Delete -> delete(event.id)
            is GenEvent_118_.Refresh -> loadAll()
            is GenEvent_118_.Search -> search(event.query)
            is GenEvent_118_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_118_.Loading; _state.value = GenState_118_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_118_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_118_.Success(searchUseCase(query)) } }
}
