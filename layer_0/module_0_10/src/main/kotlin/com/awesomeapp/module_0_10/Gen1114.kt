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

data class GenModel_1114_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1114_ {
    data class Load(val id: Long) : GenEvent_1114_()
    data class Update(val model: GenModel_1114_) : GenEvent_1114_()
    data class Delete(val id: Long) : GenEvent_1114_()
    data object Refresh : GenEvent_1114_()
    data class Search(val query: String) : GenEvent_1114_()
    data class Filter(val predicate: String) : GenEvent_1114_()
}

sealed class GenState_1114_ {
    data object Idle : GenState_1114_()
    data object Loading : GenState_1114_()
    data class Success(val items: List<GenModel_1114_>) : GenState_1114_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1114_()
    data class Partial(val items: List<GenModel_1114_>, val hasMore: Boolean) : GenState_1114_()
}

interface GenRepository_1114_ {
    suspend fun getAll(): List<GenModel_1114_>
    suspend fun getById(id: Long): GenModel_1114_?
    suspend fun save(model: GenModel_1114_): GenModel_1114_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1114_>
}

@Singleton
class GenRepositoryImpl_1114_ @Inject constructor() : GenRepository_1114_ {
    private val store = mutableMapOf<Long, GenModel_1114_>()
    override suspend fun getAll(): List<GenModel_1114_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1114_? = store[id]
    override suspend fun save(model: GenModel_1114_): GenModel_1114_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1114_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1114_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1114_ @Inject constructor(
    private val repository: GenRepositoryImpl_1114_
) : GenUseCase_1114_<Unit, List<GenModel_1114_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1114_> = repository.getAll()
}

class GenSaveUseCase_1114_ @Inject constructor(
    private val repository: GenRepositoryImpl_1114_
) : GenUseCase_1114_<GenModel_1114_, GenModel_1114_> {
    override suspend fun invoke(params: GenModel_1114_): GenModel_1114_ = repository.save(params)
}

class GenDeleteUseCase_1114_ @Inject constructor(
    private val repository: GenRepositoryImpl_1114_
) : GenUseCase_1114_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1114_ @Inject constructor(
    private val repository: GenRepositoryImpl_1114_
) : GenUseCase_1114_<String, List<GenModel_1114_>> {
    override suspend fun invoke(params: String): List<GenModel_1114_> = repository.search(params)
}

abstract class GenMapper_1114_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1114_ : GenMapper_1114_<GenModel_1114_, String>() {
    override fun map(input: GenModel_1114_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1114_ : GenMapper_1114_<String, GenModel_1114_>() {
    override fun map(input: String): GenModel_1114_ {
        val parts = input.split(":")
        return GenModel_1114_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1114_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1114_,
    private val saveUseCase: GenSaveUseCase_1114_,
    private val deleteUseCase: GenDeleteUseCase_1114_,
    private val searchUseCase: GenSearchUseCase_1114_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1114_>(GenState_1114_.Idle)
    val state: StateFlow<GenState_1114_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1114_) {
        when (event) {
            is GenEvent_1114_.Load -> loadAll()
            is GenEvent_1114_.Update -> save(event.model)
            is GenEvent_1114_.Delete -> delete(event.id)
            is GenEvent_1114_.Refresh -> loadAll()
            is GenEvent_1114_.Search -> search(event.query)
            is GenEvent_1114_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1114_.Loading; _state.value = GenState_1114_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1114_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1114_.Success(searchUseCase(query)) } }
}
