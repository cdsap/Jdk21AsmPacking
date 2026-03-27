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

data class GenModel_3492_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3492_ {
    data class Load(val id: Long) : GenEvent_3492_()
    data class Update(val model: GenModel_3492_) : GenEvent_3492_()
    data class Delete(val id: Long) : GenEvent_3492_()
    data object Refresh : GenEvent_3492_()
    data class Search(val query: String) : GenEvent_3492_()
    data class Filter(val predicate: String) : GenEvent_3492_()
}

sealed class GenState_3492_ {
    data object Idle : GenState_3492_()
    data object Loading : GenState_3492_()
    data class Success(val items: List<GenModel_3492_>) : GenState_3492_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3492_()
    data class Partial(val items: List<GenModel_3492_>, val hasMore: Boolean) : GenState_3492_()
}

interface GenRepository_3492_ {
    suspend fun getAll(): List<GenModel_3492_>
    suspend fun getById(id: Long): GenModel_3492_?
    suspend fun save(model: GenModel_3492_): GenModel_3492_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3492_>
}

@Singleton
class GenRepositoryImpl_3492_ @Inject constructor() : GenRepository_3492_ {
    private val store = mutableMapOf<Long, GenModel_3492_>()
    override suspend fun getAll(): List<GenModel_3492_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3492_? = store[id]
    override suspend fun save(model: GenModel_3492_): GenModel_3492_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3492_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3492_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3492_ @Inject constructor(
    private val repository: GenRepositoryImpl_3492_
) : GenUseCase_3492_<Unit, List<GenModel_3492_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3492_> = repository.getAll()
}

class GenSaveUseCase_3492_ @Inject constructor(
    private val repository: GenRepositoryImpl_3492_
) : GenUseCase_3492_<GenModel_3492_, GenModel_3492_> {
    override suspend fun invoke(params: GenModel_3492_): GenModel_3492_ = repository.save(params)
}

class GenDeleteUseCase_3492_ @Inject constructor(
    private val repository: GenRepositoryImpl_3492_
) : GenUseCase_3492_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3492_ @Inject constructor(
    private val repository: GenRepositoryImpl_3492_
) : GenUseCase_3492_<String, List<GenModel_3492_>> {
    override suspend fun invoke(params: String): List<GenModel_3492_> = repository.search(params)
}

abstract class GenMapper_3492_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3492_ : GenMapper_3492_<GenModel_3492_, String>() {
    override fun map(input: GenModel_3492_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3492_ : GenMapper_3492_<String, GenModel_3492_>() {
    override fun map(input: String): GenModel_3492_ {
        val parts = input.split(":")
        return GenModel_3492_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3492_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3492_,
    private val saveUseCase: GenSaveUseCase_3492_,
    private val deleteUseCase: GenDeleteUseCase_3492_,
    private val searchUseCase: GenSearchUseCase_3492_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3492_>(GenState_3492_.Idle)
    val state: StateFlow<GenState_3492_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3492_) {
        when (event) {
            is GenEvent_3492_.Load -> loadAll()
            is GenEvent_3492_.Update -> save(event.model)
            is GenEvent_3492_.Delete -> delete(event.id)
            is GenEvent_3492_.Refresh -> loadAll()
            is GenEvent_3492_.Search -> search(event.query)
            is GenEvent_3492_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3492_.Loading; _state.value = GenState_3492_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3492_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3492_.Success(searchUseCase(query)) } }
}
