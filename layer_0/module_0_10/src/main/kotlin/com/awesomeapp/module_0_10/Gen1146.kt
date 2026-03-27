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

data class GenModel_1146_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1146_ {
    data class Load(val id: Long) : GenEvent_1146_()
    data class Update(val model: GenModel_1146_) : GenEvent_1146_()
    data class Delete(val id: Long) : GenEvent_1146_()
    data object Refresh : GenEvent_1146_()
    data class Search(val query: String) : GenEvent_1146_()
    data class Filter(val predicate: String) : GenEvent_1146_()
}

sealed class GenState_1146_ {
    data object Idle : GenState_1146_()
    data object Loading : GenState_1146_()
    data class Success(val items: List<GenModel_1146_>) : GenState_1146_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1146_()
    data class Partial(val items: List<GenModel_1146_>, val hasMore: Boolean) : GenState_1146_()
}

interface GenRepository_1146_ {
    suspend fun getAll(): List<GenModel_1146_>
    suspend fun getById(id: Long): GenModel_1146_?
    suspend fun save(model: GenModel_1146_): GenModel_1146_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1146_>
}

@Singleton
class GenRepositoryImpl_1146_ @Inject constructor() : GenRepository_1146_ {
    private val store = mutableMapOf<Long, GenModel_1146_>()
    override suspend fun getAll(): List<GenModel_1146_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1146_? = store[id]
    override suspend fun save(model: GenModel_1146_): GenModel_1146_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1146_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1146_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1146_ @Inject constructor(
    private val repository: GenRepositoryImpl_1146_
) : GenUseCase_1146_<Unit, List<GenModel_1146_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1146_> = repository.getAll()
}

class GenSaveUseCase_1146_ @Inject constructor(
    private val repository: GenRepositoryImpl_1146_
) : GenUseCase_1146_<GenModel_1146_, GenModel_1146_> {
    override suspend fun invoke(params: GenModel_1146_): GenModel_1146_ = repository.save(params)
}

class GenDeleteUseCase_1146_ @Inject constructor(
    private val repository: GenRepositoryImpl_1146_
) : GenUseCase_1146_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1146_ @Inject constructor(
    private val repository: GenRepositoryImpl_1146_
) : GenUseCase_1146_<String, List<GenModel_1146_>> {
    override suspend fun invoke(params: String): List<GenModel_1146_> = repository.search(params)
}

abstract class GenMapper_1146_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1146_ : GenMapper_1146_<GenModel_1146_, String>() {
    override fun map(input: GenModel_1146_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1146_ : GenMapper_1146_<String, GenModel_1146_>() {
    override fun map(input: String): GenModel_1146_ {
        val parts = input.split(":")
        return GenModel_1146_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1146_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1146_,
    private val saveUseCase: GenSaveUseCase_1146_,
    private val deleteUseCase: GenDeleteUseCase_1146_,
    private val searchUseCase: GenSearchUseCase_1146_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1146_>(GenState_1146_.Idle)
    val state: StateFlow<GenState_1146_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1146_) {
        when (event) {
            is GenEvent_1146_.Load -> loadAll()
            is GenEvent_1146_.Update -> save(event.model)
            is GenEvent_1146_.Delete -> delete(event.id)
            is GenEvent_1146_.Refresh -> loadAll()
            is GenEvent_1146_.Search -> search(event.query)
            is GenEvent_1146_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1146_.Loading; _state.value = GenState_1146_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1146_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1146_.Success(searchUseCase(query)) } }
}
