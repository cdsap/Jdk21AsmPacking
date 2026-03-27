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

data class GenModel_2543_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2543_ {
    data class Load(val id: Long) : GenEvent_2543_()
    data class Update(val model: GenModel_2543_) : GenEvent_2543_()
    data class Delete(val id: Long) : GenEvent_2543_()
    data object Refresh : GenEvent_2543_()
    data class Search(val query: String) : GenEvent_2543_()
    data class Filter(val predicate: String) : GenEvent_2543_()
}

sealed class GenState_2543_ {
    data object Idle : GenState_2543_()
    data object Loading : GenState_2543_()
    data class Success(val items: List<GenModel_2543_>) : GenState_2543_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2543_()
    data class Partial(val items: List<GenModel_2543_>, val hasMore: Boolean) : GenState_2543_()
}

interface GenRepository_2543_ {
    suspend fun getAll(): List<GenModel_2543_>
    suspend fun getById(id: Long): GenModel_2543_?
    suspend fun save(model: GenModel_2543_): GenModel_2543_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2543_>
}

@Singleton
class GenRepositoryImpl_2543_ @Inject constructor() : GenRepository_2543_ {
    private val store = mutableMapOf<Long, GenModel_2543_>()
    override suspend fun getAll(): List<GenModel_2543_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2543_? = store[id]
    override suspend fun save(model: GenModel_2543_): GenModel_2543_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2543_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2543_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2543_ @Inject constructor(
    private val repository: GenRepositoryImpl_2543_
) : GenUseCase_2543_<Unit, List<GenModel_2543_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2543_> = repository.getAll()
}

class GenSaveUseCase_2543_ @Inject constructor(
    private val repository: GenRepositoryImpl_2543_
) : GenUseCase_2543_<GenModel_2543_, GenModel_2543_> {
    override suspend fun invoke(params: GenModel_2543_): GenModel_2543_ = repository.save(params)
}

class GenDeleteUseCase_2543_ @Inject constructor(
    private val repository: GenRepositoryImpl_2543_
) : GenUseCase_2543_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2543_ @Inject constructor(
    private val repository: GenRepositoryImpl_2543_
) : GenUseCase_2543_<String, List<GenModel_2543_>> {
    override suspend fun invoke(params: String): List<GenModel_2543_> = repository.search(params)
}

abstract class GenMapper_2543_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2543_ : GenMapper_2543_<GenModel_2543_, String>() {
    override fun map(input: GenModel_2543_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2543_ : GenMapper_2543_<String, GenModel_2543_>() {
    override fun map(input: String): GenModel_2543_ {
        val parts = input.split(":")
        return GenModel_2543_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2543_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2543_,
    private val saveUseCase: GenSaveUseCase_2543_,
    private val deleteUseCase: GenDeleteUseCase_2543_,
    private val searchUseCase: GenSearchUseCase_2543_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2543_>(GenState_2543_.Idle)
    val state: StateFlow<GenState_2543_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2543_) {
        when (event) {
            is GenEvent_2543_.Load -> loadAll()
            is GenEvent_2543_.Update -> save(event.model)
            is GenEvent_2543_.Delete -> delete(event.id)
            is GenEvent_2543_.Refresh -> loadAll()
            is GenEvent_2543_.Search -> search(event.query)
            is GenEvent_2543_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2543_.Loading; _state.value = GenState_2543_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2543_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2543_.Success(searchUseCase(query)) } }
}
