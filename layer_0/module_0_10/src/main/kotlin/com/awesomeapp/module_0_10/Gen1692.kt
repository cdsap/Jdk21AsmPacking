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

data class GenModel_1692_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1692_ {
    data class Load(val id: Long) : GenEvent_1692_()
    data class Update(val model: GenModel_1692_) : GenEvent_1692_()
    data class Delete(val id: Long) : GenEvent_1692_()
    data object Refresh : GenEvent_1692_()
    data class Search(val query: String) : GenEvent_1692_()
    data class Filter(val predicate: String) : GenEvent_1692_()
}

sealed class GenState_1692_ {
    data object Idle : GenState_1692_()
    data object Loading : GenState_1692_()
    data class Success(val items: List<GenModel_1692_>) : GenState_1692_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1692_()
    data class Partial(val items: List<GenModel_1692_>, val hasMore: Boolean) : GenState_1692_()
}

interface GenRepository_1692_ {
    suspend fun getAll(): List<GenModel_1692_>
    suspend fun getById(id: Long): GenModel_1692_?
    suspend fun save(model: GenModel_1692_): GenModel_1692_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1692_>
}

@Singleton
class GenRepositoryImpl_1692_ @Inject constructor() : GenRepository_1692_ {
    private val store = mutableMapOf<Long, GenModel_1692_>()
    override suspend fun getAll(): List<GenModel_1692_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1692_? = store[id]
    override suspend fun save(model: GenModel_1692_): GenModel_1692_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1692_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1692_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1692_ @Inject constructor(
    private val repository: GenRepositoryImpl_1692_
) : GenUseCase_1692_<Unit, List<GenModel_1692_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1692_> = repository.getAll()
}

class GenSaveUseCase_1692_ @Inject constructor(
    private val repository: GenRepositoryImpl_1692_
) : GenUseCase_1692_<GenModel_1692_, GenModel_1692_> {
    override suspend fun invoke(params: GenModel_1692_): GenModel_1692_ = repository.save(params)
}

class GenDeleteUseCase_1692_ @Inject constructor(
    private val repository: GenRepositoryImpl_1692_
) : GenUseCase_1692_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1692_ @Inject constructor(
    private val repository: GenRepositoryImpl_1692_
) : GenUseCase_1692_<String, List<GenModel_1692_>> {
    override suspend fun invoke(params: String): List<GenModel_1692_> = repository.search(params)
}

abstract class GenMapper_1692_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1692_ : GenMapper_1692_<GenModel_1692_, String>() {
    override fun map(input: GenModel_1692_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1692_ : GenMapper_1692_<String, GenModel_1692_>() {
    override fun map(input: String): GenModel_1692_ {
        val parts = input.split(":")
        return GenModel_1692_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1692_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1692_,
    private val saveUseCase: GenSaveUseCase_1692_,
    private val deleteUseCase: GenDeleteUseCase_1692_,
    private val searchUseCase: GenSearchUseCase_1692_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1692_>(GenState_1692_.Idle)
    val state: StateFlow<GenState_1692_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1692_) {
        when (event) {
            is GenEvent_1692_.Load -> loadAll()
            is GenEvent_1692_.Update -> save(event.model)
            is GenEvent_1692_.Delete -> delete(event.id)
            is GenEvent_1692_.Refresh -> loadAll()
            is GenEvent_1692_.Search -> search(event.query)
            is GenEvent_1692_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1692_.Loading; _state.value = GenState_1692_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1692_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1692_.Success(searchUseCase(query)) } }
}
