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

data class GenModel_2024_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2024_ {
    data class Load(val id: Long) : GenEvent_2024_()
    data class Update(val model: GenModel_2024_) : GenEvent_2024_()
    data class Delete(val id: Long) : GenEvent_2024_()
    data object Refresh : GenEvent_2024_()
    data class Search(val query: String) : GenEvent_2024_()
    data class Filter(val predicate: String) : GenEvent_2024_()
}

sealed class GenState_2024_ {
    data object Idle : GenState_2024_()
    data object Loading : GenState_2024_()
    data class Success(val items: List<GenModel_2024_>) : GenState_2024_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2024_()
    data class Partial(val items: List<GenModel_2024_>, val hasMore: Boolean) : GenState_2024_()
}

interface GenRepository_2024_ {
    suspend fun getAll(): List<GenModel_2024_>
    suspend fun getById(id: Long): GenModel_2024_?
    suspend fun save(model: GenModel_2024_): GenModel_2024_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2024_>
}

@Singleton
class GenRepositoryImpl_2024_ @Inject constructor() : GenRepository_2024_ {
    private val store = mutableMapOf<Long, GenModel_2024_>()
    override suspend fun getAll(): List<GenModel_2024_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2024_? = store[id]
    override suspend fun save(model: GenModel_2024_): GenModel_2024_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2024_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2024_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2024_ @Inject constructor(
    private val repository: GenRepositoryImpl_2024_
) : GenUseCase_2024_<Unit, List<GenModel_2024_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2024_> = repository.getAll()
}

class GenSaveUseCase_2024_ @Inject constructor(
    private val repository: GenRepositoryImpl_2024_
) : GenUseCase_2024_<GenModel_2024_, GenModel_2024_> {
    override suspend fun invoke(params: GenModel_2024_): GenModel_2024_ = repository.save(params)
}

class GenDeleteUseCase_2024_ @Inject constructor(
    private val repository: GenRepositoryImpl_2024_
) : GenUseCase_2024_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2024_ @Inject constructor(
    private val repository: GenRepositoryImpl_2024_
) : GenUseCase_2024_<String, List<GenModel_2024_>> {
    override suspend fun invoke(params: String): List<GenModel_2024_> = repository.search(params)
}

abstract class GenMapper_2024_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2024_ : GenMapper_2024_<GenModel_2024_, String>() {
    override fun map(input: GenModel_2024_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2024_ : GenMapper_2024_<String, GenModel_2024_>() {
    override fun map(input: String): GenModel_2024_ {
        val parts = input.split(":")
        return GenModel_2024_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2024_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2024_,
    private val saveUseCase: GenSaveUseCase_2024_,
    private val deleteUseCase: GenDeleteUseCase_2024_,
    private val searchUseCase: GenSearchUseCase_2024_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2024_>(GenState_2024_.Idle)
    val state: StateFlow<GenState_2024_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2024_) {
        when (event) {
            is GenEvent_2024_.Load -> loadAll()
            is GenEvent_2024_.Update -> save(event.model)
            is GenEvent_2024_.Delete -> delete(event.id)
            is GenEvent_2024_.Refresh -> loadAll()
            is GenEvent_2024_.Search -> search(event.query)
            is GenEvent_2024_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2024_.Loading; _state.value = GenState_2024_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2024_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2024_.Success(searchUseCase(query)) } }
}
