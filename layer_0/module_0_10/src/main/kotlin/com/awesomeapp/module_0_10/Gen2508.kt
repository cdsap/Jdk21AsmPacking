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

data class GenModel_2508_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2508_ {
    data class Load(val id: Long) : GenEvent_2508_()
    data class Update(val model: GenModel_2508_) : GenEvent_2508_()
    data class Delete(val id: Long) : GenEvent_2508_()
    data object Refresh : GenEvent_2508_()
    data class Search(val query: String) : GenEvent_2508_()
    data class Filter(val predicate: String) : GenEvent_2508_()
}

sealed class GenState_2508_ {
    data object Idle : GenState_2508_()
    data object Loading : GenState_2508_()
    data class Success(val items: List<GenModel_2508_>) : GenState_2508_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2508_()
    data class Partial(val items: List<GenModel_2508_>, val hasMore: Boolean) : GenState_2508_()
}

interface GenRepository_2508_ {
    suspend fun getAll(): List<GenModel_2508_>
    suspend fun getById(id: Long): GenModel_2508_?
    suspend fun save(model: GenModel_2508_): GenModel_2508_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2508_>
}

@Singleton
class GenRepositoryImpl_2508_ @Inject constructor() : GenRepository_2508_ {
    private val store = mutableMapOf<Long, GenModel_2508_>()
    override suspend fun getAll(): List<GenModel_2508_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2508_? = store[id]
    override suspend fun save(model: GenModel_2508_): GenModel_2508_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2508_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2508_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2508_ @Inject constructor(
    private val repository: GenRepositoryImpl_2508_
) : GenUseCase_2508_<Unit, List<GenModel_2508_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2508_> = repository.getAll()
}

class GenSaveUseCase_2508_ @Inject constructor(
    private val repository: GenRepositoryImpl_2508_
) : GenUseCase_2508_<GenModel_2508_, GenModel_2508_> {
    override suspend fun invoke(params: GenModel_2508_): GenModel_2508_ = repository.save(params)
}

class GenDeleteUseCase_2508_ @Inject constructor(
    private val repository: GenRepositoryImpl_2508_
) : GenUseCase_2508_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2508_ @Inject constructor(
    private val repository: GenRepositoryImpl_2508_
) : GenUseCase_2508_<String, List<GenModel_2508_>> {
    override suspend fun invoke(params: String): List<GenModel_2508_> = repository.search(params)
}

abstract class GenMapper_2508_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2508_ : GenMapper_2508_<GenModel_2508_, String>() {
    override fun map(input: GenModel_2508_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2508_ : GenMapper_2508_<String, GenModel_2508_>() {
    override fun map(input: String): GenModel_2508_ {
        val parts = input.split(":")
        return GenModel_2508_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2508_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2508_,
    private val saveUseCase: GenSaveUseCase_2508_,
    private val deleteUseCase: GenDeleteUseCase_2508_,
    private val searchUseCase: GenSearchUseCase_2508_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2508_>(GenState_2508_.Idle)
    val state: StateFlow<GenState_2508_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2508_) {
        when (event) {
            is GenEvent_2508_.Load -> loadAll()
            is GenEvent_2508_.Update -> save(event.model)
            is GenEvent_2508_.Delete -> delete(event.id)
            is GenEvent_2508_.Refresh -> loadAll()
            is GenEvent_2508_.Search -> search(event.query)
            is GenEvent_2508_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2508_.Loading; _state.value = GenState_2508_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2508_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2508_.Success(searchUseCase(query)) } }
}
