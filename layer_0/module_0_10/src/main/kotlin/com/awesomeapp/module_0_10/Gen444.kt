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

data class GenModel_444_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_444_ {
    data class Load(val id: Long) : GenEvent_444_()
    data class Update(val model: GenModel_444_) : GenEvent_444_()
    data class Delete(val id: Long) : GenEvent_444_()
    data object Refresh : GenEvent_444_()
    data class Search(val query: String) : GenEvent_444_()
    data class Filter(val predicate: String) : GenEvent_444_()
}

sealed class GenState_444_ {
    data object Idle : GenState_444_()
    data object Loading : GenState_444_()
    data class Success(val items: List<GenModel_444_>) : GenState_444_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_444_()
    data class Partial(val items: List<GenModel_444_>, val hasMore: Boolean) : GenState_444_()
}

interface GenRepository_444_ {
    suspend fun getAll(): List<GenModel_444_>
    suspend fun getById(id: Long): GenModel_444_?
    suspend fun save(model: GenModel_444_): GenModel_444_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_444_>
}

@Singleton
class GenRepositoryImpl_444_ @Inject constructor() : GenRepository_444_ {
    private val store = mutableMapOf<Long, GenModel_444_>()
    override suspend fun getAll(): List<GenModel_444_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_444_? = store[id]
    override suspend fun save(model: GenModel_444_): GenModel_444_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_444_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_444_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_444_ @Inject constructor(
    private val repository: GenRepositoryImpl_444_
) : GenUseCase_444_<Unit, List<GenModel_444_>> {
    override suspend fun invoke(params: Unit): List<GenModel_444_> = repository.getAll()
}

class GenSaveUseCase_444_ @Inject constructor(
    private val repository: GenRepositoryImpl_444_
) : GenUseCase_444_<GenModel_444_, GenModel_444_> {
    override suspend fun invoke(params: GenModel_444_): GenModel_444_ = repository.save(params)
}

class GenDeleteUseCase_444_ @Inject constructor(
    private val repository: GenRepositoryImpl_444_
) : GenUseCase_444_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_444_ @Inject constructor(
    private val repository: GenRepositoryImpl_444_
) : GenUseCase_444_<String, List<GenModel_444_>> {
    override suspend fun invoke(params: String): List<GenModel_444_> = repository.search(params)
}

abstract class GenMapper_444_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_444_ : GenMapper_444_<GenModel_444_, String>() {
    override fun map(input: GenModel_444_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_444_ : GenMapper_444_<String, GenModel_444_>() {
    override fun map(input: String): GenModel_444_ {
        val parts = input.split(":")
        return GenModel_444_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_444_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_444_,
    private val saveUseCase: GenSaveUseCase_444_,
    private val deleteUseCase: GenDeleteUseCase_444_,
    private val searchUseCase: GenSearchUseCase_444_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_444_>(GenState_444_.Idle)
    val state: StateFlow<GenState_444_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_444_) {
        when (event) {
            is GenEvent_444_.Load -> loadAll()
            is GenEvent_444_.Update -> save(event.model)
            is GenEvent_444_.Delete -> delete(event.id)
            is GenEvent_444_.Refresh -> loadAll()
            is GenEvent_444_.Search -> search(event.query)
            is GenEvent_444_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_444_.Loading; _state.value = GenState_444_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_444_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_444_.Success(searchUseCase(query)) } }
}
