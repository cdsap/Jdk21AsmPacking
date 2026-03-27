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

data class GenModel_766_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_766_ {
    data class Load(val id: Long) : GenEvent_766_()
    data class Update(val model: GenModel_766_) : GenEvent_766_()
    data class Delete(val id: Long) : GenEvent_766_()
    data object Refresh : GenEvent_766_()
    data class Search(val query: String) : GenEvent_766_()
    data class Filter(val predicate: String) : GenEvent_766_()
}

sealed class GenState_766_ {
    data object Idle : GenState_766_()
    data object Loading : GenState_766_()
    data class Success(val items: List<GenModel_766_>) : GenState_766_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_766_()
    data class Partial(val items: List<GenModel_766_>, val hasMore: Boolean) : GenState_766_()
}

interface GenRepository_766_ {
    suspend fun getAll(): List<GenModel_766_>
    suspend fun getById(id: Long): GenModel_766_?
    suspend fun save(model: GenModel_766_): GenModel_766_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_766_>
}

@Singleton
class GenRepositoryImpl_766_ @Inject constructor() : GenRepository_766_ {
    private val store = mutableMapOf<Long, GenModel_766_>()
    override suspend fun getAll(): List<GenModel_766_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_766_? = store[id]
    override suspend fun save(model: GenModel_766_): GenModel_766_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_766_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_766_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_766_ @Inject constructor(
    private val repository: GenRepositoryImpl_766_
) : GenUseCase_766_<Unit, List<GenModel_766_>> {
    override suspend fun invoke(params: Unit): List<GenModel_766_> = repository.getAll()
}

class GenSaveUseCase_766_ @Inject constructor(
    private val repository: GenRepositoryImpl_766_
) : GenUseCase_766_<GenModel_766_, GenModel_766_> {
    override suspend fun invoke(params: GenModel_766_): GenModel_766_ = repository.save(params)
}

class GenDeleteUseCase_766_ @Inject constructor(
    private val repository: GenRepositoryImpl_766_
) : GenUseCase_766_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_766_ @Inject constructor(
    private val repository: GenRepositoryImpl_766_
) : GenUseCase_766_<String, List<GenModel_766_>> {
    override suspend fun invoke(params: String): List<GenModel_766_> = repository.search(params)
}

abstract class GenMapper_766_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_766_ : GenMapper_766_<GenModel_766_, String>() {
    override fun map(input: GenModel_766_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_766_ : GenMapper_766_<String, GenModel_766_>() {
    override fun map(input: String): GenModel_766_ {
        val parts = input.split(":")
        return GenModel_766_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_766_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_766_,
    private val saveUseCase: GenSaveUseCase_766_,
    private val deleteUseCase: GenDeleteUseCase_766_,
    private val searchUseCase: GenSearchUseCase_766_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_766_>(GenState_766_.Idle)
    val state: StateFlow<GenState_766_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_766_) {
        when (event) {
            is GenEvent_766_.Load -> loadAll()
            is GenEvent_766_.Update -> save(event.model)
            is GenEvent_766_.Delete -> delete(event.id)
            is GenEvent_766_.Refresh -> loadAll()
            is GenEvent_766_.Search -> search(event.query)
            is GenEvent_766_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_766_.Loading; _state.value = GenState_766_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_766_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_766_.Success(searchUseCase(query)) } }
}
