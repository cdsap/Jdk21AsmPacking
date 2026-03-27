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

data class GenModel_937_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_937_ {
    data class Load(val id: Long) : GenEvent_937_()
    data class Update(val model: GenModel_937_) : GenEvent_937_()
    data class Delete(val id: Long) : GenEvent_937_()
    data object Refresh : GenEvent_937_()
    data class Search(val query: String) : GenEvent_937_()
    data class Filter(val predicate: String) : GenEvent_937_()
}

sealed class GenState_937_ {
    data object Idle : GenState_937_()
    data object Loading : GenState_937_()
    data class Success(val items: List<GenModel_937_>) : GenState_937_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_937_()
    data class Partial(val items: List<GenModel_937_>, val hasMore: Boolean) : GenState_937_()
}

interface GenRepository_937_ {
    suspend fun getAll(): List<GenModel_937_>
    suspend fun getById(id: Long): GenModel_937_?
    suspend fun save(model: GenModel_937_): GenModel_937_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_937_>
}

@Singleton
class GenRepositoryImpl_937_ @Inject constructor() : GenRepository_937_ {
    private val store = mutableMapOf<Long, GenModel_937_>()
    override suspend fun getAll(): List<GenModel_937_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_937_? = store[id]
    override suspend fun save(model: GenModel_937_): GenModel_937_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_937_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_937_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_937_ @Inject constructor(
    private val repository: GenRepositoryImpl_937_
) : GenUseCase_937_<Unit, List<GenModel_937_>> {
    override suspend fun invoke(params: Unit): List<GenModel_937_> = repository.getAll()
}

class GenSaveUseCase_937_ @Inject constructor(
    private val repository: GenRepositoryImpl_937_
) : GenUseCase_937_<GenModel_937_, GenModel_937_> {
    override suspend fun invoke(params: GenModel_937_): GenModel_937_ = repository.save(params)
}

class GenDeleteUseCase_937_ @Inject constructor(
    private val repository: GenRepositoryImpl_937_
) : GenUseCase_937_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_937_ @Inject constructor(
    private val repository: GenRepositoryImpl_937_
) : GenUseCase_937_<String, List<GenModel_937_>> {
    override suspend fun invoke(params: String): List<GenModel_937_> = repository.search(params)
}

abstract class GenMapper_937_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_937_ : GenMapper_937_<GenModel_937_, String>() {
    override fun map(input: GenModel_937_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_937_ : GenMapper_937_<String, GenModel_937_>() {
    override fun map(input: String): GenModel_937_ {
        val parts = input.split(":")
        return GenModel_937_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_937_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_937_,
    private val saveUseCase: GenSaveUseCase_937_,
    private val deleteUseCase: GenDeleteUseCase_937_,
    private val searchUseCase: GenSearchUseCase_937_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_937_>(GenState_937_.Idle)
    val state: StateFlow<GenState_937_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_937_) {
        when (event) {
            is GenEvent_937_.Load -> loadAll()
            is GenEvent_937_.Update -> save(event.model)
            is GenEvent_937_.Delete -> delete(event.id)
            is GenEvent_937_.Refresh -> loadAll()
            is GenEvent_937_.Search -> search(event.query)
            is GenEvent_937_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_937_.Loading; _state.value = GenState_937_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_937_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_937_.Success(searchUseCase(query)) } }
}
