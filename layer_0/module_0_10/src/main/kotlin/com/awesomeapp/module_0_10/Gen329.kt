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

data class GenModel_329_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_329_ {
    data class Load(val id: Long) : GenEvent_329_()
    data class Update(val model: GenModel_329_) : GenEvent_329_()
    data class Delete(val id: Long) : GenEvent_329_()
    data object Refresh : GenEvent_329_()
    data class Search(val query: String) : GenEvent_329_()
    data class Filter(val predicate: String) : GenEvent_329_()
}

sealed class GenState_329_ {
    data object Idle : GenState_329_()
    data object Loading : GenState_329_()
    data class Success(val items: List<GenModel_329_>) : GenState_329_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_329_()
    data class Partial(val items: List<GenModel_329_>, val hasMore: Boolean) : GenState_329_()
}

interface GenRepository_329_ {
    suspend fun getAll(): List<GenModel_329_>
    suspend fun getById(id: Long): GenModel_329_?
    suspend fun save(model: GenModel_329_): GenModel_329_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_329_>
}

@Singleton
class GenRepositoryImpl_329_ @Inject constructor() : GenRepository_329_ {
    private val store = mutableMapOf<Long, GenModel_329_>()
    override suspend fun getAll(): List<GenModel_329_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_329_? = store[id]
    override suspend fun save(model: GenModel_329_): GenModel_329_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_329_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_329_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_329_ @Inject constructor(
    private val repository: GenRepositoryImpl_329_
) : GenUseCase_329_<Unit, List<GenModel_329_>> {
    override suspend fun invoke(params: Unit): List<GenModel_329_> = repository.getAll()
}

class GenSaveUseCase_329_ @Inject constructor(
    private val repository: GenRepositoryImpl_329_
) : GenUseCase_329_<GenModel_329_, GenModel_329_> {
    override suspend fun invoke(params: GenModel_329_): GenModel_329_ = repository.save(params)
}

class GenDeleteUseCase_329_ @Inject constructor(
    private val repository: GenRepositoryImpl_329_
) : GenUseCase_329_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_329_ @Inject constructor(
    private val repository: GenRepositoryImpl_329_
) : GenUseCase_329_<String, List<GenModel_329_>> {
    override suspend fun invoke(params: String): List<GenModel_329_> = repository.search(params)
}

abstract class GenMapper_329_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_329_ : GenMapper_329_<GenModel_329_, String>() {
    override fun map(input: GenModel_329_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_329_ : GenMapper_329_<String, GenModel_329_>() {
    override fun map(input: String): GenModel_329_ {
        val parts = input.split(":")
        return GenModel_329_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_329_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_329_,
    private val saveUseCase: GenSaveUseCase_329_,
    private val deleteUseCase: GenDeleteUseCase_329_,
    private val searchUseCase: GenSearchUseCase_329_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_329_>(GenState_329_.Idle)
    val state: StateFlow<GenState_329_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_329_) {
        when (event) {
            is GenEvent_329_.Load -> loadAll()
            is GenEvent_329_.Update -> save(event.model)
            is GenEvent_329_.Delete -> delete(event.id)
            is GenEvent_329_.Refresh -> loadAll()
            is GenEvent_329_.Search -> search(event.query)
            is GenEvent_329_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_329_.Loading; _state.value = GenState_329_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_329_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_329_.Success(searchUseCase(query)) } }
}
