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

data class GenModel_385_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_385_ {
    data class Load(val id: Long) : GenEvent_385_()
    data class Update(val model: GenModel_385_) : GenEvent_385_()
    data class Delete(val id: Long) : GenEvent_385_()
    data object Refresh : GenEvent_385_()
    data class Search(val query: String) : GenEvent_385_()
    data class Filter(val predicate: String) : GenEvent_385_()
}

sealed class GenState_385_ {
    data object Idle : GenState_385_()
    data object Loading : GenState_385_()
    data class Success(val items: List<GenModel_385_>) : GenState_385_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_385_()
    data class Partial(val items: List<GenModel_385_>, val hasMore: Boolean) : GenState_385_()
}

interface GenRepository_385_ {
    suspend fun getAll(): List<GenModel_385_>
    suspend fun getById(id: Long): GenModel_385_?
    suspend fun save(model: GenModel_385_): GenModel_385_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_385_>
}

@Singleton
class GenRepositoryImpl_385_ @Inject constructor() : GenRepository_385_ {
    private val store = mutableMapOf<Long, GenModel_385_>()
    override suspend fun getAll(): List<GenModel_385_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_385_? = store[id]
    override suspend fun save(model: GenModel_385_): GenModel_385_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_385_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_385_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_385_ @Inject constructor(
    private val repository: GenRepositoryImpl_385_
) : GenUseCase_385_<Unit, List<GenModel_385_>> {
    override suspend fun invoke(params: Unit): List<GenModel_385_> = repository.getAll()
}

class GenSaveUseCase_385_ @Inject constructor(
    private val repository: GenRepositoryImpl_385_
) : GenUseCase_385_<GenModel_385_, GenModel_385_> {
    override suspend fun invoke(params: GenModel_385_): GenModel_385_ = repository.save(params)
}

class GenDeleteUseCase_385_ @Inject constructor(
    private val repository: GenRepositoryImpl_385_
) : GenUseCase_385_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_385_ @Inject constructor(
    private val repository: GenRepositoryImpl_385_
) : GenUseCase_385_<String, List<GenModel_385_>> {
    override suspend fun invoke(params: String): List<GenModel_385_> = repository.search(params)
}

abstract class GenMapper_385_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_385_ : GenMapper_385_<GenModel_385_, String>() {
    override fun map(input: GenModel_385_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_385_ : GenMapper_385_<String, GenModel_385_>() {
    override fun map(input: String): GenModel_385_ {
        val parts = input.split(":")
        return GenModel_385_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_385_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_385_,
    private val saveUseCase: GenSaveUseCase_385_,
    private val deleteUseCase: GenDeleteUseCase_385_,
    private val searchUseCase: GenSearchUseCase_385_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_385_>(GenState_385_.Idle)
    val state: StateFlow<GenState_385_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_385_) {
        when (event) {
            is GenEvent_385_.Load -> loadAll()
            is GenEvent_385_.Update -> save(event.model)
            is GenEvent_385_.Delete -> delete(event.id)
            is GenEvent_385_.Refresh -> loadAll()
            is GenEvent_385_.Search -> search(event.query)
            is GenEvent_385_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_385_.Loading; _state.value = GenState_385_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_385_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_385_.Success(searchUseCase(query)) } }
}
