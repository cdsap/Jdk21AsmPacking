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

data class GenModel_274_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_274_ {
    data class Load(val id: Long) : GenEvent_274_()
    data class Update(val model: GenModel_274_) : GenEvent_274_()
    data class Delete(val id: Long) : GenEvent_274_()
    data object Refresh : GenEvent_274_()
    data class Search(val query: String) : GenEvent_274_()
    data class Filter(val predicate: String) : GenEvent_274_()
}

sealed class GenState_274_ {
    data object Idle : GenState_274_()
    data object Loading : GenState_274_()
    data class Success(val items: List<GenModel_274_>) : GenState_274_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_274_()
    data class Partial(val items: List<GenModel_274_>, val hasMore: Boolean) : GenState_274_()
}

interface GenRepository_274_ {
    suspend fun getAll(): List<GenModel_274_>
    suspend fun getById(id: Long): GenModel_274_?
    suspend fun save(model: GenModel_274_): GenModel_274_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_274_>
}

@Singleton
class GenRepositoryImpl_274_ @Inject constructor() : GenRepository_274_ {
    private val store = mutableMapOf<Long, GenModel_274_>()
    override suspend fun getAll(): List<GenModel_274_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_274_? = store[id]
    override suspend fun save(model: GenModel_274_): GenModel_274_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_274_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_274_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_274_ @Inject constructor(
    private val repository: GenRepositoryImpl_274_
) : GenUseCase_274_<Unit, List<GenModel_274_>> {
    override suspend fun invoke(params: Unit): List<GenModel_274_> = repository.getAll()
}

class GenSaveUseCase_274_ @Inject constructor(
    private val repository: GenRepositoryImpl_274_
) : GenUseCase_274_<GenModel_274_, GenModel_274_> {
    override suspend fun invoke(params: GenModel_274_): GenModel_274_ = repository.save(params)
}

class GenDeleteUseCase_274_ @Inject constructor(
    private val repository: GenRepositoryImpl_274_
) : GenUseCase_274_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_274_ @Inject constructor(
    private val repository: GenRepositoryImpl_274_
) : GenUseCase_274_<String, List<GenModel_274_>> {
    override suspend fun invoke(params: String): List<GenModel_274_> = repository.search(params)
}

abstract class GenMapper_274_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_274_ : GenMapper_274_<GenModel_274_, String>() {
    override fun map(input: GenModel_274_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_274_ : GenMapper_274_<String, GenModel_274_>() {
    override fun map(input: String): GenModel_274_ {
        val parts = input.split(":")
        return GenModel_274_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_274_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_274_,
    private val saveUseCase: GenSaveUseCase_274_,
    private val deleteUseCase: GenDeleteUseCase_274_,
    private val searchUseCase: GenSearchUseCase_274_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_274_>(GenState_274_.Idle)
    val state: StateFlow<GenState_274_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_274_) {
        when (event) {
            is GenEvent_274_.Load -> loadAll()
            is GenEvent_274_.Update -> save(event.model)
            is GenEvent_274_.Delete -> delete(event.id)
            is GenEvent_274_.Refresh -> loadAll()
            is GenEvent_274_.Search -> search(event.query)
            is GenEvent_274_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_274_.Loading; _state.value = GenState_274_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_274_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_274_.Success(searchUseCase(query)) } }
}
