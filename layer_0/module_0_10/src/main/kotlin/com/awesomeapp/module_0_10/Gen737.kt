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

data class GenModel_737_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_737_ {
    data class Load(val id: Long) : GenEvent_737_()
    data class Update(val model: GenModel_737_) : GenEvent_737_()
    data class Delete(val id: Long) : GenEvent_737_()
    data object Refresh : GenEvent_737_()
    data class Search(val query: String) : GenEvent_737_()
    data class Filter(val predicate: String) : GenEvent_737_()
}

sealed class GenState_737_ {
    data object Idle : GenState_737_()
    data object Loading : GenState_737_()
    data class Success(val items: List<GenModel_737_>) : GenState_737_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_737_()
    data class Partial(val items: List<GenModel_737_>, val hasMore: Boolean) : GenState_737_()
}

interface GenRepository_737_ {
    suspend fun getAll(): List<GenModel_737_>
    suspend fun getById(id: Long): GenModel_737_?
    suspend fun save(model: GenModel_737_): GenModel_737_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_737_>
}

@Singleton
class GenRepositoryImpl_737_ @Inject constructor() : GenRepository_737_ {
    private val store = mutableMapOf<Long, GenModel_737_>()
    override suspend fun getAll(): List<GenModel_737_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_737_? = store[id]
    override suspend fun save(model: GenModel_737_): GenModel_737_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_737_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_737_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_737_ @Inject constructor(
    private val repository: GenRepositoryImpl_737_
) : GenUseCase_737_<Unit, List<GenModel_737_>> {
    override suspend fun invoke(params: Unit): List<GenModel_737_> = repository.getAll()
}

class GenSaveUseCase_737_ @Inject constructor(
    private val repository: GenRepositoryImpl_737_
) : GenUseCase_737_<GenModel_737_, GenModel_737_> {
    override suspend fun invoke(params: GenModel_737_): GenModel_737_ = repository.save(params)
}

class GenDeleteUseCase_737_ @Inject constructor(
    private val repository: GenRepositoryImpl_737_
) : GenUseCase_737_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_737_ @Inject constructor(
    private val repository: GenRepositoryImpl_737_
) : GenUseCase_737_<String, List<GenModel_737_>> {
    override suspend fun invoke(params: String): List<GenModel_737_> = repository.search(params)
}

abstract class GenMapper_737_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_737_ : GenMapper_737_<GenModel_737_, String>() {
    override fun map(input: GenModel_737_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_737_ : GenMapper_737_<String, GenModel_737_>() {
    override fun map(input: String): GenModel_737_ {
        val parts = input.split(":")
        return GenModel_737_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_737_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_737_,
    private val saveUseCase: GenSaveUseCase_737_,
    private val deleteUseCase: GenDeleteUseCase_737_,
    private val searchUseCase: GenSearchUseCase_737_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_737_>(GenState_737_.Idle)
    val state: StateFlow<GenState_737_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_737_) {
        when (event) {
            is GenEvent_737_.Load -> loadAll()
            is GenEvent_737_.Update -> save(event.model)
            is GenEvent_737_.Delete -> delete(event.id)
            is GenEvent_737_.Refresh -> loadAll()
            is GenEvent_737_.Search -> search(event.query)
            is GenEvent_737_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_737_.Loading; _state.value = GenState_737_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_737_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_737_.Success(searchUseCase(query)) } }
}
