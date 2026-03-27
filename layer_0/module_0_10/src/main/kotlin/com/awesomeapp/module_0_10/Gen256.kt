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

data class GenModel_256_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_256_ {
    data class Load(val id: Long) : GenEvent_256_()
    data class Update(val model: GenModel_256_) : GenEvent_256_()
    data class Delete(val id: Long) : GenEvent_256_()
    data object Refresh : GenEvent_256_()
    data class Search(val query: String) : GenEvent_256_()
    data class Filter(val predicate: String) : GenEvent_256_()
}

sealed class GenState_256_ {
    data object Idle : GenState_256_()
    data object Loading : GenState_256_()
    data class Success(val items: List<GenModel_256_>) : GenState_256_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_256_()
    data class Partial(val items: List<GenModel_256_>, val hasMore: Boolean) : GenState_256_()
}

interface GenRepository_256_ {
    suspend fun getAll(): List<GenModel_256_>
    suspend fun getById(id: Long): GenModel_256_?
    suspend fun save(model: GenModel_256_): GenModel_256_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_256_>
}

@Singleton
class GenRepositoryImpl_256_ @Inject constructor() : GenRepository_256_ {
    private val store = mutableMapOf<Long, GenModel_256_>()
    override suspend fun getAll(): List<GenModel_256_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_256_? = store[id]
    override suspend fun save(model: GenModel_256_): GenModel_256_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_256_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_256_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_256_ @Inject constructor(
    private val repository: GenRepositoryImpl_256_
) : GenUseCase_256_<Unit, List<GenModel_256_>> {
    override suspend fun invoke(params: Unit): List<GenModel_256_> = repository.getAll()
}

class GenSaveUseCase_256_ @Inject constructor(
    private val repository: GenRepositoryImpl_256_
) : GenUseCase_256_<GenModel_256_, GenModel_256_> {
    override suspend fun invoke(params: GenModel_256_): GenModel_256_ = repository.save(params)
}

class GenDeleteUseCase_256_ @Inject constructor(
    private val repository: GenRepositoryImpl_256_
) : GenUseCase_256_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_256_ @Inject constructor(
    private val repository: GenRepositoryImpl_256_
) : GenUseCase_256_<String, List<GenModel_256_>> {
    override suspend fun invoke(params: String): List<GenModel_256_> = repository.search(params)
}

abstract class GenMapper_256_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_256_ : GenMapper_256_<GenModel_256_, String>() {
    override fun map(input: GenModel_256_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_256_ : GenMapper_256_<String, GenModel_256_>() {
    override fun map(input: String): GenModel_256_ {
        val parts = input.split(":")
        return GenModel_256_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_256_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_256_,
    private val saveUseCase: GenSaveUseCase_256_,
    private val deleteUseCase: GenDeleteUseCase_256_,
    private val searchUseCase: GenSearchUseCase_256_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_256_>(GenState_256_.Idle)
    val state: StateFlow<GenState_256_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_256_) {
        when (event) {
            is GenEvent_256_.Load -> loadAll()
            is GenEvent_256_.Update -> save(event.model)
            is GenEvent_256_.Delete -> delete(event.id)
            is GenEvent_256_.Refresh -> loadAll()
            is GenEvent_256_.Search -> search(event.query)
            is GenEvent_256_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_256_.Loading; _state.value = GenState_256_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_256_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_256_.Success(searchUseCase(query)) } }
}
