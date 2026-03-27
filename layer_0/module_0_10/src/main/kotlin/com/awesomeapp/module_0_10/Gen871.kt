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

data class GenModel_871_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_871_ {
    data class Load(val id: Long) : GenEvent_871_()
    data class Update(val model: GenModel_871_) : GenEvent_871_()
    data class Delete(val id: Long) : GenEvent_871_()
    data object Refresh : GenEvent_871_()
    data class Search(val query: String) : GenEvent_871_()
    data class Filter(val predicate: String) : GenEvent_871_()
}

sealed class GenState_871_ {
    data object Idle : GenState_871_()
    data object Loading : GenState_871_()
    data class Success(val items: List<GenModel_871_>) : GenState_871_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_871_()
    data class Partial(val items: List<GenModel_871_>, val hasMore: Boolean) : GenState_871_()
}

interface GenRepository_871_ {
    suspend fun getAll(): List<GenModel_871_>
    suspend fun getById(id: Long): GenModel_871_?
    suspend fun save(model: GenModel_871_): GenModel_871_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_871_>
}

@Singleton
class GenRepositoryImpl_871_ @Inject constructor() : GenRepository_871_ {
    private val store = mutableMapOf<Long, GenModel_871_>()
    override suspend fun getAll(): List<GenModel_871_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_871_? = store[id]
    override suspend fun save(model: GenModel_871_): GenModel_871_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_871_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_871_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_871_ @Inject constructor(
    private val repository: GenRepositoryImpl_871_
) : GenUseCase_871_<Unit, List<GenModel_871_>> {
    override suspend fun invoke(params: Unit): List<GenModel_871_> = repository.getAll()
}

class GenSaveUseCase_871_ @Inject constructor(
    private val repository: GenRepositoryImpl_871_
) : GenUseCase_871_<GenModel_871_, GenModel_871_> {
    override suspend fun invoke(params: GenModel_871_): GenModel_871_ = repository.save(params)
}

class GenDeleteUseCase_871_ @Inject constructor(
    private val repository: GenRepositoryImpl_871_
) : GenUseCase_871_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_871_ @Inject constructor(
    private val repository: GenRepositoryImpl_871_
) : GenUseCase_871_<String, List<GenModel_871_>> {
    override suspend fun invoke(params: String): List<GenModel_871_> = repository.search(params)
}

abstract class GenMapper_871_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_871_ : GenMapper_871_<GenModel_871_, String>() {
    override fun map(input: GenModel_871_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_871_ : GenMapper_871_<String, GenModel_871_>() {
    override fun map(input: String): GenModel_871_ {
        val parts = input.split(":")
        return GenModel_871_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_871_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_871_,
    private val saveUseCase: GenSaveUseCase_871_,
    private val deleteUseCase: GenDeleteUseCase_871_,
    private val searchUseCase: GenSearchUseCase_871_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_871_>(GenState_871_.Idle)
    val state: StateFlow<GenState_871_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_871_) {
        when (event) {
            is GenEvent_871_.Load -> loadAll()
            is GenEvent_871_.Update -> save(event.model)
            is GenEvent_871_.Delete -> delete(event.id)
            is GenEvent_871_.Refresh -> loadAll()
            is GenEvent_871_.Search -> search(event.query)
            is GenEvent_871_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_871_.Loading; _state.value = GenState_871_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_871_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_871_.Success(searchUseCase(query)) } }
}
