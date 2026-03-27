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

data class GenModel_793_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_793_ {
    data class Load(val id: Long) : GenEvent_793_()
    data class Update(val model: GenModel_793_) : GenEvent_793_()
    data class Delete(val id: Long) : GenEvent_793_()
    data object Refresh : GenEvent_793_()
    data class Search(val query: String) : GenEvent_793_()
    data class Filter(val predicate: String) : GenEvent_793_()
}

sealed class GenState_793_ {
    data object Idle : GenState_793_()
    data object Loading : GenState_793_()
    data class Success(val items: List<GenModel_793_>) : GenState_793_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_793_()
    data class Partial(val items: List<GenModel_793_>, val hasMore: Boolean) : GenState_793_()
}

interface GenRepository_793_ {
    suspend fun getAll(): List<GenModel_793_>
    suspend fun getById(id: Long): GenModel_793_?
    suspend fun save(model: GenModel_793_): GenModel_793_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_793_>
}

@Singleton
class GenRepositoryImpl_793_ @Inject constructor() : GenRepository_793_ {
    private val store = mutableMapOf<Long, GenModel_793_>()
    override suspend fun getAll(): List<GenModel_793_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_793_? = store[id]
    override suspend fun save(model: GenModel_793_): GenModel_793_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_793_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_793_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_793_ @Inject constructor(
    private val repository: GenRepositoryImpl_793_
) : GenUseCase_793_<Unit, List<GenModel_793_>> {
    override suspend fun invoke(params: Unit): List<GenModel_793_> = repository.getAll()
}

class GenSaveUseCase_793_ @Inject constructor(
    private val repository: GenRepositoryImpl_793_
) : GenUseCase_793_<GenModel_793_, GenModel_793_> {
    override suspend fun invoke(params: GenModel_793_): GenModel_793_ = repository.save(params)
}

class GenDeleteUseCase_793_ @Inject constructor(
    private val repository: GenRepositoryImpl_793_
) : GenUseCase_793_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_793_ @Inject constructor(
    private val repository: GenRepositoryImpl_793_
) : GenUseCase_793_<String, List<GenModel_793_>> {
    override suspend fun invoke(params: String): List<GenModel_793_> = repository.search(params)
}

abstract class GenMapper_793_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_793_ : GenMapper_793_<GenModel_793_, String>() {
    override fun map(input: GenModel_793_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_793_ : GenMapper_793_<String, GenModel_793_>() {
    override fun map(input: String): GenModel_793_ {
        val parts = input.split(":")
        return GenModel_793_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_793_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_793_,
    private val saveUseCase: GenSaveUseCase_793_,
    private val deleteUseCase: GenDeleteUseCase_793_,
    private val searchUseCase: GenSearchUseCase_793_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_793_>(GenState_793_.Idle)
    val state: StateFlow<GenState_793_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_793_) {
        when (event) {
            is GenEvent_793_.Load -> loadAll()
            is GenEvent_793_.Update -> save(event.model)
            is GenEvent_793_.Delete -> delete(event.id)
            is GenEvent_793_.Refresh -> loadAll()
            is GenEvent_793_.Search -> search(event.query)
            is GenEvent_793_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_793_.Loading; _state.value = GenState_793_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_793_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_793_.Success(searchUseCase(query)) } }
}
