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

data class GenModel_3423_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3423_ {
    data class Load(val id: Long) : GenEvent_3423_()
    data class Update(val model: GenModel_3423_) : GenEvent_3423_()
    data class Delete(val id: Long) : GenEvent_3423_()
    data object Refresh : GenEvent_3423_()
    data class Search(val query: String) : GenEvent_3423_()
    data class Filter(val predicate: String) : GenEvent_3423_()
}

sealed class GenState_3423_ {
    data object Idle : GenState_3423_()
    data object Loading : GenState_3423_()
    data class Success(val items: List<GenModel_3423_>) : GenState_3423_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3423_()
    data class Partial(val items: List<GenModel_3423_>, val hasMore: Boolean) : GenState_3423_()
}

interface GenRepository_3423_ {
    suspend fun getAll(): List<GenModel_3423_>
    suspend fun getById(id: Long): GenModel_3423_?
    suspend fun save(model: GenModel_3423_): GenModel_3423_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3423_>
}

@Singleton
class GenRepositoryImpl_3423_ @Inject constructor() : GenRepository_3423_ {
    private val store = mutableMapOf<Long, GenModel_3423_>()
    override suspend fun getAll(): List<GenModel_3423_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3423_? = store[id]
    override suspend fun save(model: GenModel_3423_): GenModel_3423_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3423_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3423_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3423_ @Inject constructor(
    private val repository: GenRepositoryImpl_3423_
) : GenUseCase_3423_<Unit, List<GenModel_3423_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3423_> = repository.getAll()
}

class GenSaveUseCase_3423_ @Inject constructor(
    private val repository: GenRepositoryImpl_3423_
) : GenUseCase_3423_<GenModel_3423_, GenModel_3423_> {
    override suspend fun invoke(params: GenModel_3423_): GenModel_3423_ = repository.save(params)
}

class GenDeleteUseCase_3423_ @Inject constructor(
    private val repository: GenRepositoryImpl_3423_
) : GenUseCase_3423_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3423_ @Inject constructor(
    private val repository: GenRepositoryImpl_3423_
) : GenUseCase_3423_<String, List<GenModel_3423_>> {
    override suspend fun invoke(params: String): List<GenModel_3423_> = repository.search(params)
}

abstract class GenMapper_3423_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3423_ : GenMapper_3423_<GenModel_3423_, String>() {
    override fun map(input: GenModel_3423_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3423_ : GenMapper_3423_<String, GenModel_3423_>() {
    override fun map(input: String): GenModel_3423_ {
        val parts = input.split(":")
        return GenModel_3423_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3423_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3423_,
    private val saveUseCase: GenSaveUseCase_3423_,
    private val deleteUseCase: GenDeleteUseCase_3423_,
    private val searchUseCase: GenSearchUseCase_3423_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3423_>(GenState_3423_.Idle)
    val state: StateFlow<GenState_3423_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3423_) {
        when (event) {
            is GenEvent_3423_.Load -> loadAll()
            is GenEvent_3423_.Update -> save(event.model)
            is GenEvent_3423_.Delete -> delete(event.id)
            is GenEvent_3423_.Refresh -> loadAll()
            is GenEvent_3423_.Search -> search(event.query)
            is GenEvent_3423_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3423_.Loading; _state.value = GenState_3423_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3423_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3423_.Success(searchUseCase(query)) } }
}
