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

data class GenModel_1278_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1278_ {
    data class Load(val id: Long) : GenEvent_1278_()
    data class Update(val model: GenModel_1278_) : GenEvent_1278_()
    data class Delete(val id: Long) : GenEvent_1278_()
    data object Refresh : GenEvent_1278_()
    data class Search(val query: String) : GenEvent_1278_()
    data class Filter(val predicate: String) : GenEvent_1278_()
}

sealed class GenState_1278_ {
    data object Idle : GenState_1278_()
    data object Loading : GenState_1278_()
    data class Success(val items: List<GenModel_1278_>) : GenState_1278_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1278_()
    data class Partial(val items: List<GenModel_1278_>, val hasMore: Boolean) : GenState_1278_()
}

interface GenRepository_1278_ {
    suspend fun getAll(): List<GenModel_1278_>
    suspend fun getById(id: Long): GenModel_1278_?
    suspend fun save(model: GenModel_1278_): GenModel_1278_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1278_>
}

@Singleton
class GenRepositoryImpl_1278_ @Inject constructor() : GenRepository_1278_ {
    private val store = mutableMapOf<Long, GenModel_1278_>()
    override suspend fun getAll(): List<GenModel_1278_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1278_? = store[id]
    override suspend fun save(model: GenModel_1278_): GenModel_1278_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1278_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1278_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1278_ @Inject constructor(
    private val repository: GenRepositoryImpl_1278_
) : GenUseCase_1278_<Unit, List<GenModel_1278_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1278_> = repository.getAll()
}

class GenSaveUseCase_1278_ @Inject constructor(
    private val repository: GenRepositoryImpl_1278_
) : GenUseCase_1278_<GenModel_1278_, GenModel_1278_> {
    override suspend fun invoke(params: GenModel_1278_): GenModel_1278_ = repository.save(params)
}

class GenDeleteUseCase_1278_ @Inject constructor(
    private val repository: GenRepositoryImpl_1278_
) : GenUseCase_1278_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1278_ @Inject constructor(
    private val repository: GenRepositoryImpl_1278_
) : GenUseCase_1278_<String, List<GenModel_1278_>> {
    override suspend fun invoke(params: String): List<GenModel_1278_> = repository.search(params)
}

abstract class GenMapper_1278_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1278_ : GenMapper_1278_<GenModel_1278_, String>() {
    override fun map(input: GenModel_1278_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1278_ : GenMapper_1278_<String, GenModel_1278_>() {
    override fun map(input: String): GenModel_1278_ {
        val parts = input.split(":")
        return GenModel_1278_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1278_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1278_,
    private val saveUseCase: GenSaveUseCase_1278_,
    private val deleteUseCase: GenDeleteUseCase_1278_,
    private val searchUseCase: GenSearchUseCase_1278_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1278_>(GenState_1278_.Idle)
    val state: StateFlow<GenState_1278_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1278_) {
        when (event) {
            is GenEvent_1278_.Load -> loadAll()
            is GenEvent_1278_.Update -> save(event.model)
            is GenEvent_1278_.Delete -> delete(event.id)
            is GenEvent_1278_.Refresh -> loadAll()
            is GenEvent_1278_.Search -> search(event.query)
            is GenEvent_1278_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1278_.Loading; _state.value = GenState_1278_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1278_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1278_.Success(searchUseCase(query)) } }
}
