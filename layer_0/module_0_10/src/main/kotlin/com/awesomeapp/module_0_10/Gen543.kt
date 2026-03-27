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

data class GenModel_543_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_543_ {
    data class Load(val id: Long) : GenEvent_543_()
    data class Update(val model: GenModel_543_) : GenEvent_543_()
    data class Delete(val id: Long) : GenEvent_543_()
    data object Refresh : GenEvent_543_()
    data class Search(val query: String) : GenEvent_543_()
    data class Filter(val predicate: String) : GenEvent_543_()
}

sealed class GenState_543_ {
    data object Idle : GenState_543_()
    data object Loading : GenState_543_()
    data class Success(val items: List<GenModel_543_>) : GenState_543_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_543_()
    data class Partial(val items: List<GenModel_543_>, val hasMore: Boolean) : GenState_543_()
}

interface GenRepository_543_ {
    suspend fun getAll(): List<GenModel_543_>
    suspend fun getById(id: Long): GenModel_543_?
    suspend fun save(model: GenModel_543_): GenModel_543_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_543_>
}

@Singleton
class GenRepositoryImpl_543_ @Inject constructor() : GenRepository_543_ {
    private val store = mutableMapOf<Long, GenModel_543_>()
    override suspend fun getAll(): List<GenModel_543_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_543_? = store[id]
    override suspend fun save(model: GenModel_543_): GenModel_543_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_543_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_543_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_543_ @Inject constructor(
    private val repository: GenRepositoryImpl_543_
) : GenUseCase_543_<Unit, List<GenModel_543_>> {
    override suspend fun invoke(params: Unit): List<GenModel_543_> = repository.getAll()
}

class GenSaveUseCase_543_ @Inject constructor(
    private val repository: GenRepositoryImpl_543_
) : GenUseCase_543_<GenModel_543_, GenModel_543_> {
    override suspend fun invoke(params: GenModel_543_): GenModel_543_ = repository.save(params)
}

class GenDeleteUseCase_543_ @Inject constructor(
    private val repository: GenRepositoryImpl_543_
) : GenUseCase_543_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_543_ @Inject constructor(
    private val repository: GenRepositoryImpl_543_
) : GenUseCase_543_<String, List<GenModel_543_>> {
    override suspend fun invoke(params: String): List<GenModel_543_> = repository.search(params)
}

abstract class GenMapper_543_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_543_ : GenMapper_543_<GenModel_543_, String>() {
    override fun map(input: GenModel_543_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_543_ : GenMapper_543_<String, GenModel_543_>() {
    override fun map(input: String): GenModel_543_ {
        val parts = input.split(":")
        return GenModel_543_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_543_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_543_,
    private val saveUseCase: GenSaveUseCase_543_,
    private val deleteUseCase: GenDeleteUseCase_543_,
    private val searchUseCase: GenSearchUseCase_543_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_543_>(GenState_543_.Idle)
    val state: StateFlow<GenState_543_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_543_) {
        when (event) {
            is GenEvent_543_.Load -> loadAll()
            is GenEvent_543_.Update -> save(event.model)
            is GenEvent_543_.Delete -> delete(event.id)
            is GenEvent_543_.Refresh -> loadAll()
            is GenEvent_543_.Search -> search(event.query)
            is GenEvent_543_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_543_.Loading; _state.value = GenState_543_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_543_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_543_.Success(searchUseCase(query)) } }
}
