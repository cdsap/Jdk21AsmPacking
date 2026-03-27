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

data class GenModel_671_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_671_ {
    data class Load(val id: Long) : GenEvent_671_()
    data class Update(val model: GenModel_671_) : GenEvent_671_()
    data class Delete(val id: Long) : GenEvent_671_()
    data object Refresh : GenEvent_671_()
    data class Search(val query: String) : GenEvent_671_()
    data class Filter(val predicate: String) : GenEvent_671_()
}

sealed class GenState_671_ {
    data object Idle : GenState_671_()
    data object Loading : GenState_671_()
    data class Success(val items: List<GenModel_671_>) : GenState_671_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_671_()
    data class Partial(val items: List<GenModel_671_>, val hasMore: Boolean) : GenState_671_()
}

interface GenRepository_671_ {
    suspend fun getAll(): List<GenModel_671_>
    suspend fun getById(id: Long): GenModel_671_?
    suspend fun save(model: GenModel_671_): GenModel_671_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_671_>
}

@Singleton
class GenRepositoryImpl_671_ @Inject constructor() : GenRepository_671_ {
    private val store = mutableMapOf<Long, GenModel_671_>()
    override suspend fun getAll(): List<GenModel_671_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_671_? = store[id]
    override suspend fun save(model: GenModel_671_): GenModel_671_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_671_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_671_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_671_ @Inject constructor(
    private val repository: GenRepositoryImpl_671_
) : GenUseCase_671_<Unit, List<GenModel_671_>> {
    override suspend fun invoke(params: Unit): List<GenModel_671_> = repository.getAll()
}

class GenSaveUseCase_671_ @Inject constructor(
    private val repository: GenRepositoryImpl_671_
) : GenUseCase_671_<GenModel_671_, GenModel_671_> {
    override suspend fun invoke(params: GenModel_671_): GenModel_671_ = repository.save(params)
}

class GenDeleteUseCase_671_ @Inject constructor(
    private val repository: GenRepositoryImpl_671_
) : GenUseCase_671_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_671_ @Inject constructor(
    private val repository: GenRepositoryImpl_671_
) : GenUseCase_671_<String, List<GenModel_671_>> {
    override suspend fun invoke(params: String): List<GenModel_671_> = repository.search(params)
}

abstract class GenMapper_671_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_671_ : GenMapper_671_<GenModel_671_, String>() {
    override fun map(input: GenModel_671_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_671_ : GenMapper_671_<String, GenModel_671_>() {
    override fun map(input: String): GenModel_671_ {
        val parts = input.split(":")
        return GenModel_671_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_671_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_671_,
    private val saveUseCase: GenSaveUseCase_671_,
    private val deleteUseCase: GenDeleteUseCase_671_,
    private val searchUseCase: GenSearchUseCase_671_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_671_>(GenState_671_.Idle)
    val state: StateFlow<GenState_671_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_671_) {
        when (event) {
            is GenEvent_671_.Load -> loadAll()
            is GenEvent_671_.Update -> save(event.model)
            is GenEvent_671_.Delete -> delete(event.id)
            is GenEvent_671_.Refresh -> loadAll()
            is GenEvent_671_.Search -> search(event.query)
            is GenEvent_671_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_671_.Loading; _state.value = GenState_671_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_671_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_671_.Success(searchUseCase(query)) } }
}
