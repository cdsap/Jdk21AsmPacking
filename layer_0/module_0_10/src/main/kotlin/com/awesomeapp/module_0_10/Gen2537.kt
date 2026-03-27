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

data class GenModel_2537_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2537_ {
    data class Load(val id: Long) : GenEvent_2537_()
    data class Update(val model: GenModel_2537_) : GenEvent_2537_()
    data class Delete(val id: Long) : GenEvent_2537_()
    data object Refresh : GenEvent_2537_()
    data class Search(val query: String) : GenEvent_2537_()
    data class Filter(val predicate: String) : GenEvent_2537_()
}

sealed class GenState_2537_ {
    data object Idle : GenState_2537_()
    data object Loading : GenState_2537_()
    data class Success(val items: List<GenModel_2537_>) : GenState_2537_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2537_()
    data class Partial(val items: List<GenModel_2537_>, val hasMore: Boolean) : GenState_2537_()
}

interface GenRepository_2537_ {
    suspend fun getAll(): List<GenModel_2537_>
    suspend fun getById(id: Long): GenModel_2537_?
    suspend fun save(model: GenModel_2537_): GenModel_2537_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2537_>
}

@Singleton
class GenRepositoryImpl_2537_ @Inject constructor() : GenRepository_2537_ {
    private val store = mutableMapOf<Long, GenModel_2537_>()
    override suspend fun getAll(): List<GenModel_2537_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2537_? = store[id]
    override suspend fun save(model: GenModel_2537_): GenModel_2537_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2537_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2537_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2537_ @Inject constructor(
    private val repository: GenRepositoryImpl_2537_
) : GenUseCase_2537_<Unit, List<GenModel_2537_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2537_> = repository.getAll()
}

class GenSaveUseCase_2537_ @Inject constructor(
    private val repository: GenRepositoryImpl_2537_
) : GenUseCase_2537_<GenModel_2537_, GenModel_2537_> {
    override suspend fun invoke(params: GenModel_2537_): GenModel_2537_ = repository.save(params)
}

class GenDeleteUseCase_2537_ @Inject constructor(
    private val repository: GenRepositoryImpl_2537_
) : GenUseCase_2537_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2537_ @Inject constructor(
    private val repository: GenRepositoryImpl_2537_
) : GenUseCase_2537_<String, List<GenModel_2537_>> {
    override suspend fun invoke(params: String): List<GenModel_2537_> = repository.search(params)
}

abstract class GenMapper_2537_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2537_ : GenMapper_2537_<GenModel_2537_, String>() {
    override fun map(input: GenModel_2537_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2537_ : GenMapper_2537_<String, GenModel_2537_>() {
    override fun map(input: String): GenModel_2537_ {
        val parts = input.split(":")
        return GenModel_2537_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2537_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2537_,
    private val saveUseCase: GenSaveUseCase_2537_,
    private val deleteUseCase: GenDeleteUseCase_2537_,
    private val searchUseCase: GenSearchUseCase_2537_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2537_>(GenState_2537_.Idle)
    val state: StateFlow<GenState_2537_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2537_) {
        when (event) {
            is GenEvent_2537_.Load -> loadAll()
            is GenEvent_2537_.Update -> save(event.model)
            is GenEvent_2537_.Delete -> delete(event.id)
            is GenEvent_2537_.Refresh -> loadAll()
            is GenEvent_2537_.Search -> search(event.query)
            is GenEvent_2537_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2537_.Loading; _state.value = GenState_2537_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2537_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2537_.Success(searchUseCase(query)) } }
}
