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

data class GenModel_120_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_120_ {
    data class Load(val id: Long) : GenEvent_120_()
    data class Update(val model: GenModel_120_) : GenEvent_120_()
    data class Delete(val id: Long) : GenEvent_120_()
    data object Refresh : GenEvent_120_()
    data class Search(val query: String) : GenEvent_120_()
    data class Filter(val predicate: String) : GenEvent_120_()
}

sealed class GenState_120_ {
    data object Idle : GenState_120_()
    data object Loading : GenState_120_()
    data class Success(val items: List<GenModel_120_>) : GenState_120_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_120_()
    data class Partial(val items: List<GenModel_120_>, val hasMore: Boolean) : GenState_120_()
}

interface GenRepository_120_ {
    suspend fun getAll(): List<GenModel_120_>
    suspend fun getById(id: Long): GenModel_120_?
    suspend fun save(model: GenModel_120_): GenModel_120_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_120_>
}

@Singleton
class GenRepositoryImpl_120_ @Inject constructor() : GenRepository_120_ {
    private val store = mutableMapOf<Long, GenModel_120_>()
    override suspend fun getAll(): List<GenModel_120_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_120_? = store[id]
    override suspend fun save(model: GenModel_120_): GenModel_120_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_120_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_120_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_120_ @Inject constructor(
    private val repository: GenRepositoryImpl_120_
) : GenUseCase_120_<Unit, List<GenModel_120_>> {
    override suspend fun invoke(params: Unit): List<GenModel_120_> = repository.getAll()
}

class GenSaveUseCase_120_ @Inject constructor(
    private val repository: GenRepositoryImpl_120_
) : GenUseCase_120_<GenModel_120_, GenModel_120_> {
    override suspend fun invoke(params: GenModel_120_): GenModel_120_ = repository.save(params)
}

class GenDeleteUseCase_120_ @Inject constructor(
    private val repository: GenRepositoryImpl_120_
) : GenUseCase_120_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_120_ @Inject constructor(
    private val repository: GenRepositoryImpl_120_
) : GenUseCase_120_<String, List<GenModel_120_>> {
    override suspend fun invoke(params: String): List<GenModel_120_> = repository.search(params)
}

abstract class GenMapper_120_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_120_ : GenMapper_120_<GenModel_120_, String>() {
    override fun map(input: GenModel_120_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_120_ : GenMapper_120_<String, GenModel_120_>() {
    override fun map(input: String): GenModel_120_ {
        val parts = input.split(":")
        return GenModel_120_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_120_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_120_,
    private val saveUseCase: GenSaveUseCase_120_,
    private val deleteUseCase: GenDeleteUseCase_120_,
    private val searchUseCase: GenSearchUseCase_120_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_120_>(GenState_120_.Idle)
    val state: StateFlow<GenState_120_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_120_) {
        when (event) {
            is GenEvent_120_.Load -> loadAll()
            is GenEvent_120_.Update -> save(event.model)
            is GenEvent_120_.Delete -> delete(event.id)
            is GenEvent_120_.Refresh -> loadAll()
            is GenEvent_120_.Search -> search(event.query)
            is GenEvent_120_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_120_.Loading; _state.value = GenState_120_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_120_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_120_.Success(searchUseCase(query)) } }
}
