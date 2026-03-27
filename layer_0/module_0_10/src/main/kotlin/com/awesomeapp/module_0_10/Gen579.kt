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

data class GenModel_579_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_579_ {
    data class Load(val id: Long) : GenEvent_579_()
    data class Update(val model: GenModel_579_) : GenEvent_579_()
    data class Delete(val id: Long) : GenEvent_579_()
    data object Refresh : GenEvent_579_()
    data class Search(val query: String) : GenEvent_579_()
    data class Filter(val predicate: String) : GenEvent_579_()
}

sealed class GenState_579_ {
    data object Idle : GenState_579_()
    data object Loading : GenState_579_()
    data class Success(val items: List<GenModel_579_>) : GenState_579_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_579_()
    data class Partial(val items: List<GenModel_579_>, val hasMore: Boolean) : GenState_579_()
}

interface GenRepository_579_ {
    suspend fun getAll(): List<GenModel_579_>
    suspend fun getById(id: Long): GenModel_579_?
    suspend fun save(model: GenModel_579_): GenModel_579_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_579_>
}

@Singleton
class GenRepositoryImpl_579_ @Inject constructor() : GenRepository_579_ {
    private val store = mutableMapOf<Long, GenModel_579_>()
    override suspend fun getAll(): List<GenModel_579_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_579_? = store[id]
    override suspend fun save(model: GenModel_579_): GenModel_579_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_579_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_579_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_579_ @Inject constructor(
    private val repository: GenRepositoryImpl_579_
) : GenUseCase_579_<Unit, List<GenModel_579_>> {
    override suspend fun invoke(params: Unit): List<GenModel_579_> = repository.getAll()
}

class GenSaveUseCase_579_ @Inject constructor(
    private val repository: GenRepositoryImpl_579_
) : GenUseCase_579_<GenModel_579_, GenModel_579_> {
    override suspend fun invoke(params: GenModel_579_): GenModel_579_ = repository.save(params)
}

class GenDeleteUseCase_579_ @Inject constructor(
    private val repository: GenRepositoryImpl_579_
) : GenUseCase_579_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_579_ @Inject constructor(
    private val repository: GenRepositoryImpl_579_
) : GenUseCase_579_<String, List<GenModel_579_>> {
    override suspend fun invoke(params: String): List<GenModel_579_> = repository.search(params)
}

abstract class GenMapper_579_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_579_ : GenMapper_579_<GenModel_579_, String>() {
    override fun map(input: GenModel_579_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_579_ : GenMapper_579_<String, GenModel_579_>() {
    override fun map(input: String): GenModel_579_ {
        val parts = input.split(":")
        return GenModel_579_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_579_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_579_,
    private val saveUseCase: GenSaveUseCase_579_,
    private val deleteUseCase: GenDeleteUseCase_579_,
    private val searchUseCase: GenSearchUseCase_579_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_579_>(GenState_579_.Idle)
    val state: StateFlow<GenState_579_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_579_) {
        when (event) {
            is GenEvent_579_.Load -> loadAll()
            is GenEvent_579_.Update -> save(event.model)
            is GenEvent_579_.Delete -> delete(event.id)
            is GenEvent_579_.Refresh -> loadAll()
            is GenEvent_579_.Search -> search(event.query)
            is GenEvent_579_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_579_.Loading; _state.value = GenState_579_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_579_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_579_.Success(searchUseCase(query)) } }
}
