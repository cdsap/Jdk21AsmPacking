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

data class GenModel_828_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_828_ {
    data class Load(val id: Long) : GenEvent_828_()
    data class Update(val model: GenModel_828_) : GenEvent_828_()
    data class Delete(val id: Long) : GenEvent_828_()
    data object Refresh : GenEvent_828_()
    data class Search(val query: String) : GenEvent_828_()
    data class Filter(val predicate: String) : GenEvent_828_()
}

sealed class GenState_828_ {
    data object Idle : GenState_828_()
    data object Loading : GenState_828_()
    data class Success(val items: List<GenModel_828_>) : GenState_828_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_828_()
    data class Partial(val items: List<GenModel_828_>, val hasMore: Boolean) : GenState_828_()
}

interface GenRepository_828_ {
    suspend fun getAll(): List<GenModel_828_>
    suspend fun getById(id: Long): GenModel_828_?
    suspend fun save(model: GenModel_828_): GenModel_828_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_828_>
}

@Singleton
class GenRepositoryImpl_828_ @Inject constructor() : GenRepository_828_ {
    private val store = mutableMapOf<Long, GenModel_828_>()
    override suspend fun getAll(): List<GenModel_828_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_828_? = store[id]
    override suspend fun save(model: GenModel_828_): GenModel_828_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_828_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_828_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_828_ @Inject constructor(
    private val repository: GenRepositoryImpl_828_
) : GenUseCase_828_<Unit, List<GenModel_828_>> {
    override suspend fun invoke(params: Unit): List<GenModel_828_> = repository.getAll()
}

class GenSaveUseCase_828_ @Inject constructor(
    private val repository: GenRepositoryImpl_828_
) : GenUseCase_828_<GenModel_828_, GenModel_828_> {
    override suspend fun invoke(params: GenModel_828_): GenModel_828_ = repository.save(params)
}

class GenDeleteUseCase_828_ @Inject constructor(
    private val repository: GenRepositoryImpl_828_
) : GenUseCase_828_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_828_ @Inject constructor(
    private val repository: GenRepositoryImpl_828_
) : GenUseCase_828_<String, List<GenModel_828_>> {
    override suspend fun invoke(params: String): List<GenModel_828_> = repository.search(params)
}

abstract class GenMapper_828_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_828_ : GenMapper_828_<GenModel_828_, String>() {
    override fun map(input: GenModel_828_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_828_ : GenMapper_828_<String, GenModel_828_>() {
    override fun map(input: String): GenModel_828_ {
        val parts = input.split(":")
        return GenModel_828_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_828_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_828_,
    private val saveUseCase: GenSaveUseCase_828_,
    private val deleteUseCase: GenDeleteUseCase_828_,
    private val searchUseCase: GenSearchUseCase_828_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_828_>(GenState_828_.Idle)
    val state: StateFlow<GenState_828_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_828_) {
        when (event) {
            is GenEvent_828_.Load -> loadAll()
            is GenEvent_828_.Update -> save(event.model)
            is GenEvent_828_.Delete -> delete(event.id)
            is GenEvent_828_.Refresh -> loadAll()
            is GenEvent_828_.Search -> search(event.query)
            is GenEvent_828_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_828_.Loading; _state.value = GenState_828_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_828_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_828_.Success(searchUseCase(query)) } }
}
