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

data class GenModel_997_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_997_ {
    data class Load(val id: Long) : GenEvent_997_()
    data class Update(val model: GenModel_997_) : GenEvent_997_()
    data class Delete(val id: Long) : GenEvent_997_()
    data object Refresh : GenEvent_997_()
    data class Search(val query: String) : GenEvent_997_()
    data class Filter(val predicate: String) : GenEvent_997_()
}

sealed class GenState_997_ {
    data object Idle : GenState_997_()
    data object Loading : GenState_997_()
    data class Success(val items: List<GenModel_997_>) : GenState_997_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_997_()
    data class Partial(val items: List<GenModel_997_>, val hasMore: Boolean) : GenState_997_()
}

interface GenRepository_997_ {
    suspend fun getAll(): List<GenModel_997_>
    suspend fun getById(id: Long): GenModel_997_?
    suspend fun save(model: GenModel_997_): GenModel_997_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_997_>
}

@Singleton
class GenRepositoryImpl_997_ @Inject constructor() : GenRepository_997_ {
    private val store = mutableMapOf<Long, GenModel_997_>()
    override suspend fun getAll(): List<GenModel_997_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_997_? = store[id]
    override suspend fun save(model: GenModel_997_): GenModel_997_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_997_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_997_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_997_ @Inject constructor(
    private val repository: GenRepositoryImpl_997_
) : GenUseCase_997_<Unit, List<GenModel_997_>> {
    override suspend fun invoke(params: Unit): List<GenModel_997_> = repository.getAll()
}

class GenSaveUseCase_997_ @Inject constructor(
    private val repository: GenRepositoryImpl_997_
) : GenUseCase_997_<GenModel_997_, GenModel_997_> {
    override suspend fun invoke(params: GenModel_997_): GenModel_997_ = repository.save(params)
}

class GenDeleteUseCase_997_ @Inject constructor(
    private val repository: GenRepositoryImpl_997_
) : GenUseCase_997_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_997_ @Inject constructor(
    private val repository: GenRepositoryImpl_997_
) : GenUseCase_997_<String, List<GenModel_997_>> {
    override suspend fun invoke(params: String): List<GenModel_997_> = repository.search(params)
}

abstract class GenMapper_997_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_997_ : GenMapper_997_<GenModel_997_, String>() {
    override fun map(input: GenModel_997_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_997_ : GenMapper_997_<String, GenModel_997_>() {
    override fun map(input: String): GenModel_997_ {
        val parts = input.split(":")
        return GenModel_997_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_997_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_997_,
    private val saveUseCase: GenSaveUseCase_997_,
    private val deleteUseCase: GenDeleteUseCase_997_,
    private val searchUseCase: GenSearchUseCase_997_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_997_>(GenState_997_.Idle)
    val state: StateFlow<GenState_997_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_997_) {
        when (event) {
            is GenEvent_997_.Load -> loadAll()
            is GenEvent_997_.Update -> save(event.model)
            is GenEvent_997_.Delete -> delete(event.id)
            is GenEvent_997_.Refresh -> loadAll()
            is GenEvent_997_.Search -> search(event.query)
            is GenEvent_997_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_997_.Loading; _state.value = GenState_997_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_997_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_997_.Success(searchUseCase(query)) } }
}
