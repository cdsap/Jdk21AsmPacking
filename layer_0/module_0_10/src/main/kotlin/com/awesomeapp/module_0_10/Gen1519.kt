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

data class GenModel_1519_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1519_ {
    data class Load(val id: Long) : GenEvent_1519_()
    data class Update(val model: GenModel_1519_) : GenEvent_1519_()
    data class Delete(val id: Long) : GenEvent_1519_()
    data object Refresh : GenEvent_1519_()
    data class Search(val query: String) : GenEvent_1519_()
    data class Filter(val predicate: String) : GenEvent_1519_()
}

sealed class GenState_1519_ {
    data object Idle : GenState_1519_()
    data object Loading : GenState_1519_()
    data class Success(val items: List<GenModel_1519_>) : GenState_1519_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1519_()
    data class Partial(val items: List<GenModel_1519_>, val hasMore: Boolean) : GenState_1519_()
}

interface GenRepository_1519_ {
    suspend fun getAll(): List<GenModel_1519_>
    suspend fun getById(id: Long): GenModel_1519_?
    suspend fun save(model: GenModel_1519_): GenModel_1519_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1519_>
}

@Singleton
class GenRepositoryImpl_1519_ @Inject constructor() : GenRepository_1519_ {
    private val store = mutableMapOf<Long, GenModel_1519_>()
    override suspend fun getAll(): List<GenModel_1519_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1519_? = store[id]
    override suspend fun save(model: GenModel_1519_): GenModel_1519_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1519_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1519_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1519_ @Inject constructor(
    private val repository: GenRepositoryImpl_1519_
) : GenUseCase_1519_<Unit, List<GenModel_1519_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1519_> = repository.getAll()
}

class GenSaveUseCase_1519_ @Inject constructor(
    private val repository: GenRepositoryImpl_1519_
) : GenUseCase_1519_<GenModel_1519_, GenModel_1519_> {
    override suspend fun invoke(params: GenModel_1519_): GenModel_1519_ = repository.save(params)
}

class GenDeleteUseCase_1519_ @Inject constructor(
    private val repository: GenRepositoryImpl_1519_
) : GenUseCase_1519_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1519_ @Inject constructor(
    private val repository: GenRepositoryImpl_1519_
) : GenUseCase_1519_<String, List<GenModel_1519_>> {
    override suspend fun invoke(params: String): List<GenModel_1519_> = repository.search(params)
}

abstract class GenMapper_1519_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1519_ : GenMapper_1519_<GenModel_1519_, String>() {
    override fun map(input: GenModel_1519_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1519_ : GenMapper_1519_<String, GenModel_1519_>() {
    override fun map(input: String): GenModel_1519_ {
        val parts = input.split(":")
        return GenModel_1519_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1519_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1519_,
    private val saveUseCase: GenSaveUseCase_1519_,
    private val deleteUseCase: GenDeleteUseCase_1519_,
    private val searchUseCase: GenSearchUseCase_1519_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1519_>(GenState_1519_.Idle)
    val state: StateFlow<GenState_1519_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1519_) {
        when (event) {
            is GenEvent_1519_.Load -> loadAll()
            is GenEvent_1519_.Update -> save(event.model)
            is GenEvent_1519_.Delete -> delete(event.id)
            is GenEvent_1519_.Refresh -> loadAll()
            is GenEvent_1519_.Search -> search(event.query)
            is GenEvent_1519_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1519_.Loading; _state.value = GenState_1519_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1519_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1519_.Success(searchUseCase(query)) } }
}
