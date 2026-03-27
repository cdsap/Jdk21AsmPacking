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

data class GenModel_428_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_428_ {
    data class Load(val id: Long) : GenEvent_428_()
    data class Update(val model: GenModel_428_) : GenEvent_428_()
    data class Delete(val id: Long) : GenEvent_428_()
    data object Refresh : GenEvent_428_()
    data class Search(val query: String) : GenEvent_428_()
    data class Filter(val predicate: String) : GenEvent_428_()
}

sealed class GenState_428_ {
    data object Idle : GenState_428_()
    data object Loading : GenState_428_()
    data class Success(val items: List<GenModel_428_>) : GenState_428_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_428_()
    data class Partial(val items: List<GenModel_428_>, val hasMore: Boolean) : GenState_428_()
}

interface GenRepository_428_ {
    suspend fun getAll(): List<GenModel_428_>
    suspend fun getById(id: Long): GenModel_428_?
    suspend fun save(model: GenModel_428_): GenModel_428_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_428_>
}

@Singleton
class GenRepositoryImpl_428_ @Inject constructor() : GenRepository_428_ {
    private val store = mutableMapOf<Long, GenModel_428_>()
    override suspend fun getAll(): List<GenModel_428_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_428_? = store[id]
    override suspend fun save(model: GenModel_428_): GenModel_428_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_428_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_428_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_428_ @Inject constructor(
    private val repository: GenRepositoryImpl_428_
) : GenUseCase_428_<Unit, List<GenModel_428_>> {
    override suspend fun invoke(params: Unit): List<GenModel_428_> = repository.getAll()
}

class GenSaveUseCase_428_ @Inject constructor(
    private val repository: GenRepositoryImpl_428_
) : GenUseCase_428_<GenModel_428_, GenModel_428_> {
    override suspend fun invoke(params: GenModel_428_): GenModel_428_ = repository.save(params)
}

class GenDeleteUseCase_428_ @Inject constructor(
    private val repository: GenRepositoryImpl_428_
) : GenUseCase_428_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_428_ @Inject constructor(
    private val repository: GenRepositoryImpl_428_
) : GenUseCase_428_<String, List<GenModel_428_>> {
    override suspend fun invoke(params: String): List<GenModel_428_> = repository.search(params)
}

abstract class GenMapper_428_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_428_ : GenMapper_428_<GenModel_428_, String>() {
    override fun map(input: GenModel_428_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_428_ : GenMapper_428_<String, GenModel_428_>() {
    override fun map(input: String): GenModel_428_ {
        val parts = input.split(":")
        return GenModel_428_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_428_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_428_,
    private val saveUseCase: GenSaveUseCase_428_,
    private val deleteUseCase: GenDeleteUseCase_428_,
    private val searchUseCase: GenSearchUseCase_428_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_428_>(GenState_428_.Idle)
    val state: StateFlow<GenState_428_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_428_) {
        when (event) {
            is GenEvent_428_.Load -> loadAll()
            is GenEvent_428_.Update -> save(event.model)
            is GenEvent_428_.Delete -> delete(event.id)
            is GenEvent_428_.Refresh -> loadAll()
            is GenEvent_428_.Search -> search(event.query)
            is GenEvent_428_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_428_.Loading; _state.value = GenState_428_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_428_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_428_.Success(searchUseCase(query)) } }
}
