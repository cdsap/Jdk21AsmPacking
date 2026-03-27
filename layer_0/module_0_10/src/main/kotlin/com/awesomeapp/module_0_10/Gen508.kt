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

data class GenModel_508_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_508_ {
    data class Load(val id: Long) : GenEvent_508_()
    data class Update(val model: GenModel_508_) : GenEvent_508_()
    data class Delete(val id: Long) : GenEvent_508_()
    data object Refresh : GenEvent_508_()
    data class Search(val query: String) : GenEvent_508_()
    data class Filter(val predicate: String) : GenEvent_508_()
}

sealed class GenState_508_ {
    data object Idle : GenState_508_()
    data object Loading : GenState_508_()
    data class Success(val items: List<GenModel_508_>) : GenState_508_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_508_()
    data class Partial(val items: List<GenModel_508_>, val hasMore: Boolean) : GenState_508_()
}

interface GenRepository_508_ {
    suspend fun getAll(): List<GenModel_508_>
    suspend fun getById(id: Long): GenModel_508_?
    suspend fun save(model: GenModel_508_): GenModel_508_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_508_>
}

@Singleton
class GenRepositoryImpl_508_ @Inject constructor() : GenRepository_508_ {
    private val store = mutableMapOf<Long, GenModel_508_>()
    override suspend fun getAll(): List<GenModel_508_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_508_? = store[id]
    override suspend fun save(model: GenModel_508_): GenModel_508_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_508_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_508_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_508_ @Inject constructor(
    private val repository: GenRepositoryImpl_508_
) : GenUseCase_508_<Unit, List<GenModel_508_>> {
    override suspend fun invoke(params: Unit): List<GenModel_508_> = repository.getAll()
}

class GenSaveUseCase_508_ @Inject constructor(
    private val repository: GenRepositoryImpl_508_
) : GenUseCase_508_<GenModel_508_, GenModel_508_> {
    override suspend fun invoke(params: GenModel_508_): GenModel_508_ = repository.save(params)
}

class GenDeleteUseCase_508_ @Inject constructor(
    private val repository: GenRepositoryImpl_508_
) : GenUseCase_508_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_508_ @Inject constructor(
    private val repository: GenRepositoryImpl_508_
) : GenUseCase_508_<String, List<GenModel_508_>> {
    override suspend fun invoke(params: String): List<GenModel_508_> = repository.search(params)
}

abstract class GenMapper_508_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_508_ : GenMapper_508_<GenModel_508_, String>() {
    override fun map(input: GenModel_508_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_508_ : GenMapper_508_<String, GenModel_508_>() {
    override fun map(input: String): GenModel_508_ {
        val parts = input.split(":")
        return GenModel_508_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_508_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_508_,
    private val saveUseCase: GenSaveUseCase_508_,
    private val deleteUseCase: GenDeleteUseCase_508_,
    private val searchUseCase: GenSearchUseCase_508_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_508_>(GenState_508_.Idle)
    val state: StateFlow<GenState_508_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_508_) {
        when (event) {
            is GenEvent_508_.Load -> loadAll()
            is GenEvent_508_.Update -> save(event.model)
            is GenEvent_508_.Delete -> delete(event.id)
            is GenEvent_508_.Refresh -> loadAll()
            is GenEvent_508_.Search -> search(event.query)
            is GenEvent_508_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_508_.Loading; _state.value = GenState_508_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_508_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_508_.Success(searchUseCase(query)) } }
}
