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

data class GenModel_692_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_692_ {
    data class Load(val id: Long) : GenEvent_692_()
    data class Update(val model: GenModel_692_) : GenEvent_692_()
    data class Delete(val id: Long) : GenEvent_692_()
    data object Refresh : GenEvent_692_()
    data class Search(val query: String) : GenEvent_692_()
    data class Filter(val predicate: String) : GenEvent_692_()
}

sealed class GenState_692_ {
    data object Idle : GenState_692_()
    data object Loading : GenState_692_()
    data class Success(val items: List<GenModel_692_>) : GenState_692_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_692_()
    data class Partial(val items: List<GenModel_692_>, val hasMore: Boolean) : GenState_692_()
}

interface GenRepository_692_ {
    suspend fun getAll(): List<GenModel_692_>
    suspend fun getById(id: Long): GenModel_692_?
    suspend fun save(model: GenModel_692_): GenModel_692_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_692_>
}

@Singleton
class GenRepositoryImpl_692_ @Inject constructor() : GenRepository_692_ {
    private val store = mutableMapOf<Long, GenModel_692_>()
    override suspend fun getAll(): List<GenModel_692_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_692_? = store[id]
    override suspend fun save(model: GenModel_692_): GenModel_692_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_692_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_692_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_692_ @Inject constructor(
    private val repository: GenRepositoryImpl_692_
) : GenUseCase_692_<Unit, List<GenModel_692_>> {
    override suspend fun invoke(params: Unit): List<GenModel_692_> = repository.getAll()
}

class GenSaveUseCase_692_ @Inject constructor(
    private val repository: GenRepositoryImpl_692_
) : GenUseCase_692_<GenModel_692_, GenModel_692_> {
    override suspend fun invoke(params: GenModel_692_): GenModel_692_ = repository.save(params)
}

class GenDeleteUseCase_692_ @Inject constructor(
    private val repository: GenRepositoryImpl_692_
) : GenUseCase_692_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_692_ @Inject constructor(
    private val repository: GenRepositoryImpl_692_
) : GenUseCase_692_<String, List<GenModel_692_>> {
    override suspend fun invoke(params: String): List<GenModel_692_> = repository.search(params)
}

abstract class GenMapper_692_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_692_ : GenMapper_692_<GenModel_692_, String>() {
    override fun map(input: GenModel_692_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_692_ : GenMapper_692_<String, GenModel_692_>() {
    override fun map(input: String): GenModel_692_ {
        val parts = input.split(":")
        return GenModel_692_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_692_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_692_,
    private val saveUseCase: GenSaveUseCase_692_,
    private val deleteUseCase: GenDeleteUseCase_692_,
    private val searchUseCase: GenSearchUseCase_692_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_692_>(GenState_692_.Idle)
    val state: StateFlow<GenState_692_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_692_) {
        when (event) {
            is GenEvent_692_.Load -> loadAll()
            is GenEvent_692_.Update -> save(event.model)
            is GenEvent_692_.Delete -> delete(event.id)
            is GenEvent_692_.Refresh -> loadAll()
            is GenEvent_692_.Search -> search(event.query)
            is GenEvent_692_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_692_.Loading; _state.value = GenState_692_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_692_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_692_.Success(searchUseCase(query)) } }
}
