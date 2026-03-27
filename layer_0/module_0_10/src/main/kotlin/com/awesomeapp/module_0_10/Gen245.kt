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

data class GenModel_245_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_245_ {
    data class Load(val id: Long) : GenEvent_245_()
    data class Update(val model: GenModel_245_) : GenEvent_245_()
    data class Delete(val id: Long) : GenEvent_245_()
    data object Refresh : GenEvent_245_()
    data class Search(val query: String) : GenEvent_245_()
    data class Filter(val predicate: String) : GenEvent_245_()
}

sealed class GenState_245_ {
    data object Idle : GenState_245_()
    data object Loading : GenState_245_()
    data class Success(val items: List<GenModel_245_>) : GenState_245_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_245_()
    data class Partial(val items: List<GenModel_245_>, val hasMore: Boolean) : GenState_245_()
}

interface GenRepository_245_ {
    suspend fun getAll(): List<GenModel_245_>
    suspend fun getById(id: Long): GenModel_245_?
    suspend fun save(model: GenModel_245_): GenModel_245_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_245_>
}

@Singleton
class GenRepositoryImpl_245_ @Inject constructor() : GenRepository_245_ {
    private val store = mutableMapOf<Long, GenModel_245_>()
    override suspend fun getAll(): List<GenModel_245_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_245_? = store[id]
    override suspend fun save(model: GenModel_245_): GenModel_245_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_245_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_245_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_245_ @Inject constructor(
    private val repository: GenRepositoryImpl_245_
) : GenUseCase_245_<Unit, List<GenModel_245_>> {
    override suspend fun invoke(params: Unit): List<GenModel_245_> = repository.getAll()
}

class GenSaveUseCase_245_ @Inject constructor(
    private val repository: GenRepositoryImpl_245_
) : GenUseCase_245_<GenModel_245_, GenModel_245_> {
    override suspend fun invoke(params: GenModel_245_): GenModel_245_ = repository.save(params)
}

class GenDeleteUseCase_245_ @Inject constructor(
    private val repository: GenRepositoryImpl_245_
) : GenUseCase_245_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_245_ @Inject constructor(
    private val repository: GenRepositoryImpl_245_
) : GenUseCase_245_<String, List<GenModel_245_>> {
    override suspend fun invoke(params: String): List<GenModel_245_> = repository.search(params)
}

abstract class GenMapper_245_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_245_ : GenMapper_245_<GenModel_245_, String>() {
    override fun map(input: GenModel_245_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_245_ : GenMapper_245_<String, GenModel_245_>() {
    override fun map(input: String): GenModel_245_ {
        val parts = input.split(":")
        return GenModel_245_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_245_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_245_,
    private val saveUseCase: GenSaveUseCase_245_,
    private val deleteUseCase: GenDeleteUseCase_245_,
    private val searchUseCase: GenSearchUseCase_245_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_245_>(GenState_245_.Idle)
    val state: StateFlow<GenState_245_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_245_) {
        when (event) {
            is GenEvent_245_.Load -> loadAll()
            is GenEvent_245_.Update -> save(event.model)
            is GenEvent_245_.Delete -> delete(event.id)
            is GenEvent_245_.Refresh -> loadAll()
            is GenEvent_245_.Search -> search(event.query)
            is GenEvent_245_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_245_.Loading; _state.value = GenState_245_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_245_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_245_.Success(searchUseCase(query)) } }
}
