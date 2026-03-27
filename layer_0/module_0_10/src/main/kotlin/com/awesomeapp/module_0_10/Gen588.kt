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

data class GenModel_588_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_588_ {
    data class Load(val id: Long) : GenEvent_588_()
    data class Update(val model: GenModel_588_) : GenEvent_588_()
    data class Delete(val id: Long) : GenEvent_588_()
    data object Refresh : GenEvent_588_()
    data class Search(val query: String) : GenEvent_588_()
    data class Filter(val predicate: String) : GenEvent_588_()
}

sealed class GenState_588_ {
    data object Idle : GenState_588_()
    data object Loading : GenState_588_()
    data class Success(val items: List<GenModel_588_>) : GenState_588_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_588_()
    data class Partial(val items: List<GenModel_588_>, val hasMore: Boolean) : GenState_588_()
}

interface GenRepository_588_ {
    suspend fun getAll(): List<GenModel_588_>
    suspend fun getById(id: Long): GenModel_588_?
    suspend fun save(model: GenModel_588_): GenModel_588_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_588_>
}

@Singleton
class GenRepositoryImpl_588_ @Inject constructor() : GenRepository_588_ {
    private val store = mutableMapOf<Long, GenModel_588_>()
    override suspend fun getAll(): List<GenModel_588_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_588_? = store[id]
    override suspend fun save(model: GenModel_588_): GenModel_588_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_588_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_588_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_588_ @Inject constructor(
    private val repository: GenRepositoryImpl_588_
) : GenUseCase_588_<Unit, List<GenModel_588_>> {
    override suspend fun invoke(params: Unit): List<GenModel_588_> = repository.getAll()
}

class GenSaveUseCase_588_ @Inject constructor(
    private val repository: GenRepositoryImpl_588_
) : GenUseCase_588_<GenModel_588_, GenModel_588_> {
    override suspend fun invoke(params: GenModel_588_): GenModel_588_ = repository.save(params)
}

class GenDeleteUseCase_588_ @Inject constructor(
    private val repository: GenRepositoryImpl_588_
) : GenUseCase_588_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_588_ @Inject constructor(
    private val repository: GenRepositoryImpl_588_
) : GenUseCase_588_<String, List<GenModel_588_>> {
    override suspend fun invoke(params: String): List<GenModel_588_> = repository.search(params)
}

abstract class GenMapper_588_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_588_ : GenMapper_588_<GenModel_588_, String>() {
    override fun map(input: GenModel_588_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_588_ : GenMapper_588_<String, GenModel_588_>() {
    override fun map(input: String): GenModel_588_ {
        val parts = input.split(":")
        return GenModel_588_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_588_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_588_,
    private val saveUseCase: GenSaveUseCase_588_,
    private val deleteUseCase: GenDeleteUseCase_588_,
    private val searchUseCase: GenSearchUseCase_588_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_588_>(GenState_588_.Idle)
    val state: StateFlow<GenState_588_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_588_) {
        when (event) {
            is GenEvent_588_.Load -> loadAll()
            is GenEvent_588_.Update -> save(event.model)
            is GenEvent_588_.Delete -> delete(event.id)
            is GenEvent_588_.Refresh -> loadAll()
            is GenEvent_588_.Search -> search(event.query)
            is GenEvent_588_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_588_.Loading; _state.value = GenState_588_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_588_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_588_.Success(searchUseCase(query)) } }
}
