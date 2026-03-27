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

data class GenModel_296_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_296_ {
    data class Load(val id: Long) : GenEvent_296_()
    data class Update(val model: GenModel_296_) : GenEvent_296_()
    data class Delete(val id: Long) : GenEvent_296_()
    data object Refresh : GenEvent_296_()
    data class Search(val query: String) : GenEvent_296_()
    data class Filter(val predicate: String) : GenEvent_296_()
}

sealed class GenState_296_ {
    data object Idle : GenState_296_()
    data object Loading : GenState_296_()
    data class Success(val items: List<GenModel_296_>) : GenState_296_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_296_()
    data class Partial(val items: List<GenModel_296_>, val hasMore: Boolean) : GenState_296_()
}

interface GenRepository_296_ {
    suspend fun getAll(): List<GenModel_296_>
    suspend fun getById(id: Long): GenModel_296_?
    suspend fun save(model: GenModel_296_): GenModel_296_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_296_>
}

@Singleton
class GenRepositoryImpl_296_ @Inject constructor() : GenRepository_296_ {
    private val store = mutableMapOf<Long, GenModel_296_>()
    override suspend fun getAll(): List<GenModel_296_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_296_? = store[id]
    override suspend fun save(model: GenModel_296_): GenModel_296_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_296_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_296_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_296_ @Inject constructor(
    private val repository: GenRepositoryImpl_296_
) : GenUseCase_296_<Unit, List<GenModel_296_>> {
    override suspend fun invoke(params: Unit): List<GenModel_296_> = repository.getAll()
}

class GenSaveUseCase_296_ @Inject constructor(
    private val repository: GenRepositoryImpl_296_
) : GenUseCase_296_<GenModel_296_, GenModel_296_> {
    override suspend fun invoke(params: GenModel_296_): GenModel_296_ = repository.save(params)
}

class GenDeleteUseCase_296_ @Inject constructor(
    private val repository: GenRepositoryImpl_296_
) : GenUseCase_296_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_296_ @Inject constructor(
    private val repository: GenRepositoryImpl_296_
) : GenUseCase_296_<String, List<GenModel_296_>> {
    override suspend fun invoke(params: String): List<GenModel_296_> = repository.search(params)
}

abstract class GenMapper_296_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_296_ : GenMapper_296_<GenModel_296_, String>() {
    override fun map(input: GenModel_296_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_296_ : GenMapper_296_<String, GenModel_296_>() {
    override fun map(input: String): GenModel_296_ {
        val parts = input.split(":")
        return GenModel_296_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_296_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_296_,
    private val saveUseCase: GenSaveUseCase_296_,
    private val deleteUseCase: GenDeleteUseCase_296_,
    private val searchUseCase: GenSearchUseCase_296_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_296_>(GenState_296_.Idle)
    val state: StateFlow<GenState_296_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_296_) {
        when (event) {
            is GenEvent_296_.Load -> loadAll()
            is GenEvent_296_.Update -> save(event.model)
            is GenEvent_296_.Delete -> delete(event.id)
            is GenEvent_296_.Refresh -> loadAll()
            is GenEvent_296_.Search -> search(event.query)
            is GenEvent_296_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_296_.Loading; _state.value = GenState_296_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_296_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_296_.Success(searchUseCase(query)) } }
}
