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

data class GenModel_788_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_788_ {
    data class Load(val id: Long) : GenEvent_788_()
    data class Update(val model: GenModel_788_) : GenEvent_788_()
    data class Delete(val id: Long) : GenEvent_788_()
    data object Refresh : GenEvent_788_()
    data class Search(val query: String) : GenEvent_788_()
    data class Filter(val predicate: String) : GenEvent_788_()
}

sealed class GenState_788_ {
    data object Idle : GenState_788_()
    data object Loading : GenState_788_()
    data class Success(val items: List<GenModel_788_>) : GenState_788_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_788_()
    data class Partial(val items: List<GenModel_788_>, val hasMore: Boolean) : GenState_788_()
}

interface GenRepository_788_ {
    suspend fun getAll(): List<GenModel_788_>
    suspend fun getById(id: Long): GenModel_788_?
    suspend fun save(model: GenModel_788_): GenModel_788_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_788_>
}

@Singleton
class GenRepositoryImpl_788_ @Inject constructor() : GenRepository_788_ {
    private val store = mutableMapOf<Long, GenModel_788_>()
    override suspend fun getAll(): List<GenModel_788_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_788_? = store[id]
    override suspend fun save(model: GenModel_788_): GenModel_788_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_788_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_788_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_788_ @Inject constructor(
    private val repository: GenRepositoryImpl_788_
) : GenUseCase_788_<Unit, List<GenModel_788_>> {
    override suspend fun invoke(params: Unit): List<GenModel_788_> = repository.getAll()
}

class GenSaveUseCase_788_ @Inject constructor(
    private val repository: GenRepositoryImpl_788_
) : GenUseCase_788_<GenModel_788_, GenModel_788_> {
    override suspend fun invoke(params: GenModel_788_): GenModel_788_ = repository.save(params)
}

class GenDeleteUseCase_788_ @Inject constructor(
    private val repository: GenRepositoryImpl_788_
) : GenUseCase_788_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_788_ @Inject constructor(
    private val repository: GenRepositoryImpl_788_
) : GenUseCase_788_<String, List<GenModel_788_>> {
    override suspend fun invoke(params: String): List<GenModel_788_> = repository.search(params)
}

abstract class GenMapper_788_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_788_ : GenMapper_788_<GenModel_788_, String>() {
    override fun map(input: GenModel_788_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_788_ : GenMapper_788_<String, GenModel_788_>() {
    override fun map(input: String): GenModel_788_ {
        val parts = input.split(":")
        return GenModel_788_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_788_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_788_,
    private val saveUseCase: GenSaveUseCase_788_,
    private val deleteUseCase: GenDeleteUseCase_788_,
    private val searchUseCase: GenSearchUseCase_788_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_788_>(GenState_788_.Idle)
    val state: StateFlow<GenState_788_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_788_) {
        when (event) {
            is GenEvent_788_.Load -> loadAll()
            is GenEvent_788_.Update -> save(event.model)
            is GenEvent_788_.Delete -> delete(event.id)
            is GenEvent_788_.Refresh -> loadAll()
            is GenEvent_788_.Search -> search(event.query)
            is GenEvent_788_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_788_.Loading; _state.value = GenState_788_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_788_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_788_.Success(searchUseCase(query)) } }
}
