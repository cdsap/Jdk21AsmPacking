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

data class GenModel_726_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_726_ {
    data class Load(val id: Long) : GenEvent_726_()
    data class Update(val model: GenModel_726_) : GenEvent_726_()
    data class Delete(val id: Long) : GenEvent_726_()
    data object Refresh : GenEvent_726_()
    data class Search(val query: String) : GenEvent_726_()
    data class Filter(val predicate: String) : GenEvent_726_()
}

sealed class GenState_726_ {
    data object Idle : GenState_726_()
    data object Loading : GenState_726_()
    data class Success(val items: List<GenModel_726_>) : GenState_726_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_726_()
    data class Partial(val items: List<GenModel_726_>, val hasMore: Boolean) : GenState_726_()
}

interface GenRepository_726_ {
    suspend fun getAll(): List<GenModel_726_>
    suspend fun getById(id: Long): GenModel_726_?
    suspend fun save(model: GenModel_726_): GenModel_726_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_726_>
}

@Singleton
class GenRepositoryImpl_726_ @Inject constructor() : GenRepository_726_ {
    private val store = mutableMapOf<Long, GenModel_726_>()
    override suspend fun getAll(): List<GenModel_726_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_726_? = store[id]
    override suspend fun save(model: GenModel_726_): GenModel_726_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_726_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_726_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_726_ @Inject constructor(
    private val repository: GenRepositoryImpl_726_
) : GenUseCase_726_<Unit, List<GenModel_726_>> {
    override suspend fun invoke(params: Unit): List<GenModel_726_> = repository.getAll()
}

class GenSaveUseCase_726_ @Inject constructor(
    private val repository: GenRepositoryImpl_726_
) : GenUseCase_726_<GenModel_726_, GenModel_726_> {
    override suspend fun invoke(params: GenModel_726_): GenModel_726_ = repository.save(params)
}

class GenDeleteUseCase_726_ @Inject constructor(
    private val repository: GenRepositoryImpl_726_
) : GenUseCase_726_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_726_ @Inject constructor(
    private val repository: GenRepositoryImpl_726_
) : GenUseCase_726_<String, List<GenModel_726_>> {
    override suspend fun invoke(params: String): List<GenModel_726_> = repository.search(params)
}

abstract class GenMapper_726_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_726_ : GenMapper_726_<GenModel_726_, String>() {
    override fun map(input: GenModel_726_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_726_ : GenMapper_726_<String, GenModel_726_>() {
    override fun map(input: String): GenModel_726_ {
        val parts = input.split(":")
        return GenModel_726_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_726_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_726_,
    private val saveUseCase: GenSaveUseCase_726_,
    private val deleteUseCase: GenDeleteUseCase_726_,
    private val searchUseCase: GenSearchUseCase_726_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_726_>(GenState_726_.Idle)
    val state: StateFlow<GenState_726_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_726_) {
        when (event) {
            is GenEvent_726_.Load -> loadAll()
            is GenEvent_726_.Update -> save(event.model)
            is GenEvent_726_.Delete -> delete(event.id)
            is GenEvent_726_.Refresh -> loadAll()
            is GenEvent_726_.Search -> search(event.query)
            is GenEvent_726_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_726_.Loading; _state.value = GenState_726_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_726_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_726_.Success(searchUseCase(query)) } }
}
