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

data class GenModel_1579_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1579_ {
    data class Load(val id: Long) : GenEvent_1579_()
    data class Update(val model: GenModel_1579_) : GenEvent_1579_()
    data class Delete(val id: Long) : GenEvent_1579_()
    data object Refresh : GenEvent_1579_()
    data class Search(val query: String) : GenEvent_1579_()
    data class Filter(val predicate: String) : GenEvent_1579_()
}

sealed class GenState_1579_ {
    data object Idle : GenState_1579_()
    data object Loading : GenState_1579_()
    data class Success(val items: List<GenModel_1579_>) : GenState_1579_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1579_()
    data class Partial(val items: List<GenModel_1579_>, val hasMore: Boolean) : GenState_1579_()
}

interface GenRepository_1579_ {
    suspend fun getAll(): List<GenModel_1579_>
    suspend fun getById(id: Long): GenModel_1579_?
    suspend fun save(model: GenModel_1579_): GenModel_1579_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1579_>
}

@Singleton
class GenRepositoryImpl_1579_ @Inject constructor() : GenRepository_1579_ {
    private val store = mutableMapOf<Long, GenModel_1579_>()
    override suspend fun getAll(): List<GenModel_1579_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1579_? = store[id]
    override suspend fun save(model: GenModel_1579_): GenModel_1579_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1579_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1579_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1579_ @Inject constructor(
    private val repository: GenRepositoryImpl_1579_
) : GenUseCase_1579_<Unit, List<GenModel_1579_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1579_> = repository.getAll()
}

class GenSaveUseCase_1579_ @Inject constructor(
    private val repository: GenRepositoryImpl_1579_
) : GenUseCase_1579_<GenModel_1579_, GenModel_1579_> {
    override suspend fun invoke(params: GenModel_1579_): GenModel_1579_ = repository.save(params)
}

class GenDeleteUseCase_1579_ @Inject constructor(
    private val repository: GenRepositoryImpl_1579_
) : GenUseCase_1579_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1579_ @Inject constructor(
    private val repository: GenRepositoryImpl_1579_
) : GenUseCase_1579_<String, List<GenModel_1579_>> {
    override suspend fun invoke(params: String): List<GenModel_1579_> = repository.search(params)
}

abstract class GenMapper_1579_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1579_ : GenMapper_1579_<GenModel_1579_, String>() {
    override fun map(input: GenModel_1579_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1579_ : GenMapper_1579_<String, GenModel_1579_>() {
    override fun map(input: String): GenModel_1579_ {
        val parts = input.split(":")
        return GenModel_1579_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1579_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1579_,
    private val saveUseCase: GenSaveUseCase_1579_,
    private val deleteUseCase: GenDeleteUseCase_1579_,
    private val searchUseCase: GenSearchUseCase_1579_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1579_>(GenState_1579_.Idle)
    val state: StateFlow<GenState_1579_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1579_) {
        when (event) {
            is GenEvent_1579_.Load -> loadAll()
            is GenEvent_1579_.Update -> save(event.model)
            is GenEvent_1579_.Delete -> delete(event.id)
            is GenEvent_1579_.Refresh -> loadAll()
            is GenEvent_1579_.Search -> search(event.query)
            is GenEvent_1579_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1579_.Loading; _state.value = GenState_1579_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1579_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1579_.Success(searchUseCase(query)) } }
}
