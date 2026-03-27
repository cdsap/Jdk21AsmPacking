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

data class GenModel_171_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_171_ {
    data class Load(val id: Long) : GenEvent_171_()
    data class Update(val model: GenModel_171_) : GenEvent_171_()
    data class Delete(val id: Long) : GenEvent_171_()
    data object Refresh : GenEvent_171_()
    data class Search(val query: String) : GenEvent_171_()
    data class Filter(val predicate: String) : GenEvent_171_()
}

sealed class GenState_171_ {
    data object Idle : GenState_171_()
    data object Loading : GenState_171_()
    data class Success(val items: List<GenModel_171_>) : GenState_171_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_171_()
    data class Partial(val items: List<GenModel_171_>, val hasMore: Boolean) : GenState_171_()
}

interface GenRepository_171_ {
    suspend fun getAll(): List<GenModel_171_>
    suspend fun getById(id: Long): GenModel_171_?
    suspend fun save(model: GenModel_171_): GenModel_171_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_171_>
}

@Singleton
class GenRepositoryImpl_171_ @Inject constructor() : GenRepository_171_ {
    private val store = mutableMapOf<Long, GenModel_171_>()
    override suspend fun getAll(): List<GenModel_171_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_171_? = store[id]
    override suspend fun save(model: GenModel_171_): GenModel_171_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_171_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_171_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_171_ @Inject constructor(
    private val repository: GenRepositoryImpl_171_
) : GenUseCase_171_<Unit, List<GenModel_171_>> {
    override suspend fun invoke(params: Unit): List<GenModel_171_> = repository.getAll()
}

class GenSaveUseCase_171_ @Inject constructor(
    private val repository: GenRepositoryImpl_171_
) : GenUseCase_171_<GenModel_171_, GenModel_171_> {
    override suspend fun invoke(params: GenModel_171_): GenModel_171_ = repository.save(params)
}

class GenDeleteUseCase_171_ @Inject constructor(
    private val repository: GenRepositoryImpl_171_
) : GenUseCase_171_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_171_ @Inject constructor(
    private val repository: GenRepositoryImpl_171_
) : GenUseCase_171_<String, List<GenModel_171_>> {
    override suspend fun invoke(params: String): List<GenModel_171_> = repository.search(params)
}

abstract class GenMapper_171_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_171_ : GenMapper_171_<GenModel_171_, String>() {
    override fun map(input: GenModel_171_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_171_ : GenMapper_171_<String, GenModel_171_>() {
    override fun map(input: String): GenModel_171_ {
        val parts = input.split(":")
        return GenModel_171_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_171_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_171_,
    private val saveUseCase: GenSaveUseCase_171_,
    private val deleteUseCase: GenDeleteUseCase_171_,
    private val searchUseCase: GenSearchUseCase_171_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_171_>(GenState_171_.Idle)
    val state: StateFlow<GenState_171_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_171_) {
        when (event) {
            is GenEvent_171_.Load -> loadAll()
            is GenEvent_171_.Update -> save(event.model)
            is GenEvent_171_.Delete -> delete(event.id)
            is GenEvent_171_.Refresh -> loadAll()
            is GenEvent_171_.Search -> search(event.query)
            is GenEvent_171_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_171_.Loading; _state.value = GenState_171_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_171_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_171_.Success(searchUseCase(query)) } }
}
