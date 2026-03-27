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

data class GenModel_1014_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1014_ {
    data class Load(val id: Long) : GenEvent_1014_()
    data class Update(val model: GenModel_1014_) : GenEvent_1014_()
    data class Delete(val id: Long) : GenEvent_1014_()
    data object Refresh : GenEvent_1014_()
    data class Search(val query: String) : GenEvent_1014_()
    data class Filter(val predicate: String) : GenEvent_1014_()
}

sealed class GenState_1014_ {
    data object Idle : GenState_1014_()
    data object Loading : GenState_1014_()
    data class Success(val items: List<GenModel_1014_>) : GenState_1014_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1014_()
    data class Partial(val items: List<GenModel_1014_>, val hasMore: Boolean) : GenState_1014_()
}

interface GenRepository_1014_ {
    suspend fun getAll(): List<GenModel_1014_>
    suspend fun getById(id: Long): GenModel_1014_?
    suspend fun save(model: GenModel_1014_): GenModel_1014_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1014_>
}

@Singleton
class GenRepositoryImpl_1014_ @Inject constructor() : GenRepository_1014_ {
    private val store = mutableMapOf<Long, GenModel_1014_>()
    override suspend fun getAll(): List<GenModel_1014_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1014_? = store[id]
    override suspend fun save(model: GenModel_1014_): GenModel_1014_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1014_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1014_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1014_ @Inject constructor(
    private val repository: GenRepositoryImpl_1014_
) : GenUseCase_1014_<Unit, List<GenModel_1014_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1014_> = repository.getAll()
}

class GenSaveUseCase_1014_ @Inject constructor(
    private val repository: GenRepositoryImpl_1014_
) : GenUseCase_1014_<GenModel_1014_, GenModel_1014_> {
    override suspend fun invoke(params: GenModel_1014_): GenModel_1014_ = repository.save(params)
}

class GenDeleteUseCase_1014_ @Inject constructor(
    private val repository: GenRepositoryImpl_1014_
) : GenUseCase_1014_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1014_ @Inject constructor(
    private val repository: GenRepositoryImpl_1014_
) : GenUseCase_1014_<String, List<GenModel_1014_>> {
    override suspend fun invoke(params: String): List<GenModel_1014_> = repository.search(params)
}

abstract class GenMapper_1014_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1014_ : GenMapper_1014_<GenModel_1014_, String>() {
    override fun map(input: GenModel_1014_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1014_ : GenMapper_1014_<String, GenModel_1014_>() {
    override fun map(input: String): GenModel_1014_ {
        val parts = input.split(":")
        return GenModel_1014_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1014_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1014_,
    private val saveUseCase: GenSaveUseCase_1014_,
    private val deleteUseCase: GenDeleteUseCase_1014_,
    private val searchUseCase: GenSearchUseCase_1014_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1014_>(GenState_1014_.Idle)
    val state: StateFlow<GenState_1014_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1014_) {
        when (event) {
            is GenEvent_1014_.Load -> loadAll()
            is GenEvent_1014_.Update -> save(event.model)
            is GenEvent_1014_.Delete -> delete(event.id)
            is GenEvent_1014_.Refresh -> loadAll()
            is GenEvent_1014_.Search -> search(event.query)
            is GenEvent_1014_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1014_.Loading; _state.value = GenState_1014_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1014_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1014_.Success(searchUseCase(query)) } }
}
