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

data class GenModel_263_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_263_ {
    data class Load(val id: Long) : GenEvent_263_()
    data class Update(val model: GenModel_263_) : GenEvent_263_()
    data class Delete(val id: Long) : GenEvent_263_()
    data object Refresh : GenEvent_263_()
    data class Search(val query: String) : GenEvent_263_()
    data class Filter(val predicate: String) : GenEvent_263_()
}

sealed class GenState_263_ {
    data object Idle : GenState_263_()
    data object Loading : GenState_263_()
    data class Success(val items: List<GenModel_263_>) : GenState_263_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_263_()
    data class Partial(val items: List<GenModel_263_>, val hasMore: Boolean) : GenState_263_()
}

interface GenRepository_263_ {
    suspend fun getAll(): List<GenModel_263_>
    suspend fun getById(id: Long): GenModel_263_?
    suspend fun save(model: GenModel_263_): GenModel_263_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_263_>
}

@Singleton
class GenRepositoryImpl_263_ @Inject constructor() : GenRepository_263_ {
    private val store = mutableMapOf<Long, GenModel_263_>()
    override suspend fun getAll(): List<GenModel_263_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_263_? = store[id]
    override suspend fun save(model: GenModel_263_): GenModel_263_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_263_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_263_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_263_ @Inject constructor(
    private val repository: GenRepositoryImpl_263_
) : GenUseCase_263_<Unit, List<GenModel_263_>> {
    override suspend fun invoke(params: Unit): List<GenModel_263_> = repository.getAll()
}

class GenSaveUseCase_263_ @Inject constructor(
    private val repository: GenRepositoryImpl_263_
) : GenUseCase_263_<GenModel_263_, GenModel_263_> {
    override suspend fun invoke(params: GenModel_263_): GenModel_263_ = repository.save(params)
}

class GenDeleteUseCase_263_ @Inject constructor(
    private val repository: GenRepositoryImpl_263_
) : GenUseCase_263_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_263_ @Inject constructor(
    private val repository: GenRepositoryImpl_263_
) : GenUseCase_263_<String, List<GenModel_263_>> {
    override suspend fun invoke(params: String): List<GenModel_263_> = repository.search(params)
}

abstract class GenMapper_263_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_263_ : GenMapper_263_<GenModel_263_, String>() {
    override fun map(input: GenModel_263_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_263_ : GenMapper_263_<String, GenModel_263_>() {
    override fun map(input: String): GenModel_263_ {
        val parts = input.split(":")
        return GenModel_263_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_263_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_263_,
    private val saveUseCase: GenSaveUseCase_263_,
    private val deleteUseCase: GenDeleteUseCase_263_,
    private val searchUseCase: GenSearchUseCase_263_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_263_>(GenState_263_.Idle)
    val state: StateFlow<GenState_263_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_263_) {
        when (event) {
            is GenEvent_263_.Load -> loadAll()
            is GenEvent_263_.Update -> save(event.model)
            is GenEvent_263_.Delete -> delete(event.id)
            is GenEvent_263_.Refresh -> loadAll()
            is GenEvent_263_.Search -> search(event.query)
            is GenEvent_263_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_263_.Loading; _state.value = GenState_263_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_263_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_263_.Success(searchUseCase(query)) } }
}
