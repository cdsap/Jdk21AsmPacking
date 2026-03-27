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

data class GenModel_332_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_332_ {
    data class Load(val id: Long) : GenEvent_332_()
    data class Update(val model: GenModel_332_) : GenEvent_332_()
    data class Delete(val id: Long) : GenEvent_332_()
    data object Refresh : GenEvent_332_()
    data class Search(val query: String) : GenEvent_332_()
    data class Filter(val predicate: String) : GenEvent_332_()
}

sealed class GenState_332_ {
    data object Idle : GenState_332_()
    data object Loading : GenState_332_()
    data class Success(val items: List<GenModel_332_>) : GenState_332_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_332_()
    data class Partial(val items: List<GenModel_332_>, val hasMore: Boolean) : GenState_332_()
}

interface GenRepository_332_ {
    suspend fun getAll(): List<GenModel_332_>
    suspend fun getById(id: Long): GenModel_332_?
    suspend fun save(model: GenModel_332_): GenModel_332_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_332_>
}

@Singleton
class GenRepositoryImpl_332_ @Inject constructor() : GenRepository_332_ {
    private val store = mutableMapOf<Long, GenModel_332_>()
    override suspend fun getAll(): List<GenModel_332_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_332_? = store[id]
    override suspend fun save(model: GenModel_332_): GenModel_332_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_332_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_332_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_332_ @Inject constructor(
    private val repository: GenRepositoryImpl_332_
) : GenUseCase_332_<Unit, List<GenModel_332_>> {
    override suspend fun invoke(params: Unit): List<GenModel_332_> = repository.getAll()
}

class GenSaveUseCase_332_ @Inject constructor(
    private val repository: GenRepositoryImpl_332_
) : GenUseCase_332_<GenModel_332_, GenModel_332_> {
    override suspend fun invoke(params: GenModel_332_): GenModel_332_ = repository.save(params)
}

class GenDeleteUseCase_332_ @Inject constructor(
    private val repository: GenRepositoryImpl_332_
) : GenUseCase_332_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_332_ @Inject constructor(
    private val repository: GenRepositoryImpl_332_
) : GenUseCase_332_<String, List<GenModel_332_>> {
    override suspend fun invoke(params: String): List<GenModel_332_> = repository.search(params)
}

abstract class GenMapper_332_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_332_ : GenMapper_332_<GenModel_332_, String>() {
    override fun map(input: GenModel_332_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_332_ : GenMapper_332_<String, GenModel_332_>() {
    override fun map(input: String): GenModel_332_ {
        val parts = input.split(":")
        return GenModel_332_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_332_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_332_,
    private val saveUseCase: GenSaveUseCase_332_,
    private val deleteUseCase: GenDeleteUseCase_332_,
    private val searchUseCase: GenSearchUseCase_332_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_332_>(GenState_332_.Idle)
    val state: StateFlow<GenState_332_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_332_) {
        when (event) {
            is GenEvent_332_.Load -> loadAll()
            is GenEvent_332_.Update -> save(event.model)
            is GenEvent_332_.Delete -> delete(event.id)
            is GenEvent_332_.Refresh -> loadAll()
            is GenEvent_332_.Search -> search(event.query)
            is GenEvent_332_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_332_.Loading; _state.value = GenState_332_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_332_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_332_.Success(searchUseCase(query)) } }
}
