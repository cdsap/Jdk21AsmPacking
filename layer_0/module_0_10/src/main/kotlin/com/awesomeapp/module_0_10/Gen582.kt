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

data class GenModel_582_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_582_ {
    data class Load(val id: Long) : GenEvent_582_()
    data class Update(val model: GenModel_582_) : GenEvent_582_()
    data class Delete(val id: Long) : GenEvent_582_()
    data object Refresh : GenEvent_582_()
    data class Search(val query: String) : GenEvent_582_()
    data class Filter(val predicate: String) : GenEvent_582_()
}

sealed class GenState_582_ {
    data object Idle : GenState_582_()
    data object Loading : GenState_582_()
    data class Success(val items: List<GenModel_582_>) : GenState_582_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_582_()
    data class Partial(val items: List<GenModel_582_>, val hasMore: Boolean) : GenState_582_()
}

interface GenRepository_582_ {
    suspend fun getAll(): List<GenModel_582_>
    suspend fun getById(id: Long): GenModel_582_?
    suspend fun save(model: GenModel_582_): GenModel_582_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_582_>
}

@Singleton
class GenRepositoryImpl_582_ @Inject constructor() : GenRepository_582_ {
    private val store = mutableMapOf<Long, GenModel_582_>()
    override suspend fun getAll(): List<GenModel_582_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_582_? = store[id]
    override suspend fun save(model: GenModel_582_): GenModel_582_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_582_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_582_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_582_ @Inject constructor(
    private val repository: GenRepositoryImpl_582_
) : GenUseCase_582_<Unit, List<GenModel_582_>> {
    override suspend fun invoke(params: Unit): List<GenModel_582_> = repository.getAll()
}

class GenSaveUseCase_582_ @Inject constructor(
    private val repository: GenRepositoryImpl_582_
) : GenUseCase_582_<GenModel_582_, GenModel_582_> {
    override suspend fun invoke(params: GenModel_582_): GenModel_582_ = repository.save(params)
}

class GenDeleteUseCase_582_ @Inject constructor(
    private val repository: GenRepositoryImpl_582_
) : GenUseCase_582_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_582_ @Inject constructor(
    private val repository: GenRepositoryImpl_582_
) : GenUseCase_582_<String, List<GenModel_582_>> {
    override suspend fun invoke(params: String): List<GenModel_582_> = repository.search(params)
}

abstract class GenMapper_582_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_582_ : GenMapper_582_<GenModel_582_, String>() {
    override fun map(input: GenModel_582_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_582_ : GenMapper_582_<String, GenModel_582_>() {
    override fun map(input: String): GenModel_582_ {
        val parts = input.split(":")
        return GenModel_582_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_582_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_582_,
    private val saveUseCase: GenSaveUseCase_582_,
    private val deleteUseCase: GenDeleteUseCase_582_,
    private val searchUseCase: GenSearchUseCase_582_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_582_>(GenState_582_.Idle)
    val state: StateFlow<GenState_582_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_582_) {
        when (event) {
            is GenEvent_582_.Load -> loadAll()
            is GenEvent_582_.Update -> save(event.model)
            is GenEvent_582_.Delete -> delete(event.id)
            is GenEvent_582_.Refresh -> loadAll()
            is GenEvent_582_.Search -> search(event.query)
            is GenEvent_582_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_582_.Loading; _state.value = GenState_582_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_582_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_582_.Success(searchUseCase(query)) } }
}
