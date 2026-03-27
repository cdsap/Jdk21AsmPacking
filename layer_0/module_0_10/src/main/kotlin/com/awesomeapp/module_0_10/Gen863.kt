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

data class GenModel_863_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_863_ {
    data class Load(val id: Long) : GenEvent_863_()
    data class Update(val model: GenModel_863_) : GenEvent_863_()
    data class Delete(val id: Long) : GenEvent_863_()
    data object Refresh : GenEvent_863_()
    data class Search(val query: String) : GenEvent_863_()
    data class Filter(val predicate: String) : GenEvent_863_()
}

sealed class GenState_863_ {
    data object Idle : GenState_863_()
    data object Loading : GenState_863_()
    data class Success(val items: List<GenModel_863_>) : GenState_863_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_863_()
    data class Partial(val items: List<GenModel_863_>, val hasMore: Boolean) : GenState_863_()
}

interface GenRepository_863_ {
    suspend fun getAll(): List<GenModel_863_>
    suspend fun getById(id: Long): GenModel_863_?
    suspend fun save(model: GenModel_863_): GenModel_863_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_863_>
}

@Singleton
class GenRepositoryImpl_863_ @Inject constructor() : GenRepository_863_ {
    private val store = mutableMapOf<Long, GenModel_863_>()
    override suspend fun getAll(): List<GenModel_863_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_863_? = store[id]
    override suspend fun save(model: GenModel_863_): GenModel_863_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_863_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_863_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_863_ @Inject constructor(
    private val repository: GenRepositoryImpl_863_
) : GenUseCase_863_<Unit, List<GenModel_863_>> {
    override suspend fun invoke(params: Unit): List<GenModel_863_> = repository.getAll()
}

class GenSaveUseCase_863_ @Inject constructor(
    private val repository: GenRepositoryImpl_863_
) : GenUseCase_863_<GenModel_863_, GenModel_863_> {
    override suspend fun invoke(params: GenModel_863_): GenModel_863_ = repository.save(params)
}

class GenDeleteUseCase_863_ @Inject constructor(
    private val repository: GenRepositoryImpl_863_
) : GenUseCase_863_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_863_ @Inject constructor(
    private val repository: GenRepositoryImpl_863_
) : GenUseCase_863_<String, List<GenModel_863_>> {
    override suspend fun invoke(params: String): List<GenModel_863_> = repository.search(params)
}

abstract class GenMapper_863_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_863_ : GenMapper_863_<GenModel_863_, String>() {
    override fun map(input: GenModel_863_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_863_ : GenMapper_863_<String, GenModel_863_>() {
    override fun map(input: String): GenModel_863_ {
        val parts = input.split(":")
        return GenModel_863_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_863_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_863_,
    private val saveUseCase: GenSaveUseCase_863_,
    private val deleteUseCase: GenDeleteUseCase_863_,
    private val searchUseCase: GenSearchUseCase_863_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_863_>(GenState_863_.Idle)
    val state: StateFlow<GenState_863_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_863_) {
        when (event) {
            is GenEvent_863_.Load -> loadAll()
            is GenEvent_863_.Update -> save(event.model)
            is GenEvent_863_.Delete -> delete(event.id)
            is GenEvent_863_.Refresh -> loadAll()
            is GenEvent_863_.Search -> search(event.query)
            is GenEvent_863_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_863_.Loading; _state.value = GenState_863_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_863_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_863_.Success(searchUseCase(query)) } }
}
