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

data class GenModel_3217_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3217_ {
    data class Load(val id: Long) : GenEvent_3217_()
    data class Update(val model: GenModel_3217_) : GenEvent_3217_()
    data class Delete(val id: Long) : GenEvent_3217_()
    data object Refresh : GenEvent_3217_()
    data class Search(val query: String) : GenEvent_3217_()
    data class Filter(val predicate: String) : GenEvent_3217_()
}

sealed class GenState_3217_ {
    data object Idle : GenState_3217_()
    data object Loading : GenState_3217_()
    data class Success(val items: List<GenModel_3217_>) : GenState_3217_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3217_()
    data class Partial(val items: List<GenModel_3217_>, val hasMore: Boolean) : GenState_3217_()
}

interface GenRepository_3217_ {
    suspend fun getAll(): List<GenModel_3217_>
    suspend fun getById(id: Long): GenModel_3217_?
    suspend fun save(model: GenModel_3217_): GenModel_3217_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3217_>
}

@Singleton
class GenRepositoryImpl_3217_ @Inject constructor() : GenRepository_3217_ {
    private val store = mutableMapOf<Long, GenModel_3217_>()
    override suspend fun getAll(): List<GenModel_3217_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3217_? = store[id]
    override suspend fun save(model: GenModel_3217_): GenModel_3217_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3217_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3217_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3217_ @Inject constructor(
    private val repository: GenRepositoryImpl_3217_
) : GenUseCase_3217_<Unit, List<GenModel_3217_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3217_> = repository.getAll()
}

class GenSaveUseCase_3217_ @Inject constructor(
    private val repository: GenRepositoryImpl_3217_
) : GenUseCase_3217_<GenModel_3217_, GenModel_3217_> {
    override suspend fun invoke(params: GenModel_3217_): GenModel_3217_ = repository.save(params)
}

class GenDeleteUseCase_3217_ @Inject constructor(
    private val repository: GenRepositoryImpl_3217_
) : GenUseCase_3217_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3217_ @Inject constructor(
    private val repository: GenRepositoryImpl_3217_
) : GenUseCase_3217_<String, List<GenModel_3217_>> {
    override suspend fun invoke(params: String): List<GenModel_3217_> = repository.search(params)
}

abstract class GenMapper_3217_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3217_ : GenMapper_3217_<GenModel_3217_, String>() {
    override fun map(input: GenModel_3217_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3217_ : GenMapper_3217_<String, GenModel_3217_>() {
    override fun map(input: String): GenModel_3217_ {
        val parts = input.split(":")
        return GenModel_3217_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3217_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3217_,
    private val saveUseCase: GenSaveUseCase_3217_,
    private val deleteUseCase: GenDeleteUseCase_3217_,
    private val searchUseCase: GenSearchUseCase_3217_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3217_>(GenState_3217_.Idle)
    val state: StateFlow<GenState_3217_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3217_) {
        when (event) {
            is GenEvent_3217_.Load -> loadAll()
            is GenEvent_3217_.Update -> save(event.model)
            is GenEvent_3217_.Delete -> delete(event.id)
            is GenEvent_3217_.Refresh -> loadAll()
            is GenEvent_3217_.Search -> search(event.query)
            is GenEvent_3217_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3217_.Loading; _state.value = GenState_3217_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3217_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3217_.Success(searchUseCase(query)) } }
}
