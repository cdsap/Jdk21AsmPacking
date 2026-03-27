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

data class GenModel_83_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_83_ {
    data class Load(val id: Long) : GenEvent_83_()
    data class Update(val model: GenModel_83_) : GenEvent_83_()
    data class Delete(val id: Long) : GenEvent_83_()
    data object Refresh : GenEvent_83_()
    data class Search(val query: String) : GenEvent_83_()
    data class Filter(val predicate: String) : GenEvent_83_()
}

sealed class GenState_83_ {
    data object Idle : GenState_83_()
    data object Loading : GenState_83_()
    data class Success(val items: List<GenModel_83_>) : GenState_83_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_83_()
    data class Partial(val items: List<GenModel_83_>, val hasMore: Boolean) : GenState_83_()
}

interface GenRepository_83_ {
    suspend fun getAll(): List<GenModel_83_>
    suspend fun getById(id: Long): GenModel_83_?
    suspend fun save(model: GenModel_83_): GenModel_83_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_83_>
}

@Singleton
class GenRepositoryImpl_83_ @Inject constructor() : GenRepository_83_ {
    private val store = mutableMapOf<Long, GenModel_83_>()
    override suspend fun getAll(): List<GenModel_83_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_83_? = store[id]
    override suspend fun save(model: GenModel_83_): GenModel_83_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_83_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_83_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_83_ @Inject constructor(
    private val repository: GenRepositoryImpl_83_
) : GenUseCase_83_<Unit, List<GenModel_83_>> {
    override suspend fun invoke(params: Unit): List<GenModel_83_> = repository.getAll()
}

class GenSaveUseCase_83_ @Inject constructor(
    private val repository: GenRepositoryImpl_83_
) : GenUseCase_83_<GenModel_83_, GenModel_83_> {
    override suspend fun invoke(params: GenModel_83_): GenModel_83_ = repository.save(params)
}

class GenDeleteUseCase_83_ @Inject constructor(
    private val repository: GenRepositoryImpl_83_
) : GenUseCase_83_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_83_ @Inject constructor(
    private val repository: GenRepositoryImpl_83_
) : GenUseCase_83_<String, List<GenModel_83_>> {
    override suspend fun invoke(params: String): List<GenModel_83_> = repository.search(params)
}

abstract class GenMapper_83_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_83_ : GenMapper_83_<GenModel_83_, String>() {
    override fun map(input: GenModel_83_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_83_ : GenMapper_83_<String, GenModel_83_>() {
    override fun map(input: String): GenModel_83_ {
        val parts = input.split(":")
        return GenModel_83_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_83_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_83_,
    private val saveUseCase: GenSaveUseCase_83_,
    private val deleteUseCase: GenDeleteUseCase_83_,
    private val searchUseCase: GenSearchUseCase_83_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_83_>(GenState_83_.Idle)
    val state: StateFlow<GenState_83_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_83_) {
        when (event) {
            is GenEvent_83_.Load -> loadAll()
            is GenEvent_83_.Update -> save(event.model)
            is GenEvent_83_.Delete -> delete(event.id)
            is GenEvent_83_.Refresh -> loadAll()
            is GenEvent_83_.Search -> search(event.query)
            is GenEvent_83_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_83_.Loading; _state.value = GenState_83_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_83_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_83_.Success(searchUseCase(query)) } }
}
