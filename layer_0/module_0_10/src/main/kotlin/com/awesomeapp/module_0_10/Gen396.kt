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

data class GenModel_396_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_396_ {
    data class Load(val id: Long) : GenEvent_396_()
    data class Update(val model: GenModel_396_) : GenEvent_396_()
    data class Delete(val id: Long) : GenEvent_396_()
    data object Refresh : GenEvent_396_()
    data class Search(val query: String) : GenEvent_396_()
    data class Filter(val predicate: String) : GenEvent_396_()
}

sealed class GenState_396_ {
    data object Idle : GenState_396_()
    data object Loading : GenState_396_()
    data class Success(val items: List<GenModel_396_>) : GenState_396_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_396_()
    data class Partial(val items: List<GenModel_396_>, val hasMore: Boolean) : GenState_396_()
}

interface GenRepository_396_ {
    suspend fun getAll(): List<GenModel_396_>
    suspend fun getById(id: Long): GenModel_396_?
    suspend fun save(model: GenModel_396_): GenModel_396_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_396_>
}

@Singleton
class GenRepositoryImpl_396_ @Inject constructor() : GenRepository_396_ {
    private val store = mutableMapOf<Long, GenModel_396_>()
    override suspend fun getAll(): List<GenModel_396_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_396_? = store[id]
    override suspend fun save(model: GenModel_396_): GenModel_396_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_396_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_396_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_396_ @Inject constructor(
    private val repository: GenRepositoryImpl_396_
) : GenUseCase_396_<Unit, List<GenModel_396_>> {
    override suspend fun invoke(params: Unit): List<GenModel_396_> = repository.getAll()
}

class GenSaveUseCase_396_ @Inject constructor(
    private val repository: GenRepositoryImpl_396_
) : GenUseCase_396_<GenModel_396_, GenModel_396_> {
    override suspend fun invoke(params: GenModel_396_): GenModel_396_ = repository.save(params)
}

class GenDeleteUseCase_396_ @Inject constructor(
    private val repository: GenRepositoryImpl_396_
) : GenUseCase_396_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_396_ @Inject constructor(
    private val repository: GenRepositoryImpl_396_
) : GenUseCase_396_<String, List<GenModel_396_>> {
    override suspend fun invoke(params: String): List<GenModel_396_> = repository.search(params)
}

abstract class GenMapper_396_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_396_ : GenMapper_396_<GenModel_396_, String>() {
    override fun map(input: GenModel_396_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_396_ : GenMapper_396_<String, GenModel_396_>() {
    override fun map(input: String): GenModel_396_ {
        val parts = input.split(":")
        return GenModel_396_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_396_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_396_,
    private val saveUseCase: GenSaveUseCase_396_,
    private val deleteUseCase: GenDeleteUseCase_396_,
    private val searchUseCase: GenSearchUseCase_396_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_396_>(GenState_396_.Idle)
    val state: StateFlow<GenState_396_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_396_) {
        when (event) {
            is GenEvent_396_.Load -> loadAll()
            is GenEvent_396_.Update -> save(event.model)
            is GenEvent_396_.Delete -> delete(event.id)
            is GenEvent_396_.Refresh -> loadAll()
            is GenEvent_396_.Search -> search(event.query)
            is GenEvent_396_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_396_.Loading; _state.value = GenState_396_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_396_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_396_.Success(searchUseCase(query)) } }
}
