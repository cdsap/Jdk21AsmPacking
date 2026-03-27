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

data class GenModel_223_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_223_ {
    data class Load(val id: Long) : GenEvent_223_()
    data class Update(val model: GenModel_223_) : GenEvent_223_()
    data class Delete(val id: Long) : GenEvent_223_()
    data object Refresh : GenEvent_223_()
    data class Search(val query: String) : GenEvent_223_()
    data class Filter(val predicate: String) : GenEvent_223_()
}

sealed class GenState_223_ {
    data object Idle : GenState_223_()
    data object Loading : GenState_223_()
    data class Success(val items: List<GenModel_223_>) : GenState_223_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_223_()
    data class Partial(val items: List<GenModel_223_>, val hasMore: Boolean) : GenState_223_()
}

interface GenRepository_223_ {
    suspend fun getAll(): List<GenModel_223_>
    suspend fun getById(id: Long): GenModel_223_?
    suspend fun save(model: GenModel_223_): GenModel_223_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_223_>
}

@Singleton
class GenRepositoryImpl_223_ @Inject constructor() : GenRepository_223_ {
    private val store = mutableMapOf<Long, GenModel_223_>()
    override suspend fun getAll(): List<GenModel_223_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_223_? = store[id]
    override suspend fun save(model: GenModel_223_): GenModel_223_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_223_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_223_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_223_ @Inject constructor(
    private val repository: GenRepositoryImpl_223_
) : GenUseCase_223_<Unit, List<GenModel_223_>> {
    override suspend fun invoke(params: Unit): List<GenModel_223_> = repository.getAll()
}

class GenSaveUseCase_223_ @Inject constructor(
    private val repository: GenRepositoryImpl_223_
) : GenUseCase_223_<GenModel_223_, GenModel_223_> {
    override suspend fun invoke(params: GenModel_223_): GenModel_223_ = repository.save(params)
}

class GenDeleteUseCase_223_ @Inject constructor(
    private val repository: GenRepositoryImpl_223_
) : GenUseCase_223_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_223_ @Inject constructor(
    private val repository: GenRepositoryImpl_223_
) : GenUseCase_223_<String, List<GenModel_223_>> {
    override suspend fun invoke(params: String): List<GenModel_223_> = repository.search(params)
}

abstract class GenMapper_223_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_223_ : GenMapper_223_<GenModel_223_, String>() {
    override fun map(input: GenModel_223_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_223_ : GenMapper_223_<String, GenModel_223_>() {
    override fun map(input: String): GenModel_223_ {
        val parts = input.split(":")
        return GenModel_223_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_223_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_223_,
    private val saveUseCase: GenSaveUseCase_223_,
    private val deleteUseCase: GenDeleteUseCase_223_,
    private val searchUseCase: GenSearchUseCase_223_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_223_>(GenState_223_.Idle)
    val state: StateFlow<GenState_223_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_223_) {
        when (event) {
            is GenEvent_223_.Load -> loadAll()
            is GenEvent_223_.Update -> save(event.model)
            is GenEvent_223_.Delete -> delete(event.id)
            is GenEvent_223_.Refresh -> loadAll()
            is GenEvent_223_.Search -> search(event.query)
            is GenEvent_223_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_223_.Loading; _state.value = GenState_223_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_223_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_223_.Success(searchUseCase(query)) } }
}
