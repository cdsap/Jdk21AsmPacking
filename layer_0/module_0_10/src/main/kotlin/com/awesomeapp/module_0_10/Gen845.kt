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

data class GenModel_845_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_845_ {
    data class Load(val id: Long) : GenEvent_845_()
    data class Update(val model: GenModel_845_) : GenEvent_845_()
    data class Delete(val id: Long) : GenEvent_845_()
    data object Refresh : GenEvent_845_()
    data class Search(val query: String) : GenEvent_845_()
    data class Filter(val predicate: String) : GenEvent_845_()
}

sealed class GenState_845_ {
    data object Idle : GenState_845_()
    data object Loading : GenState_845_()
    data class Success(val items: List<GenModel_845_>) : GenState_845_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_845_()
    data class Partial(val items: List<GenModel_845_>, val hasMore: Boolean) : GenState_845_()
}

interface GenRepository_845_ {
    suspend fun getAll(): List<GenModel_845_>
    suspend fun getById(id: Long): GenModel_845_?
    suspend fun save(model: GenModel_845_): GenModel_845_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_845_>
}

@Singleton
class GenRepositoryImpl_845_ @Inject constructor() : GenRepository_845_ {
    private val store = mutableMapOf<Long, GenModel_845_>()
    override suspend fun getAll(): List<GenModel_845_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_845_? = store[id]
    override suspend fun save(model: GenModel_845_): GenModel_845_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_845_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_845_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_845_ @Inject constructor(
    private val repository: GenRepositoryImpl_845_
) : GenUseCase_845_<Unit, List<GenModel_845_>> {
    override suspend fun invoke(params: Unit): List<GenModel_845_> = repository.getAll()
}

class GenSaveUseCase_845_ @Inject constructor(
    private val repository: GenRepositoryImpl_845_
) : GenUseCase_845_<GenModel_845_, GenModel_845_> {
    override suspend fun invoke(params: GenModel_845_): GenModel_845_ = repository.save(params)
}

class GenDeleteUseCase_845_ @Inject constructor(
    private val repository: GenRepositoryImpl_845_
) : GenUseCase_845_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_845_ @Inject constructor(
    private val repository: GenRepositoryImpl_845_
) : GenUseCase_845_<String, List<GenModel_845_>> {
    override suspend fun invoke(params: String): List<GenModel_845_> = repository.search(params)
}

abstract class GenMapper_845_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_845_ : GenMapper_845_<GenModel_845_, String>() {
    override fun map(input: GenModel_845_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_845_ : GenMapper_845_<String, GenModel_845_>() {
    override fun map(input: String): GenModel_845_ {
        val parts = input.split(":")
        return GenModel_845_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_845_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_845_,
    private val saveUseCase: GenSaveUseCase_845_,
    private val deleteUseCase: GenDeleteUseCase_845_,
    private val searchUseCase: GenSearchUseCase_845_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_845_>(GenState_845_.Idle)
    val state: StateFlow<GenState_845_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_845_) {
        when (event) {
            is GenEvent_845_.Load -> loadAll()
            is GenEvent_845_.Update -> save(event.model)
            is GenEvent_845_.Delete -> delete(event.id)
            is GenEvent_845_.Refresh -> loadAll()
            is GenEvent_845_.Search -> search(event.query)
            is GenEvent_845_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_845_.Loading; _state.value = GenState_845_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_845_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_845_.Success(searchUseCase(query)) } }
}
