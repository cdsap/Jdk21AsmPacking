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

data class GenModel_1680_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1680_ {
    data class Load(val id: Long) : GenEvent_1680_()
    data class Update(val model: GenModel_1680_) : GenEvent_1680_()
    data class Delete(val id: Long) : GenEvent_1680_()
    data object Refresh : GenEvent_1680_()
    data class Search(val query: String) : GenEvent_1680_()
    data class Filter(val predicate: String) : GenEvent_1680_()
}

sealed class GenState_1680_ {
    data object Idle : GenState_1680_()
    data object Loading : GenState_1680_()
    data class Success(val items: List<GenModel_1680_>) : GenState_1680_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1680_()
    data class Partial(val items: List<GenModel_1680_>, val hasMore: Boolean) : GenState_1680_()
}

interface GenRepository_1680_ {
    suspend fun getAll(): List<GenModel_1680_>
    suspend fun getById(id: Long): GenModel_1680_?
    suspend fun save(model: GenModel_1680_): GenModel_1680_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1680_>
}

@Singleton
class GenRepositoryImpl_1680_ @Inject constructor() : GenRepository_1680_ {
    private val store = mutableMapOf<Long, GenModel_1680_>()
    override suspend fun getAll(): List<GenModel_1680_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1680_? = store[id]
    override suspend fun save(model: GenModel_1680_): GenModel_1680_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1680_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1680_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1680_ @Inject constructor(
    private val repository: GenRepositoryImpl_1680_
) : GenUseCase_1680_<Unit, List<GenModel_1680_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1680_> = repository.getAll()
}

class GenSaveUseCase_1680_ @Inject constructor(
    private val repository: GenRepositoryImpl_1680_
) : GenUseCase_1680_<GenModel_1680_, GenModel_1680_> {
    override suspend fun invoke(params: GenModel_1680_): GenModel_1680_ = repository.save(params)
}

class GenDeleteUseCase_1680_ @Inject constructor(
    private val repository: GenRepositoryImpl_1680_
) : GenUseCase_1680_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1680_ @Inject constructor(
    private val repository: GenRepositoryImpl_1680_
) : GenUseCase_1680_<String, List<GenModel_1680_>> {
    override suspend fun invoke(params: String): List<GenModel_1680_> = repository.search(params)
}

abstract class GenMapper_1680_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1680_ : GenMapper_1680_<GenModel_1680_, String>() {
    override fun map(input: GenModel_1680_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1680_ : GenMapper_1680_<String, GenModel_1680_>() {
    override fun map(input: String): GenModel_1680_ {
        val parts = input.split(":")
        return GenModel_1680_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1680_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1680_,
    private val saveUseCase: GenSaveUseCase_1680_,
    private val deleteUseCase: GenDeleteUseCase_1680_,
    private val searchUseCase: GenSearchUseCase_1680_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1680_>(GenState_1680_.Idle)
    val state: StateFlow<GenState_1680_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1680_) {
        when (event) {
            is GenEvent_1680_.Load -> loadAll()
            is GenEvent_1680_.Update -> save(event.model)
            is GenEvent_1680_.Delete -> delete(event.id)
            is GenEvent_1680_.Refresh -> loadAll()
            is GenEvent_1680_.Search -> search(event.query)
            is GenEvent_1680_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1680_.Loading; _state.value = GenState_1680_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1680_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1680_.Success(searchUseCase(query)) } }
}
