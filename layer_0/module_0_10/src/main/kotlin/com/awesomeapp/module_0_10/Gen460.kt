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

data class GenModel_460_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_460_ {
    data class Load(val id: Long) : GenEvent_460_()
    data class Update(val model: GenModel_460_) : GenEvent_460_()
    data class Delete(val id: Long) : GenEvent_460_()
    data object Refresh : GenEvent_460_()
    data class Search(val query: String) : GenEvent_460_()
    data class Filter(val predicate: String) : GenEvent_460_()
}

sealed class GenState_460_ {
    data object Idle : GenState_460_()
    data object Loading : GenState_460_()
    data class Success(val items: List<GenModel_460_>) : GenState_460_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_460_()
    data class Partial(val items: List<GenModel_460_>, val hasMore: Boolean) : GenState_460_()
}

interface GenRepository_460_ {
    suspend fun getAll(): List<GenModel_460_>
    suspend fun getById(id: Long): GenModel_460_?
    suspend fun save(model: GenModel_460_): GenModel_460_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_460_>
}

@Singleton
class GenRepositoryImpl_460_ @Inject constructor() : GenRepository_460_ {
    private val store = mutableMapOf<Long, GenModel_460_>()
    override suspend fun getAll(): List<GenModel_460_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_460_? = store[id]
    override suspend fun save(model: GenModel_460_): GenModel_460_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_460_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_460_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_460_ @Inject constructor(
    private val repository: GenRepositoryImpl_460_
) : GenUseCase_460_<Unit, List<GenModel_460_>> {
    override suspend fun invoke(params: Unit): List<GenModel_460_> = repository.getAll()
}

class GenSaveUseCase_460_ @Inject constructor(
    private val repository: GenRepositoryImpl_460_
) : GenUseCase_460_<GenModel_460_, GenModel_460_> {
    override suspend fun invoke(params: GenModel_460_): GenModel_460_ = repository.save(params)
}

class GenDeleteUseCase_460_ @Inject constructor(
    private val repository: GenRepositoryImpl_460_
) : GenUseCase_460_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_460_ @Inject constructor(
    private val repository: GenRepositoryImpl_460_
) : GenUseCase_460_<String, List<GenModel_460_>> {
    override suspend fun invoke(params: String): List<GenModel_460_> = repository.search(params)
}

abstract class GenMapper_460_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_460_ : GenMapper_460_<GenModel_460_, String>() {
    override fun map(input: GenModel_460_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_460_ : GenMapper_460_<String, GenModel_460_>() {
    override fun map(input: String): GenModel_460_ {
        val parts = input.split(":")
        return GenModel_460_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_460_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_460_,
    private val saveUseCase: GenSaveUseCase_460_,
    private val deleteUseCase: GenDeleteUseCase_460_,
    private val searchUseCase: GenSearchUseCase_460_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_460_>(GenState_460_.Idle)
    val state: StateFlow<GenState_460_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_460_) {
        when (event) {
            is GenEvent_460_.Load -> loadAll()
            is GenEvent_460_.Update -> save(event.model)
            is GenEvent_460_.Delete -> delete(event.id)
            is GenEvent_460_.Refresh -> loadAll()
            is GenEvent_460_.Search -> search(event.query)
            is GenEvent_460_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_460_.Loading; _state.value = GenState_460_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_460_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_460_.Success(searchUseCase(query)) } }
}
