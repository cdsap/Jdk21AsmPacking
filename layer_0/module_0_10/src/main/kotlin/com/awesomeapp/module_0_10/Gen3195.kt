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

data class GenModel_3195_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3195_ {
    data class Load(val id: Long) : GenEvent_3195_()
    data class Update(val model: GenModel_3195_) : GenEvent_3195_()
    data class Delete(val id: Long) : GenEvent_3195_()
    data object Refresh : GenEvent_3195_()
    data class Search(val query: String) : GenEvent_3195_()
    data class Filter(val predicate: String) : GenEvent_3195_()
}

sealed class GenState_3195_ {
    data object Idle : GenState_3195_()
    data object Loading : GenState_3195_()
    data class Success(val items: List<GenModel_3195_>) : GenState_3195_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3195_()
    data class Partial(val items: List<GenModel_3195_>, val hasMore: Boolean) : GenState_3195_()
}

interface GenRepository_3195_ {
    suspend fun getAll(): List<GenModel_3195_>
    suspend fun getById(id: Long): GenModel_3195_?
    suspend fun save(model: GenModel_3195_): GenModel_3195_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3195_>
}

@Singleton
class GenRepositoryImpl_3195_ @Inject constructor() : GenRepository_3195_ {
    private val store = mutableMapOf<Long, GenModel_3195_>()
    override suspend fun getAll(): List<GenModel_3195_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3195_? = store[id]
    override suspend fun save(model: GenModel_3195_): GenModel_3195_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3195_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3195_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3195_ @Inject constructor(
    private val repository: GenRepositoryImpl_3195_
) : GenUseCase_3195_<Unit, List<GenModel_3195_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3195_> = repository.getAll()
}

class GenSaveUseCase_3195_ @Inject constructor(
    private val repository: GenRepositoryImpl_3195_
) : GenUseCase_3195_<GenModel_3195_, GenModel_3195_> {
    override suspend fun invoke(params: GenModel_3195_): GenModel_3195_ = repository.save(params)
}

class GenDeleteUseCase_3195_ @Inject constructor(
    private val repository: GenRepositoryImpl_3195_
) : GenUseCase_3195_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3195_ @Inject constructor(
    private val repository: GenRepositoryImpl_3195_
) : GenUseCase_3195_<String, List<GenModel_3195_>> {
    override suspend fun invoke(params: String): List<GenModel_3195_> = repository.search(params)
}

abstract class GenMapper_3195_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3195_ : GenMapper_3195_<GenModel_3195_, String>() {
    override fun map(input: GenModel_3195_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3195_ : GenMapper_3195_<String, GenModel_3195_>() {
    override fun map(input: String): GenModel_3195_ {
        val parts = input.split(":")
        return GenModel_3195_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3195_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3195_,
    private val saveUseCase: GenSaveUseCase_3195_,
    private val deleteUseCase: GenDeleteUseCase_3195_,
    private val searchUseCase: GenSearchUseCase_3195_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3195_>(GenState_3195_.Idle)
    val state: StateFlow<GenState_3195_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3195_) {
        when (event) {
            is GenEvent_3195_.Load -> loadAll()
            is GenEvent_3195_.Update -> save(event.model)
            is GenEvent_3195_.Delete -> delete(event.id)
            is GenEvent_3195_.Refresh -> loadAll()
            is GenEvent_3195_.Search -> search(event.query)
            is GenEvent_3195_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3195_.Loading; _state.value = GenState_3195_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3195_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3195_.Success(searchUseCase(query)) } }
}
