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

data class GenModel_130_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_130_ {
    data class Load(val id: Long) : GenEvent_130_()
    data class Update(val model: GenModel_130_) : GenEvent_130_()
    data class Delete(val id: Long) : GenEvent_130_()
    data object Refresh : GenEvent_130_()
    data class Search(val query: String) : GenEvent_130_()
    data class Filter(val predicate: String) : GenEvent_130_()
}

sealed class GenState_130_ {
    data object Idle : GenState_130_()
    data object Loading : GenState_130_()
    data class Success(val items: List<GenModel_130_>) : GenState_130_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_130_()
    data class Partial(val items: List<GenModel_130_>, val hasMore: Boolean) : GenState_130_()
}

interface GenRepository_130_ {
    suspend fun getAll(): List<GenModel_130_>
    suspend fun getById(id: Long): GenModel_130_?
    suspend fun save(model: GenModel_130_): GenModel_130_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_130_>
}

@Singleton
class GenRepositoryImpl_130_ @Inject constructor() : GenRepository_130_ {
    private val store = mutableMapOf<Long, GenModel_130_>()
    override suspend fun getAll(): List<GenModel_130_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_130_? = store[id]
    override suspend fun save(model: GenModel_130_): GenModel_130_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_130_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_130_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_130_ @Inject constructor(
    private val repository: GenRepositoryImpl_130_
) : GenUseCase_130_<Unit, List<GenModel_130_>> {
    override suspend fun invoke(params: Unit): List<GenModel_130_> = repository.getAll()
}

class GenSaveUseCase_130_ @Inject constructor(
    private val repository: GenRepositoryImpl_130_
) : GenUseCase_130_<GenModel_130_, GenModel_130_> {
    override suspend fun invoke(params: GenModel_130_): GenModel_130_ = repository.save(params)
}

class GenDeleteUseCase_130_ @Inject constructor(
    private val repository: GenRepositoryImpl_130_
) : GenUseCase_130_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_130_ @Inject constructor(
    private val repository: GenRepositoryImpl_130_
) : GenUseCase_130_<String, List<GenModel_130_>> {
    override suspend fun invoke(params: String): List<GenModel_130_> = repository.search(params)
}

abstract class GenMapper_130_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_130_ : GenMapper_130_<GenModel_130_, String>() {
    override fun map(input: GenModel_130_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_130_ : GenMapper_130_<String, GenModel_130_>() {
    override fun map(input: String): GenModel_130_ {
        val parts = input.split(":")
        return GenModel_130_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_130_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_130_,
    private val saveUseCase: GenSaveUseCase_130_,
    private val deleteUseCase: GenDeleteUseCase_130_,
    private val searchUseCase: GenSearchUseCase_130_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_130_>(GenState_130_.Idle)
    val state: StateFlow<GenState_130_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_130_) {
        when (event) {
            is GenEvent_130_.Load -> loadAll()
            is GenEvent_130_.Update -> save(event.model)
            is GenEvent_130_.Delete -> delete(event.id)
            is GenEvent_130_.Refresh -> loadAll()
            is GenEvent_130_.Search -> search(event.query)
            is GenEvent_130_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_130_.Loading; _state.value = GenState_130_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_130_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_130_.Success(searchUseCase(query)) } }
}
