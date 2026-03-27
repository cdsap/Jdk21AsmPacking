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

data class GenModel_913_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_913_ {
    data class Load(val id: Long) : GenEvent_913_()
    data class Update(val model: GenModel_913_) : GenEvent_913_()
    data class Delete(val id: Long) : GenEvent_913_()
    data object Refresh : GenEvent_913_()
    data class Search(val query: String) : GenEvent_913_()
    data class Filter(val predicate: String) : GenEvent_913_()
}

sealed class GenState_913_ {
    data object Idle : GenState_913_()
    data object Loading : GenState_913_()
    data class Success(val items: List<GenModel_913_>) : GenState_913_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_913_()
    data class Partial(val items: List<GenModel_913_>, val hasMore: Boolean) : GenState_913_()
}

interface GenRepository_913_ {
    suspend fun getAll(): List<GenModel_913_>
    suspend fun getById(id: Long): GenModel_913_?
    suspend fun save(model: GenModel_913_): GenModel_913_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_913_>
}

@Singleton
class GenRepositoryImpl_913_ @Inject constructor() : GenRepository_913_ {
    private val store = mutableMapOf<Long, GenModel_913_>()
    override suspend fun getAll(): List<GenModel_913_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_913_? = store[id]
    override suspend fun save(model: GenModel_913_): GenModel_913_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_913_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_913_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_913_ @Inject constructor(
    private val repository: GenRepositoryImpl_913_
) : GenUseCase_913_<Unit, List<GenModel_913_>> {
    override suspend fun invoke(params: Unit): List<GenModel_913_> = repository.getAll()
}

class GenSaveUseCase_913_ @Inject constructor(
    private val repository: GenRepositoryImpl_913_
) : GenUseCase_913_<GenModel_913_, GenModel_913_> {
    override suspend fun invoke(params: GenModel_913_): GenModel_913_ = repository.save(params)
}

class GenDeleteUseCase_913_ @Inject constructor(
    private val repository: GenRepositoryImpl_913_
) : GenUseCase_913_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_913_ @Inject constructor(
    private val repository: GenRepositoryImpl_913_
) : GenUseCase_913_<String, List<GenModel_913_>> {
    override suspend fun invoke(params: String): List<GenModel_913_> = repository.search(params)
}

abstract class GenMapper_913_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_913_ : GenMapper_913_<GenModel_913_, String>() {
    override fun map(input: GenModel_913_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_913_ : GenMapper_913_<String, GenModel_913_>() {
    override fun map(input: String): GenModel_913_ {
        val parts = input.split(":")
        return GenModel_913_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_913_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_913_,
    private val saveUseCase: GenSaveUseCase_913_,
    private val deleteUseCase: GenDeleteUseCase_913_,
    private val searchUseCase: GenSearchUseCase_913_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_913_>(GenState_913_.Idle)
    val state: StateFlow<GenState_913_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_913_) {
        when (event) {
            is GenEvent_913_.Load -> loadAll()
            is GenEvent_913_.Update -> save(event.model)
            is GenEvent_913_.Delete -> delete(event.id)
            is GenEvent_913_.Refresh -> loadAll()
            is GenEvent_913_.Search -> search(event.query)
            is GenEvent_913_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_913_.Loading; _state.value = GenState_913_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_913_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_913_.Success(searchUseCase(query)) } }
}
