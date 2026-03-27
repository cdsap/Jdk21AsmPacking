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

data class GenModel_1737_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1737_ {
    data class Load(val id: Long) : GenEvent_1737_()
    data class Update(val model: GenModel_1737_) : GenEvent_1737_()
    data class Delete(val id: Long) : GenEvent_1737_()
    data object Refresh : GenEvent_1737_()
    data class Search(val query: String) : GenEvent_1737_()
    data class Filter(val predicate: String) : GenEvent_1737_()
}

sealed class GenState_1737_ {
    data object Idle : GenState_1737_()
    data object Loading : GenState_1737_()
    data class Success(val items: List<GenModel_1737_>) : GenState_1737_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1737_()
    data class Partial(val items: List<GenModel_1737_>, val hasMore: Boolean) : GenState_1737_()
}

interface GenRepository_1737_ {
    suspend fun getAll(): List<GenModel_1737_>
    suspend fun getById(id: Long): GenModel_1737_?
    suspend fun save(model: GenModel_1737_): GenModel_1737_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1737_>
}

@Singleton
class GenRepositoryImpl_1737_ @Inject constructor() : GenRepository_1737_ {
    private val store = mutableMapOf<Long, GenModel_1737_>()
    override suspend fun getAll(): List<GenModel_1737_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1737_? = store[id]
    override suspend fun save(model: GenModel_1737_): GenModel_1737_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1737_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1737_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1737_ @Inject constructor(
    private val repository: GenRepositoryImpl_1737_
) : GenUseCase_1737_<Unit, List<GenModel_1737_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1737_> = repository.getAll()
}

class GenSaveUseCase_1737_ @Inject constructor(
    private val repository: GenRepositoryImpl_1737_
) : GenUseCase_1737_<GenModel_1737_, GenModel_1737_> {
    override suspend fun invoke(params: GenModel_1737_): GenModel_1737_ = repository.save(params)
}

class GenDeleteUseCase_1737_ @Inject constructor(
    private val repository: GenRepositoryImpl_1737_
) : GenUseCase_1737_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1737_ @Inject constructor(
    private val repository: GenRepositoryImpl_1737_
) : GenUseCase_1737_<String, List<GenModel_1737_>> {
    override suspend fun invoke(params: String): List<GenModel_1737_> = repository.search(params)
}

abstract class GenMapper_1737_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1737_ : GenMapper_1737_<GenModel_1737_, String>() {
    override fun map(input: GenModel_1737_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1737_ : GenMapper_1737_<String, GenModel_1737_>() {
    override fun map(input: String): GenModel_1737_ {
        val parts = input.split(":")
        return GenModel_1737_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1737_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1737_,
    private val saveUseCase: GenSaveUseCase_1737_,
    private val deleteUseCase: GenDeleteUseCase_1737_,
    private val searchUseCase: GenSearchUseCase_1737_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1737_>(GenState_1737_.Idle)
    val state: StateFlow<GenState_1737_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1737_) {
        when (event) {
            is GenEvent_1737_.Load -> loadAll()
            is GenEvent_1737_.Update -> save(event.model)
            is GenEvent_1737_.Delete -> delete(event.id)
            is GenEvent_1737_.Refresh -> loadAll()
            is GenEvent_1737_.Search -> search(event.query)
            is GenEvent_1737_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1737_.Loading; _state.value = GenState_1737_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1737_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1737_.Success(searchUseCase(query)) } }
}
