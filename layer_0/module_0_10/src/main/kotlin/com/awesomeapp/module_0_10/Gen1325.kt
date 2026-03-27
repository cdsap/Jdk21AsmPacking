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

data class GenModel_1325_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1325_ {
    data class Load(val id: Long) : GenEvent_1325_()
    data class Update(val model: GenModel_1325_) : GenEvent_1325_()
    data class Delete(val id: Long) : GenEvent_1325_()
    data object Refresh : GenEvent_1325_()
    data class Search(val query: String) : GenEvent_1325_()
    data class Filter(val predicate: String) : GenEvent_1325_()
}

sealed class GenState_1325_ {
    data object Idle : GenState_1325_()
    data object Loading : GenState_1325_()
    data class Success(val items: List<GenModel_1325_>) : GenState_1325_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1325_()
    data class Partial(val items: List<GenModel_1325_>, val hasMore: Boolean) : GenState_1325_()
}

interface GenRepository_1325_ {
    suspend fun getAll(): List<GenModel_1325_>
    suspend fun getById(id: Long): GenModel_1325_?
    suspend fun save(model: GenModel_1325_): GenModel_1325_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1325_>
}

@Singleton
class GenRepositoryImpl_1325_ @Inject constructor() : GenRepository_1325_ {
    private val store = mutableMapOf<Long, GenModel_1325_>()
    override suspend fun getAll(): List<GenModel_1325_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1325_? = store[id]
    override suspend fun save(model: GenModel_1325_): GenModel_1325_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1325_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1325_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1325_ @Inject constructor(
    private val repository: GenRepositoryImpl_1325_
) : GenUseCase_1325_<Unit, List<GenModel_1325_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1325_> = repository.getAll()
}

class GenSaveUseCase_1325_ @Inject constructor(
    private val repository: GenRepositoryImpl_1325_
) : GenUseCase_1325_<GenModel_1325_, GenModel_1325_> {
    override suspend fun invoke(params: GenModel_1325_): GenModel_1325_ = repository.save(params)
}

class GenDeleteUseCase_1325_ @Inject constructor(
    private val repository: GenRepositoryImpl_1325_
) : GenUseCase_1325_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1325_ @Inject constructor(
    private val repository: GenRepositoryImpl_1325_
) : GenUseCase_1325_<String, List<GenModel_1325_>> {
    override suspend fun invoke(params: String): List<GenModel_1325_> = repository.search(params)
}

abstract class GenMapper_1325_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1325_ : GenMapper_1325_<GenModel_1325_, String>() {
    override fun map(input: GenModel_1325_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1325_ : GenMapper_1325_<String, GenModel_1325_>() {
    override fun map(input: String): GenModel_1325_ {
        val parts = input.split(":")
        return GenModel_1325_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1325_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1325_,
    private val saveUseCase: GenSaveUseCase_1325_,
    private val deleteUseCase: GenDeleteUseCase_1325_,
    private val searchUseCase: GenSearchUseCase_1325_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1325_>(GenState_1325_.Idle)
    val state: StateFlow<GenState_1325_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1325_) {
        when (event) {
            is GenEvent_1325_.Load -> loadAll()
            is GenEvent_1325_.Update -> save(event.model)
            is GenEvent_1325_.Delete -> delete(event.id)
            is GenEvent_1325_.Refresh -> loadAll()
            is GenEvent_1325_.Search -> search(event.query)
            is GenEvent_1325_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1325_.Loading; _state.value = GenState_1325_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1325_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1325_.Success(searchUseCase(query)) } }
}
