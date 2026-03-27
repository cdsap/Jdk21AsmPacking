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

data class GenModel_152_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_152_ {
    data class Load(val id: Long) : GenEvent_152_()
    data class Update(val model: GenModel_152_) : GenEvent_152_()
    data class Delete(val id: Long) : GenEvent_152_()
    data object Refresh : GenEvent_152_()
    data class Search(val query: String) : GenEvent_152_()
    data class Filter(val predicate: String) : GenEvent_152_()
}

sealed class GenState_152_ {
    data object Idle : GenState_152_()
    data object Loading : GenState_152_()
    data class Success(val items: List<GenModel_152_>) : GenState_152_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_152_()
    data class Partial(val items: List<GenModel_152_>, val hasMore: Boolean) : GenState_152_()
}

interface GenRepository_152_ {
    suspend fun getAll(): List<GenModel_152_>
    suspend fun getById(id: Long): GenModel_152_?
    suspend fun save(model: GenModel_152_): GenModel_152_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_152_>
}

@Singleton
class GenRepositoryImpl_152_ @Inject constructor() : GenRepository_152_ {
    private val store = mutableMapOf<Long, GenModel_152_>()
    override suspend fun getAll(): List<GenModel_152_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_152_? = store[id]
    override suspend fun save(model: GenModel_152_): GenModel_152_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_152_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_152_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_152_ @Inject constructor(
    private val repository: GenRepositoryImpl_152_
) : GenUseCase_152_<Unit, List<GenModel_152_>> {
    override suspend fun invoke(params: Unit): List<GenModel_152_> = repository.getAll()
}

class GenSaveUseCase_152_ @Inject constructor(
    private val repository: GenRepositoryImpl_152_
) : GenUseCase_152_<GenModel_152_, GenModel_152_> {
    override suspend fun invoke(params: GenModel_152_): GenModel_152_ = repository.save(params)
}

class GenDeleteUseCase_152_ @Inject constructor(
    private val repository: GenRepositoryImpl_152_
) : GenUseCase_152_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_152_ @Inject constructor(
    private val repository: GenRepositoryImpl_152_
) : GenUseCase_152_<String, List<GenModel_152_>> {
    override suspend fun invoke(params: String): List<GenModel_152_> = repository.search(params)
}

abstract class GenMapper_152_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_152_ : GenMapper_152_<GenModel_152_, String>() {
    override fun map(input: GenModel_152_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_152_ : GenMapper_152_<String, GenModel_152_>() {
    override fun map(input: String): GenModel_152_ {
        val parts = input.split(":")
        return GenModel_152_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_152_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_152_,
    private val saveUseCase: GenSaveUseCase_152_,
    private val deleteUseCase: GenDeleteUseCase_152_,
    private val searchUseCase: GenSearchUseCase_152_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_152_>(GenState_152_.Idle)
    val state: StateFlow<GenState_152_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_152_) {
        when (event) {
            is GenEvent_152_.Load -> loadAll()
            is GenEvent_152_.Update -> save(event.model)
            is GenEvent_152_.Delete -> delete(event.id)
            is GenEvent_152_.Refresh -> loadAll()
            is GenEvent_152_.Search -> search(event.query)
            is GenEvent_152_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_152_.Loading; _state.value = GenState_152_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_152_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_152_.Success(searchUseCase(query)) } }
}
