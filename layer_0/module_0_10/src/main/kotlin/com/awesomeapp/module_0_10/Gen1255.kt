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

data class GenModel_1255_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1255_ {
    data class Load(val id: Long) : GenEvent_1255_()
    data class Update(val model: GenModel_1255_) : GenEvent_1255_()
    data class Delete(val id: Long) : GenEvent_1255_()
    data object Refresh : GenEvent_1255_()
    data class Search(val query: String) : GenEvent_1255_()
    data class Filter(val predicate: String) : GenEvent_1255_()
}

sealed class GenState_1255_ {
    data object Idle : GenState_1255_()
    data object Loading : GenState_1255_()
    data class Success(val items: List<GenModel_1255_>) : GenState_1255_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1255_()
    data class Partial(val items: List<GenModel_1255_>, val hasMore: Boolean) : GenState_1255_()
}

interface GenRepository_1255_ {
    suspend fun getAll(): List<GenModel_1255_>
    suspend fun getById(id: Long): GenModel_1255_?
    suspend fun save(model: GenModel_1255_): GenModel_1255_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1255_>
}

@Singleton
class GenRepositoryImpl_1255_ @Inject constructor() : GenRepository_1255_ {
    private val store = mutableMapOf<Long, GenModel_1255_>()
    override suspend fun getAll(): List<GenModel_1255_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1255_? = store[id]
    override suspend fun save(model: GenModel_1255_): GenModel_1255_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1255_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1255_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1255_ @Inject constructor(
    private val repository: GenRepositoryImpl_1255_
) : GenUseCase_1255_<Unit, List<GenModel_1255_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1255_> = repository.getAll()
}

class GenSaveUseCase_1255_ @Inject constructor(
    private val repository: GenRepositoryImpl_1255_
) : GenUseCase_1255_<GenModel_1255_, GenModel_1255_> {
    override suspend fun invoke(params: GenModel_1255_): GenModel_1255_ = repository.save(params)
}

class GenDeleteUseCase_1255_ @Inject constructor(
    private val repository: GenRepositoryImpl_1255_
) : GenUseCase_1255_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1255_ @Inject constructor(
    private val repository: GenRepositoryImpl_1255_
) : GenUseCase_1255_<String, List<GenModel_1255_>> {
    override suspend fun invoke(params: String): List<GenModel_1255_> = repository.search(params)
}

abstract class GenMapper_1255_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1255_ : GenMapper_1255_<GenModel_1255_, String>() {
    override fun map(input: GenModel_1255_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1255_ : GenMapper_1255_<String, GenModel_1255_>() {
    override fun map(input: String): GenModel_1255_ {
        val parts = input.split(":")
        return GenModel_1255_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1255_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1255_,
    private val saveUseCase: GenSaveUseCase_1255_,
    private val deleteUseCase: GenDeleteUseCase_1255_,
    private val searchUseCase: GenSearchUseCase_1255_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1255_>(GenState_1255_.Idle)
    val state: StateFlow<GenState_1255_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1255_) {
        when (event) {
            is GenEvent_1255_.Load -> loadAll()
            is GenEvent_1255_.Update -> save(event.model)
            is GenEvent_1255_.Delete -> delete(event.id)
            is GenEvent_1255_.Refresh -> loadAll()
            is GenEvent_1255_.Search -> search(event.query)
            is GenEvent_1255_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1255_.Loading; _state.value = GenState_1255_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1255_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1255_.Success(searchUseCase(query)) } }
}
