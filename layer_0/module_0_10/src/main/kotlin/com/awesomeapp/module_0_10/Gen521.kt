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

data class GenModel_521_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_521_ {
    data class Load(val id: Long) : GenEvent_521_()
    data class Update(val model: GenModel_521_) : GenEvent_521_()
    data class Delete(val id: Long) : GenEvent_521_()
    data object Refresh : GenEvent_521_()
    data class Search(val query: String) : GenEvent_521_()
    data class Filter(val predicate: String) : GenEvent_521_()
}

sealed class GenState_521_ {
    data object Idle : GenState_521_()
    data object Loading : GenState_521_()
    data class Success(val items: List<GenModel_521_>) : GenState_521_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_521_()
    data class Partial(val items: List<GenModel_521_>, val hasMore: Boolean) : GenState_521_()
}

interface GenRepository_521_ {
    suspend fun getAll(): List<GenModel_521_>
    suspend fun getById(id: Long): GenModel_521_?
    suspend fun save(model: GenModel_521_): GenModel_521_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_521_>
}

@Singleton
class GenRepositoryImpl_521_ @Inject constructor() : GenRepository_521_ {
    private val store = mutableMapOf<Long, GenModel_521_>()
    override suspend fun getAll(): List<GenModel_521_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_521_? = store[id]
    override suspend fun save(model: GenModel_521_): GenModel_521_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_521_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_521_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_521_ @Inject constructor(
    private val repository: GenRepositoryImpl_521_
) : GenUseCase_521_<Unit, List<GenModel_521_>> {
    override suspend fun invoke(params: Unit): List<GenModel_521_> = repository.getAll()
}

class GenSaveUseCase_521_ @Inject constructor(
    private val repository: GenRepositoryImpl_521_
) : GenUseCase_521_<GenModel_521_, GenModel_521_> {
    override suspend fun invoke(params: GenModel_521_): GenModel_521_ = repository.save(params)
}

class GenDeleteUseCase_521_ @Inject constructor(
    private val repository: GenRepositoryImpl_521_
) : GenUseCase_521_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_521_ @Inject constructor(
    private val repository: GenRepositoryImpl_521_
) : GenUseCase_521_<String, List<GenModel_521_>> {
    override suspend fun invoke(params: String): List<GenModel_521_> = repository.search(params)
}

abstract class GenMapper_521_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_521_ : GenMapper_521_<GenModel_521_, String>() {
    override fun map(input: GenModel_521_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_521_ : GenMapper_521_<String, GenModel_521_>() {
    override fun map(input: String): GenModel_521_ {
        val parts = input.split(":")
        return GenModel_521_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_521_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_521_,
    private val saveUseCase: GenSaveUseCase_521_,
    private val deleteUseCase: GenDeleteUseCase_521_,
    private val searchUseCase: GenSearchUseCase_521_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_521_>(GenState_521_.Idle)
    val state: StateFlow<GenState_521_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_521_) {
        when (event) {
            is GenEvent_521_.Load -> loadAll()
            is GenEvent_521_.Update -> save(event.model)
            is GenEvent_521_.Delete -> delete(event.id)
            is GenEvent_521_.Refresh -> loadAll()
            is GenEvent_521_.Search -> search(event.query)
            is GenEvent_521_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_521_.Loading; _state.value = GenState_521_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_521_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_521_.Success(searchUseCase(query)) } }
}
