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

data class GenModel_144_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_144_ {
    data class Load(val id: Long) : GenEvent_144_()
    data class Update(val model: GenModel_144_) : GenEvent_144_()
    data class Delete(val id: Long) : GenEvent_144_()
    data object Refresh : GenEvent_144_()
    data class Search(val query: String) : GenEvent_144_()
    data class Filter(val predicate: String) : GenEvent_144_()
}

sealed class GenState_144_ {
    data object Idle : GenState_144_()
    data object Loading : GenState_144_()
    data class Success(val items: List<GenModel_144_>) : GenState_144_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_144_()
    data class Partial(val items: List<GenModel_144_>, val hasMore: Boolean) : GenState_144_()
}

interface GenRepository_144_ {
    suspend fun getAll(): List<GenModel_144_>
    suspend fun getById(id: Long): GenModel_144_?
    suspend fun save(model: GenModel_144_): GenModel_144_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_144_>
}

@Singleton
class GenRepositoryImpl_144_ @Inject constructor() : GenRepository_144_ {
    private val store = mutableMapOf<Long, GenModel_144_>()
    override suspend fun getAll(): List<GenModel_144_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_144_? = store[id]
    override suspend fun save(model: GenModel_144_): GenModel_144_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_144_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_144_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_144_ @Inject constructor(
    private val repository: GenRepositoryImpl_144_
) : GenUseCase_144_<Unit, List<GenModel_144_>> {
    override suspend fun invoke(params: Unit): List<GenModel_144_> = repository.getAll()
}

class GenSaveUseCase_144_ @Inject constructor(
    private val repository: GenRepositoryImpl_144_
) : GenUseCase_144_<GenModel_144_, GenModel_144_> {
    override suspend fun invoke(params: GenModel_144_): GenModel_144_ = repository.save(params)
}

class GenDeleteUseCase_144_ @Inject constructor(
    private val repository: GenRepositoryImpl_144_
) : GenUseCase_144_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_144_ @Inject constructor(
    private val repository: GenRepositoryImpl_144_
) : GenUseCase_144_<String, List<GenModel_144_>> {
    override suspend fun invoke(params: String): List<GenModel_144_> = repository.search(params)
}

abstract class GenMapper_144_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_144_ : GenMapper_144_<GenModel_144_, String>() {
    override fun map(input: GenModel_144_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_144_ : GenMapper_144_<String, GenModel_144_>() {
    override fun map(input: String): GenModel_144_ {
        val parts = input.split(":")
        return GenModel_144_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_144_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_144_,
    private val saveUseCase: GenSaveUseCase_144_,
    private val deleteUseCase: GenDeleteUseCase_144_,
    private val searchUseCase: GenSearchUseCase_144_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_144_>(GenState_144_.Idle)
    val state: StateFlow<GenState_144_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_144_) {
        when (event) {
            is GenEvent_144_.Load -> loadAll()
            is GenEvent_144_.Update -> save(event.model)
            is GenEvent_144_.Delete -> delete(event.id)
            is GenEvent_144_.Refresh -> loadAll()
            is GenEvent_144_.Search -> search(event.query)
            is GenEvent_144_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_144_.Loading; _state.value = GenState_144_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_144_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_144_.Success(searchUseCase(query)) } }
}
