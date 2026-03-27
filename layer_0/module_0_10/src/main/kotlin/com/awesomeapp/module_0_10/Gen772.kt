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

data class GenModel_772_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_772_ {
    data class Load(val id: Long) : GenEvent_772_()
    data class Update(val model: GenModel_772_) : GenEvent_772_()
    data class Delete(val id: Long) : GenEvent_772_()
    data object Refresh : GenEvent_772_()
    data class Search(val query: String) : GenEvent_772_()
    data class Filter(val predicate: String) : GenEvent_772_()
}

sealed class GenState_772_ {
    data object Idle : GenState_772_()
    data object Loading : GenState_772_()
    data class Success(val items: List<GenModel_772_>) : GenState_772_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_772_()
    data class Partial(val items: List<GenModel_772_>, val hasMore: Boolean) : GenState_772_()
}

interface GenRepository_772_ {
    suspend fun getAll(): List<GenModel_772_>
    suspend fun getById(id: Long): GenModel_772_?
    suspend fun save(model: GenModel_772_): GenModel_772_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_772_>
}

@Singleton
class GenRepositoryImpl_772_ @Inject constructor() : GenRepository_772_ {
    private val store = mutableMapOf<Long, GenModel_772_>()
    override suspend fun getAll(): List<GenModel_772_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_772_? = store[id]
    override suspend fun save(model: GenModel_772_): GenModel_772_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_772_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_772_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_772_ @Inject constructor(
    private val repository: GenRepositoryImpl_772_
) : GenUseCase_772_<Unit, List<GenModel_772_>> {
    override suspend fun invoke(params: Unit): List<GenModel_772_> = repository.getAll()
}

class GenSaveUseCase_772_ @Inject constructor(
    private val repository: GenRepositoryImpl_772_
) : GenUseCase_772_<GenModel_772_, GenModel_772_> {
    override suspend fun invoke(params: GenModel_772_): GenModel_772_ = repository.save(params)
}

class GenDeleteUseCase_772_ @Inject constructor(
    private val repository: GenRepositoryImpl_772_
) : GenUseCase_772_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_772_ @Inject constructor(
    private val repository: GenRepositoryImpl_772_
) : GenUseCase_772_<String, List<GenModel_772_>> {
    override suspend fun invoke(params: String): List<GenModel_772_> = repository.search(params)
}

abstract class GenMapper_772_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_772_ : GenMapper_772_<GenModel_772_, String>() {
    override fun map(input: GenModel_772_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_772_ : GenMapper_772_<String, GenModel_772_>() {
    override fun map(input: String): GenModel_772_ {
        val parts = input.split(":")
        return GenModel_772_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_772_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_772_,
    private val saveUseCase: GenSaveUseCase_772_,
    private val deleteUseCase: GenDeleteUseCase_772_,
    private val searchUseCase: GenSearchUseCase_772_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_772_>(GenState_772_.Idle)
    val state: StateFlow<GenState_772_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_772_) {
        when (event) {
            is GenEvent_772_.Load -> loadAll()
            is GenEvent_772_.Update -> save(event.model)
            is GenEvent_772_.Delete -> delete(event.id)
            is GenEvent_772_.Refresh -> loadAll()
            is GenEvent_772_.Search -> search(event.query)
            is GenEvent_772_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_772_.Loading; _state.value = GenState_772_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_772_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_772_.Success(searchUseCase(query)) } }
}
