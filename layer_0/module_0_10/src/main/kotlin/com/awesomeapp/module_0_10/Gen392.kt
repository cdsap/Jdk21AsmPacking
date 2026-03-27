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

data class GenModel_392_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_392_ {
    data class Load(val id: Long) : GenEvent_392_()
    data class Update(val model: GenModel_392_) : GenEvent_392_()
    data class Delete(val id: Long) : GenEvent_392_()
    data object Refresh : GenEvent_392_()
    data class Search(val query: String) : GenEvent_392_()
    data class Filter(val predicate: String) : GenEvent_392_()
}

sealed class GenState_392_ {
    data object Idle : GenState_392_()
    data object Loading : GenState_392_()
    data class Success(val items: List<GenModel_392_>) : GenState_392_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_392_()
    data class Partial(val items: List<GenModel_392_>, val hasMore: Boolean) : GenState_392_()
}

interface GenRepository_392_ {
    suspend fun getAll(): List<GenModel_392_>
    suspend fun getById(id: Long): GenModel_392_?
    suspend fun save(model: GenModel_392_): GenModel_392_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_392_>
}

@Singleton
class GenRepositoryImpl_392_ @Inject constructor() : GenRepository_392_ {
    private val store = mutableMapOf<Long, GenModel_392_>()
    override suspend fun getAll(): List<GenModel_392_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_392_? = store[id]
    override suspend fun save(model: GenModel_392_): GenModel_392_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_392_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_392_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_392_ @Inject constructor(
    private val repository: GenRepositoryImpl_392_
) : GenUseCase_392_<Unit, List<GenModel_392_>> {
    override suspend fun invoke(params: Unit): List<GenModel_392_> = repository.getAll()
}

class GenSaveUseCase_392_ @Inject constructor(
    private val repository: GenRepositoryImpl_392_
) : GenUseCase_392_<GenModel_392_, GenModel_392_> {
    override suspend fun invoke(params: GenModel_392_): GenModel_392_ = repository.save(params)
}

class GenDeleteUseCase_392_ @Inject constructor(
    private val repository: GenRepositoryImpl_392_
) : GenUseCase_392_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_392_ @Inject constructor(
    private val repository: GenRepositoryImpl_392_
) : GenUseCase_392_<String, List<GenModel_392_>> {
    override suspend fun invoke(params: String): List<GenModel_392_> = repository.search(params)
}

abstract class GenMapper_392_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_392_ : GenMapper_392_<GenModel_392_, String>() {
    override fun map(input: GenModel_392_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_392_ : GenMapper_392_<String, GenModel_392_>() {
    override fun map(input: String): GenModel_392_ {
        val parts = input.split(":")
        return GenModel_392_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_392_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_392_,
    private val saveUseCase: GenSaveUseCase_392_,
    private val deleteUseCase: GenDeleteUseCase_392_,
    private val searchUseCase: GenSearchUseCase_392_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_392_>(GenState_392_.Idle)
    val state: StateFlow<GenState_392_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_392_) {
        when (event) {
            is GenEvent_392_.Load -> loadAll()
            is GenEvent_392_.Update -> save(event.model)
            is GenEvent_392_.Delete -> delete(event.id)
            is GenEvent_392_.Refresh -> loadAll()
            is GenEvent_392_.Search -> search(event.query)
            is GenEvent_392_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_392_.Loading; _state.value = GenState_392_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_392_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_392_.Success(searchUseCase(query)) } }
}
