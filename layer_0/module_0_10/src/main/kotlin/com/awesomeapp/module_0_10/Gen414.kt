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

data class GenModel_414_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_414_ {
    data class Load(val id: Long) : GenEvent_414_()
    data class Update(val model: GenModel_414_) : GenEvent_414_()
    data class Delete(val id: Long) : GenEvent_414_()
    data object Refresh : GenEvent_414_()
    data class Search(val query: String) : GenEvent_414_()
    data class Filter(val predicate: String) : GenEvent_414_()
}

sealed class GenState_414_ {
    data object Idle : GenState_414_()
    data object Loading : GenState_414_()
    data class Success(val items: List<GenModel_414_>) : GenState_414_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_414_()
    data class Partial(val items: List<GenModel_414_>, val hasMore: Boolean) : GenState_414_()
}

interface GenRepository_414_ {
    suspend fun getAll(): List<GenModel_414_>
    suspend fun getById(id: Long): GenModel_414_?
    suspend fun save(model: GenModel_414_): GenModel_414_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_414_>
}

@Singleton
class GenRepositoryImpl_414_ @Inject constructor() : GenRepository_414_ {
    private val store = mutableMapOf<Long, GenModel_414_>()
    override suspend fun getAll(): List<GenModel_414_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_414_? = store[id]
    override suspend fun save(model: GenModel_414_): GenModel_414_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_414_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_414_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_414_ @Inject constructor(
    private val repository: GenRepositoryImpl_414_
) : GenUseCase_414_<Unit, List<GenModel_414_>> {
    override suspend fun invoke(params: Unit): List<GenModel_414_> = repository.getAll()
}

class GenSaveUseCase_414_ @Inject constructor(
    private val repository: GenRepositoryImpl_414_
) : GenUseCase_414_<GenModel_414_, GenModel_414_> {
    override suspend fun invoke(params: GenModel_414_): GenModel_414_ = repository.save(params)
}

class GenDeleteUseCase_414_ @Inject constructor(
    private val repository: GenRepositoryImpl_414_
) : GenUseCase_414_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_414_ @Inject constructor(
    private val repository: GenRepositoryImpl_414_
) : GenUseCase_414_<String, List<GenModel_414_>> {
    override suspend fun invoke(params: String): List<GenModel_414_> = repository.search(params)
}

abstract class GenMapper_414_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_414_ : GenMapper_414_<GenModel_414_, String>() {
    override fun map(input: GenModel_414_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_414_ : GenMapper_414_<String, GenModel_414_>() {
    override fun map(input: String): GenModel_414_ {
        val parts = input.split(":")
        return GenModel_414_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_414_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_414_,
    private val saveUseCase: GenSaveUseCase_414_,
    private val deleteUseCase: GenDeleteUseCase_414_,
    private val searchUseCase: GenSearchUseCase_414_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_414_>(GenState_414_.Idle)
    val state: StateFlow<GenState_414_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_414_) {
        when (event) {
            is GenEvent_414_.Load -> loadAll()
            is GenEvent_414_.Update -> save(event.model)
            is GenEvent_414_.Delete -> delete(event.id)
            is GenEvent_414_.Refresh -> loadAll()
            is GenEvent_414_.Search -> search(event.query)
            is GenEvent_414_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_414_.Loading; _state.value = GenState_414_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_414_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_414_.Success(searchUseCase(query)) } }
}
