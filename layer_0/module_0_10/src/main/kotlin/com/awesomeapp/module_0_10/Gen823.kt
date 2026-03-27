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

data class GenModel_823_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_823_ {
    data class Load(val id: Long) : GenEvent_823_()
    data class Update(val model: GenModel_823_) : GenEvent_823_()
    data class Delete(val id: Long) : GenEvent_823_()
    data object Refresh : GenEvent_823_()
    data class Search(val query: String) : GenEvent_823_()
    data class Filter(val predicate: String) : GenEvent_823_()
}

sealed class GenState_823_ {
    data object Idle : GenState_823_()
    data object Loading : GenState_823_()
    data class Success(val items: List<GenModel_823_>) : GenState_823_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_823_()
    data class Partial(val items: List<GenModel_823_>, val hasMore: Boolean) : GenState_823_()
}

interface GenRepository_823_ {
    suspend fun getAll(): List<GenModel_823_>
    suspend fun getById(id: Long): GenModel_823_?
    suspend fun save(model: GenModel_823_): GenModel_823_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_823_>
}

@Singleton
class GenRepositoryImpl_823_ @Inject constructor() : GenRepository_823_ {
    private val store = mutableMapOf<Long, GenModel_823_>()
    override suspend fun getAll(): List<GenModel_823_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_823_? = store[id]
    override suspend fun save(model: GenModel_823_): GenModel_823_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_823_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_823_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_823_ @Inject constructor(
    private val repository: GenRepositoryImpl_823_
) : GenUseCase_823_<Unit, List<GenModel_823_>> {
    override suspend fun invoke(params: Unit): List<GenModel_823_> = repository.getAll()
}

class GenSaveUseCase_823_ @Inject constructor(
    private val repository: GenRepositoryImpl_823_
) : GenUseCase_823_<GenModel_823_, GenModel_823_> {
    override suspend fun invoke(params: GenModel_823_): GenModel_823_ = repository.save(params)
}

class GenDeleteUseCase_823_ @Inject constructor(
    private val repository: GenRepositoryImpl_823_
) : GenUseCase_823_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_823_ @Inject constructor(
    private val repository: GenRepositoryImpl_823_
) : GenUseCase_823_<String, List<GenModel_823_>> {
    override suspend fun invoke(params: String): List<GenModel_823_> = repository.search(params)
}

abstract class GenMapper_823_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_823_ : GenMapper_823_<GenModel_823_, String>() {
    override fun map(input: GenModel_823_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_823_ : GenMapper_823_<String, GenModel_823_>() {
    override fun map(input: String): GenModel_823_ {
        val parts = input.split(":")
        return GenModel_823_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_823_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_823_,
    private val saveUseCase: GenSaveUseCase_823_,
    private val deleteUseCase: GenDeleteUseCase_823_,
    private val searchUseCase: GenSearchUseCase_823_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_823_>(GenState_823_.Idle)
    val state: StateFlow<GenState_823_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_823_) {
        when (event) {
            is GenEvent_823_.Load -> loadAll()
            is GenEvent_823_.Update -> save(event.model)
            is GenEvent_823_.Delete -> delete(event.id)
            is GenEvent_823_.Refresh -> loadAll()
            is GenEvent_823_.Search -> search(event.query)
            is GenEvent_823_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_823_.Loading; _state.value = GenState_823_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_823_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_823_.Success(searchUseCase(query)) } }
}
