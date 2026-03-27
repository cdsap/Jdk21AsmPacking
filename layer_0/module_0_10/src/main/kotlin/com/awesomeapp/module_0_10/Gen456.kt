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

data class GenModel_456_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_456_ {
    data class Load(val id: Long) : GenEvent_456_()
    data class Update(val model: GenModel_456_) : GenEvent_456_()
    data class Delete(val id: Long) : GenEvent_456_()
    data object Refresh : GenEvent_456_()
    data class Search(val query: String) : GenEvent_456_()
    data class Filter(val predicate: String) : GenEvent_456_()
}

sealed class GenState_456_ {
    data object Idle : GenState_456_()
    data object Loading : GenState_456_()
    data class Success(val items: List<GenModel_456_>) : GenState_456_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_456_()
    data class Partial(val items: List<GenModel_456_>, val hasMore: Boolean) : GenState_456_()
}

interface GenRepository_456_ {
    suspend fun getAll(): List<GenModel_456_>
    suspend fun getById(id: Long): GenModel_456_?
    suspend fun save(model: GenModel_456_): GenModel_456_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_456_>
}

@Singleton
class GenRepositoryImpl_456_ @Inject constructor() : GenRepository_456_ {
    private val store = mutableMapOf<Long, GenModel_456_>()
    override suspend fun getAll(): List<GenModel_456_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_456_? = store[id]
    override suspend fun save(model: GenModel_456_): GenModel_456_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_456_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_456_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_456_ @Inject constructor(
    private val repository: GenRepositoryImpl_456_
) : GenUseCase_456_<Unit, List<GenModel_456_>> {
    override suspend fun invoke(params: Unit): List<GenModel_456_> = repository.getAll()
}

class GenSaveUseCase_456_ @Inject constructor(
    private val repository: GenRepositoryImpl_456_
) : GenUseCase_456_<GenModel_456_, GenModel_456_> {
    override suspend fun invoke(params: GenModel_456_): GenModel_456_ = repository.save(params)
}

class GenDeleteUseCase_456_ @Inject constructor(
    private val repository: GenRepositoryImpl_456_
) : GenUseCase_456_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_456_ @Inject constructor(
    private val repository: GenRepositoryImpl_456_
) : GenUseCase_456_<String, List<GenModel_456_>> {
    override suspend fun invoke(params: String): List<GenModel_456_> = repository.search(params)
}

abstract class GenMapper_456_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_456_ : GenMapper_456_<GenModel_456_, String>() {
    override fun map(input: GenModel_456_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_456_ : GenMapper_456_<String, GenModel_456_>() {
    override fun map(input: String): GenModel_456_ {
        val parts = input.split(":")
        return GenModel_456_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_456_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_456_,
    private val saveUseCase: GenSaveUseCase_456_,
    private val deleteUseCase: GenDeleteUseCase_456_,
    private val searchUseCase: GenSearchUseCase_456_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_456_>(GenState_456_.Idle)
    val state: StateFlow<GenState_456_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_456_) {
        when (event) {
            is GenEvent_456_.Load -> loadAll()
            is GenEvent_456_.Update -> save(event.model)
            is GenEvent_456_.Delete -> delete(event.id)
            is GenEvent_456_.Refresh -> loadAll()
            is GenEvent_456_.Search -> search(event.query)
            is GenEvent_456_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_456_.Loading; _state.value = GenState_456_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_456_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_456_.Success(searchUseCase(query)) } }
}
