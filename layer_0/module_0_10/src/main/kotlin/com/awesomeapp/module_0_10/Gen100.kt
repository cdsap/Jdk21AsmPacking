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

data class GenModel_100_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_100_ {
    data class Load(val id: Long) : GenEvent_100_()
    data class Update(val model: GenModel_100_) : GenEvent_100_()
    data class Delete(val id: Long) : GenEvent_100_()
    data object Refresh : GenEvent_100_()
    data class Search(val query: String) : GenEvent_100_()
    data class Filter(val predicate: String) : GenEvent_100_()
}

sealed class GenState_100_ {
    data object Idle : GenState_100_()
    data object Loading : GenState_100_()
    data class Success(val items: List<GenModel_100_>) : GenState_100_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_100_()
    data class Partial(val items: List<GenModel_100_>, val hasMore: Boolean) : GenState_100_()
}

interface GenRepository_100_ {
    suspend fun getAll(): List<GenModel_100_>
    suspend fun getById(id: Long): GenModel_100_?
    suspend fun save(model: GenModel_100_): GenModel_100_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_100_>
}

@Singleton
class GenRepositoryImpl_100_ @Inject constructor() : GenRepository_100_ {
    private val store = mutableMapOf<Long, GenModel_100_>()
    override suspend fun getAll(): List<GenModel_100_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_100_? = store[id]
    override suspend fun save(model: GenModel_100_): GenModel_100_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_100_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_100_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_100_ @Inject constructor(
    private val repository: GenRepositoryImpl_100_
) : GenUseCase_100_<Unit, List<GenModel_100_>> {
    override suspend fun invoke(params: Unit): List<GenModel_100_> = repository.getAll()
}

class GenSaveUseCase_100_ @Inject constructor(
    private val repository: GenRepositoryImpl_100_
) : GenUseCase_100_<GenModel_100_, GenModel_100_> {
    override suspend fun invoke(params: GenModel_100_): GenModel_100_ = repository.save(params)
}

class GenDeleteUseCase_100_ @Inject constructor(
    private val repository: GenRepositoryImpl_100_
) : GenUseCase_100_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_100_ @Inject constructor(
    private val repository: GenRepositoryImpl_100_
) : GenUseCase_100_<String, List<GenModel_100_>> {
    override suspend fun invoke(params: String): List<GenModel_100_> = repository.search(params)
}

abstract class GenMapper_100_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_100_ : GenMapper_100_<GenModel_100_, String>() {
    override fun map(input: GenModel_100_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_100_ : GenMapper_100_<String, GenModel_100_>() {
    override fun map(input: String): GenModel_100_ {
        val parts = input.split(":")
        return GenModel_100_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_100_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_100_,
    private val saveUseCase: GenSaveUseCase_100_,
    private val deleteUseCase: GenDeleteUseCase_100_,
    private val searchUseCase: GenSearchUseCase_100_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_100_>(GenState_100_.Idle)
    val state: StateFlow<GenState_100_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_100_) {
        when (event) {
            is GenEvent_100_.Load -> loadAll()
            is GenEvent_100_.Update -> save(event.model)
            is GenEvent_100_.Delete -> delete(event.id)
            is GenEvent_100_.Refresh -> loadAll()
            is GenEvent_100_.Search -> search(event.query)
            is GenEvent_100_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_100_.Loading; _state.value = GenState_100_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_100_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_100_.Success(searchUseCase(query)) } }
}
