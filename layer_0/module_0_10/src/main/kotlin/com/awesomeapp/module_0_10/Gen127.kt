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

data class GenModel_127_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_127_ {
    data class Load(val id: Long) : GenEvent_127_()
    data class Update(val model: GenModel_127_) : GenEvent_127_()
    data class Delete(val id: Long) : GenEvent_127_()
    data object Refresh : GenEvent_127_()
    data class Search(val query: String) : GenEvent_127_()
    data class Filter(val predicate: String) : GenEvent_127_()
}

sealed class GenState_127_ {
    data object Idle : GenState_127_()
    data object Loading : GenState_127_()
    data class Success(val items: List<GenModel_127_>) : GenState_127_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_127_()
    data class Partial(val items: List<GenModel_127_>, val hasMore: Boolean) : GenState_127_()
}

interface GenRepository_127_ {
    suspend fun getAll(): List<GenModel_127_>
    suspend fun getById(id: Long): GenModel_127_?
    suspend fun save(model: GenModel_127_): GenModel_127_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_127_>
}

@Singleton
class GenRepositoryImpl_127_ @Inject constructor() : GenRepository_127_ {
    private val store = mutableMapOf<Long, GenModel_127_>()
    override suspend fun getAll(): List<GenModel_127_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_127_? = store[id]
    override suspend fun save(model: GenModel_127_): GenModel_127_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_127_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_127_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_127_ @Inject constructor(
    private val repository: GenRepositoryImpl_127_
) : GenUseCase_127_<Unit, List<GenModel_127_>> {
    override suspend fun invoke(params: Unit): List<GenModel_127_> = repository.getAll()
}

class GenSaveUseCase_127_ @Inject constructor(
    private val repository: GenRepositoryImpl_127_
) : GenUseCase_127_<GenModel_127_, GenModel_127_> {
    override suspend fun invoke(params: GenModel_127_): GenModel_127_ = repository.save(params)
}

class GenDeleteUseCase_127_ @Inject constructor(
    private val repository: GenRepositoryImpl_127_
) : GenUseCase_127_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_127_ @Inject constructor(
    private val repository: GenRepositoryImpl_127_
) : GenUseCase_127_<String, List<GenModel_127_>> {
    override suspend fun invoke(params: String): List<GenModel_127_> = repository.search(params)
}

abstract class GenMapper_127_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_127_ : GenMapper_127_<GenModel_127_, String>() {
    override fun map(input: GenModel_127_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_127_ : GenMapper_127_<String, GenModel_127_>() {
    override fun map(input: String): GenModel_127_ {
        val parts = input.split(":")
        return GenModel_127_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_127_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_127_,
    private val saveUseCase: GenSaveUseCase_127_,
    private val deleteUseCase: GenDeleteUseCase_127_,
    private val searchUseCase: GenSearchUseCase_127_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_127_>(GenState_127_.Idle)
    val state: StateFlow<GenState_127_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_127_) {
        when (event) {
            is GenEvent_127_.Load -> loadAll()
            is GenEvent_127_.Update -> save(event.model)
            is GenEvent_127_.Delete -> delete(event.id)
            is GenEvent_127_.Refresh -> loadAll()
            is GenEvent_127_.Search -> search(event.query)
            is GenEvent_127_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_127_.Loading; _state.value = GenState_127_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_127_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_127_.Success(searchUseCase(query)) } }
}
