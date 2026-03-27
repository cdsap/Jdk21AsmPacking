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

data class GenModel_362_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_362_ {
    data class Load(val id: Long) : GenEvent_362_()
    data class Update(val model: GenModel_362_) : GenEvent_362_()
    data class Delete(val id: Long) : GenEvent_362_()
    data object Refresh : GenEvent_362_()
    data class Search(val query: String) : GenEvent_362_()
    data class Filter(val predicate: String) : GenEvent_362_()
}

sealed class GenState_362_ {
    data object Idle : GenState_362_()
    data object Loading : GenState_362_()
    data class Success(val items: List<GenModel_362_>) : GenState_362_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_362_()
    data class Partial(val items: List<GenModel_362_>, val hasMore: Boolean) : GenState_362_()
}

interface GenRepository_362_ {
    suspend fun getAll(): List<GenModel_362_>
    suspend fun getById(id: Long): GenModel_362_?
    suspend fun save(model: GenModel_362_): GenModel_362_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_362_>
}

@Singleton
class GenRepositoryImpl_362_ @Inject constructor() : GenRepository_362_ {
    private val store = mutableMapOf<Long, GenModel_362_>()
    override suspend fun getAll(): List<GenModel_362_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_362_? = store[id]
    override suspend fun save(model: GenModel_362_): GenModel_362_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_362_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_362_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_362_ @Inject constructor(
    private val repository: GenRepositoryImpl_362_
) : GenUseCase_362_<Unit, List<GenModel_362_>> {
    override suspend fun invoke(params: Unit): List<GenModel_362_> = repository.getAll()
}

class GenSaveUseCase_362_ @Inject constructor(
    private val repository: GenRepositoryImpl_362_
) : GenUseCase_362_<GenModel_362_, GenModel_362_> {
    override suspend fun invoke(params: GenModel_362_): GenModel_362_ = repository.save(params)
}

class GenDeleteUseCase_362_ @Inject constructor(
    private val repository: GenRepositoryImpl_362_
) : GenUseCase_362_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_362_ @Inject constructor(
    private val repository: GenRepositoryImpl_362_
) : GenUseCase_362_<String, List<GenModel_362_>> {
    override suspend fun invoke(params: String): List<GenModel_362_> = repository.search(params)
}

abstract class GenMapper_362_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_362_ : GenMapper_362_<GenModel_362_, String>() {
    override fun map(input: GenModel_362_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_362_ : GenMapper_362_<String, GenModel_362_>() {
    override fun map(input: String): GenModel_362_ {
        val parts = input.split(":")
        return GenModel_362_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_362_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_362_,
    private val saveUseCase: GenSaveUseCase_362_,
    private val deleteUseCase: GenDeleteUseCase_362_,
    private val searchUseCase: GenSearchUseCase_362_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_362_>(GenState_362_.Idle)
    val state: StateFlow<GenState_362_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_362_) {
        when (event) {
            is GenEvent_362_.Load -> loadAll()
            is GenEvent_362_.Update -> save(event.model)
            is GenEvent_362_.Delete -> delete(event.id)
            is GenEvent_362_.Refresh -> loadAll()
            is GenEvent_362_.Search -> search(event.query)
            is GenEvent_362_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_362_.Loading; _state.value = GenState_362_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_362_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_362_.Success(searchUseCase(query)) } }
}
