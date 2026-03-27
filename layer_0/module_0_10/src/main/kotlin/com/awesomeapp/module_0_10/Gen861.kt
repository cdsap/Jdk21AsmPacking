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

data class GenModel_861_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_861_ {
    data class Load(val id: Long) : GenEvent_861_()
    data class Update(val model: GenModel_861_) : GenEvent_861_()
    data class Delete(val id: Long) : GenEvent_861_()
    data object Refresh : GenEvent_861_()
    data class Search(val query: String) : GenEvent_861_()
    data class Filter(val predicate: String) : GenEvent_861_()
}

sealed class GenState_861_ {
    data object Idle : GenState_861_()
    data object Loading : GenState_861_()
    data class Success(val items: List<GenModel_861_>) : GenState_861_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_861_()
    data class Partial(val items: List<GenModel_861_>, val hasMore: Boolean) : GenState_861_()
}

interface GenRepository_861_ {
    suspend fun getAll(): List<GenModel_861_>
    suspend fun getById(id: Long): GenModel_861_?
    suspend fun save(model: GenModel_861_): GenModel_861_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_861_>
}

@Singleton
class GenRepositoryImpl_861_ @Inject constructor() : GenRepository_861_ {
    private val store = mutableMapOf<Long, GenModel_861_>()
    override suspend fun getAll(): List<GenModel_861_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_861_? = store[id]
    override suspend fun save(model: GenModel_861_): GenModel_861_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_861_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_861_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_861_ @Inject constructor(
    private val repository: GenRepositoryImpl_861_
) : GenUseCase_861_<Unit, List<GenModel_861_>> {
    override suspend fun invoke(params: Unit): List<GenModel_861_> = repository.getAll()
}

class GenSaveUseCase_861_ @Inject constructor(
    private val repository: GenRepositoryImpl_861_
) : GenUseCase_861_<GenModel_861_, GenModel_861_> {
    override suspend fun invoke(params: GenModel_861_): GenModel_861_ = repository.save(params)
}

class GenDeleteUseCase_861_ @Inject constructor(
    private val repository: GenRepositoryImpl_861_
) : GenUseCase_861_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_861_ @Inject constructor(
    private val repository: GenRepositoryImpl_861_
) : GenUseCase_861_<String, List<GenModel_861_>> {
    override suspend fun invoke(params: String): List<GenModel_861_> = repository.search(params)
}

abstract class GenMapper_861_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_861_ : GenMapper_861_<GenModel_861_, String>() {
    override fun map(input: GenModel_861_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_861_ : GenMapper_861_<String, GenModel_861_>() {
    override fun map(input: String): GenModel_861_ {
        val parts = input.split(":")
        return GenModel_861_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_861_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_861_,
    private val saveUseCase: GenSaveUseCase_861_,
    private val deleteUseCase: GenDeleteUseCase_861_,
    private val searchUseCase: GenSearchUseCase_861_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_861_>(GenState_861_.Idle)
    val state: StateFlow<GenState_861_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_861_) {
        when (event) {
            is GenEvent_861_.Load -> loadAll()
            is GenEvent_861_.Update -> save(event.model)
            is GenEvent_861_.Delete -> delete(event.id)
            is GenEvent_861_.Refresh -> loadAll()
            is GenEvent_861_.Search -> search(event.query)
            is GenEvent_861_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_861_.Loading; _state.value = GenState_861_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_861_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_861_.Success(searchUseCase(query)) } }
}
