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

data class GenModel_528_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_528_ {
    data class Load(val id: Long) : GenEvent_528_()
    data class Update(val model: GenModel_528_) : GenEvent_528_()
    data class Delete(val id: Long) : GenEvent_528_()
    data object Refresh : GenEvent_528_()
    data class Search(val query: String) : GenEvent_528_()
    data class Filter(val predicate: String) : GenEvent_528_()
}

sealed class GenState_528_ {
    data object Idle : GenState_528_()
    data object Loading : GenState_528_()
    data class Success(val items: List<GenModel_528_>) : GenState_528_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_528_()
    data class Partial(val items: List<GenModel_528_>, val hasMore: Boolean) : GenState_528_()
}

interface GenRepository_528_ {
    suspend fun getAll(): List<GenModel_528_>
    suspend fun getById(id: Long): GenModel_528_?
    suspend fun save(model: GenModel_528_): GenModel_528_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_528_>
}

@Singleton
class GenRepositoryImpl_528_ @Inject constructor() : GenRepository_528_ {
    private val store = mutableMapOf<Long, GenModel_528_>()
    override suspend fun getAll(): List<GenModel_528_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_528_? = store[id]
    override suspend fun save(model: GenModel_528_): GenModel_528_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_528_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_528_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_528_ @Inject constructor(
    private val repository: GenRepositoryImpl_528_
) : GenUseCase_528_<Unit, List<GenModel_528_>> {
    override suspend fun invoke(params: Unit): List<GenModel_528_> = repository.getAll()
}

class GenSaveUseCase_528_ @Inject constructor(
    private val repository: GenRepositoryImpl_528_
) : GenUseCase_528_<GenModel_528_, GenModel_528_> {
    override suspend fun invoke(params: GenModel_528_): GenModel_528_ = repository.save(params)
}

class GenDeleteUseCase_528_ @Inject constructor(
    private val repository: GenRepositoryImpl_528_
) : GenUseCase_528_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_528_ @Inject constructor(
    private val repository: GenRepositoryImpl_528_
) : GenUseCase_528_<String, List<GenModel_528_>> {
    override suspend fun invoke(params: String): List<GenModel_528_> = repository.search(params)
}

abstract class GenMapper_528_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_528_ : GenMapper_528_<GenModel_528_, String>() {
    override fun map(input: GenModel_528_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_528_ : GenMapper_528_<String, GenModel_528_>() {
    override fun map(input: String): GenModel_528_ {
        val parts = input.split(":")
        return GenModel_528_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_528_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_528_,
    private val saveUseCase: GenSaveUseCase_528_,
    private val deleteUseCase: GenDeleteUseCase_528_,
    private val searchUseCase: GenSearchUseCase_528_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_528_>(GenState_528_.Idle)
    val state: StateFlow<GenState_528_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_528_) {
        when (event) {
            is GenEvent_528_.Load -> loadAll()
            is GenEvent_528_.Update -> save(event.model)
            is GenEvent_528_.Delete -> delete(event.id)
            is GenEvent_528_.Refresh -> loadAll()
            is GenEvent_528_.Search -> search(event.query)
            is GenEvent_528_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_528_.Loading; _state.value = GenState_528_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_528_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_528_.Success(searchUseCase(query)) } }
}
