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

data class GenModel_449_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_449_ {
    data class Load(val id: Long) : GenEvent_449_()
    data class Update(val model: GenModel_449_) : GenEvent_449_()
    data class Delete(val id: Long) : GenEvent_449_()
    data object Refresh : GenEvent_449_()
    data class Search(val query: String) : GenEvent_449_()
    data class Filter(val predicate: String) : GenEvent_449_()
}

sealed class GenState_449_ {
    data object Idle : GenState_449_()
    data object Loading : GenState_449_()
    data class Success(val items: List<GenModel_449_>) : GenState_449_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_449_()
    data class Partial(val items: List<GenModel_449_>, val hasMore: Boolean) : GenState_449_()
}

interface GenRepository_449_ {
    suspend fun getAll(): List<GenModel_449_>
    suspend fun getById(id: Long): GenModel_449_?
    suspend fun save(model: GenModel_449_): GenModel_449_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_449_>
}

@Singleton
class GenRepositoryImpl_449_ @Inject constructor() : GenRepository_449_ {
    private val store = mutableMapOf<Long, GenModel_449_>()
    override suspend fun getAll(): List<GenModel_449_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_449_? = store[id]
    override suspend fun save(model: GenModel_449_): GenModel_449_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_449_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_449_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_449_ @Inject constructor(
    private val repository: GenRepositoryImpl_449_
) : GenUseCase_449_<Unit, List<GenModel_449_>> {
    override suspend fun invoke(params: Unit): List<GenModel_449_> = repository.getAll()
}

class GenSaveUseCase_449_ @Inject constructor(
    private val repository: GenRepositoryImpl_449_
) : GenUseCase_449_<GenModel_449_, GenModel_449_> {
    override suspend fun invoke(params: GenModel_449_): GenModel_449_ = repository.save(params)
}

class GenDeleteUseCase_449_ @Inject constructor(
    private val repository: GenRepositoryImpl_449_
) : GenUseCase_449_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_449_ @Inject constructor(
    private val repository: GenRepositoryImpl_449_
) : GenUseCase_449_<String, List<GenModel_449_>> {
    override suspend fun invoke(params: String): List<GenModel_449_> = repository.search(params)
}

abstract class GenMapper_449_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_449_ : GenMapper_449_<GenModel_449_, String>() {
    override fun map(input: GenModel_449_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_449_ : GenMapper_449_<String, GenModel_449_>() {
    override fun map(input: String): GenModel_449_ {
        val parts = input.split(":")
        return GenModel_449_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_449_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_449_,
    private val saveUseCase: GenSaveUseCase_449_,
    private val deleteUseCase: GenDeleteUseCase_449_,
    private val searchUseCase: GenSearchUseCase_449_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_449_>(GenState_449_.Idle)
    val state: StateFlow<GenState_449_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_449_) {
        when (event) {
            is GenEvent_449_.Load -> loadAll()
            is GenEvent_449_.Update -> save(event.model)
            is GenEvent_449_.Delete -> delete(event.id)
            is GenEvent_449_.Refresh -> loadAll()
            is GenEvent_449_.Search -> search(event.query)
            is GenEvent_449_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_449_.Loading; _state.value = GenState_449_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_449_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_449_.Success(searchUseCase(query)) } }
}
