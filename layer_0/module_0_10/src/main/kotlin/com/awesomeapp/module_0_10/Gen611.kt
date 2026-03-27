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

data class GenModel_611_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_611_ {
    data class Load(val id: Long) : GenEvent_611_()
    data class Update(val model: GenModel_611_) : GenEvent_611_()
    data class Delete(val id: Long) : GenEvent_611_()
    data object Refresh : GenEvent_611_()
    data class Search(val query: String) : GenEvent_611_()
    data class Filter(val predicate: String) : GenEvent_611_()
}

sealed class GenState_611_ {
    data object Idle : GenState_611_()
    data object Loading : GenState_611_()
    data class Success(val items: List<GenModel_611_>) : GenState_611_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_611_()
    data class Partial(val items: List<GenModel_611_>, val hasMore: Boolean) : GenState_611_()
}

interface GenRepository_611_ {
    suspend fun getAll(): List<GenModel_611_>
    suspend fun getById(id: Long): GenModel_611_?
    suspend fun save(model: GenModel_611_): GenModel_611_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_611_>
}

@Singleton
class GenRepositoryImpl_611_ @Inject constructor() : GenRepository_611_ {
    private val store = mutableMapOf<Long, GenModel_611_>()
    override suspend fun getAll(): List<GenModel_611_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_611_? = store[id]
    override suspend fun save(model: GenModel_611_): GenModel_611_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_611_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_611_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_611_ @Inject constructor(
    private val repository: GenRepositoryImpl_611_
) : GenUseCase_611_<Unit, List<GenModel_611_>> {
    override suspend fun invoke(params: Unit): List<GenModel_611_> = repository.getAll()
}

class GenSaveUseCase_611_ @Inject constructor(
    private val repository: GenRepositoryImpl_611_
) : GenUseCase_611_<GenModel_611_, GenModel_611_> {
    override suspend fun invoke(params: GenModel_611_): GenModel_611_ = repository.save(params)
}

class GenDeleteUseCase_611_ @Inject constructor(
    private val repository: GenRepositoryImpl_611_
) : GenUseCase_611_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_611_ @Inject constructor(
    private val repository: GenRepositoryImpl_611_
) : GenUseCase_611_<String, List<GenModel_611_>> {
    override suspend fun invoke(params: String): List<GenModel_611_> = repository.search(params)
}

abstract class GenMapper_611_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_611_ : GenMapper_611_<GenModel_611_, String>() {
    override fun map(input: GenModel_611_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_611_ : GenMapper_611_<String, GenModel_611_>() {
    override fun map(input: String): GenModel_611_ {
        val parts = input.split(":")
        return GenModel_611_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_611_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_611_,
    private val saveUseCase: GenSaveUseCase_611_,
    private val deleteUseCase: GenDeleteUseCase_611_,
    private val searchUseCase: GenSearchUseCase_611_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_611_>(GenState_611_.Idle)
    val state: StateFlow<GenState_611_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_611_) {
        when (event) {
            is GenEvent_611_.Load -> loadAll()
            is GenEvent_611_.Update -> save(event.model)
            is GenEvent_611_.Delete -> delete(event.id)
            is GenEvent_611_.Refresh -> loadAll()
            is GenEvent_611_.Search -> search(event.query)
            is GenEvent_611_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_611_.Loading; _state.value = GenState_611_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_611_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_611_.Success(searchUseCase(query)) } }
}
