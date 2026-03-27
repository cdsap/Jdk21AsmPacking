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

data class GenModel_25_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_25_ {
    data class Load(val id: Long) : GenEvent_25_()
    data class Update(val model: GenModel_25_) : GenEvent_25_()
    data class Delete(val id: Long) : GenEvent_25_()
    data object Refresh : GenEvent_25_()
    data class Search(val query: String) : GenEvent_25_()
    data class Filter(val predicate: String) : GenEvent_25_()
}

sealed class GenState_25_ {
    data object Idle : GenState_25_()
    data object Loading : GenState_25_()
    data class Success(val items: List<GenModel_25_>) : GenState_25_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_25_()
    data class Partial(val items: List<GenModel_25_>, val hasMore: Boolean) : GenState_25_()
}

interface GenRepository_25_ {
    suspend fun getAll(): List<GenModel_25_>
    suspend fun getById(id: Long): GenModel_25_?
    suspend fun save(model: GenModel_25_): GenModel_25_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_25_>
}

@Singleton
class GenRepositoryImpl_25_ @Inject constructor() : GenRepository_25_ {
    private val store = mutableMapOf<Long, GenModel_25_>()
    override suspend fun getAll(): List<GenModel_25_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_25_? = store[id]
    override suspend fun save(model: GenModel_25_): GenModel_25_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_25_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_25_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_25_ @Inject constructor(
    private val repository: GenRepositoryImpl_25_
) : GenUseCase_25_<Unit, List<GenModel_25_>> {
    override suspend fun invoke(params: Unit): List<GenModel_25_> = repository.getAll()
}

class GenSaveUseCase_25_ @Inject constructor(
    private val repository: GenRepositoryImpl_25_
) : GenUseCase_25_<GenModel_25_, GenModel_25_> {
    override suspend fun invoke(params: GenModel_25_): GenModel_25_ = repository.save(params)
}

class GenDeleteUseCase_25_ @Inject constructor(
    private val repository: GenRepositoryImpl_25_
) : GenUseCase_25_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_25_ @Inject constructor(
    private val repository: GenRepositoryImpl_25_
) : GenUseCase_25_<String, List<GenModel_25_>> {
    override suspend fun invoke(params: String): List<GenModel_25_> = repository.search(params)
}

abstract class GenMapper_25_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_25_ : GenMapper_25_<GenModel_25_, String>() {
    override fun map(input: GenModel_25_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_25_ : GenMapper_25_<String, GenModel_25_>() {
    override fun map(input: String): GenModel_25_ {
        val parts = input.split(":")
        return GenModel_25_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_25_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_25_,
    private val saveUseCase: GenSaveUseCase_25_,
    private val deleteUseCase: GenDeleteUseCase_25_,
    private val searchUseCase: GenSearchUseCase_25_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_25_>(GenState_25_.Idle)
    val state: StateFlow<GenState_25_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_25_) {
        when (event) {
            is GenEvent_25_.Load -> loadAll()
            is GenEvent_25_.Update -> save(event.model)
            is GenEvent_25_.Delete -> delete(event.id)
            is GenEvent_25_.Refresh -> loadAll()
            is GenEvent_25_.Search -> search(event.query)
            is GenEvent_25_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_25_.Loading; _state.value = GenState_25_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_25_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_25_.Success(searchUseCase(query)) } }
}
