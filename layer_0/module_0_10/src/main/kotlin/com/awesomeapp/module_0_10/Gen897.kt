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

data class GenModel_897_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_897_ {
    data class Load(val id: Long) : GenEvent_897_()
    data class Update(val model: GenModel_897_) : GenEvent_897_()
    data class Delete(val id: Long) : GenEvent_897_()
    data object Refresh : GenEvent_897_()
    data class Search(val query: String) : GenEvent_897_()
    data class Filter(val predicate: String) : GenEvent_897_()
}

sealed class GenState_897_ {
    data object Idle : GenState_897_()
    data object Loading : GenState_897_()
    data class Success(val items: List<GenModel_897_>) : GenState_897_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_897_()
    data class Partial(val items: List<GenModel_897_>, val hasMore: Boolean) : GenState_897_()
}

interface GenRepository_897_ {
    suspend fun getAll(): List<GenModel_897_>
    suspend fun getById(id: Long): GenModel_897_?
    suspend fun save(model: GenModel_897_): GenModel_897_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_897_>
}

@Singleton
class GenRepositoryImpl_897_ @Inject constructor() : GenRepository_897_ {
    private val store = mutableMapOf<Long, GenModel_897_>()
    override suspend fun getAll(): List<GenModel_897_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_897_? = store[id]
    override suspend fun save(model: GenModel_897_): GenModel_897_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_897_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_897_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_897_ @Inject constructor(
    private val repository: GenRepositoryImpl_897_
) : GenUseCase_897_<Unit, List<GenModel_897_>> {
    override suspend fun invoke(params: Unit): List<GenModel_897_> = repository.getAll()
}

class GenSaveUseCase_897_ @Inject constructor(
    private val repository: GenRepositoryImpl_897_
) : GenUseCase_897_<GenModel_897_, GenModel_897_> {
    override suspend fun invoke(params: GenModel_897_): GenModel_897_ = repository.save(params)
}

class GenDeleteUseCase_897_ @Inject constructor(
    private val repository: GenRepositoryImpl_897_
) : GenUseCase_897_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_897_ @Inject constructor(
    private val repository: GenRepositoryImpl_897_
) : GenUseCase_897_<String, List<GenModel_897_>> {
    override suspend fun invoke(params: String): List<GenModel_897_> = repository.search(params)
}

abstract class GenMapper_897_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_897_ : GenMapper_897_<GenModel_897_, String>() {
    override fun map(input: GenModel_897_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_897_ : GenMapper_897_<String, GenModel_897_>() {
    override fun map(input: String): GenModel_897_ {
        val parts = input.split(":")
        return GenModel_897_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_897_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_897_,
    private val saveUseCase: GenSaveUseCase_897_,
    private val deleteUseCase: GenDeleteUseCase_897_,
    private val searchUseCase: GenSearchUseCase_897_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_897_>(GenState_897_.Idle)
    val state: StateFlow<GenState_897_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_897_) {
        when (event) {
            is GenEvent_897_.Load -> loadAll()
            is GenEvent_897_.Update -> save(event.model)
            is GenEvent_897_.Delete -> delete(event.id)
            is GenEvent_897_.Refresh -> loadAll()
            is GenEvent_897_.Search -> search(event.query)
            is GenEvent_897_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_897_.Loading; _state.value = GenState_897_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_897_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_897_.Success(searchUseCase(query)) } }
}
