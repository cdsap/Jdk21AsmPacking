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

data class GenModel_923_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_923_ {
    data class Load(val id: Long) : GenEvent_923_()
    data class Update(val model: GenModel_923_) : GenEvent_923_()
    data class Delete(val id: Long) : GenEvent_923_()
    data object Refresh : GenEvent_923_()
    data class Search(val query: String) : GenEvent_923_()
    data class Filter(val predicate: String) : GenEvent_923_()
}

sealed class GenState_923_ {
    data object Idle : GenState_923_()
    data object Loading : GenState_923_()
    data class Success(val items: List<GenModel_923_>) : GenState_923_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_923_()
    data class Partial(val items: List<GenModel_923_>, val hasMore: Boolean) : GenState_923_()
}

interface GenRepository_923_ {
    suspend fun getAll(): List<GenModel_923_>
    suspend fun getById(id: Long): GenModel_923_?
    suspend fun save(model: GenModel_923_): GenModel_923_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_923_>
}

@Singleton
class GenRepositoryImpl_923_ @Inject constructor() : GenRepository_923_ {
    private val store = mutableMapOf<Long, GenModel_923_>()
    override suspend fun getAll(): List<GenModel_923_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_923_? = store[id]
    override suspend fun save(model: GenModel_923_): GenModel_923_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_923_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_923_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_923_ @Inject constructor(
    private val repository: GenRepositoryImpl_923_
) : GenUseCase_923_<Unit, List<GenModel_923_>> {
    override suspend fun invoke(params: Unit): List<GenModel_923_> = repository.getAll()
}

class GenSaveUseCase_923_ @Inject constructor(
    private val repository: GenRepositoryImpl_923_
) : GenUseCase_923_<GenModel_923_, GenModel_923_> {
    override suspend fun invoke(params: GenModel_923_): GenModel_923_ = repository.save(params)
}

class GenDeleteUseCase_923_ @Inject constructor(
    private val repository: GenRepositoryImpl_923_
) : GenUseCase_923_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_923_ @Inject constructor(
    private val repository: GenRepositoryImpl_923_
) : GenUseCase_923_<String, List<GenModel_923_>> {
    override suspend fun invoke(params: String): List<GenModel_923_> = repository.search(params)
}

abstract class GenMapper_923_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_923_ : GenMapper_923_<GenModel_923_, String>() {
    override fun map(input: GenModel_923_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_923_ : GenMapper_923_<String, GenModel_923_>() {
    override fun map(input: String): GenModel_923_ {
        val parts = input.split(":")
        return GenModel_923_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_923_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_923_,
    private val saveUseCase: GenSaveUseCase_923_,
    private val deleteUseCase: GenDeleteUseCase_923_,
    private val searchUseCase: GenSearchUseCase_923_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_923_>(GenState_923_.Idle)
    val state: StateFlow<GenState_923_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_923_) {
        when (event) {
            is GenEvent_923_.Load -> loadAll()
            is GenEvent_923_.Update -> save(event.model)
            is GenEvent_923_.Delete -> delete(event.id)
            is GenEvent_923_.Refresh -> loadAll()
            is GenEvent_923_.Search -> search(event.query)
            is GenEvent_923_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_923_.Loading; _state.value = GenState_923_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_923_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_923_.Success(searchUseCase(query)) } }
}
