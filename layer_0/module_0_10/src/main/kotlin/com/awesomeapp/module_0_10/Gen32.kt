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

data class GenModel_32_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_32_ {
    data class Load(val id: Long) : GenEvent_32_()
    data class Update(val model: GenModel_32_) : GenEvent_32_()
    data class Delete(val id: Long) : GenEvent_32_()
    data object Refresh : GenEvent_32_()
    data class Search(val query: String) : GenEvent_32_()
    data class Filter(val predicate: String) : GenEvent_32_()
}

sealed class GenState_32_ {
    data object Idle : GenState_32_()
    data object Loading : GenState_32_()
    data class Success(val items: List<GenModel_32_>) : GenState_32_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_32_()
    data class Partial(val items: List<GenModel_32_>, val hasMore: Boolean) : GenState_32_()
}

interface GenRepository_32_ {
    suspend fun getAll(): List<GenModel_32_>
    suspend fun getById(id: Long): GenModel_32_?
    suspend fun save(model: GenModel_32_): GenModel_32_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_32_>
}

@Singleton
class GenRepositoryImpl_32_ @Inject constructor() : GenRepository_32_ {
    private val store = mutableMapOf<Long, GenModel_32_>()
    override suspend fun getAll(): List<GenModel_32_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_32_? = store[id]
    override suspend fun save(model: GenModel_32_): GenModel_32_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_32_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_32_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_32_ @Inject constructor(
    private val repository: GenRepositoryImpl_32_
) : GenUseCase_32_<Unit, List<GenModel_32_>> {
    override suspend fun invoke(params: Unit): List<GenModel_32_> = repository.getAll()
}

class GenSaveUseCase_32_ @Inject constructor(
    private val repository: GenRepositoryImpl_32_
) : GenUseCase_32_<GenModel_32_, GenModel_32_> {
    override suspend fun invoke(params: GenModel_32_): GenModel_32_ = repository.save(params)
}

class GenDeleteUseCase_32_ @Inject constructor(
    private val repository: GenRepositoryImpl_32_
) : GenUseCase_32_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_32_ @Inject constructor(
    private val repository: GenRepositoryImpl_32_
) : GenUseCase_32_<String, List<GenModel_32_>> {
    override suspend fun invoke(params: String): List<GenModel_32_> = repository.search(params)
}

abstract class GenMapper_32_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_32_ : GenMapper_32_<GenModel_32_, String>() {
    override fun map(input: GenModel_32_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_32_ : GenMapper_32_<String, GenModel_32_>() {
    override fun map(input: String): GenModel_32_ {
        val parts = input.split(":")
        return GenModel_32_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_32_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_32_,
    private val saveUseCase: GenSaveUseCase_32_,
    private val deleteUseCase: GenDeleteUseCase_32_,
    private val searchUseCase: GenSearchUseCase_32_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_32_>(GenState_32_.Idle)
    val state: StateFlow<GenState_32_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_32_) {
        when (event) {
            is GenEvent_32_.Load -> loadAll()
            is GenEvent_32_.Update -> save(event.model)
            is GenEvent_32_.Delete -> delete(event.id)
            is GenEvent_32_.Refresh -> loadAll()
            is GenEvent_32_.Search -> search(event.query)
            is GenEvent_32_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_32_.Loading; _state.value = GenState_32_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_32_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_32_.Success(searchUseCase(query)) } }
}
