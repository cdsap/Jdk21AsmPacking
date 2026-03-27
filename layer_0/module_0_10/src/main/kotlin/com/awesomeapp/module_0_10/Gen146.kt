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

data class GenModel_146_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_146_ {
    data class Load(val id: Long) : GenEvent_146_()
    data class Update(val model: GenModel_146_) : GenEvent_146_()
    data class Delete(val id: Long) : GenEvent_146_()
    data object Refresh : GenEvent_146_()
    data class Search(val query: String) : GenEvent_146_()
    data class Filter(val predicate: String) : GenEvent_146_()
}

sealed class GenState_146_ {
    data object Idle : GenState_146_()
    data object Loading : GenState_146_()
    data class Success(val items: List<GenModel_146_>) : GenState_146_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_146_()
    data class Partial(val items: List<GenModel_146_>, val hasMore: Boolean) : GenState_146_()
}

interface GenRepository_146_ {
    suspend fun getAll(): List<GenModel_146_>
    suspend fun getById(id: Long): GenModel_146_?
    suspend fun save(model: GenModel_146_): GenModel_146_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_146_>
}

@Singleton
class GenRepositoryImpl_146_ @Inject constructor() : GenRepository_146_ {
    private val store = mutableMapOf<Long, GenModel_146_>()
    override suspend fun getAll(): List<GenModel_146_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_146_? = store[id]
    override suspend fun save(model: GenModel_146_): GenModel_146_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_146_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_146_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_146_ @Inject constructor(
    private val repository: GenRepositoryImpl_146_
) : GenUseCase_146_<Unit, List<GenModel_146_>> {
    override suspend fun invoke(params: Unit): List<GenModel_146_> = repository.getAll()
}

class GenSaveUseCase_146_ @Inject constructor(
    private val repository: GenRepositoryImpl_146_
) : GenUseCase_146_<GenModel_146_, GenModel_146_> {
    override suspend fun invoke(params: GenModel_146_): GenModel_146_ = repository.save(params)
}

class GenDeleteUseCase_146_ @Inject constructor(
    private val repository: GenRepositoryImpl_146_
) : GenUseCase_146_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_146_ @Inject constructor(
    private val repository: GenRepositoryImpl_146_
) : GenUseCase_146_<String, List<GenModel_146_>> {
    override suspend fun invoke(params: String): List<GenModel_146_> = repository.search(params)
}

abstract class GenMapper_146_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_146_ : GenMapper_146_<GenModel_146_, String>() {
    override fun map(input: GenModel_146_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_146_ : GenMapper_146_<String, GenModel_146_>() {
    override fun map(input: String): GenModel_146_ {
        val parts = input.split(":")
        return GenModel_146_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_146_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_146_,
    private val saveUseCase: GenSaveUseCase_146_,
    private val deleteUseCase: GenDeleteUseCase_146_,
    private val searchUseCase: GenSearchUseCase_146_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_146_>(GenState_146_.Idle)
    val state: StateFlow<GenState_146_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_146_) {
        when (event) {
            is GenEvent_146_.Load -> loadAll()
            is GenEvent_146_.Update -> save(event.model)
            is GenEvent_146_.Delete -> delete(event.id)
            is GenEvent_146_.Refresh -> loadAll()
            is GenEvent_146_.Search -> search(event.query)
            is GenEvent_146_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_146_.Loading; _state.value = GenState_146_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_146_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_146_.Success(searchUseCase(query)) } }
}
