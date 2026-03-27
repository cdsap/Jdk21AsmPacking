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

data class GenModel_63_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_63_ {
    data class Load(val id: Long) : GenEvent_63_()
    data class Update(val model: GenModel_63_) : GenEvent_63_()
    data class Delete(val id: Long) : GenEvent_63_()
    data object Refresh : GenEvent_63_()
    data class Search(val query: String) : GenEvent_63_()
    data class Filter(val predicate: String) : GenEvent_63_()
}

sealed class GenState_63_ {
    data object Idle : GenState_63_()
    data object Loading : GenState_63_()
    data class Success(val items: List<GenModel_63_>) : GenState_63_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_63_()
    data class Partial(val items: List<GenModel_63_>, val hasMore: Boolean) : GenState_63_()
}

interface GenRepository_63_ {
    suspend fun getAll(): List<GenModel_63_>
    suspend fun getById(id: Long): GenModel_63_?
    suspend fun save(model: GenModel_63_): GenModel_63_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_63_>
}

@Singleton
class GenRepositoryImpl_63_ @Inject constructor() : GenRepository_63_ {
    private val store = mutableMapOf<Long, GenModel_63_>()
    override suspend fun getAll(): List<GenModel_63_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_63_? = store[id]
    override suspend fun save(model: GenModel_63_): GenModel_63_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_63_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_63_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_63_ @Inject constructor(
    private val repository: GenRepositoryImpl_63_
) : GenUseCase_63_<Unit, List<GenModel_63_>> {
    override suspend fun invoke(params: Unit): List<GenModel_63_> = repository.getAll()
}

class GenSaveUseCase_63_ @Inject constructor(
    private val repository: GenRepositoryImpl_63_
) : GenUseCase_63_<GenModel_63_, GenModel_63_> {
    override suspend fun invoke(params: GenModel_63_): GenModel_63_ = repository.save(params)
}

class GenDeleteUseCase_63_ @Inject constructor(
    private val repository: GenRepositoryImpl_63_
) : GenUseCase_63_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_63_ @Inject constructor(
    private val repository: GenRepositoryImpl_63_
) : GenUseCase_63_<String, List<GenModel_63_>> {
    override suspend fun invoke(params: String): List<GenModel_63_> = repository.search(params)
}

abstract class GenMapper_63_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_63_ : GenMapper_63_<GenModel_63_, String>() {
    override fun map(input: GenModel_63_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_63_ : GenMapper_63_<String, GenModel_63_>() {
    override fun map(input: String): GenModel_63_ {
        val parts = input.split(":")
        return GenModel_63_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_63_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_63_,
    private val saveUseCase: GenSaveUseCase_63_,
    private val deleteUseCase: GenDeleteUseCase_63_,
    private val searchUseCase: GenSearchUseCase_63_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_63_>(GenState_63_.Idle)
    val state: StateFlow<GenState_63_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_63_) {
        when (event) {
            is GenEvent_63_.Load -> loadAll()
            is GenEvent_63_.Update -> save(event.model)
            is GenEvent_63_.Delete -> delete(event.id)
            is GenEvent_63_.Refresh -> loadAll()
            is GenEvent_63_.Search -> search(event.query)
            is GenEvent_63_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_63_.Loading; _state.value = GenState_63_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_63_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_63_.Success(searchUseCase(query)) } }
}
