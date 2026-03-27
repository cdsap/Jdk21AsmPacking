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

data class GenModel_789_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_789_ {
    data class Load(val id: Long) : GenEvent_789_()
    data class Update(val model: GenModel_789_) : GenEvent_789_()
    data class Delete(val id: Long) : GenEvent_789_()
    data object Refresh : GenEvent_789_()
    data class Search(val query: String) : GenEvent_789_()
    data class Filter(val predicate: String) : GenEvent_789_()
}

sealed class GenState_789_ {
    data object Idle : GenState_789_()
    data object Loading : GenState_789_()
    data class Success(val items: List<GenModel_789_>) : GenState_789_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_789_()
    data class Partial(val items: List<GenModel_789_>, val hasMore: Boolean) : GenState_789_()
}

interface GenRepository_789_ {
    suspend fun getAll(): List<GenModel_789_>
    suspend fun getById(id: Long): GenModel_789_?
    suspend fun save(model: GenModel_789_): GenModel_789_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_789_>
}

@Singleton
class GenRepositoryImpl_789_ @Inject constructor() : GenRepository_789_ {
    private val store = mutableMapOf<Long, GenModel_789_>()
    override suspend fun getAll(): List<GenModel_789_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_789_? = store[id]
    override suspend fun save(model: GenModel_789_): GenModel_789_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_789_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_789_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_789_ @Inject constructor(
    private val repository: GenRepositoryImpl_789_
) : GenUseCase_789_<Unit, List<GenModel_789_>> {
    override suspend fun invoke(params: Unit): List<GenModel_789_> = repository.getAll()
}

class GenSaveUseCase_789_ @Inject constructor(
    private val repository: GenRepositoryImpl_789_
) : GenUseCase_789_<GenModel_789_, GenModel_789_> {
    override suspend fun invoke(params: GenModel_789_): GenModel_789_ = repository.save(params)
}

class GenDeleteUseCase_789_ @Inject constructor(
    private val repository: GenRepositoryImpl_789_
) : GenUseCase_789_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_789_ @Inject constructor(
    private val repository: GenRepositoryImpl_789_
) : GenUseCase_789_<String, List<GenModel_789_>> {
    override suspend fun invoke(params: String): List<GenModel_789_> = repository.search(params)
}

abstract class GenMapper_789_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_789_ : GenMapper_789_<GenModel_789_, String>() {
    override fun map(input: GenModel_789_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_789_ : GenMapper_789_<String, GenModel_789_>() {
    override fun map(input: String): GenModel_789_ {
        val parts = input.split(":")
        return GenModel_789_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_789_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_789_,
    private val saveUseCase: GenSaveUseCase_789_,
    private val deleteUseCase: GenDeleteUseCase_789_,
    private val searchUseCase: GenSearchUseCase_789_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_789_>(GenState_789_.Idle)
    val state: StateFlow<GenState_789_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_789_) {
        when (event) {
            is GenEvent_789_.Load -> loadAll()
            is GenEvent_789_.Update -> save(event.model)
            is GenEvent_789_.Delete -> delete(event.id)
            is GenEvent_789_.Refresh -> loadAll()
            is GenEvent_789_.Search -> search(event.query)
            is GenEvent_789_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_789_.Loading; _state.value = GenState_789_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_789_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_789_.Success(searchUseCase(query)) } }
}
