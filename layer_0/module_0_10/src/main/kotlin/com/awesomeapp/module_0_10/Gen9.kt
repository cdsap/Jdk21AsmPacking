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

data class GenModel_9_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_9_ {
    data class Load(val id: Long) : GenEvent_9_()
    data class Update(val model: GenModel_9_) : GenEvent_9_()
    data class Delete(val id: Long) : GenEvent_9_()
    data object Refresh : GenEvent_9_()
    data class Search(val query: String) : GenEvent_9_()
    data class Filter(val predicate: String) : GenEvent_9_()
}

sealed class GenState_9_ {
    data object Idle : GenState_9_()
    data object Loading : GenState_9_()
    data class Success(val items: List<GenModel_9_>) : GenState_9_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_9_()
    data class Partial(val items: List<GenModel_9_>, val hasMore: Boolean) : GenState_9_()
}

interface GenRepository_9_ {
    suspend fun getAll(): List<GenModel_9_>
    suspend fun getById(id: Long): GenModel_9_?
    suspend fun save(model: GenModel_9_): GenModel_9_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_9_>
}

@Singleton
class GenRepositoryImpl_9_ @Inject constructor() : GenRepository_9_ {
    private val store = mutableMapOf<Long, GenModel_9_>()
    override suspend fun getAll(): List<GenModel_9_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_9_? = store[id]
    override suspend fun save(model: GenModel_9_): GenModel_9_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_9_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_9_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_9_ @Inject constructor(
    private val repository: GenRepositoryImpl_9_
) : GenUseCase_9_<Unit, List<GenModel_9_>> {
    override suspend fun invoke(params: Unit): List<GenModel_9_> = repository.getAll()
}

class GenSaveUseCase_9_ @Inject constructor(
    private val repository: GenRepositoryImpl_9_
) : GenUseCase_9_<GenModel_9_, GenModel_9_> {
    override suspend fun invoke(params: GenModel_9_): GenModel_9_ = repository.save(params)
}

class GenDeleteUseCase_9_ @Inject constructor(
    private val repository: GenRepositoryImpl_9_
) : GenUseCase_9_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_9_ @Inject constructor(
    private val repository: GenRepositoryImpl_9_
) : GenUseCase_9_<String, List<GenModel_9_>> {
    override suspend fun invoke(params: String): List<GenModel_9_> = repository.search(params)
}

abstract class GenMapper_9_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_9_ : GenMapper_9_<GenModel_9_, String>() {
    override fun map(input: GenModel_9_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_9_ : GenMapper_9_<String, GenModel_9_>() {
    override fun map(input: String): GenModel_9_ {
        val parts = input.split(":")
        return GenModel_9_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_9_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_9_,
    private val saveUseCase: GenSaveUseCase_9_,
    private val deleteUseCase: GenDeleteUseCase_9_,
    private val searchUseCase: GenSearchUseCase_9_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_9_>(GenState_9_.Idle)
    val state: StateFlow<GenState_9_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_9_) {
        when (event) {
            is GenEvent_9_.Load -> loadAll()
            is GenEvent_9_.Update -> save(event.model)
            is GenEvent_9_.Delete -> delete(event.id)
            is GenEvent_9_.Refresh -> loadAll()
            is GenEvent_9_.Search -> search(event.query)
            is GenEvent_9_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_9_.Loading; _state.value = GenState_9_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_9_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_9_.Success(searchUseCase(query)) } }
}
