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

data class GenModel_524_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_524_ {
    data class Load(val id: Long) : GenEvent_524_()
    data class Update(val model: GenModel_524_) : GenEvent_524_()
    data class Delete(val id: Long) : GenEvent_524_()
    data object Refresh : GenEvent_524_()
    data class Search(val query: String) : GenEvent_524_()
    data class Filter(val predicate: String) : GenEvent_524_()
}

sealed class GenState_524_ {
    data object Idle : GenState_524_()
    data object Loading : GenState_524_()
    data class Success(val items: List<GenModel_524_>) : GenState_524_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_524_()
    data class Partial(val items: List<GenModel_524_>, val hasMore: Boolean) : GenState_524_()
}

interface GenRepository_524_ {
    suspend fun getAll(): List<GenModel_524_>
    suspend fun getById(id: Long): GenModel_524_?
    suspend fun save(model: GenModel_524_): GenModel_524_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_524_>
}

@Singleton
class GenRepositoryImpl_524_ @Inject constructor() : GenRepository_524_ {
    private val store = mutableMapOf<Long, GenModel_524_>()
    override suspend fun getAll(): List<GenModel_524_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_524_? = store[id]
    override suspend fun save(model: GenModel_524_): GenModel_524_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_524_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_524_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_524_ @Inject constructor(
    private val repository: GenRepositoryImpl_524_
) : GenUseCase_524_<Unit, List<GenModel_524_>> {
    override suspend fun invoke(params: Unit): List<GenModel_524_> = repository.getAll()
}

class GenSaveUseCase_524_ @Inject constructor(
    private val repository: GenRepositoryImpl_524_
) : GenUseCase_524_<GenModel_524_, GenModel_524_> {
    override suspend fun invoke(params: GenModel_524_): GenModel_524_ = repository.save(params)
}

class GenDeleteUseCase_524_ @Inject constructor(
    private val repository: GenRepositoryImpl_524_
) : GenUseCase_524_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_524_ @Inject constructor(
    private val repository: GenRepositoryImpl_524_
) : GenUseCase_524_<String, List<GenModel_524_>> {
    override suspend fun invoke(params: String): List<GenModel_524_> = repository.search(params)
}

abstract class GenMapper_524_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_524_ : GenMapper_524_<GenModel_524_, String>() {
    override fun map(input: GenModel_524_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_524_ : GenMapper_524_<String, GenModel_524_>() {
    override fun map(input: String): GenModel_524_ {
        val parts = input.split(":")
        return GenModel_524_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_524_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_524_,
    private val saveUseCase: GenSaveUseCase_524_,
    private val deleteUseCase: GenDeleteUseCase_524_,
    private val searchUseCase: GenSearchUseCase_524_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_524_>(GenState_524_.Idle)
    val state: StateFlow<GenState_524_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_524_) {
        when (event) {
            is GenEvent_524_.Load -> loadAll()
            is GenEvent_524_.Update -> save(event.model)
            is GenEvent_524_.Delete -> delete(event.id)
            is GenEvent_524_.Refresh -> loadAll()
            is GenEvent_524_.Search -> search(event.query)
            is GenEvent_524_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_524_.Loading; _state.value = GenState_524_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_524_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_524_.Success(searchUseCase(query)) } }
}
