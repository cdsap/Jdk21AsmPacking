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

data class GenModel_630_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_630_ {
    data class Load(val id: Long) : GenEvent_630_()
    data class Update(val model: GenModel_630_) : GenEvent_630_()
    data class Delete(val id: Long) : GenEvent_630_()
    data object Refresh : GenEvent_630_()
    data class Search(val query: String) : GenEvent_630_()
    data class Filter(val predicate: String) : GenEvent_630_()
}

sealed class GenState_630_ {
    data object Idle : GenState_630_()
    data object Loading : GenState_630_()
    data class Success(val items: List<GenModel_630_>) : GenState_630_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_630_()
    data class Partial(val items: List<GenModel_630_>, val hasMore: Boolean) : GenState_630_()
}

interface GenRepository_630_ {
    suspend fun getAll(): List<GenModel_630_>
    suspend fun getById(id: Long): GenModel_630_?
    suspend fun save(model: GenModel_630_): GenModel_630_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_630_>
}

@Singleton
class GenRepositoryImpl_630_ @Inject constructor() : GenRepository_630_ {
    private val store = mutableMapOf<Long, GenModel_630_>()
    override suspend fun getAll(): List<GenModel_630_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_630_? = store[id]
    override suspend fun save(model: GenModel_630_): GenModel_630_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_630_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_630_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_630_ @Inject constructor(
    private val repository: GenRepositoryImpl_630_
) : GenUseCase_630_<Unit, List<GenModel_630_>> {
    override suspend fun invoke(params: Unit): List<GenModel_630_> = repository.getAll()
}

class GenSaveUseCase_630_ @Inject constructor(
    private val repository: GenRepositoryImpl_630_
) : GenUseCase_630_<GenModel_630_, GenModel_630_> {
    override suspend fun invoke(params: GenModel_630_): GenModel_630_ = repository.save(params)
}

class GenDeleteUseCase_630_ @Inject constructor(
    private val repository: GenRepositoryImpl_630_
) : GenUseCase_630_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_630_ @Inject constructor(
    private val repository: GenRepositoryImpl_630_
) : GenUseCase_630_<String, List<GenModel_630_>> {
    override suspend fun invoke(params: String): List<GenModel_630_> = repository.search(params)
}

abstract class GenMapper_630_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_630_ : GenMapper_630_<GenModel_630_, String>() {
    override fun map(input: GenModel_630_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_630_ : GenMapper_630_<String, GenModel_630_>() {
    override fun map(input: String): GenModel_630_ {
        val parts = input.split(":")
        return GenModel_630_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_630_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_630_,
    private val saveUseCase: GenSaveUseCase_630_,
    private val deleteUseCase: GenDeleteUseCase_630_,
    private val searchUseCase: GenSearchUseCase_630_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_630_>(GenState_630_.Idle)
    val state: StateFlow<GenState_630_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_630_) {
        when (event) {
            is GenEvent_630_.Load -> loadAll()
            is GenEvent_630_.Update -> save(event.model)
            is GenEvent_630_.Delete -> delete(event.id)
            is GenEvent_630_.Refresh -> loadAll()
            is GenEvent_630_.Search -> search(event.query)
            is GenEvent_630_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_630_.Loading; _state.value = GenState_630_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_630_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_630_.Success(searchUseCase(query)) } }
}
