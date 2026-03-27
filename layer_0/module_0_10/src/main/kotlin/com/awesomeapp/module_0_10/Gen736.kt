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

data class GenModel_736_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_736_ {
    data class Load(val id: Long) : GenEvent_736_()
    data class Update(val model: GenModel_736_) : GenEvent_736_()
    data class Delete(val id: Long) : GenEvent_736_()
    data object Refresh : GenEvent_736_()
    data class Search(val query: String) : GenEvent_736_()
    data class Filter(val predicate: String) : GenEvent_736_()
}

sealed class GenState_736_ {
    data object Idle : GenState_736_()
    data object Loading : GenState_736_()
    data class Success(val items: List<GenModel_736_>) : GenState_736_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_736_()
    data class Partial(val items: List<GenModel_736_>, val hasMore: Boolean) : GenState_736_()
}

interface GenRepository_736_ {
    suspend fun getAll(): List<GenModel_736_>
    suspend fun getById(id: Long): GenModel_736_?
    suspend fun save(model: GenModel_736_): GenModel_736_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_736_>
}

@Singleton
class GenRepositoryImpl_736_ @Inject constructor() : GenRepository_736_ {
    private val store = mutableMapOf<Long, GenModel_736_>()
    override suspend fun getAll(): List<GenModel_736_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_736_? = store[id]
    override suspend fun save(model: GenModel_736_): GenModel_736_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_736_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_736_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_736_ @Inject constructor(
    private val repository: GenRepositoryImpl_736_
) : GenUseCase_736_<Unit, List<GenModel_736_>> {
    override suspend fun invoke(params: Unit): List<GenModel_736_> = repository.getAll()
}

class GenSaveUseCase_736_ @Inject constructor(
    private val repository: GenRepositoryImpl_736_
) : GenUseCase_736_<GenModel_736_, GenModel_736_> {
    override suspend fun invoke(params: GenModel_736_): GenModel_736_ = repository.save(params)
}

class GenDeleteUseCase_736_ @Inject constructor(
    private val repository: GenRepositoryImpl_736_
) : GenUseCase_736_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_736_ @Inject constructor(
    private val repository: GenRepositoryImpl_736_
) : GenUseCase_736_<String, List<GenModel_736_>> {
    override suspend fun invoke(params: String): List<GenModel_736_> = repository.search(params)
}

abstract class GenMapper_736_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_736_ : GenMapper_736_<GenModel_736_, String>() {
    override fun map(input: GenModel_736_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_736_ : GenMapper_736_<String, GenModel_736_>() {
    override fun map(input: String): GenModel_736_ {
        val parts = input.split(":")
        return GenModel_736_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_736_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_736_,
    private val saveUseCase: GenSaveUseCase_736_,
    private val deleteUseCase: GenDeleteUseCase_736_,
    private val searchUseCase: GenSearchUseCase_736_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_736_>(GenState_736_.Idle)
    val state: StateFlow<GenState_736_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_736_) {
        when (event) {
            is GenEvent_736_.Load -> loadAll()
            is GenEvent_736_.Update -> save(event.model)
            is GenEvent_736_.Delete -> delete(event.id)
            is GenEvent_736_.Refresh -> loadAll()
            is GenEvent_736_.Search -> search(event.query)
            is GenEvent_736_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_736_.Loading; _state.value = GenState_736_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_736_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_736_.Success(searchUseCase(query)) } }
}
