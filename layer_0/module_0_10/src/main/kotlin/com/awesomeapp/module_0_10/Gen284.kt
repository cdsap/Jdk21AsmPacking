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

data class GenModel_284_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_284_ {
    data class Load(val id: Long) : GenEvent_284_()
    data class Update(val model: GenModel_284_) : GenEvent_284_()
    data class Delete(val id: Long) : GenEvent_284_()
    data object Refresh : GenEvent_284_()
    data class Search(val query: String) : GenEvent_284_()
    data class Filter(val predicate: String) : GenEvent_284_()
}

sealed class GenState_284_ {
    data object Idle : GenState_284_()
    data object Loading : GenState_284_()
    data class Success(val items: List<GenModel_284_>) : GenState_284_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_284_()
    data class Partial(val items: List<GenModel_284_>, val hasMore: Boolean) : GenState_284_()
}

interface GenRepository_284_ {
    suspend fun getAll(): List<GenModel_284_>
    suspend fun getById(id: Long): GenModel_284_?
    suspend fun save(model: GenModel_284_): GenModel_284_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_284_>
}

@Singleton
class GenRepositoryImpl_284_ @Inject constructor() : GenRepository_284_ {
    private val store = mutableMapOf<Long, GenModel_284_>()
    override suspend fun getAll(): List<GenModel_284_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_284_? = store[id]
    override suspend fun save(model: GenModel_284_): GenModel_284_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_284_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_284_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_284_ @Inject constructor(
    private val repository: GenRepositoryImpl_284_
) : GenUseCase_284_<Unit, List<GenModel_284_>> {
    override suspend fun invoke(params: Unit): List<GenModel_284_> = repository.getAll()
}

class GenSaveUseCase_284_ @Inject constructor(
    private val repository: GenRepositoryImpl_284_
) : GenUseCase_284_<GenModel_284_, GenModel_284_> {
    override suspend fun invoke(params: GenModel_284_): GenModel_284_ = repository.save(params)
}

class GenDeleteUseCase_284_ @Inject constructor(
    private val repository: GenRepositoryImpl_284_
) : GenUseCase_284_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_284_ @Inject constructor(
    private val repository: GenRepositoryImpl_284_
) : GenUseCase_284_<String, List<GenModel_284_>> {
    override suspend fun invoke(params: String): List<GenModel_284_> = repository.search(params)
}

abstract class GenMapper_284_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_284_ : GenMapper_284_<GenModel_284_, String>() {
    override fun map(input: GenModel_284_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_284_ : GenMapper_284_<String, GenModel_284_>() {
    override fun map(input: String): GenModel_284_ {
        val parts = input.split(":")
        return GenModel_284_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_284_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_284_,
    private val saveUseCase: GenSaveUseCase_284_,
    private val deleteUseCase: GenDeleteUseCase_284_,
    private val searchUseCase: GenSearchUseCase_284_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_284_>(GenState_284_.Idle)
    val state: StateFlow<GenState_284_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_284_) {
        when (event) {
            is GenEvent_284_.Load -> loadAll()
            is GenEvent_284_.Update -> save(event.model)
            is GenEvent_284_.Delete -> delete(event.id)
            is GenEvent_284_.Refresh -> loadAll()
            is GenEvent_284_.Search -> search(event.query)
            is GenEvent_284_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_284_.Loading; _state.value = GenState_284_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_284_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_284_.Success(searchUseCase(query)) } }
}
