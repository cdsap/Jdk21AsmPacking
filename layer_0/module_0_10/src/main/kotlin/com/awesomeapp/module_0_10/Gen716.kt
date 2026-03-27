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

data class GenModel_716_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_716_ {
    data class Load(val id: Long) : GenEvent_716_()
    data class Update(val model: GenModel_716_) : GenEvent_716_()
    data class Delete(val id: Long) : GenEvent_716_()
    data object Refresh : GenEvent_716_()
    data class Search(val query: String) : GenEvent_716_()
    data class Filter(val predicate: String) : GenEvent_716_()
}

sealed class GenState_716_ {
    data object Idle : GenState_716_()
    data object Loading : GenState_716_()
    data class Success(val items: List<GenModel_716_>) : GenState_716_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_716_()
    data class Partial(val items: List<GenModel_716_>, val hasMore: Boolean) : GenState_716_()
}

interface GenRepository_716_ {
    suspend fun getAll(): List<GenModel_716_>
    suspend fun getById(id: Long): GenModel_716_?
    suspend fun save(model: GenModel_716_): GenModel_716_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_716_>
}

@Singleton
class GenRepositoryImpl_716_ @Inject constructor() : GenRepository_716_ {
    private val store = mutableMapOf<Long, GenModel_716_>()
    override suspend fun getAll(): List<GenModel_716_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_716_? = store[id]
    override suspend fun save(model: GenModel_716_): GenModel_716_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_716_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_716_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_716_ @Inject constructor(
    private val repository: GenRepositoryImpl_716_
) : GenUseCase_716_<Unit, List<GenModel_716_>> {
    override suspend fun invoke(params: Unit): List<GenModel_716_> = repository.getAll()
}

class GenSaveUseCase_716_ @Inject constructor(
    private val repository: GenRepositoryImpl_716_
) : GenUseCase_716_<GenModel_716_, GenModel_716_> {
    override suspend fun invoke(params: GenModel_716_): GenModel_716_ = repository.save(params)
}

class GenDeleteUseCase_716_ @Inject constructor(
    private val repository: GenRepositoryImpl_716_
) : GenUseCase_716_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_716_ @Inject constructor(
    private val repository: GenRepositoryImpl_716_
) : GenUseCase_716_<String, List<GenModel_716_>> {
    override suspend fun invoke(params: String): List<GenModel_716_> = repository.search(params)
}

abstract class GenMapper_716_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_716_ : GenMapper_716_<GenModel_716_, String>() {
    override fun map(input: GenModel_716_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_716_ : GenMapper_716_<String, GenModel_716_>() {
    override fun map(input: String): GenModel_716_ {
        val parts = input.split(":")
        return GenModel_716_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_716_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_716_,
    private val saveUseCase: GenSaveUseCase_716_,
    private val deleteUseCase: GenDeleteUseCase_716_,
    private val searchUseCase: GenSearchUseCase_716_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_716_>(GenState_716_.Idle)
    val state: StateFlow<GenState_716_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_716_) {
        when (event) {
            is GenEvent_716_.Load -> loadAll()
            is GenEvent_716_.Update -> save(event.model)
            is GenEvent_716_.Delete -> delete(event.id)
            is GenEvent_716_.Refresh -> loadAll()
            is GenEvent_716_.Search -> search(event.query)
            is GenEvent_716_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_716_.Loading; _state.value = GenState_716_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_716_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_716_.Success(searchUseCase(query)) } }
}
