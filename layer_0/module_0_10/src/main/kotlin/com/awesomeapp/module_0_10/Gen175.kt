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

data class GenModel_175_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_175_ {
    data class Load(val id: Long) : GenEvent_175_()
    data class Update(val model: GenModel_175_) : GenEvent_175_()
    data class Delete(val id: Long) : GenEvent_175_()
    data object Refresh : GenEvent_175_()
    data class Search(val query: String) : GenEvent_175_()
    data class Filter(val predicate: String) : GenEvent_175_()
}

sealed class GenState_175_ {
    data object Idle : GenState_175_()
    data object Loading : GenState_175_()
    data class Success(val items: List<GenModel_175_>) : GenState_175_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_175_()
    data class Partial(val items: List<GenModel_175_>, val hasMore: Boolean) : GenState_175_()
}

interface GenRepository_175_ {
    suspend fun getAll(): List<GenModel_175_>
    suspend fun getById(id: Long): GenModel_175_?
    suspend fun save(model: GenModel_175_): GenModel_175_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_175_>
}

@Singleton
class GenRepositoryImpl_175_ @Inject constructor() : GenRepository_175_ {
    private val store = mutableMapOf<Long, GenModel_175_>()
    override suspend fun getAll(): List<GenModel_175_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_175_? = store[id]
    override suspend fun save(model: GenModel_175_): GenModel_175_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_175_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_175_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_175_ @Inject constructor(
    private val repository: GenRepositoryImpl_175_
) : GenUseCase_175_<Unit, List<GenModel_175_>> {
    override suspend fun invoke(params: Unit): List<GenModel_175_> = repository.getAll()
}

class GenSaveUseCase_175_ @Inject constructor(
    private val repository: GenRepositoryImpl_175_
) : GenUseCase_175_<GenModel_175_, GenModel_175_> {
    override suspend fun invoke(params: GenModel_175_): GenModel_175_ = repository.save(params)
}

class GenDeleteUseCase_175_ @Inject constructor(
    private val repository: GenRepositoryImpl_175_
) : GenUseCase_175_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_175_ @Inject constructor(
    private val repository: GenRepositoryImpl_175_
) : GenUseCase_175_<String, List<GenModel_175_>> {
    override suspend fun invoke(params: String): List<GenModel_175_> = repository.search(params)
}

abstract class GenMapper_175_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_175_ : GenMapper_175_<GenModel_175_, String>() {
    override fun map(input: GenModel_175_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_175_ : GenMapper_175_<String, GenModel_175_>() {
    override fun map(input: String): GenModel_175_ {
        val parts = input.split(":")
        return GenModel_175_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_175_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_175_,
    private val saveUseCase: GenSaveUseCase_175_,
    private val deleteUseCase: GenDeleteUseCase_175_,
    private val searchUseCase: GenSearchUseCase_175_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_175_>(GenState_175_.Idle)
    val state: StateFlow<GenState_175_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_175_) {
        when (event) {
            is GenEvent_175_.Load -> loadAll()
            is GenEvent_175_.Update -> save(event.model)
            is GenEvent_175_.Delete -> delete(event.id)
            is GenEvent_175_.Refresh -> loadAll()
            is GenEvent_175_.Search -> search(event.query)
            is GenEvent_175_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_175_.Loading; _state.value = GenState_175_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_175_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_175_.Success(searchUseCase(query)) } }
}
