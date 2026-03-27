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

data class GenModel_3002_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3002_ {
    data class Load(val id: Long) : GenEvent_3002_()
    data class Update(val model: GenModel_3002_) : GenEvent_3002_()
    data class Delete(val id: Long) : GenEvent_3002_()
    data object Refresh : GenEvent_3002_()
    data class Search(val query: String) : GenEvent_3002_()
    data class Filter(val predicate: String) : GenEvent_3002_()
}

sealed class GenState_3002_ {
    data object Idle : GenState_3002_()
    data object Loading : GenState_3002_()
    data class Success(val items: List<GenModel_3002_>) : GenState_3002_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3002_()
    data class Partial(val items: List<GenModel_3002_>, val hasMore: Boolean) : GenState_3002_()
}

interface GenRepository_3002_ {
    suspend fun getAll(): List<GenModel_3002_>
    suspend fun getById(id: Long): GenModel_3002_?
    suspend fun save(model: GenModel_3002_): GenModel_3002_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3002_>
}

@Singleton
class GenRepositoryImpl_3002_ @Inject constructor() : GenRepository_3002_ {
    private val store = mutableMapOf<Long, GenModel_3002_>()
    override suspend fun getAll(): List<GenModel_3002_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3002_? = store[id]
    override suspend fun save(model: GenModel_3002_): GenModel_3002_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3002_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3002_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3002_ @Inject constructor(
    private val repository: GenRepositoryImpl_3002_
) : GenUseCase_3002_<Unit, List<GenModel_3002_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3002_> = repository.getAll()
}

class GenSaveUseCase_3002_ @Inject constructor(
    private val repository: GenRepositoryImpl_3002_
) : GenUseCase_3002_<GenModel_3002_, GenModel_3002_> {
    override suspend fun invoke(params: GenModel_3002_): GenModel_3002_ = repository.save(params)
}

class GenDeleteUseCase_3002_ @Inject constructor(
    private val repository: GenRepositoryImpl_3002_
) : GenUseCase_3002_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3002_ @Inject constructor(
    private val repository: GenRepositoryImpl_3002_
) : GenUseCase_3002_<String, List<GenModel_3002_>> {
    override suspend fun invoke(params: String): List<GenModel_3002_> = repository.search(params)
}

abstract class GenMapper_3002_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3002_ : GenMapper_3002_<GenModel_3002_, String>() {
    override fun map(input: GenModel_3002_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3002_ : GenMapper_3002_<String, GenModel_3002_>() {
    override fun map(input: String): GenModel_3002_ {
        val parts = input.split(":")
        return GenModel_3002_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3002_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3002_,
    private val saveUseCase: GenSaveUseCase_3002_,
    private val deleteUseCase: GenDeleteUseCase_3002_,
    private val searchUseCase: GenSearchUseCase_3002_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3002_>(GenState_3002_.Idle)
    val state: StateFlow<GenState_3002_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3002_) {
        when (event) {
            is GenEvent_3002_.Load -> loadAll()
            is GenEvent_3002_.Update -> save(event.model)
            is GenEvent_3002_.Delete -> delete(event.id)
            is GenEvent_3002_.Refresh -> loadAll()
            is GenEvent_3002_.Search -> search(event.query)
            is GenEvent_3002_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3002_.Loading; _state.value = GenState_3002_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3002_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3002_.Success(searchUseCase(query)) } }
}
