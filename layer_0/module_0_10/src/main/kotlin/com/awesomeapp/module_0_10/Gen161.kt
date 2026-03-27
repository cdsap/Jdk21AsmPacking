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

data class GenModel_161_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_161_ {
    data class Load(val id: Long) : GenEvent_161_()
    data class Update(val model: GenModel_161_) : GenEvent_161_()
    data class Delete(val id: Long) : GenEvent_161_()
    data object Refresh : GenEvent_161_()
    data class Search(val query: String) : GenEvent_161_()
    data class Filter(val predicate: String) : GenEvent_161_()
}

sealed class GenState_161_ {
    data object Idle : GenState_161_()
    data object Loading : GenState_161_()
    data class Success(val items: List<GenModel_161_>) : GenState_161_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_161_()
    data class Partial(val items: List<GenModel_161_>, val hasMore: Boolean) : GenState_161_()
}

interface GenRepository_161_ {
    suspend fun getAll(): List<GenModel_161_>
    suspend fun getById(id: Long): GenModel_161_?
    suspend fun save(model: GenModel_161_): GenModel_161_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_161_>
}

@Singleton
class GenRepositoryImpl_161_ @Inject constructor() : GenRepository_161_ {
    private val store = mutableMapOf<Long, GenModel_161_>()
    override suspend fun getAll(): List<GenModel_161_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_161_? = store[id]
    override suspend fun save(model: GenModel_161_): GenModel_161_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_161_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_161_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_161_ @Inject constructor(
    private val repository: GenRepositoryImpl_161_
) : GenUseCase_161_<Unit, List<GenModel_161_>> {
    override suspend fun invoke(params: Unit): List<GenModel_161_> = repository.getAll()
}

class GenSaveUseCase_161_ @Inject constructor(
    private val repository: GenRepositoryImpl_161_
) : GenUseCase_161_<GenModel_161_, GenModel_161_> {
    override suspend fun invoke(params: GenModel_161_): GenModel_161_ = repository.save(params)
}

class GenDeleteUseCase_161_ @Inject constructor(
    private val repository: GenRepositoryImpl_161_
) : GenUseCase_161_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_161_ @Inject constructor(
    private val repository: GenRepositoryImpl_161_
) : GenUseCase_161_<String, List<GenModel_161_>> {
    override suspend fun invoke(params: String): List<GenModel_161_> = repository.search(params)
}

abstract class GenMapper_161_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_161_ : GenMapper_161_<GenModel_161_, String>() {
    override fun map(input: GenModel_161_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_161_ : GenMapper_161_<String, GenModel_161_>() {
    override fun map(input: String): GenModel_161_ {
        val parts = input.split(":")
        return GenModel_161_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_161_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_161_,
    private val saveUseCase: GenSaveUseCase_161_,
    private val deleteUseCase: GenDeleteUseCase_161_,
    private val searchUseCase: GenSearchUseCase_161_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_161_>(GenState_161_.Idle)
    val state: StateFlow<GenState_161_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_161_) {
        when (event) {
            is GenEvent_161_.Load -> loadAll()
            is GenEvent_161_.Update -> save(event.model)
            is GenEvent_161_.Delete -> delete(event.id)
            is GenEvent_161_.Refresh -> loadAll()
            is GenEvent_161_.Search -> search(event.query)
            is GenEvent_161_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_161_.Loading; _state.value = GenState_161_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_161_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_161_.Success(searchUseCase(query)) } }
}
