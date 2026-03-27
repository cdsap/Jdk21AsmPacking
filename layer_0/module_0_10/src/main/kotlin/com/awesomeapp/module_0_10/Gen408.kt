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

data class GenModel_408_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_408_ {
    data class Load(val id: Long) : GenEvent_408_()
    data class Update(val model: GenModel_408_) : GenEvent_408_()
    data class Delete(val id: Long) : GenEvent_408_()
    data object Refresh : GenEvent_408_()
    data class Search(val query: String) : GenEvent_408_()
    data class Filter(val predicate: String) : GenEvent_408_()
}

sealed class GenState_408_ {
    data object Idle : GenState_408_()
    data object Loading : GenState_408_()
    data class Success(val items: List<GenModel_408_>) : GenState_408_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_408_()
    data class Partial(val items: List<GenModel_408_>, val hasMore: Boolean) : GenState_408_()
}

interface GenRepository_408_ {
    suspend fun getAll(): List<GenModel_408_>
    suspend fun getById(id: Long): GenModel_408_?
    suspend fun save(model: GenModel_408_): GenModel_408_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_408_>
}

@Singleton
class GenRepositoryImpl_408_ @Inject constructor() : GenRepository_408_ {
    private val store = mutableMapOf<Long, GenModel_408_>()
    override suspend fun getAll(): List<GenModel_408_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_408_? = store[id]
    override suspend fun save(model: GenModel_408_): GenModel_408_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_408_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_408_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_408_ @Inject constructor(
    private val repository: GenRepositoryImpl_408_
) : GenUseCase_408_<Unit, List<GenModel_408_>> {
    override suspend fun invoke(params: Unit): List<GenModel_408_> = repository.getAll()
}

class GenSaveUseCase_408_ @Inject constructor(
    private val repository: GenRepositoryImpl_408_
) : GenUseCase_408_<GenModel_408_, GenModel_408_> {
    override suspend fun invoke(params: GenModel_408_): GenModel_408_ = repository.save(params)
}

class GenDeleteUseCase_408_ @Inject constructor(
    private val repository: GenRepositoryImpl_408_
) : GenUseCase_408_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_408_ @Inject constructor(
    private val repository: GenRepositoryImpl_408_
) : GenUseCase_408_<String, List<GenModel_408_>> {
    override suspend fun invoke(params: String): List<GenModel_408_> = repository.search(params)
}

abstract class GenMapper_408_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_408_ : GenMapper_408_<GenModel_408_, String>() {
    override fun map(input: GenModel_408_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_408_ : GenMapper_408_<String, GenModel_408_>() {
    override fun map(input: String): GenModel_408_ {
        val parts = input.split(":")
        return GenModel_408_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_408_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_408_,
    private val saveUseCase: GenSaveUseCase_408_,
    private val deleteUseCase: GenDeleteUseCase_408_,
    private val searchUseCase: GenSearchUseCase_408_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_408_>(GenState_408_.Idle)
    val state: StateFlow<GenState_408_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_408_) {
        when (event) {
            is GenEvent_408_.Load -> loadAll()
            is GenEvent_408_.Update -> save(event.model)
            is GenEvent_408_.Delete -> delete(event.id)
            is GenEvent_408_.Refresh -> loadAll()
            is GenEvent_408_.Search -> search(event.query)
            is GenEvent_408_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_408_.Loading; _state.value = GenState_408_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_408_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_408_.Success(searchUseCase(query)) } }
}
