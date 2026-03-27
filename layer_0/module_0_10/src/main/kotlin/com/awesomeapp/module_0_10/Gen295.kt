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

data class GenModel_295_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_295_ {
    data class Load(val id: Long) : GenEvent_295_()
    data class Update(val model: GenModel_295_) : GenEvent_295_()
    data class Delete(val id: Long) : GenEvent_295_()
    data object Refresh : GenEvent_295_()
    data class Search(val query: String) : GenEvent_295_()
    data class Filter(val predicate: String) : GenEvent_295_()
}

sealed class GenState_295_ {
    data object Idle : GenState_295_()
    data object Loading : GenState_295_()
    data class Success(val items: List<GenModel_295_>) : GenState_295_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_295_()
    data class Partial(val items: List<GenModel_295_>, val hasMore: Boolean) : GenState_295_()
}

interface GenRepository_295_ {
    suspend fun getAll(): List<GenModel_295_>
    suspend fun getById(id: Long): GenModel_295_?
    suspend fun save(model: GenModel_295_): GenModel_295_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_295_>
}

@Singleton
class GenRepositoryImpl_295_ @Inject constructor() : GenRepository_295_ {
    private val store = mutableMapOf<Long, GenModel_295_>()
    override suspend fun getAll(): List<GenModel_295_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_295_? = store[id]
    override suspend fun save(model: GenModel_295_): GenModel_295_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_295_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_295_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_295_ @Inject constructor(
    private val repository: GenRepositoryImpl_295_
) : GenUseCase_295_<Unit, List<GenModel_295_>> {
    override suspend fun invoke(params: Unit): List<GenModel_295_> = repository.getAll()
}

class GenSaveUseCase_295_ @Inject constructor(
    private val repository: GenRepositoryImpl_295_
) : GenUseCase_295_<GenModel_295_, GenModel_295_> {
    override suspend fun invoke(params: GenModel_295_): GenModel_295_ = repository.save(params)
}

class GenDeleteUseCase_295_ @Inject constructor(
    private val repository: GenRepositoryImpl_295_
) : GenUseCase_295_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_295_ @Inject constructor(
    private val repository: GenRepositoryImpl_295_
) : GenUseCase_295_<String, List<GenModel_295_>> {
    override suspend fun invoke(params: String): List<GenModel_295_> = repository.search(params)
}

abstract class GenMapper_295_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_295_ : GenMapper_295_<GenModel_295_, String>() {
    override fun map(input: GenModel_295_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_295_ : GenMapper_295_<String, GenModel_295_>() {
    override fun map(input: String): GenModel_295_ {
        val parts = input.split(":")
        return GenModel_295_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_295_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_295_,
    private val saveUseCase: GenSaveUseCase_295_,
    private val deleteUseCase: GenDeleteUseCase_295_,
    private val searchUseCase: GenSearchUseCase_295_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_295_>(GenState_295_.Idle)
    val state: StateFlow<GenState_295_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_295_) {
        when (event) {
            is GenEvent_295_.Load -> loadAll()
            is GenEvent_295_.Update -> save(event.model)
            is GenEvent_295_.Delete -> delete(event.id)
            is GenEvent_295_.Refresh -> loadAll()
            is GenEvent_295_.Search -> search(event.query)
            is GenEvent_295_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_295_.Loading; _state.value = GenState_295_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_295_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_295_.Success(searchUseCase(query)) } }
}
