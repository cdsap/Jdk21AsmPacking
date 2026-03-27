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

data class GenModel_714_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_714_ {
    data class Load(val id: Long) : GenEvent_714_()
    data class Update(val model: GenModel_714_) : GenEvent_714_()
    data class Delete(val id: Long) : GenEvent_714_()
    data object Refresh : GenEvent_714_()
    data class Search(val query: String) : GenEvent_714_()
    data class Filter(val predicate: String) : GenEvent_714_()
}

sealed class GenState_714_ {
    data object Idle : GenState_714_()
    data object Loading : GenState_714_()
    data class Success(val items: List<GenModel_714_>) : GenState_714_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_714_()
    data class Partial(val items: List<GenModel_714_>, val hasMore: Boolean) : GenState_714_()
}

interface GenRepository_714_ {
    suspend fun getAll(): List<GenModel_714_>
    suspend fun getById(id: Long): GenModel_714_?
    suspend fun save(model: GenModel_714_): GenModel_714_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_714_>
}

@Singleton
class GenRepositoryImpl_714_ @Inject constructor() : GenRepository_714_ {
    private val store = mutableMapOf<Long, GenModel_714_>()
    override suspend fun getAll(): List<GenModel_714_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_714_? = store[id]
    override suspend fun save(model: GenModel_714_): GenModel_714_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_714_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_714_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_714_ @Inject constructor(
    private val repository: GenRepositoryImpl_714_
) : GenUseCase_714_<Unit, List<GenModel_714_>> {
    override suspend fun invoke(params: Unit): List<GenModel_714_> = repository.getAll()
}

class GenSaveUseCase_714_ @Inject constructor(
    private val repository: GenRepositoryImpl_714_
) : GenUseCase_714_<GenModel_714_, GenModel_714_> {
    override suspend fun invoke(params: GenModel_714_): GenModel_714_ = repository.save(params)
}

class GenDeleteUseCase_714_ @Inject constructor(
    private val repository: GenRepositoryImpl_714_
) : GenUseCase_714_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_714_ @Inject constructor(
    private val repository: GenRepositoryImpl_714_
) : GenUseCase_714_<String, List<GenModel_714_>> {
    override suspend fun invoke(params: String): List<GenModel_714_> = repository.search(params)
}

abstract class GenMapper_714_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_714_ : GenMapper_714_<GenModel_714_, String>() {
    override fun map(input: GenModel_714_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_714_ : GenMapper_714_<String, GenModel_714_>() {
    override fun map(input: String): GenModel_714_ {
        val parts = input.split(":")
        return GenModel_714_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_714_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_714_,
    private val saveUseCase: GenSaveUseCase_714_,
    private val deleteUseCase: GenDeleteUseCase_714_,
    private val searchUseCase: GenSearchUseCase_714_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_714_>(GenState_714_.Idle)
    val state: StateFlow<GenState_714_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_714_) {
        when (event) {
            is GenEvent_714_.Load -> loadAll()
            is GenEvent_714_.Update -> save(event.model)
            is GenEvent_714_.Delete -> delete(event.id)
            is GenEvent_714_.Refresh -> loadAll()
            is GenEvent_714_.Search -> search(event.query)
            is GenEvent_714_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_714_.Loading; _state.value = GenState_714_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_714_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_714_.Success(searchUseCase(query)) } }
}
