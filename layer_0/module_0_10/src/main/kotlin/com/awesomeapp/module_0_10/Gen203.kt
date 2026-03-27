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

data class GenModel_203_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_203_ {
    data class Load(val id: Long) : GenEvent_203_()
    data class Update(val model: GenModel_203_) : GenEvent_203_()
    data class Delete(val id: Long) : GenEvent_203_()
    data object Refresh : GenEvent_203_()
    data class Search(val query: String) : GenEvent_203_()
    data class Filter(val predicate: String) : GenEvent_203_()
}

sealed class GenState_203_ {
    data object Idle : GenState_203_()
    data object Loading : GenState_203_()
    data class Success(val items: List<GenModel_203_>) : GenState_203_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_203_()
    data class Partial(val items: List<GenModel_203_>, val hasMore: Boolean) : GenState_203_()
}

interface GenRepository_203_ {
    suspend fun getAll(): List<GenModel_203_>
    suspend fun getById(id: Long): GenModel_203_?
    suspend fun save(model: GenModel_203_): GenModel_203_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_203_>
}

@Singleton
class GenRepositoryImpl_203_ @Inject constructor() : GenRepository_203_ {
    private val store = mutableMapOf<Long, GenModel_203_>()
    override suspend fun getAll(): List<GenModel_203_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_203_? = store[id]
    override suspend fun save(model: GenModel_203_): GenModel_203_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_203_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_203_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_203_ @Inject constructor(
    private val repository: GenRepositoryImpl_203_
) : GenUseCase_203_<Unit, List<GenModel_203_>> {
    override suspend fun invoke(params: Unit): List<GenModel_203_> = repository.getAll()
}

class GenSaveUseCase_203_ @Inject constructor(
    private val repository: GenRepositoryImpl_203_
) : GenUseCase_203_<GenModel_203_, GenModel_203_> {
    override suspend fun invoke(params: GenModel_203_): GenModel_203_ = repository.save(params)
}

class GenDeleteUseCase_203_ @Inject constructor(
    private val repository: GenRepositoryImpl_203_
) : GenUseCase_203_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_203_ @Inject constructor(
    private val repository: GenRepositoryImpl_203_
) : GenUseCase_203_<String, List<GenModel_203_>> {
    override suspend fun invoke(params: String): List<GenModel_203_> = repository.search(params)
}

abstract class GenMapper_203_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_203_ : GenMapper_203_<GenModel_203_, String>() {
    override fun map(input: GenModel_203_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_203_ : GenMapper_203_<String, GenModel_203_>() {
    override fun map(input: String): GenModel_203_ {
        val parts = input.split(":")
        return GenModel_203_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_203_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_203_,
    private val saveUseCase: GenSaveUseCase_203_,
    private val deleteUseCase: GenDeleteUseCase_203_,
    private val searchUseCase: GenSearchUseCase_203_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_203_>(GenState_203_.Idle)
    val state: StateFlow<GenState_203_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_203_) {
        when (event) {
            is GenEvent_203_.Load -> loadAll()
            is GenEvent_203_.Update -> save(event.model)
            is GenEvent_203_.Delete -> delete(event.id)
            is GenEvent_203_.Refresh -> loadAll()
            is GenEvent_203_.Search -> search(event.query)
            is GenEvent_203_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_203_.Loading; _state.value = GenState_203_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_203_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_203_.Success(searchUseCase(query)) } }
}
