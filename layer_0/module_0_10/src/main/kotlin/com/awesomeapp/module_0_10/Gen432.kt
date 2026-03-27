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

data class GenModel_432_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_432_ {
    data class Load(val id: Long) : GenEvent_432_()
    data class Update(val model: GenModel_432_) : GenEvent_432_()
    data class Delete(val id: Long) : GenEvent_432_()
    data object Refresh : GenEvent_432_()
    data class Search(val query: String) : GenEvent_432_()
    data class Filter(val predicate: String) : GenEvent_432_()
}

sealed class GenState_432_ {
    data object Idle : GenState_432_()
    data object Loading : GenState_432_()
    data class Success(val items: List<GenModel_432_>) : GenState_432_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_432_()
    data class Partial(val items: List<GenModel_432_>, val hasMore: Boolean) : GenState_432_()
}

interface GenRepository_432_ {
    suspend fun getAll(): List<GenModel_432_>
    suspend fun getById(id: Long): GenModel_432_?
    suspend fun save(model: GenModel_432_): GenModel_432_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_432_>
}

@Singleton
class GenRepositoryImpl_432_ @Inject constructor() : GenRepository_432_ {
    private val store = mutableMapOf<Long, GenModel_432_>()
    override suspend fun getAll(): List<GenModel_432_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_432_? = store[id]
    override suspend fun save(model: GenModel_432_): GenModel_432_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_432_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_432_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_432_ @Inject constructor(
    private val repository: GenRepositoryImpl_432_
) : GenUseCase_432_<Unit, List<GenModel_432_>> {
    override suspend fun invoke(params: Unit): List<GenModel_432_> = repository.getAll()
}

class GenSaveUseCase_432_ @Inject constructor(
    private val repository: GenRepositoryImpl_432_
) : GenUseCase_432_<GenModel_432_, GenModel_432_> {
    override suspend fun invoke(params: GenModel_432_): GenModel_432_ = repository.save(params)
}

class GenDeleteUseCase_432_ @Inject constructor(
    private val repository: GenRepositoryImpl_432_
) : GenUseCase_432_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_432_ @Inject constructor(
    private val repository: GenRepositoryImpl_432_
) : GenUseCase_432_<String, List<GenModel_432_>> {
    override suspend fun invoke(params: String): List<GenModel_432_> = repository.search(params)
}

abstract class GenMapper_432_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_432_ : GenMapper_432_<GenModel_432_, String>() {
    override fun map(input: GenModel_432_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_432_ : GenMapper_432_<String, GenModel_432_>() {
    override fun map(input: String): GenModel_432_ {
        val parts = input.split(":")
        return GenModel_432_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_432_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_432_,
    private val saveUseCase: GenSaveUseCase_432_,
    private val deleteUseCase: GenDeleteUseCase_432_,
    private val searchUseCase: GenSearchUseCase_432_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_432_>(GenState_432_.Idle)
    val state: StateFlow<GenState_432_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_432_) {
        when (event) {
            is GenEvent_432_.Load -> loadAll()
            is GenEvent_432_.Update -> save(event.model)
            is GenEvent_432_.Delete -> delete(event.id)
            is GenEvent_432_.Refresh -> loadAll()
            is GenEvent_432_.Search -> search(event.query)
            is GenEvent_432_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_432_.Loading; _state.value = GenState_432_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_432_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_432_.Success(searchUseCase(query)) } }
}
