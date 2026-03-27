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

data class GenModel_282_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_282_ {
    data class Load(val id: Long) : GenEvent_282_()
    data class Update(val model: GenModel_282_) : GenEvent_282_()
    data class Delete(val id: Long) : GenEvent_282_()
    data object Refresh : GenEvent_282_()
    data class Search(val query: String) : GenEvent_282_()
    data class Filter(val predicate: String) : GenEvent_282_()
}

sealed class GenState_282_ {
    data object Idle : GenState_282_()
    data object Loading : GenState_282_()
    data class Success(val items: List<GenModel_282_>) : GenState_282_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_282_()
    data class Partial(val items: List<GenModel_282_>, val hasMore: Boolean) : GenState_282_()
}

interface GenRepository_282_ {
    suspend fun getAll(): List<GenModel_282_>
    suspend fun getById(id: Long): GenModel_282_?
    suspend fun save(model: GenModel_282_): GenModel_282_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_282_>
}

@Singleton
class GenRepositoryImpl_282_ @Inject constructor() : GenRepository_282_ {
    private val store = mutableMapOf<Long, GenModel_282_>()
    override suspend fun getAll(): List<GenModel_282_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_282_? = store[id]
    override suspend fun save(model: GenModel_282_): GenModel_282_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_282_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_282_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_282_ @Inject constructor(
    private val repository: GenRepositoryImpl_282_
) : GenUseCase_282_<Unit, List<GenModel_282_>> {
    override suspend fun invoke(params: Unit): List<GenModel_282_> = repository.getAll()
}

class GenSaveUseCase_282_ @Inject constructor(
    private val repository: GenRepositoryImpl_282_
) : GenUseCase_282_<GenModel_282_, GenModel_282_> {
    override suspend fun invoke(params: GenModel_282_): GenModel_282_ = repository.save(params)
}

class GenDeleteUseCase_282_ @Inject constructor(
    private val repository: GenRepositoryImpl_282_
) : GenUseCase_282_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_282_ @Inject constructor(
    private val repository: GenRepositoryImpl_282_
) : GenUseCase_282_<String, List<GenModel_282_>> {
    override suspend fun invoke(params: String): List<GenModel_282_> = repository.search(params)
}

abstract class GenMapper_282_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_282_ : GenMapper_282_<GenModel_282_, String>() {
    override fun map(input: GenModel_282_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_282_ : GenMapper_282_<String, GenModel_282_>() {
    override fun map(input: String): GenModel_282_ {
        val parts = input.split(":")
        return GenModel_282_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_282_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_282_,
    private val saveUseCase: GenSaveUseCase_282_,
    private val deleteUseCase: GenDeleteUseCase_282_,
    private val searchUseCase: GenSearchUseCase_282_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_282_>(GenState_282_.Idle)
    val state: StateFlow<GenState_282_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_282_) {
        when (event) {
            is GenEvent_282_.Load -> loadAll()
            is GenEvent_282_.Update -> save(event.model)
            is GenEvent_282_.Delete -> delete(event.id)
            is GenEvent_282_.Refresh -> loadAll()
            is GenEvent_282_.Search -> search(event.query)
            is GenEvent_282_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_282_.Loading; _state.value = GenState_282_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_282_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_282_.Success(searchUseCase(query)) } }
}
