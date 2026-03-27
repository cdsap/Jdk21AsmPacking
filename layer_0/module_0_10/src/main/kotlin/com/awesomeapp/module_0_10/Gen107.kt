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

data class GenModel_107_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_107_ {
    data class Load(val id: Long) : GenEvent_107_()
    data class Update(val model: GenModel_107_) : GenEvent_107_()
    data class Delete(val id: Long) : GenEvent_107_()
    data object Refresh : GenEvent_107_()
    data class Search(val query: String) : GenEvent_107_()
    data class Filter(val predicate: String) : GenEvent_107_()
}

sealed class GenState_107_ {
    data object Idle : GenState_107_()
    data object Loading : GenState_107_()
    data class Success(val items: List<GenModel_107_>) : GenState_107_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_107_()
    data class Partial(val items: List<GenModel_107_>, val hasMore: Boolean) : GenState_107_()
}

interface GenRepository_107_ {
    suspend fun getAll(): List<GenModel_107_>
    suspend fun getById(id: Long): GenModel_107_?
    suspend fun save(model: GenModel_107_): GenModel_107_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_107_>
}

@Singleton
class GenRepositoryImpl_107_ @Inject constructor() : GenRepository_107_ {
    private val store = mutableMapOf<Long, GenModel_107_>()
    override suspend fun getAll(): List<GenModel_107_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_107_? = store[id]
    override suspend fun save(model: GenModel_107_): GenModel_107_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_107_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_107_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_107_ @Inject constructor(
    private val repository: GenRepositoryImpl_107_
) : GenUseCase_107_<Unit, List<GenModel_107_>> {
    override suspend fun invoke(params: Unit): List<GenModel_107_> = repository.getAll()
}

class GenSaveUseCase_107_ @Inject constructor(
    private val repository: GenRepositoryImpl_107_
) : GenUseCase_107_<GenModel_107_, GenModel_107_> {
    override suspend fun invoke(params: GenModel_107_): GenModel_107_ = repository.save(params)
}

class GenDeleteUseCase_107_ @Inject constructor(
    private val repository: GenRepositoryImpl_107_
) : GenUseCase_107_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_107_ @Inject constructor(
    private val repository: GenRepositoryImpl_107_
) : GenUseCase_107_<String, List<GenModel_107_>> {
    override suspend fun invoke(params: String): List<GenModel_107_> = repository.search(params)
}

abstract class GenMapper_107_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_107_ : GenMapper_107_<GenModel_107_, String>() {
    override fun map(input: GenModel_107_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_107_ : GenMapper_107_<String, GenModel_107_>() {
    override fun map(input: String): GenModel_107_ {
        val parts = input.split(":")
        return GenModel_107_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_107_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_107_,
    private val saveUseCase: GenSaveUseCase_107_,
    private val deleteUseCase: GenDeleteUseCase_107_,
    private val searchUseCase: GenSearchUseCase_107_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_107_>(GenState_107_.Idle)
    val state: StateFlow<GenState_107_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_107_) {
        when (event) {
            is GenEvent_107_.Load -> loadAll()
            is GenEvent_107_.Update -> save(event.model)
            is GenEvent_107_.Delete -> delete(event.id)
            is GenEvent_107_.Refresh -> loadAll()
            is GenEvent_107_.Search -> search(event.query)
            is GenEvent_107_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_107_.Loading; _state.value = GenState_107_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_107_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_107_.Success(searchUseCase(query)) } }
}
