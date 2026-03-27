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

data class GenModel_763_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_763_ {
    data class Load(val id: Long) : GenEvent_763_()
    data class Update(val model: GenModel_763_) : GenEvent_763_()
    data class Delete(val id: Long) : GenEvent_763_()
    data object Refresh : GenEvent_763_()
    data class Search(val query: String) : GenEvent_763_()
    data class Filter(val predicate: String) : GenEvent_763_()
}

sealed class GenState_763_ {
    data object Idle : GenState_763_()
    data object Loading : GenState_763_()
    data class Success(val items: List<GenModel_763_>) : GenState_763_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_763_()
    data class Partial(val items: List<GenModel_763_>, val hasMore: Boolean) : GenState_763_()
}

interface GenRepository_763_ {
    suspend fun getAll(): List<GenModel_763_>
    suspend fun getById(id: Long): GenModel_763_?
    suspend fun save(model: GenModel_763_): GenModel_763_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_763_>
}

@Singleton
class GenRepositoryImpl_763_ @Inject constructor() : GenRepository_763_ {
    private val store = mutableMapOf<Long, GenModel_763_>()
    override suspend fun getAll(): List<GenModel_763_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_763_? = store[id]
    override suspend fun save(model: GenModel_763_): GenModel_763_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_763_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_763_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_763_ @Inject constructor(
    private val repository: GenRepositoryImpl_763_
) : GenUseCase_763_<Unit, List<GenModel_763_>> {
    override suspend fun invoke(params: Unit): List<GenModel_763_> = repository.getAll()
}

class GenSaveUseCase_763_ @Inject constructor(
    private val repository: GenRepositoryImpl_763_
) : GenUseCase_763_<GenModel_763_, GenModel_763_> {
    override suspend fun invoke(params: GenModel_763_): GenModel_763_ = repository.save(params)
}

class GenDeleteUseCase_763_ @Inject constructor(
    private val repository: GenRepositoryImpl_763_
) : GenUseCase_763_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_763_ @Inject constructor(
    private val repository: GenRepositoryImpl_763_
) : GenUseCase_763_<String, List<GenModel_763_>> {
    override suspend fun invoke(params: String): List<GenModel_763_> = repository.search(params)
}

abstract class GenMapper_763_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_763_ : GenMapper_763_<GenModel_763_, String>() {
    override fun map(input: GenModel_763_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_763_ : GenMapper_763_<String, GenModel_763_>() {
    override fun map(input: String): GenModel_763_ {
        val parts = input.split(":")
        return GenModel_763_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_763_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_763_,
    private val saveUseCase: GenSaveUseCase_763_,
    private val deleteUseCase: GenDeleteUseCase_763_,
    private val searchUseCase: GenSearchUseCase_763_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_763_>(GenState_763_.Idle)
    val state: StateFlow<GenState_763_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_763_) {
        when (event) {
            is GenEvent_763_.Load -> loadAll()
            is GenEvent_763_.Update -> save(event.model)
            is GenEvent_763_.Delete -> delete(event.id)
            is GenEvent_763_.Refresh -> loadAll()
            is GenEvent_763_.Search -> search(event.query)
            is GenEvent_763_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_763_.Loading; _state.value = GenState_763_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_763_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_763_.Success(searchUseCase(query)) } }
}
