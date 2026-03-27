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

data class GenModel_541_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_541_ {
    data class Load(val id: Long) : GenEvent_541_()
    data class Update(val model: GenModel_541_) : GenEvent_541_()
    data class Delete(val id: Long) : GenEvent_541_()
    data object Refresh : GenEvent_541_()
    data class Search(val query: String) : GenEvent_541_()
    data class Filter(val predicate: String) : GenEvent_541_()
}

sealed class GenState_541_ {
    data object Idle : GenState_541_()
    data object Loading : GenState_541_()
    data class Success(val items: List<GenModel_541_>) : GenState_541_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_541_()
    data class Partial(val items: List<GenModel_541_>, val hasMore: Boolean) : GenState_541_()
}

interface GenRepository_541_ {
    suspend fun getAll(): List<GenModel_541_>
    suspend fun getById(id: Long): GenModel_541_?
    suspend fun save(model: GenModel_541_): GenModel_541_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_541_>
}

@Singleton
class GenRepositoryImpl_541_ @Inject constructor() : GenRepository_541_ {
    private val store = mutableMapOf<Long, GenModel_541_>()
    override suspend fun getAll(): List<GenModel_541_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_541_? = store[id]
    override suspend fun save(model: GenModel_541_): GenModel_541_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_541_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_541_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_541_ @Inject constructor(
    private val repository: GenRepositoryImpl_541_
) : GenUseCase_541_<Unit, List<GenModel_541_>> {
    override suspend fun invoke(params: Unit): List<GenModel_541_> = repository.getAll()
}

class GenSaveUseCase_541_ @Inject constructor(
    private val repository: GenRepositoryImpl_541_
) : GenUseCase_541_<GenModel_541_, GenModel_541_> {
    override suspend fun invoke(params: GenModel_541_): GenModel_541_ = repository.save(params)
}

class GenDeleteUseCase_541_ @Inject constructor(
    private val repository: GenRepositoryImpl_541_
) : GenUseCase_541_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_541_ @Inject constructor(
    private val repository: GenRepositoryImpl_541_
) : GenUseCase_541_<String, List<GenModel_541_>> {
    override suspend fun invoke(params: String): List<GenModel_541_> = repository.search(params)
}

abstract class GenMapper_541_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_541_ : GenMapper_541_<GenModel_541_, String>() {
    override fun map(input: GenModel_541_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_541_ : GenMapper_541_<String, GenModel_541_>() {
    override fun map(input: String): GenModel_541_ {
        val parts = input.split(":")
        return GenModel_541_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_541_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_541_,
    private val saveUseCase: GenSaveUseCase_541_,
    private val deleteUseCase: GenDeleteUseCase_541_,
    private val searchUseCase: GenSearchUseCase_541_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_541_>(GenState_541_.Idle)
    val state: StateFlow<GenState_541_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_541_) {
        when (event) {
            is GenEvent_541_.Load -> loadAll()
            is GenEvent_541_.Update -> save(event.model)
            is GenEvent_541_.Delete -> delete(event.id)
            is GenEvent_541_.Refresh -> loadAll()
            is GenEvent_541_.Search -> search(event.query)
            is GenEvent_541_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_541_.Loading; _state.value = GenState_541_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_541_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_541_.Success(searchUseCase(query)) } }
}
