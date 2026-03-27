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

data class GenModel_1983_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1983_ {
    data class Load(val id: Long) : GenEvent_1983_()
    data class Update(val model: GenModel_1983_) : GenEvent_1983_()
    data class Delete(val id: Long) : GenEvent_1983_()
    data object Refresh : GenEvent_1983_()
    data class Search(val query: String) : GenEvent_1983_()
    data class Filter(val predicate: String) : GenEvent_1983_()
}

sealed class GenState_1983_ {
    data object Idle : GenState_1983_()
    data object Loading : GenState_1983_()
    data class Success(val items: List<GenModel_1983_>) : GenState_1983_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1983_()
    data class Partial(val items: List<GenModel_1983_>, val hasMore: Boolean) : GenState_1983_()
}

interface GenRepository_1983_ {
    suspend fun getAll(): List<GenModel_1983_>
    suspend fun getById(id: Long): GenModel_1983_?
    suspend fun save(model: GenModel_1983_): GenModel_1983_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1983_>
}

@Singleton
class GenRepositoryImpl_1983_ @Inject constructor() : GenRepository_1983_ {
    private val store = mutableMapOf<Long, GenModel_1983_>()
    override suspend fun getAll(): List<GenModel_1983_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1983_? = store[id]
    override suspend fun save(model: GenModel_1983_): GenModel_1983_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1983_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1983_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1983_ @Inject constructor(
    private val repository: GenRepositoryImpl_1983_
) : GenUseCase_1983_<Unit, List<GenModel_1983_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1983_> = repository.getAll()
}

class GenSaveUseCase_1983_ @Inject constructor(
    private val repository: GenRepositoryImpl_1983_
) : GenUseCase_1983_<GenModel_1983_, GenModel_1983_> {
    override suspend fun invoke(params: GenModel_1983_): GenModel_1983_ = repository.save(params)
}

class GenDeleteUseCase_1983_ @Inject constructor(
    private val repository: GenRepositoryImpl_1983_
) : GenUseCase_1983_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1983_ @Inject constructor(
    private val repository: GenRepositoryImpl_1983_
) : GenUseCase_1983_<String, List<GenModel_1983_>> {
    override suspend fun invoke(params: String): List<GenModel_1983_> = repository.search(params)
}

abstract class GenMapper_1983_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1983_ : GenMapper_1983_<GenModel_1983_, String>() {
    override fun map(input: GenModel_1983_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1983_ : GenMapper_1983_<String, GenModel_1983_>() {
    override fun map(input: String): GenModel_1983_ {
        val parts = input.split(":")
        return GenModel_1983_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1983_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1983_,
    private val saveUseCase: GenSaveUseCase_1983_,
    private val deleteUseCase: GenDeleteUseCase_1983_,
    private val searchUseCase: GenSearchUseCase_1983_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1983_>(GenState_1983_.Idle)
    val state: StateFlow<GenState_1983_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1983_) {
        when (event) {
            is GenEvent_1983_.Load -> loadAll()
            is GenEvent_1983_.Update -> save(event.model)
            is GenEvent_1983_.Delete -> delete(event.id)
            is GenEvent_1983_.Refresh -> loadAll()
            is GenEvent_1983_.Search -> search(event.query)
            is GenEvent_1983_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1983_.Loading; _state.value = GenState_1983_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1983_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1983_.Success(searchUseCase(query)) } }
}
