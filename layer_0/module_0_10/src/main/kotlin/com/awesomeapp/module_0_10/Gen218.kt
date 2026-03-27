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

data class GenModel_218_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_218_ {
    data class Load(val id: Long) : GenEvent_218_()
    data class Update(val model: GenModel_218_) : GenEvent_218_()
    data class Delete(val id: Long) : GenEvent_218_()
    data object Refresh : GenEvent_218_()
    data class Search(val query: String) : GenEvent_218_()
    data class Filter(val predicate: String) : GenEvent_218_()
}

sealed class GenState_218_ {
    data object Idle : GenState_218_()
    data object Loading : GenState_218_()
    data class Success(val items: List<GenModel_218_>) : GenState_218_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_218_()
    data class Partial(val items: List<GenModel_218_>, val hasMore: Boolean) : GenState_218_()
}

interface GenRepository_218_ {
    suspend fun getAll(): List<GenModel_218_>
    suspend fun getById(id: Long): GenModel_218_?
    suspend fun save(model: GenModel_218_): GenModel_218_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_218_>
}

@Singleton
class GenRepositoryImpl_218_ @Inject constructor() : GenRepository_218_ {
    private val store = mutableMapOf<Long, GenModel_218_>()
    override suspend fun getAll(): List<GenModel_218_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_218_? = store[id]
    override suspend fun save(model: GenModel_218_): GenModel_218_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_218_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_218_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_218_ @Inject constructor(
    private val repository: GenRepositoryImpl_218_
) : GenUseCase_218_<Unit, List<GenModel_218_>> {
    override suspend fun invoke(params: Unit): List<GenModel_218_> = repository.getAll()
}

class GenSaveUseCase_218_ @Inject constructor(
    private val repository: GenRepositoryImpl_218_
) : GenUseCase_218_<GenModel_218_, GenModel_218_> {
    override suspend fun invoke(params: GenModel_218_): GenModel_218_ = repository.save(params)
}

class GenDeleteUseCase_218_ @Inject constructor(
    private val repository: GenRepositoryImpl_218_
) : GenUseCase_218_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_218_ @Inject constructor(
    private val repository: GenRepositoryImpl_218_
) : GenUseCase_218_<String, List<GenModel_218_>> {
    override suspend fun invoke(params: String): List<GenModel_218_> = repository.search(params)
}

abstract class GenMapper_218_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_218_ : GenMapper_218_<GenModel_218_, String>() {
    override fun map(input: GenModel_218_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_218_ : GenMapper_218_<String, GenModel_218_>() {
    override fun map(input: String): GenModel_218_ {
        val parts = input.split(":")
        return GenModel_218_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_218_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_218_,
    private val saveUseCase: GenSaveUseCase_218_,
    private val deleteUseCase: GenDeleteUseCase_218_,
    private val searchUseCase: GenSearchUseCase_218_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_218_>(GenState_218_.Idle)
    val state: StateFlow<GenState_218_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_218_) {
        when (event) {
            is GenEvent_218_.Load -> loadAll()
            is GenEvent_218_.Update -> save(event.model)
            is GenEvent_218_.Delete -> delete(event.id)
            is GenEvent_218_.Refresh -> loadAll()
            is GenEvent_218_.Search -> search(event.query)
            is GenEvent_218_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_218_.Loading; _state.value = GenState_218_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_218_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_218_.Success(searchUseCase(query)) } }
}
