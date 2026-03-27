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

data class GenModel_888_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_888_ {
    data class Load(val id: Long) : GenEvent_888_()
    data class Update(val model: GenModel_888_) : GenEvent_888_()
    data class Delete(val id: Long) : GenEvent_888_()
    data object Refresh : GenEvent_888_()
    data class Search(val query: String) : GenEvent_888_()
    data class Filter(val predicate: String) : GenEvent_888_()
}

sealed class GenState_888_ {
    data object Idle : GenState_888_()
    data object Loading : GenState_888_()
    data class Success(val items: List<GenModel_888_>) : GenState_888_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_888_()
    data class Partial(val items: List<GenModel_888_>, val hasMore: Boolean) : GenState_888_()
}

interface GenRepository_888_ {
    suspend fun getAll(): List<GenModel_888_>
    suspend fun getById(id: Long): GenModel_888_?
    suspend fun save(model: GenModel_888_): GenModel_888_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_888_>
}

@Singleton
class GenRepositoryImpl_888_ @Inject constructor() : GenRepository_888_ {
    private val store = mutableMapOf<Long, GenModel_888_>()
    override suspend fun getAll(): List<GenModel_888_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_888_? = store[id]
    override suspend fun save(model: GenModel_888_): GenModel_888_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_888_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_888_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_888_ @Inject constructor(
    private val repository: GenRepositoryImpl_888_
) : GenUseCase_888_<Unit, List<GenModel_888_>> {
    override suspend fun invoke(params: Unit): List<GenModel_888_> = repository.getAll()
}

class GenSaveUseCase_888_ @Inject constructor(
    private val repository: GenRepositoryImpl_888_
) : GenUseCase_888_<GenModel_888_, GenModel_888_> {
    override suspend fun invoke(params: GenModel_888_): GenModel_888_ = repository.save(params)
}

class GenDeleteUseCase_888_ @Inject constructor(
    private val repository: GenRepositoryImpl_888_
) : GenUseCase_888_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_888_ @Inject constructor(
    private val repository: GenRepositoryImpl_888_
) : GenUseCase_888_<String, List<GenModel_888_>> {
    override suspend fun invoke(params: String): List<GenModel_888_> = repository.search(params)
}

abstract class GenMapper_888_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_888_ : GenMapper_888_<GenModel_888_, String>() {
    override fun map(input: GenModel_888_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_888_ : GenMapper_888_<String, GenModel_888_>() {
    override fun map(input: String): GenModel_888_ {
        val parts = input.split(":")
        return GenModel_888_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_888_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_888_,
    private val saveUseCase: GenSaveUseCase_888_,
    private val deleteUseCase: GenDeleteUseCase_888_,
    private val searchUseCase: GenSearchUseCase_888_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_888_>(GenState_888_.Idle)
    val state: StateFlow<GenState_888_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_888_) {
        when (event) {
            is GenEvent_888_.Load -> loadAll()
            is GenEvent_888_.Update -> save(event.model)
            is GenEvent_888_.Delete -> delete(event.id)
            is GenEvent_888_.Refresh -> loadAll()
            is GenEvent_888_.Search -> search(event.query)
            is GenEvent_888_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_888_.Loading; _state.value = GenState_888_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_888_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_888_.Success(searchUseCase(query)) } }
}
