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

data class GenModel_773_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_773_ {
    data class Load(val id: Long) : GenEvent_773_()
    data class Update(val model: GenModel_773_) : GenEvent_773_()
    data class Delete(val id: Long) : GenEvent_773_()
    data object Refresh : GenEvent_773_()
    data class Search(val query: String) : GenEvent_773_()
    data class Filter(val predicate: String) : GenEvent_773_()
}

sealed class GenState_773_ {
    data object Idle : GenState_773_()
    data object Loading : GenState_773_()
    data class Success(val items: List<GenModel_773_>) : GenState_773_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_773_()
    data class Partial(val items: List<GenModel_773_>, val hasMore: Boolean) : GenState_773_()
}

interface GenRepository_773_ {
    suspend fun getAll(): List<GenModel_773_>
    suspend fun getById(id: Long): GenModel_773_?
    suspend fun save(model: GenModel_773_): GenModel_773_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_773_>
}

@Singleton
class GenRepositoryImpl_773_ @Inject constructor() : GenRepository_773_ {
    private val store = mutableMapOf<Long, GenModel_773_>()
    override suspend fun getAll(): List<GenModel_773_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_773_? = store[id]
    override suspend fun save(model: GenModel_773_): GenModel_773_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_773_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_773_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_773_ @Inject constructor(
    private val repository: GenRepositoryImpl_773_
) : GenUseCase_773_<Unit, List<GenModel_773_>> {
    override suspend fun invoke(params: Unit): List<GenModel_773_> = repository.getAll()
}

class GenSaveUseCase_773_ @Inject constructor(
    private val repository: GenRepositoryImpl_773_
) : GenUseCase_773_<GenModel_773_, GenModel_773_> {
    override suspend fun invoke(params: GenModel_773_): GenModel_773_ = repository.save(params)
}

class GenDeleteUseCase_773_ @Inject constructor(
    private val repository: GenRepositoryImpl_773_
) : GenUseCase_773_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_773_ @Inject constructor(
    private val repository: GenRepositoryImpl_773_
) : GenUseCase_773_<String, List<GenModel_773_>> {
    override suspend fun invoke(params: String): List<GenModel_773_> = repository.search(params)
}

abstract class GenMapper_773_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_773_ : GenMapper_773_<GenModel_773_, String>() {
    override fun map(input: GenModel_773_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_773_ : GenMapper_773_<String, GenModel_773_>() {
    override fun map(input: String): GenModel_773_ {
        val parts = input.split(":")
        return GenModel_773_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_773_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_773_,
    private val saveUseCase: GenSaveUseCase_773_,
    private val deleteUseCase: GenDeleteUseCase_773_,
    private val searchUseCase: GenSearchUseCase_773_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_773_>(GenState_773_.Idle)
    val state: StateFlow<GenState_773_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_773_) {
        when (event) {
            is GenEvent_773_.Load -> loadAll()
            is GenEvent_773_.Update -> save(event.model)
            is GenEvent_773_.Delete -> delete(event.id)
            is GenEvent_773_.Refresh -> loadAll()
            is GenEvent_773_.Search -> search(event.query)
            is GenEvent_773_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_773_.Loading; _state.value = GenState_773_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_773_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_773_.Success(searchUseCase(query)) } }
}
