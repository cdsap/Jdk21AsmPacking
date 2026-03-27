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

data class GenModel_299_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_299_ {
    data class Load(val id: Long) : GenEvent_299_()
    data class Update(val model: GenModel_299_) : GenEvent_299_()
    data class Delete(val id: Long) : GenEvent_299_()
    data object Refresh : GenEvent_299_()
    data class Search(val query: String) : GenEvent_299_()
    data class Filter(val predicate: String) : GenEvent_299_()
}

sealed class GenState_299_ {
    data object Idle : GenState_299_()
    data object Loading : GenState_299_()
    data class Success(val items: List<GenModel_299_>) : GenState_299_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_299_()
    data class Partial(val items: List<GenModel_299_>, val hasMore: Boolean) : GenState_299_()
}

interface GenRepository_299_ {
    suspend fun getAll(): List<GenModel_299_>
    suspend fun getById(id: Long): GenModel_299_?
    suspend fun save(model: GenModel_299_): GenModel_299_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_299_>
}

@Singleton
class GenRepositoryImpl_299_ @Inject constructor() : GenRepository_299_ {
    private val store = mutableMapOf<Long, GenModel_299_>()
    override suspend fun getAll(): List<GenModel_299_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_299_? = store[id]
    override suspend fun save(model: GenModel_299_): GenModel_299_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_299_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_299_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_299_ @Inject constructor(
    private val repository: GenRepositoryImpl_299_
) : GenUseCase_299_<Unit, List<GenModel_299_>> {
    override suspend fun invoke(params: Unit): List<GenModel_299_> = repository.getAll()
}

class GenSaveUseCase_299_ @Inject constructor(
    private val repository: GenRepositoryImpl_299_
) : GenUseCase_299_<GenModel_299_, GenModel_299_> {
    override suspend fun invoke(params: GenModel_299_): GenModel_299_ = repository.save(params)
}

class GenDeleteUseCase_299_ @Inject constructor(
    private val repository: GenRepositoryImpl_299_
) : GenUseCase_299_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_299_ @Inject constructor(
    private val repository: GenRepositoryImpl_299_
) : GenUseCase_299_<String, List<GenModel_299_>> {
    override suspend fun invoke(params: String): List<GenModel_299_> = repository.search(params)
}

abstract class GenMapper_299_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_299_ : GenMapper_299_<GenModel_299_, String>() {
    override fun map(input: GenModel_299_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_299_ : GenMapper_299_<String, GenModel_299_>() {
    override fun map(input: String): GenModel_299_ {
        val parts = input.split(":")
        return GenModel_299_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_299_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_299_,
    private val saveUseCase: GenSaveUseCase_299_,
    private val deleteUseCase: GenDeleteUseCase_299_,
    private val searchUseCase: GenSearchUseCase_299_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_299_>(GenState_299_.Idle)
    val state: StateFlow<GenState_299_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_299_) {
        when (event) {
            is GenEvent_299_.Load -> loadAll()
            is GenEvent_299_.Update -> save(event.model)
            is GenEvent_299_.Delete -> delete(event.id)
            is GenEvent_299_.Refresh -> loadAll()
            is GenEvent_299_.Search -> search(event.query)
            is GenEvent_299_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_299_.Loading; _state.value = GenState_299_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_299_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_299_.Success(searchUseCase(query)) } }
}
