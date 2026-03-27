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

data class GenModel_633_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_633_ {
    data class Load(val id: Long) : GenEvent_633_()
    data class Update(val model: GenModel_633_) : GenEvent_633_()
    data class Delete(val id: Long) : GenEvent_633_()
    data object Refresh : GenEvent_633_()
    data class Search(val query: String) : GenEvent_633_()
    data class Filter(val predicate: String) : GenEvent_633_()
}

sealed class GenState_633_ {
    data object Idle : GenState_633_()
    data object Loading : GenState_633_()
    data class Success(val items: List<GenModel_633_>) : GenState_633_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_633_()
    data class Partial(val items: List<GenModel_633_>, val hasMore: Boolean) : GenState_633_()
}

interface GenRepository_633_ {
    suspend fun getAll(): List<GenModel_633_>
    suspend fun getById(id: Long): GenModel_633_?
    suspend fun save(model: GenModel_633_): GenModel_633_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_633_>
}

@Singleton
class GenRepositoryImpl_633_ @Inject constructor() : GenRepository_633_ {
    private val store = mutableMapOf<Long, GenModel_633_>()
    override suspend fun getAll(): List<GenModel_633_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_633_? = store[id]
    override suspend fun save(model: GenModel_633_): GenModel_633_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_633_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_633_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_633_ @Inject constructor(
    private val repository: GenRepositoryImpl_633_
) : GenUseCase_633_<Unit, List<GenModel_633_>> {
    override suspend fun invoke(params: Unit): List<GenModel_633_> = repository.getAll()
}

class GenSaveUseCase_633_ @Inject constructor(
    private val repository: GenRepositoryImpl_633_
) : GenUseCase_633_<GenModel_633_, GenModel_633_> {
    override suspend fun invoke(params: GenModel_633_): GenModel_633_ = repository.save(params)
}

class GenDeleteUseCase_633_ @Inject constructor(
    private val repository: GenRepositoryImpl_633_
) : GenUseCase_633_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_633_ @Inject constructor(
    private val repository: GenRepositoryImpl_633_
) : GenUseCase_633_<String, List<GenModel_633_>> {
    override suspend fun invoke(params: String): List<GenModel_633_> = repository.search(params)
}

abstract class GenMapper_633_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_633_ : GenMapper_633_<GenModel_633_, String>() {
    override fun map(input: GenModel_633_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_633_ : GenMapper_633_<String, GenModel_633_>() {
    override fun map(input: String): GenModel_633_ {
        val parts = input.split(":")
        return GenModel_633_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_633_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_633_,
    private val saveUseCase: GenSaveUseCase_633_,
    private val deleteUseCase: GenDeleteUseCase_633_,
    private val searchUseCase: GenSearchUseCase_633_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_633_>(GenState_633_.Idle)
    val state: StateFlow<GenState_633_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_633_) {
        when (event) {
            is GenEvent_633_.Load -> loadAll()
            is GenEvent_633_.Update -> save(event.model)
            is GenEvent_633_.Delete -> delete(event.id)
            is GenEvent_633_.Refresh -> loadAll()
            is GenEvent_633_.Search -> search(event.query)
            is GenEvent_633_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_633_.Loading; _state.value = GenState_633_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_633_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_633_.Success(searchUseCase(query)) } }
}
