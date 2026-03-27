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

data class GenModel_1871_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1871_ {
    data class Load(val id: Long) : GenEvent_1871_()
    data class Update(val model: GenModel_1871_) : GenEvent_1871_()
    data class Delete(val id: Long) : GenEvent_1871_()
    data object Refresh : GenEvent_1871_()
    data class Search(val query: String) : GenEvent_1871_()
    data class Filter(val predicate: String) : GenEvent_1871_()
}

sealed class GenState_1871_ {
    data object Idle : GenState_1871_()
    data object Loading : GenState_1871_()
    data class Success(val items: List<GenModel_1871_>) : GenState_1871_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1871_()
    data class Partial(val items: List<GenModel_1871_>, val hasMore: Boolean) : GenState_1871_()
}

interface GenRepository_1871_ {
    suspend fun getAll(): List<GenModel_1871_>
    suspend fun getById(id: Long): GenModel_1871_?
    suspend fun save(model: GenModel_1871_): GenModel_1871_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1871_>
}

@Singleton
class GenRepositoryImpl_1871_ @Inject constructor() : GenRepository_1871_ {
    private val store = mutableMapOf<Long, GenModel_1871_>()
    override suspend fun getAll(): List<GenModel_1871_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1871_? = store[id]
    override suspend fun save(model: GenModel_1871_): GenModel_1871_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1871_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1871_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1871_ @Inject constructor(
    private val repository: GenRepositoryImpl_1871_
) : GenUseCase_1871_<Unit, List<GenModel_1871_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1871_> = repository.getAll()
}

class GenSaveUseCase_1871_ @Inject constructor(
    private val repository: GenRepositoryImpl_1871_
) : GenUseCase_1871_<GenModel_1871_, GenModel_1871_> {
    override suspend fun invoke(params: GenModel_1871_): GenModel_1871_ = repository.save(params)
}

class GenDeleteUseCase_1871_ @Inject constructor(
    private val repository: GenRepositoryImpl_1871_
) : GenUseCase_1871_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1871_ @Inject constructor(
    private val repository: GenRepositoryImpl_1871_
) : GenUseCase_1871_<String, List<GenModel_1871_>> {
    override suspend fun invoke(params: String): List<GenModel_1871_> = repository.search(params)
}

abstract class GenMapper_1871_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1871_ : GenMapper_1871_<GenModel_1871_, String>() {
    override fun map(input: GenModel_1871_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1871_ : GenMapper_1871_<String, GenModel_1871_>() {
    override fun map(input: String): GenModel_1871_ {
        val parts = input.split(":")
        return GenModel_1871_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1871_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1871_,
    private val saveUseCase: GenSaveUseCase_1871_,
    private val deleteUseCase: GenDeleteUseCase_1871_,
    private val searchUseCase: GenSearchUseCase_1871_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1871_>(GenState_1871_.Idle)
    val state: StateFlow<GenState_1871_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1871_) {
        when (event) {
            is GenEvent_1871_.Load -> loadAll()
            is GenEvent_1871_.Update -> save(event.model)
            is GenEvent_1871_.Delete -> delete(event.id)
            is GenEvent_1871_.Refresh -> loadAll()
            is GenEvent_1871_.Search -> search(event.query)
            is GenEvent_1871_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1871_.Loading; _state.value = GenState_1871_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1871_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1871_.Success(searchUseCase(query)) } }
}
