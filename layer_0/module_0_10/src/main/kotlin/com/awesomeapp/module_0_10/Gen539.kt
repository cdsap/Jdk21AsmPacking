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

data class GenModel_539_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_539_ {
    data class Load(val id: Long) : GenEvent_539_()
    data class Update(val model: GenModel_539_) : GenEvent_539_()
    data class Delete(val id: Long) : GenEvent_539_()
    data object Refresh : GenEvent_539_()
    data class Search(val query: String) : GenEvent_539_()
    data class Filter(val predicate: String) : GenEvent_539_()
}

sealed class GenState_539_ {
    data object Idle : GenState_539_()
    data object Loading : GenState_539_()
    data class Success(val items: List<GenModel_539_>) : GenState_539_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_539_()
    data class Partial(val items: List<GenModel_539_>, val hasMore: Boolean) : GenState_539_()
}

interface GenRepository_539_ {
    suspend fun getAll(): List<GenModel_539_>
    suspend fun getById(id: Long): GenModel_539_?
    suspend fun save(model: GenModel_539_): GenModel_539_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_539_>
}

@Singleton
class GenRepositoryImpl_539_ @Inject constructor() : GenRepository_539_ {
    private val store = mutableMapOf<Long, GenModel_539_>()
    override suspend fun getAll(): List<GenModel_539_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_539_? = store[id]
    override suspend fun save(model: GenModel_539_): GenModel_539_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_539_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_539_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_539_ @Inject constructor(
    private val repository: GenRepositoryImpl_539_
) : GenUseCase_539_<Unit, List<GenModel_539_>> {
    override suspend fun invoke(params: Unit): List<GenModel_539_> = repository.getAll()
}

class GenSaveUseCase_539_ @Inject constructor(
    private val repository: GenRepositoryImpl_539_
) : GenUseCase_539_<GenModel_539_, GenModel_539_> {
    override suspend fun invoke(params: GenModel_539_): GenModel_539_ = repository.save(params)
}

class GenDeleteUseCase_539_ @Inject constructor(
    private val repository: GenRepositoryImpl_539_
) : GenUseCase_539_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_539_ @Inject constructor(
    private val repository: GenRepositoryImpl_539_
) : GenUseCase_539_<String, List<GenModel_539_>> {
    override suspend fun invoke(params: String): List<GenModel_539_> = repository.search(params)
}

abstract class GenMapper_539_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_539_ : GenMapper_539_<GenModel_539_, String>() {
    override fun map(input: GenModel_539_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_539_ : GenMapper_539_<String, GenModel_539_>() {
    override fun map(input: String): GenModel_539_ {
        val parts = input.split(":")
        return GenModel_539_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_539_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_539_,
    private val saveUseCase: GenSaveUseCase_539_,
    private val deleteUseCase: GenDeleteUseCase_539_,
    private val searchUseCase: GenSearchUseCase_539_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_539_>(GenState_539_.Idle)
    val state: StateFlow<GenState_539_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_539_) {
        when (event) {
            is GenEvent_539_.Load -> loadAll()
            is GenEvent_539_.Update -> save(event.model)
            is GenEvent_539_.Delete -> delete(event.id)
            is GenEvent_539_.Refresh -> loadAll()
            is GenEvent_539_.Search -> search(event.query)
            is GenEvent_539_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_539_.Loading; _state.value = GenState_539_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_539_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_539_.Success(searchUseCase(query)) } }
}
