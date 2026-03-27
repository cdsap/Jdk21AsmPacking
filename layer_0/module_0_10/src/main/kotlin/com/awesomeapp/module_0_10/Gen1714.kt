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

data class GenModel_1714_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1714_ {
    data class Load(val id: Long) : GenEvent_1714_()
    data class Update(val model: GenModel_1714_) : GenEvent_1714_()
    data class Delete(val id: Long) : GenEvent_1714_()
    data object Refresh : GenEvent_1714_()
    data class Search(val query: String) : GenEvent_1714_()
    data class Filter(val predicate: String) : GenEvent_1714_()
}

sealed class GenState_1714_ {
    data object Idle : GenState_1714_()
    data object Loading : GenState_1714_()
    data class Success(val items: List<GenModel_1714_>) : GenState_1714_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1714_()
    data class Partial(val items: List<GenModel_1714_>, val hasMore: Boolean) : GenState_1714_()
}

interface GenRepository_1714_ {
    suspend fun getAll(): List<GenModel_1714_>
    suspend fun getById(id: Long): GenModel_1714_?
    suspend fun save(model: GenModel_1714_): GenModel_1714_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1714_>
}

@Singleton
class GenRepositoryImpl_1714_ @Inject constructor() : GenRepository_1714_ {
    private val store = mutableMapOf<Long, GenModel_1714_>()
    override suspend fun getAll(): List<GenModel_1714_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1714_? = store[id]
    override suspend fun save(model: GenModel_1714_): GenModel_1714_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1714_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1714_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1714_ @Inject constructor(
    private val repository: GenRepositoryImpl_1714_
) : GenUseCase_1714_<Unit, List<GenModel_1714_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1714_> = repository.getAll()
}

class GenSaveUseCase_1714_ @Inject constructor(
    private val repository: GenRepositoryImpl_1714_
) : GenUseCase_1714_<GenModel_1714_, GenModel_1714_> {
    override suspend fun invoke(params: GenModel_1714_): GenModel_1714_ = repository.save(params)
}

class GenDeleteUseCase_1714_ @Inject constructor(
    private val repository: GenRepositoryImpl_1714_
) : GenUseCase_1714_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1714_ @Inject constructor(
    private val repository: GenRepositoryImpl_1714_
) : GenUseCase_1714_<String, List<GenModel_1714_>> {
    override suspend fun invoke(params: String): List<GenModel_1714_> = repository.search(params)
}

abstract class GenMapper_1714_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1714_ : GenMapper_1714_<GenModel_1714_, String>() {
    override fun map(input: GenModel_1714_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1714_ : GenMapper_1714_<String, GenModel_1714_>() {
    override fun map(input: String): GenModel_1714_ {
        val parts = input.split(":")
        return GenModel_1714_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1714_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1714_,
    private val saveUseCase: GenSaveUseCase_1714_,
    private val deleteUseCase: GenDeleteUseCase_1714_,
    private val searchUseCase: GenSearchUseCase_1714_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1714_>(GenState_1714_.Idle)
    val state: StateFlow<GenState_1714_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1714_) {
        when (event) {
            is GenEvent_1714_.Load -> loadAll()
            is GenEvent_1714_.Update -> save(event.model)
            is GenEvent_1714_.Delete -> delete(event.id)
            is GenEvent_1714_.Refresh -> loadAll()
            is GenEvent_1714_.Search -> search(event.query)
            is GenEvent_1714_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1714_.Loading; _state.value = GenState_1714_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1714_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1714_.Success(searchUseCase(query)) } }
}
