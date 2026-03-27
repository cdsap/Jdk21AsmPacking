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

data class GenModel_519_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_519_ {
    data class Load(val id: Long) : GenEvent_519_()
    data class Update(val model: GenModel_519_) : GenEvent_519_()
    data class Delete(val id: Long) : GenEvent_519_()
    data object Refresh : GenEvent_519_()
    data class Search(val query: String) : GenEvent_519_()
    data class Filter(val predicate: String) : GenEvent_519_()
}

sealed class GenState_519_ {
    data object Idle : GenState_519_()
    data object Loading : GenState_519_()
    data class Success(val items: List<GenModel_519_>) : GenState_519_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_519_()
    data class Partial(val items: List<GenModel_519_>, val hasMore: Boolean) : GenState_519_()
}

interface GenRepository_519_ {
    suspend fun getAll(): List<GenModel_519_>
    suspend fun getById(id: Long): GenModel_519_?
    suspend fun save(model: GenModel_519_): GenModel_519_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_519_>
}

@Singleton
class GenRepositoryImpl_519_ @Inject constructor() : GenRepository_519_ {
    private val store = mutableMapOf<Long, GenModel_519_>()
    override suspend fun getAll(): List<GenModel_519_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_519_? = store[id]
    override suspend fun save(model: GenModel_519_): GenModel_519_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_519_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_519_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_519_ @Inject constructor(
    private val repository: GenRepositoryImpl_519_
) : GenUseCase_519_<Unit, List<GenModel_519_>> {
    override suspend fun invoke(params: Unit): List<GenModel_519_> = repository.getAll()
}

class GenSaveUseCase_519_ @Inject constructor(
    private val repository: GenRepositoryImpl_519_
) : GenUseCase_519_<GenModel_519_, GenModel_519_> {
    override suspend fun invoke(params: GenModel_519_): GenModel_519_ = repository.save(params)
}

class GenDeleteUseCase_519_ @Inject constructor(
    private val repository: GenRepositoryImpl_519_
) : GenUseCase_519_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_519_ @Inject constructor(
    private val repository: GenRepositoryImpl_519_
) : GenUseCase_519_<String, List<GenModel_519_>> {
    override suspend fun invoke(params: String): List<GenModel_519_> = repository.search(params)
}

abstract class GenMapper_519_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_519_ : GenMapper_519_<GenModel_519_, String>() {
    override fun map(input: GenModel_519_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_519_ : GenMapper_519_<String, GenModel_519_>() {
    override fun map(input: String): GenModel_519_ {
        val parts = input.split(":")
        return GenModel_519_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_519_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_519_,
    private val saveUseCase: GenSaveUseCase_519_,
    private val deleteUseCase: GenDeleteUseCase_519_,
    private val searchUseCase: GenSearchUseCase_519_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_519_>(GenState_519_.Idle)
    val state: StateFlow<GenState_519_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_519_) {
        when (event) {
            is GenEvent_519_.Load -> loadAll()
            is GenEvent_519_.Update -> save(event.model)
            is GenEvent_519_.Delete -> delete(event.id)
            is GenEvent_519_.Refresh -> loadAll()
            is GenEvent_519_.Search -> search(event.query)
            is GenEvent_519_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_519_.Loading; _state.value = GenState_519_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_519_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_519_.Success(searchUseCase(query)) } }
}
