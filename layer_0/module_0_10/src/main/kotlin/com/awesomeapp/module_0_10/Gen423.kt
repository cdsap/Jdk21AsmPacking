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

data class GenModel_423_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_423_ {
    data class Load(val id: Long) : GenEvent_423_()
    data class Update(val model: GenModel_423_) : GenEvent_423_()
    data class Delete(val id: Long) : GenEvent_423_()
    data object Refresh : GenEvent_423_()
    data class Search(val query: String) : GenEvent_423_()
    data class Filter(val predicate: String) : GenEvent_423_()
}

sealed class GenState_423_ {
    data object Idle : GenState_423_()
    data object Loading : GenState_423_()
    data class Success(val items: List<GenModel_423_>) : GenState_423_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_423_()
    data class Partial(val items: List<GenModel_423_>, val hasMore: Boolean) : GenState_423_()
}

interface GenRepository_423_ {
    suspend fun getAll(): List<GenModel_423_>
    suspend fun getById(id: Long): GenModel_423_?
    suspend fun save(model: GenModel_423_): GenModel_423_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_423_>
}

@Singleton
class GenRepositoryImpl_423_ @Inject constructor() : GenRepository_423_ {
    private val store = mutableMapOf<Long, GenModel_423_>()
    override suspend fun getAll(): List<GenModel_423_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_423_? = store[id]
    override suspend fun save(model: GenModel_423_): GenModel_423_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_423_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_423_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_423_ @Inject constructor(
    private val repository: GenRepositoryImpl_423_
) : GenUseCase_423_<Unit, List<GenModel_423_>> {
    override suspend fun invoke(params: Unit): List<GenModel_423_> = repository.getAll()
}

class GenSaveUseCase_423_ @Inject constructor(
    private val repository: GenRepositoryImpl_423_
) : GenUseCase_423_<GenModel_423_, GenModel_423_> {
    override suspend fun invoke(params: GenModel_423_): GenModel_423_ = repository.save(params)
}

class GenDeleteUseCase_423_ @Inject constructor(
    private val repository: GenRepositoryImpl_423_
) : GenUseCase_423_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_423_ @Inject constructor(
    private val repository: GenRepositoryImpl_423_
) : GenUseCase_423_<String, List<GenModel_423_>> {
    override suspend fun invoke(params: String): List<GenModel_423_> = repository.search(params)
}

abstract class GenMapper_423_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_423_ : GenMapper_423_<GenModel_423_, String>() {
    override fun map(input: GenModel_423_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_423_ : GenMapper_423_<String, GenModel_423_>() {
    override fun map(input: String): GenModel_423_ {
        val parts = input.split(":")
        return GenModel_423_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_423_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_423_,
    private val saveUseCase: GenSaveUseCase_423_,
    private val deleteUseCase: GenDeleteUseCase_423_,
    private val searchUseCase: GenSearchUseCase_423_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_423_>(GenState_423_.Idle)
    val state: StateFlow<GenState_423_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_423_) {
        when (event) {
            is GenEvent_423_.Load -> loadAll()
            is GenEvent_423_.Update -> save(event.model)
            is GenEvent_423_.Delete -> delete(event.id)
            is GenEvent_423_.Refresh -> loadAll()
            is GenEvent_423_.Search -> search(event.query)
            is GenEvent_423_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_423_.Loading; _state.value = GenState_423_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_423_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_423_.Success(searchUseCase(query)) } }
}
