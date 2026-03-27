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

data class GenModel_1664_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1664_ {
    data class Load(val id: Long) : GenEvent_1664_()
    data class Update(val model: GenModel_1664_) : GenEvent_1664_()
    data class Delete(val id: Long) : GenEvent_1664_()
    data object Refresh : GenEvent_1664_()
    data class Search(val query: String) : GenEvent_1664_()
    data class Filter(val predicate: String) : GenEvent_1664_()
}

sealed class GenState_1664_ {
    data object Idle : GenState_1664_()
    data object Loading : GenState_1664_()
    data class Success(val items: List<GenModel_1664_>) : GenState_1664_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1664_()
    data class Partial(val items: List<GenModel_1664_>, val hasMore: Boolean) : GenState_1664_()
}

interface GenRepository_1664_ {
    suspend fun getAll(): List<GenModel_1664_>
    suspend fun getById(id: Long): GenModel_1664_?
    suspend fun save(model: GenModel_1664_): GenModel_1664_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1664_>
}

@Singleton
class GenRepositoryImpl_1664_ @Inject constructor() : GenRepository_1664_ {
    private val store = mutableMapOf<Long, GenModel_1664_>()
    override suspend fun getAll(): List<GenModel_1664_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1664_? = store[id]
    override suspend fun save(model: GenModel_1664_): GenModel_1664_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1664_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1664_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1664_ @Inject constructor(
    private val repository: GenRepositoryImpl_1664_
) : GenUseCase_1664_<Unit, List<GenModel_1664_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1664_> = repository.getAll()
}

class GenSaveUseCase_1664_ @Inject constructor(
    private val repository: GenRepositoryImpl_1664_
) : GenUseCase_1664_<GenModel_1664_, GenModel_1664_> {
    override suspend fun invoke(params: GenModel_1664_): GenModel_1664_ = repository.save(params)
}

class GenDeleteUseCase_1664_ @Inject constructor(
    private val repository: GenRepositoryImpl_1664_
) : GenUseCase_1664_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1664_ @Inject constructor(
    private val repository: GenRepositoryImpl_1664_
) : GenUseCase_1664_<String, List<GenModel_1664_>> {
    override suspend fun invoke(params: String): List<GenModel_1664_> = repository.search(params)
}

abstract class GenMapper_1664_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1664_ : GenMapper_1664_<GenModel_1664_, String>() {
    override fun map(input: GenModel_1664_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1664_ : GenMapper_1664_<String, GenModel_1664_>() {
    override fun map(input: String): GenModel_1664_ {
        val parts = input.split(":")
        return GenModel_1664_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1664_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1664_,
    private val saveUseCase: GenSaveUseCase_1664_,
    private val deleteUseCase: GenDeleteUseCase_1664_,
    private val searchUseCase: GenSearchUseCase_1664_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1664_>(GenState_1664_.Idle)
    val state: StateFlow<GenState_1664_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1664_) {
        when (event) {
            is GenEvent_1664_.Load -> loadAll()
            is GenEvent_1664_.Update -> save(event.model)
            is GenEvent_1664_.Delete -> delete(event.id)
            is GenEvent_1664_.Refresh -> loadAll()
            is GenEvent_1664_.Search -> search(event.query)
            is GenEvent_1664_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1664_.Loading; _state.value = GenState_1664_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1664_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1664_.Success(searchUseCase(query)) } }
}
