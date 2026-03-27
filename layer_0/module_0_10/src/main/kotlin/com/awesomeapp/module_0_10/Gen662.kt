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

data class GenModel_662_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_662_ {
    data class Load(val id: Long) : GenEvent_662_()
    data class Update(val model: GenModel_662_) : GenEvent_662_()
    data class Delete(val id: Long) : GenEvent_662_()
    data object Refresh : GenEvent_662_()
    data class Search(val query: String) : GenEvent_662_()
    data class Filter(val predicate: String) : GenEvent_662_()
}

sealed class GenState_662_ {
    data object Idle : GenState_662_()
    data object Loading : GenState_662_()
    data class Success(val items: List<GenModel_662_>) : GenState_662_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_662_()
    data class Partial(val items: List<GenModel_662_>, val hasMore: Boolean) : GenState_662_()
}

interface GenRepository_662_ {
    suspend fun getAll(): List<GenModel_662_>
    suspend fun getById(id: Long): GenModel_662_?
    suspend fun save(model: GenModel_662_): GenModel_662_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_662_>
}

@Singleton
class GenRepositoryImpl_662_ @Inject constructor() : GenRepository_662_ {
    private val store = mutableMapOf<Long, GenModel_662_>()
    override suspend fun getAll(): List<GenModel_662_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_662_? = store[id]
    override suspend fun save(model: GenModel_662_): GenModel_662_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_662_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_662_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_662_ @Inject constructor(
    private val repository: GenRepositoryImpl_662_
) : GenUseCase_662_<Unit, List<GenModel_662_>> {
    override suspend fun invoke(params: Unit): List<GenModel_662_> = repository.getAll()
}

class GenSaveUseCase_662_ @Inject constructor(
    private val repository: GenRepositoryImpl_662_
) : GenUseCase_662_<GenModel_662_, GenModel_662_> {
    override suspend fun invoke(params: GenModel_662_): GenModel_662_ = repository.save(params)
}

class GenDeleteUseCase_662_ @Inject constructor(
    private val repository: GenRepositoryImpl_662_
) : GenUseCase_662_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_662_ @Inject constructor(
    private val repository: GenRepositoryImpl_662_
) : GenUseCase_662_<String, List<GenModel_662_>> {
    override suspend fun invoke(params: String): List<GenModel_662_> = repository.search(params)
}

abstract class GenMapper_662_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_662_ : GenMapper_662_<GenModel_662_, String>() {
    override fun map(input: GenModel_662_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_662_ : GenMapper_662_<String, GenModel_662_>() {
    override fun map(input: String): GenModel_662_ {
        val parts = input.split(":")
        return GenModel_662_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_662_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_662_,
    private val saveUseCase: GenSaveUseCase_662_,
    private val deleteUseCase: GenDeleteUseCase_662_,
    private val searchUseCase: GenSearchUseCase_662_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_662_>(GenState_662_.Idle)
    val state: StateFlow<GenState_662_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_662_) {
        when (event) {
            is GenEvent_662_.Load -> loadAll()
            is GenEvent_662_.Update -> save(event.model)
            is GenEvent_662_.Delete -> delete(event.id)
            is GenEvent_662_.Refresh -> loadAll()
            is GenEvent_662_.Search -> search(event.query)
            is GenEvent_662_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_662_.Loading; _state.value = GenState_662_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_662_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_662_.Success(searchUseCase(query)) } }
}
