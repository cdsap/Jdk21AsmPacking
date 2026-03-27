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

data class GenModel_691_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_691_ {
    data class Load(val id: Long) : GenEvent_691_()
    data class Update(val model: GenModel_691_) : GenEvent_691_()
    data class Delete(val id: Long) : GenEvent_691_()
    data object Refresh : GenEvent_691_()
    data class Search(val query: String) : GenEvent_691_()
    data class Filter(val predicate: String) : GenEvent_691_()
}

sealed class GenState_691_ {
    data object Idle : GenState_691_()
    data object Loading : GenState_691_()
    data class Success(val items: List<GenModel_691_>) : GenState_691_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_691_()
    data class Partial(val items: List<GenModel_691_>, val hasMore: Boolean) : GenState_691_()
}

interface GenRepository_691_ {
    suspend fun getAll(): List<GenModel_691_>
    suspend fun getById(id: Long): GenModel_691_?
    suspend fun save(model: GenModel_691_): GenModel_691_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_691_>
}

@Singleton
class GenRepositoryImpl_691_ @Inject constructor() : GenRepository_691_ {
    private val store = mutableMapOf<Long, GenModel_691_>()
    override suspend fun getAll(): List<GenModel_691_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_691_? = store[id]
    override suspend fun save(model: GenModel_691_): GenModel_691_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_691_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_691_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_691_ @Inject constructor(
    private val repository: GenRepositoryImpl_691_
) : GenUseCase_691_<Unit, List<GenModel_691_>> {
    override suspend fun invoke(params: Unit): List<GenModel_691_> = repository.getAll()
}

class GenSaveUseCase_691_ @Inject constructor(
    private val repository: GenRepositoryImpl_691_
) : GenUseCase_691_<GenModel_691_, GenModel_691_> {
    override suspend fun invoke(params: GenModel_691_): GenModel_691_ = repository.save(params)
}

class GenDeleteUseCase_691_ @Inject constructor(
    private val repository: GenRepositoryImpl_691_
) : GenUseCase_691_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_691_ @Inject constructor(
    private val repository: GenRepositoryImpl_691_
) : GenUseCase_691_<String, List<GenModel_691_>> {
    override suspend fun invoke(params: String): List<GenModel_691_> = repository.search(params)
}

abstract class GenMapper_691_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_691_ : GenMapper_691_<GenModel_691_, String>() {
    override fun map(input: GenModel_691_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_691_ : GenMapper_691_<String, GenModel_691_>() {
    override fun map(input: String): GenModel_691_ {
        val parts = input.split(":")
        return GenModel_691_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_691_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_691_,
    private val saveUseCase: GenSaveUseCase_691_,
    private val deleteUseCase: GenDeleteUseCase_691_,
    private val searchUseCase: GenSearchUseCase_691_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_691_>(GenState_691_.Idle)
    val state: StateFlow<GenState_691_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_691_) {
        when (event) {
            is GenEvent_691_.Load -> loadAll()
            is GenEvent_691_.Update -> save(event.model)
            is GenEvent_691_.Delete -> delete(event.id)
            is GenEvent_691_.Refresh -> loadAll()
            is GenEvent_691_.Search -> search(event.query)
            is GenEvent_691_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_691_.Loading; _state.value = GenState_691_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_691_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_691_.Success(searchUseCase(query)) } }
}
