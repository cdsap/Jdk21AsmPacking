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

data class GenModel_782_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_782_ {
    data class Load(val id: Long) : GenEvent_782_()
    data class Update(val model: GenModel_782_) : GenEvent_782_()
    data class Delete(val id: Long) : GenEvent_782_()
    data object Refresh : GenEvent_782_()
    data class Search(val query: String) : GenEvent_782_()
    data class Filter(val predicate: String) : GenEvent_782_()
}

sealed class GenState_782_ {
    data object Idle : GenState_782_()
    data object Loading : GenState_782_()
    data class Success(val items: List<GenModel_782_>) : GenState_782_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_782_()
    data class Partial(val items: List<GenModel_782_>, val hasMore: Boolean) : GenState_782_()
}

interface GenRepository_782_ {
    suspend fun getAll(): List<GenModel_782_>
    suspend fun getById(id: Long): GenModel_782_?
    suspend fun save(model: GenModel_782_): GenModel_782_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_782_>
}

@Singleton
class GenRepositoryImpl_782_ @Inject constructor() : GenRepository_782_ {
    private val store = mutableMapOf<Long, GenModel_782_>()
    override suspend fun getAll(): List<GenModel_782_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_782_? = store[id]
    override suspend fun save(model: GenModel_782_): GenModel_782_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_782_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_782_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_782_ @Inject constructor(
    private val repository: GenRepositoryImpl_782_
) : GenUseCase_782_<Unit, List<GenModel_782_>> {
    override suspend fun invoke(params: Unit): List<GenModel_782_> = repository.getAll()
}

class GenSaveUseCase_782_ @Inject constructor(
    private val repository: GenRepositoryImpl_782_
) : GenUseCase_782_<GenModel_782_, GenModel_782_> {
    override suspend fun invoke(params: GenModel_782_): GenModel_782_ = repository.save(params)
}

class GenDeleteUseCase_782_ @Inject constructor(
    private val repository: GenRepositoryImpl_782_
) : GenUseCase_782_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_782_ @Inject constructor(
    private val repository: GenRepositoryImpl_782_
) : GenUseCase_782_<String, List<GenModel_782_>> {
    override suspend fun invoke(params: String): List<GenModel_782_> = repository.search(params)
}

abstract class GenMapper_782_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_782_ : GenMapper_782_<GenModel_782_, String>() {
    override fun map(input: GenModel_782_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_782_ : GenMapper_782_<String, GenModel_782_>() {
    override fun map(input: String): GenModel_782_ {
        val parts = input.split(":")
        return GenModel_782_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_782_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_782_,
    private val saveUseCase: GenSaveUseCase_782_,
    private val deleteUseCase: GenDeleteUseCase_782_,
    private val searchUseCase: GenSearchUseCase_782_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_782_>(GenState_782_.Idle)
    val state: StateFlow<GenState_782_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_782_) {
        when (event) {
            is GenEvent_782_.Load -> loadAll()
            is GenEvent_782_.Update -> save(event.model)
            is GenEvent_782_.Delete -> delete(event.id)
            is GenEvent_782_.Refresh -> loadAll()
            is GenEvent_782_.Search -> search(event.query)
            is GenEvent_782_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_782_.Loading; _state.value = GenState_782_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_782_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_782_.Success(searchUseCase(query)) } }
}
