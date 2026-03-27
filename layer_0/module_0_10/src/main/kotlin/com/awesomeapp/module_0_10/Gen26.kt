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

data class GenModel_26_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_26_ {
    data class Load(val id: Long) : GenEvent_26_()
    data class Update(val model: GenModel_26_) : GenEvent_26_()
    data class Delete(val id: Long) : GenEvent_26_()
    data object Refresh : GenEvent_26_()
    data class Search(val query: String) : GenEvent_26_()
    data class Filter(val predicate: String) : GenEvent_26_()
}

sealed class GenState_26_ {
    data object Idle : GenState_26_()
    data object Loading : GenState_26_()
    data class Success(val items: List<GenModel_26_>) : GenState_26_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_26_()
    data class Partial(val items: List<GenModel_26_>, val hasMore: Boolean) : GenState_26_()
}

interface GenRepository_26_ {
    suspend fun getAll(): List<GenModel_26_>
    suspend fun getById(id: Long): GenModel_26_?
    suspend fun save(model: GenModel_26_): GenModel_26_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_26_>
}

@Singleton
class GenRepositoryImpl_26_ @Inject constructor() : GenRepository_26_ {
    private val store = mutableMapOf<Long, GenModel_26_>()
    override suspend fun getAll(): List<GenModel_26_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_26_? = store[id]
    override suspend fun save(model: GenModel_26_): GenModel_26_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_26_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_26_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_26_ @Inject constructor(
    private val repository: GenRepositoryImpl_26_
) : GenUseCase_26_<Unit, List<GenModel_26_>> {
    override suspend fun invoke(params: Unit): List<GenModel_26_> = repository.getAll()
}

class GenSaveUseCase_26_ @Inject constructor(
    private val repository: GenRepositoryImpl_26_
) : GenUseCase_26_<GenModel_26_, GenModel_26_> {
    override suspend fun invoke(params: GenModel_26_): GenModel_26_ = repository.save(params)
}

class GenDeleteUseCase_26_ @Inject constructor(
    private val repository: GenRepositoryImpl_26_
) : GenUseCase_26_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_26_ @Inject constructor(
    private val repository: GenRepositoryImpl_26_
) : GenUseCase_26_<String, List<GenModel_26_>> {
    override suspend fun invoke(params: String): List<GenModel_26_> = repository.search(params)
}

abstract class GenMapper_26_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_26_ : GenMapper_26_<GenModel_26_, String>() {
    override fun map(input: GenModel_26_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_26_ : GenMapper_26_<String, GenModel_26_>() {
    override fun map(input: String): GenModel_26_ {
        val parts = input.split(":")
        return GenModel_26_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_26_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_26_,
    private val saveUseCase: GenSaveUseCase_26_,
    private val deleteUseCase: GenDeleteUseCase_26_,
    private val searchUseCase: GenSearchUseCase_26_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_26_>(GenState_26_.Idle)
    val state: StateFlow<GenState_26_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_26_) {
        when (event) {
            is GenEvent_26_.Load -> loadAll()
            is GenEvent_26_.Update -> save(event.model)
            is GenEvent_26_.Delete -> delete(event.id)
            is GenEvent_26_.Refresh -> loadAll()
            is GenEvent_26_.Search -> search(event.query)
            is GenEvent_26_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_26_.Loading; _state.value = GenState_26_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_26_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_26_.Success(searchUseCase(query)) } }
}
