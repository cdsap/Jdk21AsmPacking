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

data class GenModel_2182_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2182_ {
    data class Load(val id: Long) : GenEvent_2182_()
    data class Update(val model: GenModel_2182_) : GenEvent_2182_()
    data class Delete(val id: Long) : GenEvent_2182_()
    data object Refresh : GenEvent_2182_()
    data class Search(val query: String) : GenEvent_2182_()
    data class Filter(val predicate: String) : GenEvent_2182_()
}

sealed class GenState_2182_ {
    data object Idle : GenState_2182_()
    data object Loading : GenState_2182_()
    data class Success(val items: List<GenModel_2182_>) : GenState_2182_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2182_()
    data class Partial(val items: List<GenModel_2182_>, val hasMore: Boolean) : GenState_2182_()
}

interface GenRepository_2182_ {
    suspend fun getAll(): List<GenModel_2182_>
    suspend fun getById(id: Long): GenModel_2182_?
    suspend fun save(model: GenModel_2182_): GenModel_2182_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2182_>
}

@Singleton
class GenRepositoryImpl_2182_ @Inject constructor() : GenRepository_2182_ {
    private val store = mutableMapOf<Long, GenModel_2182_>()
    override suspend fun getAll(): List<GenModel_2182_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2182_? = store[id]
    override suspend fun save(model: GenModel_2182_): GenModel_2182_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2182_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2182_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2182_ @Inject constructor(
    private val repository: GenRepositoryImpl_2182_
) : GenUseCase_2182_<Unit, List<GenModel_2182_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2182_> = repository.getAll()
}

class GenSaveUseCase_2182_ @Inject constructor(
    private val repository: GenRepositoryImpl_2182_
) : GenUseCase_2182_<GenModel_2182_, GenModel_2182_> {
    override suspend fun invoke(params: GenModel_2182_): GenModel_2182_ = repository.save(params)
}

class GenDeleteUseCase_2182_ @Inject constructor(
    private val repository: GenRepositoryImpl_2182_
) : GenUseCase_2182_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2182_ @Inject constructor(
    private val repository: GenRepositoryImpl_2182_
) : GenUseCase_2182_<String, List<GenModel_2182_>> {
    override suspend fun invoke(params: String): List<GenModel_2182_> = repository.search(params)
}

abstract class GenMapper_2182_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2182_ : GenMapper_2182_<GenModel_2182_, String>() {
    override fun map(input: GenModel_2182_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2182_ : GenMapper_2182_<String, GenModel_2182_>() {
    override fun map(input: String): GenModel_2182_ {
        val parts = input.split(":")
        return GenModel_2182_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2182_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2182_,
    private val saveUseCase: GenSaveUseCase_2182_,
    private val deleteUseCase: GenDeleteUseCase_2182_,
    private val searchUseCase: GenSearchUseCase_2182_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2182_>(GenState_2182_.Idle)
    val state: StateFlow<GenState_2182_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2182_) {
        when (event) {
            is GenEvent_2182_.Load -> loadAll()
            is GenEvent_2182_.Update -> save(event.model)
            is GenEvent_2182_.Delete -> delete(event.id)
            is GenEvent_2182_.Refresh -> loadAll()
            is GenEvent_2182_.Search -> search(event.query)
            is GenEvent_2182_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2182_.Loading; _state.value = GenState_2182_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2182_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2182_.Success(searchUseCase(query)) } }
}
