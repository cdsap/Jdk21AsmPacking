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

data class GenModel_2729_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2729_ {
    data class Load(val id: Long) : GenEvent_2729_()
    data class Update(val model: GenModel_2729_) : GenEvent_2729_()
    data class Delete(val id: Long) : GenEvent_2729_()
    data object Refresh : GenEvent_2729_()
    data class Search(val query: String) : GenEvent_2729_()
    data class Filter(val predicate: String) : GenEvent_2729_()
}

sealed class GenState_2729_ {
    data object Idle : GenState_2729_()
    data object Loading : GenState_2729_()
    data class Success(val items: List<GenModel_2729_>) : GenState_2729_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2729_()
    data class Partial(val items: List<GenModel_2729_>, val hasMore: Boolean) : GenState_2729_()
}

interface GenRepository_2729_ {
    suspend fun getAll(): List<GenModel_2729_>
    suspend fun getById(id: Long): GenModel_2729_?
    suspend fun save(model: GenModel_2729_): GenModel_2729_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2729_>
}

@Singleton
class GenRepositoryImpl_2729_ @Inject constructor() : GenRepository_2729_ {
    private val store = mutableMapOf<Long, GenModel_2729_>()
    override suspend fun getAll(): List<GenModel_2729_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2729_? = store[id]
    override suspend fun save(model: GenModel_2729_): GenModel_2729_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2729_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2729_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2729_ @Inject constructor(
    private val repository: GenRepositoryImpl_2729_
) : GenUseCase_2729_<Unit, List<GenModel_2729_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2729_> = repository.getAll()
}

class GenSaveUseCase_2729_ @Inject constructor(
    private val repository: GenRepositoryImpl_2729_
) : GenUseCase_2729_<GenModel_2729_, GenModel_2729_> {
    override suspend fun invoke(params: GenModel_2729_): GenModel_2729_ = repository.save(params)
}

class GenDeleteUseCase_2729_ @Inject constructor(
    private val repository: GenRepositoryImpl_2729_
) : GenUseCase_2729_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2729_ @Inject constructor(
    private val repository: GenRepositoryImpl_2729_
) : GenUseCase_2729_<String, List<GenModel_2729_>> {
    override suspend fun invoke(params: String): List<GenModel_2729_> = repository.search(params)
}

abstract class GenMapper_2729_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2729_ : GenMapper_2729_<GenModel_2729_, String>() {
    override fun map(input: GenModel_2729_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2729_ : GenMapper_2729_<String, GenModel_2729_>() {
    override fun map(input: String): GenModel_2729_ {
        val parts = input.split(":")
        return GenModel_2729_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2729_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2729_,
    private val saveUseCase: GenSaveUseCase_2729_,
    private val deleteUseCase: GenDeleteUseCase_2729_,
    private val searchUseCase: GenSearchUseCase_2729_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2729_>(GenState_2729_.Idle)
    val state: StateFlow<GenState_2729_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2729_) {
        when (event) {
            is GenEvent_2729_.Load -> loadAll()
            is GenEvent_2729_.Update -> save(event.model)
            is GenEvent_2729_.Delete -> delete(event.id)
            is GenEvent_2729_.Refresh -> loadAll()
            is GenEvent_2729_.Search -> search(event.query)
            is GenEvent_2729_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2729_.Loading; _state.value = GenState_2729_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2729_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2729_.Success(searchUseCase(query)) } }
}
