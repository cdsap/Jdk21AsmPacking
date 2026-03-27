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

data class GenModel_190_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_190_ {
    data class Load(val id: Long) : GenEvent_190_()
    data class Update(val model: GenModel_190_) : GenEvent_190_()
    data class Delete(val id: Long) : GenEvent_190_()
    data object Refresh : GenEvent_190_()
    data class Search(val query: String) : GenEvent_190_()
    data class Filter(val predicate: String) : GenEvent_190_()
}

sealed class GenState_190_ {
    data object Idle : GenState_190_()
    data object Loading : GenState_190_()
    data class Success(val items: List<GenModel_190_>) : GenState_190_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_190_()
    data class Partial(val items: List<GenModel_190_>, val hasMore: Boolean) : GenState_190_()
}

interface GenRepository_190_ {
    suspend fun getAll(): List<GenModel_190_>
    suspend fun getById(id: Long): GenModel_190_?
    suspend fun save(model: GenModel_190_): GenModel_190_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_190_>
}

@Singleton
class GenRepositoryImpl_190_ @Inject constructor() : GenRepository_190_ {
    private val store = mutableMapOf<Long, GenModel_190_>()
    override suspend fun getAll(): List<GenModel_190_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_190_? = store[id]
    override suspend fun save(model: GenModel_190_): GenModel_190_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_190_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_190_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_190_ @Inject constructor(
    private val repository: GenRepositoryImpl_190_
) : GenUseCase_190_<Unit, List<GenModel_190_>> {
    override suspend fun invoke(params: Unit): List<GenModel_190_> = repository.getAll()
}

class GenSaveUseCase_190_ @Inject constructor(
    private val repository: GenRepositoryImpl_190_
) : GenUseCase_190_<GenModel_190_, GenModel_190_> {
    override suspend fun invoke(params: GenModel_190_): GenModel_190_ = repository.save(params)
}

class GenDeleteUseCase_190_ @Inject constructor(
    private val repository: GenRepositoryImpl_190_
) : GenUseCase_190_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_190_ @Inject constructor(
    private val repository: GenRepositoryImpl_190_
) : GenUseCase_190_<String, List<GenModel_190_>> {
    override suspend fun invoke(params: String): List<GenModel_190_> = repository.search(params)
}

abstract class GenMapper_190_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_190_ : GenMapper_190_<GenModel_190_, String>() {
    override fun map(input: GenModel_190_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_190_ : GenMapper_190_<String, GenModel_190_>() {
    override fun map(input: String): GenModel_190_ {
        val parts = input.split(":")
        return GenModel_190_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_190_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_190_,
    private val saveUseCase: GenSaveUseCase_190_,
    private val deleteUseCase: GenDeleteUseCase_190_,
    private val searchUseCase: GenSearchUseCase_190_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_190_>(GenState_190_.Idle)
    val state: StateFlow<GenState_190_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_190_) {
        when (event) {
            is GenEvent_190_.Load -> loadAll()
            is GenEvent_190_.Update -> save(event.model)
            is GenEvent_190_.Delete -> delete(event.id)
            is GenEvent_190_.Refresh -> loadAll()
            is GenEvent_190_.Search -> search(event.query)
            is GenEvent_190_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_190_.Loading; _state.value = GenState_190_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_190_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_190_.Success(searchUseCase(query)) } }
}
