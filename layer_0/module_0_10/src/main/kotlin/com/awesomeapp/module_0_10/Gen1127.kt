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

data class GenModel_1127_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1127_ {
    data class Load(val id: Long) : GenEvent_1127_()
    data class Update(val model: GenModel_1127_) : GenEvent_1127_()
    data class Delete(val id: Long) : GenEvent_1127_()
    data object Refresh : GenEvent_1127_()
    data class Search(val query: String) : GenEvent_1127_()
    data class Filter(val predicate: String) : GenEvent_1127_()
}

sealed class GenState_1127_ {
    data object Idle : GenState_1127_()
    data object Loading : GenState_1127_()
    data class Success(val items: List<GenModel_1127_>) : GenState_1127_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1127_()
    data class Partial(val items: List<GenModel_1127_>, val hasMore: Boolean) : GenState_1127_()
}

interface GenRepository_1127_ {
    suspend fun getAll(): List<GenModel_1127_>
    suspend fun getById(id: Long): GenModel_1127_?
    suspend fun save(model: GenModel_1127_): GenModel_1127_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1127_>
}

@Singleton
class GenRepositoryImpl_1127_ @Inject constructor() : GenRepository_1127_ {
    private val store = mutableMapOf<Long, GenModel_1127_>()
    override suspend fun getAll(): List<GenModel_1127_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1127_? = store[id]
    override suspend fun save(model: GenModel_1127_): GenModel_1127_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1127_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1127_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1127_ @Inject constructor(
    private val repository: GenRepositoryImpl_1127_
) : GenUseCase_1127_<Unit, List<GenModel_1127_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1127_> = repository.getAll()
}

class GenSaveUseCase_1127_ @Inject constructor(
    private val repository: GenRepositoryImpl_1127_
) : GenUseCase_1127_<GenModel_1127_, GenModel_1127_> {
    override suspend fun invoke(params: GenModel_1127_): GenModel_1127_ = repository.save(params)
}

class GenDeleteUseCase_1127_ @Inject constructor(
    private val repository: GenRepositoryImpl_1127_
) : GenUseCase_1127_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1127_ @Inject constructor(
    private val repository: GenRepositoryImpl_1127_
) : GenUseCase_1127_<String, List<GenModel_1127_>> {
    override suspend fun invoke(params: String): List<GenModel_1127_> = repository.search(params)
}

abstract class GenMapper_1127_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1127_ : GenMapper_1127_<GenModel_1127_, String>() {
    override fun map(input: GenModel_1127_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1127_ : GenMapper_1127_<String, GenModel_1127_>() {
    override fun map(input: String): GenModel_1127_ {
        val parts = input.split(":")
        return GenModel_1127_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1127_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1127_,
    private val saveUseCase: GenSaveUseCase_1127_,
    private val deleteUseCase: GenDeleteUseCase_1127_,
    private val searchUseCase: GenSearchUseCase_1127_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1127_>(GenState_1127_.Idle)
    val state: StateFlow<GenState_1127_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1127_) {
        when (event) {
            is GenEvent_1127_.Load -> loadAll()
            is GenEvent_1127_.Update -> save(event.model)
            is GenEvent_1127_.Delete -> delete(event.id)
            is GenEvent_1127_.Refresh -> loadAll()
            is GenEvent_1127_.Search -> search(event.query)
            is GenEvent_1127_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1127_.Loading; _state.value = GenState_1127_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1127_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1127_.Success(searchUseCase(query)) } }
}
