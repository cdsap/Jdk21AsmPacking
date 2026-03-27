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

data class GenModel_344_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_344_ {
    data class Load(val id: Long) : GenEvent_344_()
    data class Update(val model: GenModel_344_) : GenEvent_344_()
    data class Delete(val id: Long) : GenEvent_344_()
    data object Refresh : GenEvent_344_()
    data class Search(val query: String) : GenEvent_344_()
    data class Filter(val predicate: String) : GenEvent_344_()
}

sealed class GenState_344_ {
    data object Idle : GenState_344_()
    data object Loading : GenState_344_()
    data class Success(val items: List<GenModel_344_>) : GenState_344_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_344_()
    data class Partial(val items: List<GenModel_344_>, val hasMore: Boolean) : GenState_344_()
}

interface GenRepository_344_ {
    suspend fun getAll(): List<GenModel_344_>
    suspend fun getById(id: Long): GenModel_344_?
    suspend fun save(model: GenModel_344_): GenModel_344_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_344_>
}

@Singleton
class GenRepositoryImpl_344_ @Inject constructor() : GenRepository_344_ {
    private val store = mutableMapOf<Long, GenModel_344_>()
    override suspend fun getAll(): List<GenModel_344_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_344_? = store[id]
    override suspend fun save(model: GenModel_344_): GenModel_344_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_344_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_344_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_344_ @Inject constructor(
    private val repository: GenRepositoryImpl_344_
) : GenUseCase_344_<Unit, List<GenModel_344_>> {
    override suspend fun invoke(params: Unit): List<GenModel_344_> = repository.getAll()
}

class GenSaveUseCase_344_ @Inject constructor(
    private val repository: GenRepositoryImpl_344_
) : GenUseCase_344_<GenModel_344_, GenModel_344_> {
    override suspend fun invoke(params: GenModel_344_): GenModel_344_ = repository.save(params)
}

class GenDeleteUseCase_344_ @Inject constructor(
    private val repository: GenRepositoryImpl_344_
) : GenUseCase_344_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_344_ @Inject constructor(
    private val repository: GenRepositoryImpl_344_
) : GenUseCase_344_<String, List<GenModel_344_>> {
    override suspend fun invoke(params: String): List<GenModel_344_> = repository.search(params)
}

abstract class GenMapper_344_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_344_ : GenMapper_344_<GenModel_344_, String>() {
    override fun map(input: GenModel_344_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_344_ : GenMapper_344_<String, GenModel_344_>() {
    override fun map(input: String): GenModel_344_ {
        val parts = input.split(":")
        return GenModel_344_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_344_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_344_,
    private val saveUseCase: GenSaveUseCase_344_,
    private val deleteUseCase: GenDeleteUseCase_344_,
    private val searchUseCase: GenSearchUseCase_344_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_344_>(GenState_344_.Idle)
    val state: StateFlow<GenState_344_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_344_) {
        when (event) {
            is GenEvent_344_.Load -> loadAll()
            is GenEvent_344_.Update -> save(event.model)
            is GenEvent_344_.Delete -> delete(event.id)
            is GenEvent_344_.Refresh -> loadAll()
            is GenEvent_344_.Search -> search(event.query)
            is GenEvent_344_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_344_.Loading; _state.value = GenState_344_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_344_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_344_.Success(searchUseCase(query)) } }
}
