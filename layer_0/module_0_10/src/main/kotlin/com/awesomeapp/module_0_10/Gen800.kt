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

data class GenModel_800_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_800_ {
    data class Load(val id: Long) : GenEvent_800_()
    data class Update(val model: GenModel_800_) : GenEvent_800_()
    data class Delete(val id: Long) : GenEvent_800_()
    data object Refresh : GenEvent_800_()
    data class Search(val query: String) : GenEvent_800_()
    data class Filter(val predicate: String) : GenEvent_800_()
}

sealed class GenState_800_ {
    data object Idle : GenState_800_()
    data object Loading : GenState_800_()
    data class Success(val items: List<GenModel_800_>) : GenState_800_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_800_()
    data class Partial(val items: List<GenModel_800_>, val hasMore: Boolean) : GenState_800_()
}

interface GenRepository_800_ {
    suspend fun getAll(): List<GenModel_800_>
    suspend fun getById(id: Long): GenModel_800_?
    suspend fun save(model: GenModel_800_): GenModel_800_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_800_>
}

@Singleton
class GenRepositoryImpl_800_ @Inject constructor() : GenRepository_800_ {
    private val store = mutableMapOf<Long, GenModel_800_>()
    override suspend fun getAll(): List<GenModel_800_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_800_? = store[id]
    override suspend fun save(model: GenModel_800_): GenModel_800_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_800_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_800_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_800_ @Inject constructor(
    private val repository: GenRepositoryImpl_800_
) : GenUseCase_800_<Unit, List<GenModel_800_>> {
    override suspend fun invoke(params: Unit): List<GenModel_800_> = repository.getAll()
}

class GenSaveUseCase_800_ @Inject constructor(
    private val repository: GenRepositoryImpl_800_
) : GenUseCase_800_<GenModel_800_, GenModel_800_> {
    override suspend fun invoke(params: GenModel_800_): GenModel_800_ = repository.save(params)
}

class GenDeleteUseCase_800_ @Inject constructor(
    private val repository: GenRepositoryImpl_800_
) : GenUseCase_800_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_800_ @Inject constructor(
    private val repository: GenRepositoryImpl_800_
) : GenUseCase_800_<String, List<GenModel_800_>> {
    override suspend fun invoke(params: String): List<GenModel_800_> = repository.search(params)
}

abstract class GenMapper_800_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_800_ : GenMapper_800_<GenModel_800_, String>() {
    override fun map(input: GenModel_800_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_800_ : GenMapper_800_<String, GenModel_800_>() {
    override fun map(input: String): GenModel_800_ {
        val parts = input.split(":")
        return GenModel_800_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_800_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_800_,
    private val saveUseCase: GenSaveUseCase_800_,
    private val deleteUseCase: GenDeleteUseCase_800_,
    private val searchUseCase: GenSearchUseCase_800_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_800_>(GenState_800_.Idle)
    val state: StateFlow<GenState_800_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_800_) {
        when (event) {
            is GenEvent_800_.Load -> loadAll()
            is GenEvent_800_.Update -> save(event.model)
            is GenEvent_800_.Delete -> delete(event.id)
            is GenEvent_800_.Refresh -> loadAll()
            is GenEvent_800_.Search -> search(event.query)
            is GenEvent_800_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_800_.Loading; _state.value = GenState_800_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_800_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_800_.Success(searchUseCase(query)) } }
}
