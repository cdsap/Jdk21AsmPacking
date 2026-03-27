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

data class GenModel_320_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_320_ {
    data class Load(val id: Long) : GenEvent_320_()
    data class Update(val model: GenModel_320_) : GenEvent_320_()
    data class Delete(val id: Long) : GenEvent_320_()
    data object Refresh : GenEvent_320_()
    data class Search(val query: String) : GenEvent_320_()
    data class Filter(val predicate: String) : GenEvent_320_()
}

sealed class GenState_320_ {
    data object Idle : GenState_320_()
    data object Loading : GenState_320_()
    data class Success(val items: List<GenModel_320_>) : GenState_320_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_320_()
    data class Partial(val items: List<GenModel_320_>, val hasMore: Boolean) : GenState_320_()
}

interface GenRepository_320_ {
    suspend fun getAll(): List<GenModel_320_>
    suspend fun getById(id: Long): GenModel_320_?
    suspend fun save(model: GenModel_320_): GenModel_320_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_320_>
}

@Singleton
class GenRepositoryImpl_320_ @Inject constructor() : GenRepository_320_ {
    private val store = mutableMapOf<Long, GenModel_320_>()
    override suspend fun getAll(): List<GenModel_320_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_320_? = store[id]
    override suspend fun save(model: GenModel_320_): GenModel_320_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_320_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_320_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_320_ @Inject constructor(
    private val repository: GenRepositoryImpl_320_
) : GenUseCase_320_<Unit, List<GenModel_320_>> {
    override suspend fun invoke(params: Unit): List<GenModel_320_> = repository.getAll()
}

class GenSaveUseCase_320_ @Inject constructor(
    private val repository: GenRepositoryImpl_320_
) : GenUseCase_320_<GenModel_320_, GenModel_320_> {
    override suspend fun invoke(params: GenModel_320_): GenModel_320_ = repository.save(params)
}

class GenDeleteUseCase_320_ @Inject constructor(
    private val repository: GenRepositoryImpl_320_
) : GenUseCase_320_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_320_ @Inject constructor(
    private val repository: GenRepositoryImpl_320_
) : GenUseCase_320_<String, List<GenModel_320_>> {
    override suspend fun invoke(params: String): List<GenModel_320_> = repository.search(params)
}

abstract class GenMapper_320_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_320_ : GenMapper_320_<GenModel_320_, String>() {
    override fun map(input: GenModel_320_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_320_ : GenMapper_320_<String, GenModel_320_>() {
    override fun map(input: String): GenModel_320_ {
        val parts = input.split(":")
        return GenModel_320_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_320_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_320_,
    private val saveUseCase: GenSaveUseCase_320_,
    private val deleteUseCase: GenDeleteUseCase_320_,
    private val searchUseCase: GenSearchUseCase_320_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_320_>(GenState_320_.Idle)
    val state: StateFlow<GenState_320_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_320_) {
        when (event) {
            is GenEvent_320_.Load -> loadAll()
            is GenEvent_320_.Update -> save(event.model)
            is GenEvent_320_.Delete -> delete(event.id)
            is GenEvent_320_.Refresh -> loadAll()
            is GenEvent_320_.Search -> search(event.query)
            is GenEvent_320_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_320_.Loading; _state.value = GenState_320_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_320_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_320_.Success(searchUseCase(query)) } }
}
