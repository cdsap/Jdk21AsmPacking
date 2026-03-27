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

data class GenModel_903_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_903_ {
    data class Load(val id: Long) : GenEvent_903_()
    data class Update(val model: GenModel_903_) : GenEvent_903_()
    data class Delete(val id: Long) : GenEvent_903_()
    data object Refresh : GenEvent_903_()
    data class Search(val query: String) : GenEvent_903_()
    data class Filter(val predicate: String) : GenEvent_903_()
}

sealed class GenState_903_ {
    data object Idle : GenState_903_()
    data object Loading : GenState_903_()
    data class Success(val items: List<GenModel_903_>) : GenState_903_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_903_()
    data class Partial(val items: List<GenModel_903_>, val hasMore: Boolean) : GenState_903_()
}

interface GenRepository_903_ {
    suspend fun getAll(): List<GenModel_903_>
    suspend fun getById(id: Long): GenModel_903_?
    suspend fun save(model: GenModel_903_): GenModel_903_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_903_>
}

@Singleton
class GenRepositoryImpl_903_ @Inject constructor() : GenRepository_903_ {
    private val store = mutableMapOf<Long, GenModel_903_>()
    override suspend fun getAll(): List<GenModel_903_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_903_? = store[id]
    override suspend fun save(model: GenModel_903_): GenModel_903_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_903_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_903_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_903_ @Inject constructor(
    private val repository: GenRepositoryImpl_903_
) : GenUseCase_903_<Unit, List<GenModel_903_>> {
    override suspend fun invoke(params: Unit): List<GenModel_903_> = repository.getAll()
}

class GenSaveUseCase_903_ @Inject constructor(
    private val repository: GenRepositoryImpl_903_
) : GenUseCase_903_<GenModel_903_, GenModel_903_> {
    override suspend fun invoke(params: GenModel_903_): GenModel_903_ = repository.save(params)
}

class GenDeleteUseCase_903_ @Inject constructor(
    private val repository: GenRepositoryImpl_903_
) : GenUseCase_903_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_903_ @Inject constructor(
    private val repository: GenRepositoryImpl_903_
) : GenUseCase_903_<String, List<GenModel_903_>> {
    override suspend fun invoke(params: String): List<GenModel_903_> = repository.search(params)
}

abstract class GenMapper_903_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_903_ : GenMapper_903_<GenModel_903_, String>() {
    override fun map(input: GenModel_903_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_903_ : GenMapper_903_<String, GenModel_903_>() {
    override fun map(input: String): GenModel_903_ {
        val parts = input.split(":")
        return GenModel_903_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_903_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_903_,
    private val saveUseCase: GenSaveUseCase_903_,
    private val deleteUseCase: GenDeleteUseCase_903_,
    private val searchUseCase: GenSearchUseCase_903_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_903_>(GenState_903_.Idle)
    val state: StateFlow<GenState_903_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_903_) {
        when (event) {
            is GenEvent_903_.Load -> loadAll()
            is GenEvent_903_.Update -> save(event.model)
            is GenEvent_903_.Delete -> delete(event.id)
            is GenEvent_903_.Refresh -> loadAll()
            is GenEvent_903_.Search -> search(event.query)
            is GenEvent_903_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_903_.Loading; _state.value = GenState_903_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_903_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_903_.Success(searchUseCase(query)) } }
}
