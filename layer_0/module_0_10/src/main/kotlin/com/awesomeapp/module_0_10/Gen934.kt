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

data class GenModel_934_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_934_ {
    data class Load(val id: Long) : GenEvent_934_()
    data class Update(val model: GenModel_934_) : GenEvent_934_()
    data class Delete(val id: Long) : GenEvent_934_()
    data object Refresh : GenEvent_934_()
    data class Search(val query: String) : GenEvent_934_()
    data class Filter(val predicate: String) : GenEvent_934_()
}

sealed class GenState_934_ {
    data object Idle : GenState_934_()
    data object Loading : GenState_934_()
    data class Success(val items: List<GenModel_934_>) : GenState_934_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_934_()
    data class Partial(val items: List<GenModel_934_>, val hasMore: Boolean) : GenState_934_()
}

interface GenRepository_934_ {
    suspend fun getAll(): List<GenModel_934_>
    suspend fun getById(id: Long): GenModel_934_?
    suspend fun save(model: GenModel_934_): GenModel_934_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_934_>
}

@Singleton
class GenRepositoryImpl_934_ @Inject constructor() : GenRepository_934_ {
    private val store = mutableMapOf<Long, GenModel_934_>()
    override suspend fun getAll(): List<GenModel_934_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_934_? = store[id]
    override suspend fun save(model: GenModel_934_): GenModel_934_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_934_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_934_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_934_ @Inject constructor(
    private val repository: GenRepositoryImpl_934_
) : GenUseCase_934_<Unit, List<GenModel_934_>> {
    override suspend fun invoke(params: Unit): List<GenModel_934_> = repository.getAll()
}

class GenSaveUseCase_934_ @Inject constructor(
    private val repository: GenRepositoryImpl_934_
) : GenUseCase_934_<GenModel_934_, GenModel_934_> {
    override suspend fun invoke(params: GenModel_934_): GenModel_934_ = repository.save(params)
}

class GenDeleteUseCase_934_ @Inject constructor(
    private val repository: GenRepositoryImpl_934_
) : GenUseCase_934_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_934_ @Inject constructor(
    private val repository: GenRepositoryImpl_934_
) : GenUseCase_934_<String, List<GenModel_934_>> {
    override suspend fun invoke(params: String): List<GenModel_934_> = repository.search(params)
}

abstract class GenMapper_934_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_934_ : GenMapper_934_<GenModel_934_, String>() {
    override fun map(input: GenModel_934_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_934_ : GenMapper_934_<String, GenModel_934_>() {
    override fun map(input: String): GenModel_934_ {
        val parts = input.split(":")
        return GenModel_934_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_934_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_934_,
    private val saveUseCase: GenSaveUseCase_934_,
    private val deleteUseCase: GenDeleteUseCase_934_,
    private val searchUseCase: GenSearchUseCase_934_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_934_>(GenState_934_.Idle)
    val state: StateFlow<GenState_934_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_934_) {
        when (event) {
            is GenEvent_934_.Load -> loadAll()
            is GenEvent_934_.Update -> save(event.model)
            is GenEvent_934_.Delete -> delete(event.id)
            is GenEvent_934_.Refresh -> loadAll()
            is GenEvent_934_.Search -> search(event.query)
            is GenEvent_934_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_934_.Loading; _state.value = GenState_934_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_934_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_934_.Success(searchUseCase(query)) } }
}
