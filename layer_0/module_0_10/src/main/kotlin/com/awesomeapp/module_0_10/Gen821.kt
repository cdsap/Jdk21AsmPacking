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

data class GenModel_821_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_821_ {
    data class Load(val id: Long) : GenEvent_821_()
    data class Update(val model: GenModel_821_) : GenEvent_821_()
    data class Delete(val id: Long) : GenEvent_821_()
    data object Refresh : GenEvent_821_()
    data class Search(val query: String) : GenEvent_821_()
    data class Filter(val predicate: String) : GenEvent_821_()
}

sealed class GenState_821_ {
    data object Idle : GenState_821_()
    data object Loading : GenState_821_()
    data class Success(val items: List<GenModel_821_>) : GenState_821_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_821_()
    data class Partial(val items: List<GenModel_821_>, val hasMore: Boolean) : GenState_821_()
}

interface GenRepository_821_ {
    suspend fun getAll(): List<GenModel_821_>
    suspend fun getById(id: Long): GenModel_821_?
    suspend fun save(model: GenModel_821_): GenModel_821_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_821_>
}

@Singleton
class GenRepositoryImpl_821_ @Inject constructor() : GenRepository_821_ {
    private val store = mutableMapOf<Long, GenModel_821_>()
    override suspend fun getAll(): List<GenModel_821_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_821_? = store[id]
    override suspend fun save(model: GenModel_821_): GenModel_821_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_821_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_821_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_821_ @Inject constructor(
    private val repository: GenRepositoryImpl_821_
) : GenUseCase_821_<Unit, List<GenModel_821_>> {
    override suspend fun invoke(params: Unit): List<GenModel_821_> = repository.getAll()
}

class GenSaveUseCase_821_ @Inject constructor(
    private val repository: GenRepositoryImpl_821_
) : GenUseCase_821_<GenModel_821_, GenModel_821_> {
    override suspend fun invoke(params: GenModel_821_): GenModel_821_ = repository.save(params)
}

class GenDeleteUseCase_821_ @Inject constructor(
    private val repository: GenRepositoryImpl_821_
) : GenUseCase_821_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_821_ @Inject constructor(
    private val repository: GenRepositoryImpl_821_
) : GenUseCase_821_<String, List<GenModel_821_>> {
    override suspend fun invoke(params: String): List<GenModel_821_> = repository.search(params)
}

abstract class GenMapper_821_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_821_ : GenMapper_821_<GenModel_821_, String>() {
    override fun map(input: GenModel_821_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_821_ : GenMapper_821_<String, GenModel_821_>() {
    override fun map(input: String): GenModel_821_ {
        val parts = input.split(":")
        return GenModel_821_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_821_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_821_,
    private val saveUseCase: GenSaveUseCase_821_,
    private val deleteUseCase: GenDeleteUseCase_821_,
    private val searchUseCase: GenSearchUseCase_821_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_821_>(GenState_821_.Idle)
    val state: StateFlow<GenState_821_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_821_) {
        when (event) {
            is GenEvent_821_.Load -> loadAll()
            is GenEvent_821_.Update -> save(event.model)
            is GenEvent_821_.Delete -> delete(event.id)
            is GenEvent_821_.Refresh -> loadAll()
            is GenEvent_821_.Search -> search(event.query)
            is GenEvent_821_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_821_.Loading; _state.value = GenState_821_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_821_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_821_.Success(searchUseCase(query)) } }
}
