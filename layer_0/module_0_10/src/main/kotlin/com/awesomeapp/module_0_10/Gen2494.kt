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

data class GenModel_2494_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2494_ {
    data class Load(val id: Long) : GenEvent_2494_()
    data class Update(val model: GenModel_2494_) : GenEvent_2494_()
    data class Delete(val id: Long) : GenEvent_2494_()
    data object Refresh : GenEvent_2494_()
    data class Search(val query: String) : GenEvent_2494_()
    data class Filter(val predicate: String) : GenEvent_2494_()
}

sealed class GenState_2494_ {
    data object Idle : GenState_2494_()
    data object Loading : GenState_2494_()
    data class Success(val items: List<GenModel_2494_>) : GenState_2494_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2494_()
    data class Partial(val items: List<GenModel_2494_>, val hasMore: Boolean) : GenState_2494_()
}

interface GenRepository_2494_ {
    suspend fun getAll(): List<GenModel_2494_>
    suspend fun getById(id: Long): GenModel_2494_?
    suspend fun save(model: GenModel_2494_): GenModel_2494_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2494_>
}

@Singleton
class GenRepositoryImpl_2494_ @Inject constructor() : GenRepository_2494_ {
    private val store = mutableMapOf<Long, GenModel_2494_>()
    override suspend fun getAll(): List<GenModel_2494_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2494_? = store[id]
    override suspend fun save(model: GenModel_2494_): GenModel_2494_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2494_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2494_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2494_ @Inject constructor(
    private val repository: GenRepositoryImpl_2494_
) : GenUseCase_2494_<Unit, List<GenModel_2494_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2494_> = repository.getAll()
}

class GenSaveUseCase_2494_ @Inject constructor(
    private val repository: GenRepositoryImpl_2494_
) : GenUseCase_2494_<GenModel_2494_, GenModel_2494_> {
    override suspend fun invoke(params: GenModel_2494_): GenModel_2494_ = repository.save(params)
}

class GenDeleteUseCase_2494_ @Inject constructor(
    private val repository: GenRepositoryImpl_2494_
) : GenUseCase_2494_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2494_ @Inject constructor(
    private val repository: GenRepositoryImpl_2494_
) : GenUseCase_2494_<String, List<GenModel_2494_>> {
    override suspend fun invoke(params: String): List<GenModel_2494_> = repository.search(params)
}

abstract class GenMapper_2494_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2494_ : GenMapper_2494_<GenModel_2494_, String>() {
    override fun map(input: GenModel_2494_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2494_ : GenMapper_2494_<String, GenModel_2494_>() {
    override fun map(input: String): GenModel_2494_ {
        val parts = input.split(":")
        return GenModel_2494_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2494_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2494_,
    private val saveUseCase: GenSaveUseCase_2494_,
    private val deleteUseCase: GenDeleteUseCase_2494_,
    private val searchUseCase: GenSearchUseCase_2494_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2494_>(GenState_2494_.Idle)
    val state: StateFlow<GenState_2494_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2494_) {
        when (event) {
            is GenEvent_2494_.Load -> loadAll()
            is GenEvent_2494_.Update -> save(event.model)
            is GenEvent_2494_.Delete -> delete(event.id)
            is GenEvent_2494_.Refresh -> loadAll()
            is GenEvent_2494_.Search -> search(event.query)
            is GenEvent_2494_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2494_.Loading; _state.value = GenState_2494_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2494_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2494_.Success(searchUseCase(query)) } }
}
