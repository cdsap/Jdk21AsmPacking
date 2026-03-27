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

data class GenModel_91_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_91_ {
    data class Load(val id: Long) : GenEvent_91_()
    data class Update(val model: GenModel_91_) : GenEvent_91_()
    data class Delete(val id: Long) : GenEvent_91_()
    data object Refresh : GenEvent_91_()
    data class Search(val query: String) : GenEvent_91_()
    data class Filter(val predicate: String) : GenEvent_91_()
}

sealed class GenState_91_ {
    data object Idle : GenState_91_()
    data object Loading : GenState_91_()
    data class Success(val items: List<GenModel_91_>) : GenState_91_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_91_()
    data class Partial(val items: List<GenModel_91_>, val hasMore: Boolean) : GenState_91_()
}

interface GenRepository_91_ {
    suspend fun getAll(): List<GenModel_91_>
    suspend fun getById(id: Long): GenModel_91_?
    suspend fun save(model: GenModel_91_): GenModel_91_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_91_>
}

@Singleton
class GenRepositoryImpl_91_ @Inject constructor() : GenRepository_91_ {
    private val store = mutableMapOf<Long, GenModel_91_>()
    override suspend fun getAll(): List<GenModel_91_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_91_? = store[id]
    override suspend fun save(model: GenModel_91_): GenModel_91_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_91_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_91_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_91_ @Inject constructor(
    private val repository: GenRepositoryImpl_91_
) : GenUseCase_91_<Unit, List<GenModel_91_>> {
    override suspend fun invoke(params: Unit): List<GenModel_91_> = repository.getAll()
}

class GenSaveUseCase_91_ @Inject constructor(
    private val repository: GenRepositoryImpl_91_
) : GenUseCase_91_<GenModel_91_, GenModel_91_> {
    override suspend fun invoke(params: GenModel_91_): GenModel_91_ = repository.save(params)
}

class GenDeleteUseCase_91_ @Inject constructor(
    private val repository: GenRepositoryImpl_91_
) : GenUseCase_91_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_91_ @Inject constructor(
    private val repository: GenRepositoryImpl_91_
) : GenUseCase_91_<String, List<GenModel_91_>> {
    override suspend fun invoke(params: String): List<GenModel_91_> = repository.search(params)
}

abstract class GenMapper_91_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_91_ : GenMapper_91_<GenModel_91_, String>() {
    override fun map(input: GenModel_91_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_91_ : GenMapper_91_<String, GenModel_91_>() {
    override fun map(input: String): GenModel_91_ {
        val parts = input.split(":")
        return GenModel_91_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_91_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_91_,
    private val saveUseCase: GenSaveUseCase_91_,
    private val deleteUseCase: GenDeleteUseCase_91_,
    private val searchUseCase: GenSearchUseCase_91_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_91_>(GenState_91_.Idle)
    val state: StateFlow<GenState_91_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_91_) {
        when (event) {
            is GenEvent_91_.Load -> loadAll()
            is GenEvent_91_.Update -> save(event.model)
            is GenEvent_91_.Delete -> delete(event.id)
            is GenEvent_91_.Refresh -> loadAll()
            is GenEvent_91_.Search -> search(event.query)
            is GenEvent_91_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_91_.Loading; _state.value = GenState_91_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_91_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_91_.Success(searchUseCase(query)) } }
}
