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

data class GenModel_1345_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1345_ {
    data class Load(val id: Long) : GenEvent_1345_()
    data class Update(val model: GenModel_1345_) : GenEvent_1345_()
    data class Delete(val id: Long) : GenEvent_1345_()
    data object Refresh : GenEvent_1345_()
    data class Search(val query: String) : GenEvent_1345_()
    data class Filter(val predicate: String) : GenEvent_1345_()
}

sealed class GenState_1345_ {
    data object Idle : GenState_1345_()
    data object Loading : GenState_1345_()
    data class Success(val items: List<GenModel_1345_>) : GenState_1345_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1345_()
    data class Partial(val items: List<GenModel_1345_>, val hasMore: Boolean) : GenState_1345_()
}

interface GenRepository_1345_ {
    suspend fun getAll(): List<GenModel_1345_>
    suspend fun getById(id: Long): GenModel_1345_?
    suspend fun save(model: GenModel_1345_): GenModel_1345_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1345_>
}

@Singleton
class GenRepositoryImpl_1345_ @Inject constructor() : GenRepository_1345_ {
    private val store = mutableMapOf<Long, GenModel_1345_>()
    override suspend fun getAll(): List<GenModel_1345_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1345_? = store[id]
    override suspend fun save(model: GenModel_1345_): GenModel_1345_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1345_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1345_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1345_ @Inject constructor(
    private val repository: GenRepositoryImpl_1345_
) : GenUseCase_1345_<Unit, List<GenModel_1345_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1345_> = repository.getAll()
}

class GenSaveUseCase_1345_ @Inject constructor(
    private val repository: GenRepositoryImpl_1345_
) : GenUseCase_1345_<GenModel_1345_, GenModel_1345_> {
    override suspend fun invoke(params: GenModel_1345_): GenModel_1345_ = repository.save(params)
}

class GenDeleteUseCase_1345_ @Inject constructor(
    private val repository: GenRepositoryImpl_1345_
) : GenUseCase_1345_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1345_ @Inject constructor(
    private val repository: GenRepositoryImpl_1345_
) : GenUseCase_1345_<String, List<GenModel_1345_>> {
    override suspend fun invoke(params: String): List<GenModel_1345_> = repository.search(params)
}

abstract class GenMapper_1345_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1345_ : GenMapper_1345_<GenModel_1345_, String>() {
    override fun map(input: GenModel_1345_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1345_ : GenMapper_1345_<String, GenModel_1345_>() {
    override fun map(input: String): GenModel_1345_ {
        val parts = input.split(":")
        return GenModel_1345_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1345_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1345_,
    private val saveUseCase: GenSaveUseCase_1345_,
    private val deleteUseCase: GenDeleteUseCase_1345_,
    private val searchUseCase: GenSearchUseCase_1345_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1345_>(GenState_1345_.Idle)
    val state: StateFlow<GenState_1345_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1345_) {
        when (event) {
            is GenEvent_1345_.Load -> loadAll()
            is GenEvent_1345_.Update -> save(event.model)
            is GenEvent_1345_.Delete -> delete(event.id)
            is GenEvent_1345_.Refresh -> loadAll()
            is GenEvent_1345_.Search -> search(event.query)
            is GenEvent_1345_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1345_.Loading; _state.value = GenState_1345_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1345_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1345_.Success(searchUseCase(query)) } }
}
