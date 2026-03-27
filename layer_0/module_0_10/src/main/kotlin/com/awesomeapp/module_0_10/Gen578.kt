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

data class GenModel_578_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_578_ {
    data class Load(val id: Long) : GenEvent_578_()
    data class Update(val model: GenModel_578_) : GenEvent_578_()
    data class Delete(val id: Long) : GenEvent_578_()
    data object Refresh : GenEvent_578_()
    data class Search(val query: String) : GenEvent_578_()
    data class Filter(val predicate: String) : GenEvent_578_()
}

sealed class GenState_578_ {
    data object Idle : GenState_578_()
    data object Loading : GenState_578_()
    data class Success(val items: List<GenModel_578_>) : GenState_578_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_578_()
    data class Partial(val items: List<GenModel_578_>, val hasMore: Boolean) : GenState_578_()
}

interface GenRepository_578_ {
    suspend fun getAll(): List<GenModel_578_>
    suspend fun getById(id: Long): GenModel_578_?
    suspend fun save(model: GenModel_578_): GenModel_578_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_578_>
}

@Singleton
class GenRepositoryImpl_578_ @Inject constructor() : GenRepository_578_ {
    private val store = mutableMapOf<Long, GenModel_578_>()
    override suspend fun getAll(): List<GenModel_578_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_578_? = store[id]
    override suspend fun save(model: GenModel_578_): GenModel_578_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_578_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_578_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_578_ @Inject constructor(
    private val repository: GenRepositoryImpl_578_
) : GenUseCase_578_<Unit, List<GenModel_578_>> {
    override suspend fun invoke(params: Unit): List<GenModel_578_> = repository.getAll()
}

class GenSaveUseCase_578_ @Inject constructor(
    private val repository: GenRepositoryImpl_578_
) : GenUseCase_578_<GenModel_578_, GenModel_578_> {
    override suspend fun invoke(params: GenModel_578_): GenModel_578_ = repository.save(params)
}

class GenDeleteUseCase_578_ @Inject constructor(
    private val repository: GenRepositoryImpl_578_
) : GenUseCase_578_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_578_ @Inject constructor(
    private val repository: GenRepositoryImpl_578_
) : GenUseCase_578_<String, List<GenModel_578_>> {
    override suspend fun invoke(params: String): List<GenModel_578_> = repository.search(params)
}

abstract class GenMapper_578_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_578_ : GenMapper_578_<GenModel_578_, String>() {
    override fun map(input: GenModel_578_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_578_ : GenMapper_578_<String, GenModel_578_>() {
    override fun map(input: String): GenModel_578_ {
        val parts = input.split(":")
        return GenModel_578_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_578_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_578_,
    private val saveUseCase: GenSaveUseCase_578_,
    private val deleteUseCase: GenDeleteUseCase_578_,
    private val searchUseCase: GenSearchUseCase_578_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_578_>(GenState_578_.Idle)
    val state: StateFlow<GenState_578_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_578_) {
        when (event) {
            is GenEvent_578_.Load -> loadAll()
            is GenEvent_578_.Update -> save(event.model)
            is GenEvent_578_.Delete -> delete(event.id)
            is GenEvent_578_.Refresh -> loadAll()
            is GenEvent_578_.Search -> search(event.query)
            is GenEvent_578_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_578_.Loading; _state.value = GenState_578_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_578_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_578_.Success(searchUseCase(query)) } }
}
