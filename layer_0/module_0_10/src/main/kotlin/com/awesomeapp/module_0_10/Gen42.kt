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

data class GenModel_42_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_42_ {
    data class Load(val id: Long) : GenEvent_42_()
    data class Update(val model: GenModel_42_) : GenEvent_42_()
    data class Delete(val id: Long) : GenEvent_42_()
    data object Refresh : GenEvent_42_()
    data class Search(val query: String) : GenEvent_42_()
    data class Filter(val predicate: String) : GenEvent_42_()
}

sealed class GenState_42_ {
    data object Idle : GenState_42_()
    data object Loading : GenState_42_()
    data class Success(val items: List<GenModel_42_>) : GenState_42_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_42_()
    data class Partial(val items: List<GenModel_42_>, val hasMore: Boolean) : GenState_42_()
}

interface GenRepository_42_ {
    suspend fun getAll(): List<GenModel_42_>
    suspend fun getById(id: Long): GenModel_42_?
    suspend fun save(model: GenModel_42_): GenModel_42_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_42_>
}

@Singleton
class GenRepositoryImpl_42_ @Inject constructor() : GenRepository_42_ {
    private val store = mutableMapOf<Long, GenModel_42_>()
    override suspend fun getAll(): List<GenModel_42_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_42_? = store[id]
    override suspend fun save(model: GenModel_42_): GenModel_42_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_42_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_42_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_42_ @Inject constructor(
    private val repository: GenRepositoryImpl_42_
) : GenUseCase_42_<Unit, List<GenModel_42_>> {
    override suspend fun invoke(params: Unit): List<GenModel_42_> = repository.getAll()
}

class GenSaveUseCase_42_ @Inject constructor(
    private val repository: GenRepositoryImpl_42_
) : GenUseCase_42_<GenModel_42_, GenModel_42_> {
    override suspend fun invoke(params: GenModel_42_): GenModel_42_ = repository.save(params)
}

class GenDeleteUseCase_42_ @Inject constructor(
    private val repository: GenRepositoryImpl_42_
) : GenUseCase_42_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_42_ @Inject constructor(
    private val repository: GenRepositoryImpl_42_
) : GenUseCase_42_<String, List<GenModel_42_>> {
    override suspend fun invoke(params: String): List<GenModel_42_> = repository.search(params)
}

abstract class GenMapper_42_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_42_ : GenMapper_42_<GenModel_42_, String>() {
    override fun map(input: GenModel_42_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_42_ : GenMapper_42_<String, GenModel_42_>() {
    override fun map(input: String): GenModel_42_ {
        val parts = input.split(":")
        return GenModel_42_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_42_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_42_,
    private val saveUseCase: GenSaveUseCase_42_,
    private val deleteUseCase: GenDeleteUseCase_42_,
    private val searchUseCase: GenSearchUseCase_42_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_42_>(GenState_42_.Idle)
    val state: StateFlow<GenState_42_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_42_) {
        when (event) {
            is GenEvent_42_.Load -> loadAll()
            is GenEvent_42_.Update -> save(event.model)
            is GenEvent_42_.Delete -> delete(event.id)
            is GenEvent_42_.Refresh -> loadAll()
            is GenEvent_42_.Search -> search(event.query)
            is GenEvent_42_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_42_.Loading; _state.value = GenState_42_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_42_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_42_.Success(searchUseCase(query)) } }
}
