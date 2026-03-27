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

data class GenModel_391_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_391_ {
    data class Load(val id: Long) : GenEvent_391_()
    data class Update(val model: GenModel_391_) : GenEvent_391_()
    data class Delete(val id: Long) : GenEvent_391_()
    data object Refresh : GenEvent_391_()
    data class Search(val query: String) : GenEvent_391_()
    data class Filter(val predicate: String) : GenEvent_391_()
}

sealed class GenState_391_ {
    data object Idle : GenState_391_()
    data object Loading : GenState_391_()
    data class Success(val items: List<GenModel_391_>) : GenState_391_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_391_()
    data class Partial(val items: List<GenModel_391_>, val hasMore: Boolean) : GenState_391_()
}

interface GenRepository_391_ {
    suspend fun getAll(): List<GenModel_391_>
    suspend fun getById(id: Long): GenModel_391_?
    suspend fun save(model: GenModel_391_): GenModel_391_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_391_>
}

@Singleton
class GenRepositoryImpl_391_ @Inject constructor() : GenRepository_391_ {
    private val store = mutableMapOf<Long, GenModel_391_>()
    override suspend fun getAll(): List<GenModel_391_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_391_? = store[id]
    override suspend fun save(model: GenModel_391_): GenModel_391_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_391_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_391_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_391_ @Inject constructor(
    private val repository: GenRepositoryImpl_391_
) : GenUseCase_391_<Unit, List<GenModel_391_>> {
    override suspend fun invoke(params: Unit): List<GenModel_391_> = repository.getAll()
}

class GenSaveUseCase_391_ @Inject constructor(
    private val repository: GenRepositoryImpl_391_
) : GenUseCase_391_<GenModel_391_, GenModel_391_> {
    override suspend fun invoke(params: GenModel_391_): GenModel_391_ = repository.save(params)
}

class GenDeleteUseCase_391_ @Inject constructor(
    private val repository: GenRepositoryImpl_391_
) : GenUseCase_391_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_391_ @Inject constructor(
    private val repository: GenRepositoryImpl_391_
) : GenUseCase_391_<String, List<GenModel_391_>> {
    override suspend fun invoke(params: String): List<GenModel_391_> = repository.search(params)
}

abstract class GenMapper_391_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_391_ : GenMapper_391_<GenModel_391_, String>() {
    override fun map(input: GenModel_391_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_391_ : GenMapper_391_<String, GenModel_391_>() {
    override fun map(input: String): GenModel_391_ {
        val parts = input.split(":")
        return GenModel_391_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_391_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_391_,
    private val saveUseCase: GenSaveUseCase_391_,
    private val deleteUseCase: GenDeleteUseCase_391_,
    private val searchUseCase: GenSearchUseCase_391_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_391_>(GenState_391_.Idle)
    val state: StateFlow<GenState_391_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_391_) {
        when (event) {
            is GenEvent_391_.Load -> loadAll()
            is GenEvent_391_.Update -> save(event.model)
            is GenEvent_391_.Delete -> delete(event.id)
            is GenEvent_391_.Refresh -> loadAll()
            is GenEvent_391_.Search -> search(event.query)
            is GenEvent_391_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_391_.Loading; _state.value = GenState_391_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_391_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_391_.Success(searchUseCase(query)) } }
}
