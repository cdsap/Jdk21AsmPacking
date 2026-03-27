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

data class GenModel_222_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_222_ {
    data class Load(val id: Long) : GenEvent_222_()
    data class Update(val model: GenModel_222_) : GenEvent_222_()
    data class Delete(val id: Long) : GenEvent_222_()
    data object Refresh : GenEvent_222_()
    data class Search(val query: String) : GenEvent_222_()
    data class Filter(val predicate: String) : GenEvent_222_()
}

sealed class GenState_222_ {
    data object Idle : GenState_222_()
    data object Loading : GenState_222_()
    data class Success(val items: List<GenModel_222_>) : GenState_222_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_222_()
    data class Partial(val items: List<GenModel_222_>, val hasMore: Boolean) : GenState_222_()
}

interface GenRepository_222_ {
    suspend fun getAll(): List<GenModel_222_>
    suspend fun getById(id: Long): GenModel_222_?
    suspend fun save(model: GenModel_222_): GenModel_222_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_222_>
}

@Singleton
class GenRepositoryImpl_222_ @Inject constructor() : GenRepository_222_ {
    private val store = mutableMapOf<Long, GenModel_222_>()
    override suspend fun getAll(): List<GenModel_222_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_222_? = store[id]
    override suspend fun save(model: GenModel_222_): GenModel_222_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_222_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_222_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_222_ @Inject constructor(
    private val repository: GenRepositoryImpl_222_
) : GenUseCase_222_<Unit, List<GenModel_222_>> {
    override suspend fun invoke(params: Unit): List<GenModel_222_> = repository.getAll()
}

class GenSaveUseCase_222_ @Inject constructor(
    private val repository: GenRepositoryImpl_222_
) : GenUseCase_222_<GenModel_222_, GenModel_222_> {
    override suspend fun invoke(params: GenModel_222_): GenModel_222_ = repository.save(params)
}

class GenDeleteUseCase_222_ @Inject constructor(
    private val repository: GenRepositoryImpl_222_
) : GenUseCase_222_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_222_ @Inject constructor(
    private val repository: GenRepositoryImpl_222_
) : GenUseCase_222_<String, List<GenModel_222_>> {
    override suspend fun invoke(params: String): List<GenModel_222_> = repository.search(params)
}

abstract class GenMapper_222_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_222_ : GenMapper_222_<GenModel_222_, String>() {
    override fun map(input: GenModel_222_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_222_ : GenMapper_222_<String, GenModel_222_>() {
    override fun map(input: String): GenModel_222_ {
        val parts = input.split(":")
        return GenModel_222_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_222_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_222_,
    private val saveUseCase: GenSaveUseCase_222_,
    private val deleteUseCase: GenDeleteUseCase_222_,
    private val searchUseCase: GenSearchUseCase_222_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_222_>(GenState_222_.Idle)
    val state: StateFlow<GenState_222_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_222_) {
        when (event) {
            is GenEvent_222_.Load -> loadAll()
            is GenEvent_222_.Update -> save(event.model)
            is GenEvent_222_.Delete -> delete(event.id)
            is GenEvent_222_.Refresh -> loadAll()
            is GenEvent_222_.Search -> search(event.query)
            is GenEvent_222_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_222_.Loading; _state.value = GenState_222_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_222_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_222_.Success(searchUseCase(query)) } }
}
