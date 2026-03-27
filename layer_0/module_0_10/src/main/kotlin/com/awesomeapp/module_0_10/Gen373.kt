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

data class GenModel_373_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_373_ {
    data class Load(val id: Long) : GenEvent_373_()
    data class Update(val model: GenModel_373_) : GenEvent_373_()
    data class Delete(val id: Long) : GenEvent_373_()
    data object Refresh : GenEvent_373_()
    data class Search(val query: String) : GenEvent_373_()
    data class Filter(val predicate: String) : GenEvent_373_()
}

sealed class GenState_373_ {
    data object Idle : GenState_373_()
    data object Loading : GenState_373_()
    data class Success(val items: List<GenModel_373_>) : GenState_373_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_373_()
    data class Partial(val items: List<GenModel_373_>, val hasMore: Boolean) : GenState_373_()
}

interface GenRepository_373_ {
    suspend fun getAll(): List<GenModel_373_>
    suspend fun getById(id: Long): GenModel_373_?
    suspend fun save(model: GenModel_373_): GenModel_373_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_373_>
}

@Singleton
class GenRepositoryImpl_373_ @Inject constructor() : GenRepository_373_ {
    private val store = mutableMapOf<Long, GenModel_373_>()
    override suspend fun getAll(): List<GenModel_373_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_373_? = store[id]
    override suspend fun save(model: GenModel_373_): GenModel_373_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_373_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_373_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_373_ @Inject constructor(
    private val repository: GenRepositoryImpl_373_
) : GenUseCase_373_<Unit, List<GenModel_373_>> {
    override suspend fun invoke(params: Unit): List<GenModel_373_> = repository.getAll()
}

class GenSaveUseCase_373_ @Inject constructor(
    private val repository: GenRepositoryImpl_373_
) : GenUseCase_373_<GenModel_373_, GenModel_373_> {
    override suspend fun invoke(params: GenModel_373_): GenModel_373_ = repository.save(params)
}

class GenDeleteUseCase_373_ @Inject constructor(
    private val repository: GenRepositoryImpl_373_
) : GenUseCase_373_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_373_ @Inject constructor(
    private val repository: GenRepositoryImpl_373_
) : GenUseCase_373_<String, List<GenModel_373_>> {
    override suspend fun invoke(params: String): List<GenModel_373_> = repository.search(params)
}

abstract class GenMapper_373_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_373_ : GenMapper_373_<GenModel_373_, String>() {
    override fun map(input: GenModel_373_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_373_ : GenMapper_373_<String, GenModel_373_>() {
    override fun map(input: String): GenModel_373_ {
        val parts = input.split(":")
        return GenModel_373_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_373_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_373_,
    private val saveUseCase: GenSaveUseCase_373_,
    private val deleteUseCase: GenDeleteUseCase_373_,
    private val searchUseCase: GenSearchUseCase_373_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_373_>(GenState_373_.Idle)
    val state: StateFlow<GenState_373_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_373_) {
        when (event) {
            is GenEvent_373_.Load -> loadAll()
            is GenEvent_373_.Update -> save(event.model)
            is GenEvent_373_.Delete -> delete(event.id)
            is GenEvent_373_.Refresh -> loadAll()
            is GenEvent_373_.Search -> search(event.query)
            is GenEvent_373_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_373_.Loading; _state.value = GenState_373_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_373_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_373_.Success(searchUseCase(query)) } }
}
