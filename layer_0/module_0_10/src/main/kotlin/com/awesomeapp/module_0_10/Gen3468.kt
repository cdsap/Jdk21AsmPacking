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

data class GenModel_3468_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3468_ {
    data class Load(val id: Long) : GenEvent_3468_()
    data class Update(val model: GenModel_3468_) : GenEvent_3468_()
    data class Delete(val id: Long) : GenEvent_3468_()
    data object Refresh : GenEvent_3468_()
    data class Search(val query: String) : GenEvent_3468_()
    data class Filter(val predicate: String) : GenEvent_3468_()
}

sealed class GenState_3468_ {
    data object Idle : GenState_3468_()
    data object Loading : GenState_3468_()
    data class Success(val items: List<GenModel_3468_>) : GenState_3468_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3468_()
    data class Partial(val items: List<GenModel_3468_>, val hasMore: Boolean) : GenState_3468_()
}

interface GenRepository_3468_ {
    suspend fun getAll(): List<GenModel_3468_>
    suspend fun getById(id: Long): GenModel_3468_?
    suspend fun save(model: GenModel_3468_): GenModel_3468_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3468_>
}

@Singleton
class GenRepositoryImpl_3468_ @Inject constructor() : GenRepository_3468_ {
    private val store = mutableMapOf<Long, GenModel_3468_>()
    override suspend fun getAll(): List<GenModel_3468_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3468_? = store[id]
    override suspend fun save(model: GenModel_3468_): GenModel_3468_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3468_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3468_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3468_ @Inject constructor(
    private val repository: GenRepositoryImpl_3468_
) : GenUseCase_3468_<Unit, List<GenModel_3468_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3468_> = repository.getAll()
}

class GenSaveUseCase_3468_ @Inject constructor(
    private val repository: GenRepositoryImpl_3468_
) : GenUseCase_3468_<GenModel_3468_, GenModel_3468_> {
    override suspend fun invoke(params: GenModel_3468_): GenModel_3468_ = repository.save(params)
}

class GenDeleteUseCase_3468_ @Inject constructor(
    private val repository: GenRepositoryImpl_3468_
) : GenUseCase_3468_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3468_ @Inject constructor(
    private val repository: GenRepositoryImpl_3468_
) : GenUseCase_3468_<String, List<GenModel_3468_>> {
    override suspend fun invoke(params: String): List<GenModel_3468_> = repository.search(params)
}

abstract class GenMapper_3468_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3468_ : GenMapper_3468_<GenModel_3468_, String>() {
    override fun map(input: GenModel_3468_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3468_ : GenMapper_3468_<String, GenModel_3468_>() {
    override fun map(input: String): GenModel_3468_ {
        val parts = input.split(":")
        return GenModel_3468_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3468_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3468_,
    private val saveUseCase: GenSaveUseCase_3468_,
    private val deleteUseCase: GenDeleteUseCase_3468_,
    private val searchUseCase: GenSearchUseCase_3468_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3468_>(GenState_3468_.Idle)
    val state: StateFlow<GenState_3468_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3468_) {
        when (event) {
            is GenEvent_3468_.Load -> loadAll()
            is GenEvent_3468_.Update -> save(event.model)
            is GenEvent_3468_.Delete -> delete(event.id)
            is GenEvent_3468_.Refresh -> loadAll()
            is GenEvent_3468_.Search -> search(event.query)
            is GenEvent_3468_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3468_.Loading; _state.value = GenState_3468_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3468_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3468_.Success(searchUseCase(query)) } }
}
