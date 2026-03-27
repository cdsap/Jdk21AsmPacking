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

data class GenModel_183_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_183_ {
    data class Load(val id: Long) : GenEvent_183_()
    data class Update(val model: GenModel_183_) : GenEvent_183_()
    data class Delete(val id: Long) : GenEvent_183_()
    data object Refresh : GenEvent_183_()
    data class Search(val query: String) : GenEvent_183_()
    data class Filter(val predicate: String) : GenEvent_183_()
}

sealed class GenState_183_ {
    data object Idle : GenState_183_()
    data object Loading : GenState_183_()
    data class Success(val items: List<GenModel_183_>) : GenState_183_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_183_()
    data class Partial(val items: List<GenModel_183_>, val hasMore: Boolean) : GenState_183_()
}

interface GenRepository_183_ {
    suspend fun getAll(): List<GenModel_183_>
    suspend fun getById(id: Long): GenModel_183_?
    suspend fun save(model: GenModel_183_): GenModel_183_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_183_>
}

@Singleton
class GenRepositoryImpl_183_ @Inject constructor() : GenRepository_183_ {
    private val store = mutableMapOf<Long, GenModel_183_>()
    override suspend fun getAll(): List<GenModel_183_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_183_? = store[id]
    override suspend fun save(model: GenModel_183_): GenModel_183_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_183_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_183_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_183_ @Inject constructor(
    private val repository: GenRepositoryImpl_183_
) : GenUseCase_183_<Unit, List<GenModel_183_>> {
    override suspend fun invoke(params: Unit): List<GenModel_183_> = repository.getAll()
}

class GenSaveUseCase_183_ @Inject constructor(
    private val repository: GenRepositoryImpl_183_
) : GenUseCase_183_<GenModel_183_, GenModel_183_> {
    override suspend fun invoke(params: GenModel_183_): GenModel_183_ = repository.save(params)
}

class GenDeleteUseCase_183_ @Inject constructor(
    private val repository: GenRepositoryImpl_183_
) : GenUseCase_183_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_183_ @Inject constructor(
    private val repository: GenRepositoryImpl_183_
) : GenUseCase_183_<String, List<GenModel_183_>> {
    override suspend fun invoke(params: String): List<GenModel_183_> = repository.search(params)
}

abstract class GenMapper_183_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_183_ : GenMapper_183_<GenModel_183_, String>() {
    override fun map(input: GenModel_183_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_183_ : GenMapper_183_<String, GenModel_183_>() {
    override fun map(input: String): GenModel_183_ {
        val parts = input.split(":")
        return GenModel_183_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_183_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_183_,
    private val saveUseCase: GenSaveUseCase_183_,
    private val deleteUseCase: GenDeleteUseCase_183_,
    private val searchUseCase: GenSearchUseCase_183_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_183_>(GenState_183_.Idle)
    val state: StateFlow<GenState_183_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_183_) {
        when (event) {
            is GenEvent_183_.Load -> loadAll()
            is GenEvent_183_.Update -> save(event.model)
            is GenEvent_183_.Delete -> delete(event.id)
            is GenEvent_183_.Refresh -> loadAll()
            is GenEvent_183_.Search -> search(event.query)
            is GenEvent_183_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_183_.Loading; _state.value = GenState_183_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_183_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_183_.Success(searchUseCase(query)) } }
}
