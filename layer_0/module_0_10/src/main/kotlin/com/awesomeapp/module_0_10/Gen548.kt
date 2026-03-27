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

data class GenModel_548_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_548_ {
    data class Load(val id: Long) : GenEvent_548_()
    data class Update(val model: GenModel_548_) : GenEvent_548_()
    data class Delete(val id: Long) : GenEvent_548_()
    data object Refresh : GenEvent_548_()
    data class Search(val query: String) : GenEvent_548_()
    data class Filter(val predicate: String) : GenEvent_548_()
}

sealed class GenState_548_ {
    data object Idle : GenState_548_()
    data object Loading : GenState_548_()
    data class Success(val items: List<GenModel_548_>) : GenState_548_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_548_()
    data class Partial(val items: List<GenModel_548_>, val hasMore: Boolean) : GenState_548_()
}

interface GenRepository_548_ {
    suspend fun getAll(): List<GenModel_548_>
    suspend fun getById(id: Long): GenModel_548_?
    suspend fun save(model: GenModel_548_): GenModel_548_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_548_>
}

@Singleton
class GenRepositoryImpl_548_ @Inject constructor() : GenRepository_548_ {
    private val store = mutableMapOf<Long, GenModel_548_>()
    override suspend fun getAll(): List<GenModel_548_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_548_? = store[id]
    override suspend fun save(model: GenModel_548_): GenModel_548_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_548_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_548_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_548_ @Inject constructor(
    private val repository: GenRepositoryImpl_548_
) : GenUseCase_548_<Unit, List<GenModel_548_>> {
    override suspend fun invoke(params: Unit): List<GenModel_548_> = repository.getAll()
}

class GenSaveUseCase_548_ @Inject constructor(
    private val repository: GenRepositoryImpl_548_
) : GenUseCase_548_<GenModel_548_, GenModel_548_> {
    override suspend fun invoke(params: GenModel_548_): GenModel_548_ = repository.save(params)
}

class GenDeleteUseCase_548_ @Inject constructor(
    private val repository: GenRepositoryImpl_548_
) : GenUseCase_548_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_548_ @Inject constructor(
    private val repository: GenRepositoryImpl_548_
) : GenUseCase_548_<String, List<GenModel_548_>> {
    override suspend fun invoke(params: String): List<GenModel_548_> = repository.search(params)
}

abstract class GenMapper_548_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_548_ : GenMapper_548_<GenModel_548_, String>() {
    override fun map(input: GenModel_548_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_548_ : GenMapper_548_<String, GenModel_548_>() {
    override fun map(input: String): GenModel_548_ {
        val parts = input.split(":")
        return GenModel_548_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_548_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_548_,
    private val saveUseCase: GenSaveUseCase_548_,
    private val deleteUseCase: GenDeleteUseCase_548_,
    private val searchUseCase: GenSearchUseCase_548_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_548_>(GenState_548_.Idle)
    val state: StateFlow<GenState_548_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_548_) {
        when (event) {
            is GenEvent_548_.Load -> loadAll()
            is GenEvent_548_.Update -> save(event.model)
            is GenEvent_548_.Delete -> delete(event.id)
            is GenEvent_548_.Refresh -> loadAll()
            is GenEvent_548_.Search -> search(event.query)
            is GenEvent_548_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_548_.Loading; _state.value = GenState_548_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_548_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_548_.Success(searchUseCase(query)) } }
}
