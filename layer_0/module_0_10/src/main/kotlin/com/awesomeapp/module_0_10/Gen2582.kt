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

data class GenModel_2582_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2582_ {
    data class Load(val id: Long) : GenEvent_2582_()
    data class Update(val model: GenModel_2582_) : GenEvent_2582_()
    data class Delete(val id: Long) : GenEvent_2582_()
    data object Refresh : GenEvent_2582_()
    data class Search(val query: String) : GenEvent_2582_()
    data class Filter(val predicate: String) : GenEvent_2582_()
}

sealed class GenState_2582_ {
    data object Idle : GenState_2582_()
    data object Loading : GenState_2582_()
    data class Success(val items: List<GenModel_2582_>) : GenState_2582_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2582_()
    data class Partial(val items: List<GenModel_2582_>, val hasMore: Boolean) : GenState_2582_()
}

interface GenRepository_2582_ {
    suspend fun getAll(): List<GenModel_2582_>
    suspend fun getById(id: Long): GenModel_2582_?
    suspend fun save(model: GenModel_2582_): GenModel_2582_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2582_>
}

@Singleton
class GenRepositoryImpl_2582_ @Inject constructor() : GenRepository_2582_ {
    private val store = mutableMapOf<Long, GenModel_2582_>()
    override suspend fun getAll(): List<GenModel_2582_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2582_? = store[id]
    override suspend fun save(model: GenModel_2582_): GenModel_2582_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2582_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2582_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2582_ @Inject constructor(
    private val repository: GenRepositoryImpl_2582_
) : GenUseCase_2582_<Unit, List<GenModel_2582_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2582_> = repository.getAll()
}

class GenSaveUseCase_2582_ @Inject constructor(
    private val repository: GenRepositoryImpl_2582_
) : GenUseCase_2582_<GenModel_2582_, GenModel_2582_> {
    override suspend fun invoke(params: GenModel_2582_): GenModel_2582_ = repository.save(params)
}

class GenDeleteUseCase_2582_ @Inject constructor(
    private val repository: GenRepositoryImpl_2582_
) : GenUseCase_2582_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2582_ @Inject constructor(
    private val repository: GenRepositoryImpl_2582_
) : GenUseCase_2582_<String, List<GenModel_2582_>> {
    override suspend fun invoke(params: String): List<GenModel_2582_> = repository.search(params)
}

abstract class GenMapper_2582_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2582_ : GenMapper_2582_<GenModel_2582_, String>() {
    override fun map(input: GenModel_2582_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2582_ : GenMapper_2582_<String, GenModel_2582_>() {
    override fun map(input: String): GenModel_2582_ {
        val parts = input.split(":")
        return GenModel_2582_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2582_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2582_,
    private val saveUseCase: GenSaveUseCase_2582_,
    private val deleteUseCase: GenDeleteUseCase_2582_,
    private val searchUseCase: GenSearchUseCase_2582_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2582_>(GenState_2582_.Idle)
    val state: StateFlow<GenState_2582_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2582_) {
        when (event) {
            is GenEvent_2582_.Load -> loadAll()
            is GenEvent_2582_.Update -> save(event.model)
            is GenEvent_2582_.Delete -> delete(event.id)
            is GenEvent_2582_.Refresh -> loadAll()
            is GenEvent_2582_.Search -> search(event.query)
            is GenEvent_2582_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2582_.Loading; _state.value = GenState_2582_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2582_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2582_.Success(searchUseCase(query)) } }
}
