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

data class GenModel_846_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_846_ {
    data class Load(val id: Long) : GenEvent_846_()
    data class Update(val model: GenModel_846_) : GenEvent_846_()
    data class Delete(val id: Long) : GenEvent_846_()
    data object Refresh : GenEvent_846_()
    data class Search(val query: String) : GenEvent_846_()
    data class Filter(val predicate: String) : GenEvent_846_()
}

sealed class GenState_846_ {
    data object Idle : GenState_846_()
    data object Loading : GenState_846_()
    data class Success(val items: List<GenModel_846_>) : GenState_846_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_846_()
    data class Partial(val items: List<GenModel_846_>, val hasMore: Boolean) : GenState_846_()
}

interface GenRepository_846_ {
    suspend fun getAll(): List<GenModel_846_>
    suspend fun getById(id: Long): GenModel_846_?
    suspend fun save(model: GenModel_846_): GenModel_846_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_846_>
}

@Singleton
class GenRepositoryImpl_846_ @Inject constructor() : GenRepository_846_ {
    private val store = mutableMapOf<Long, GenModel_846_>()
    override suspend fun getAll(): List<GenModel_846_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_846_? = store[id]
    override suspend fun save(model: GenModel_846_): GenModel_846_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_846_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_846_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_846_ @Inject constructor(
    private val repository: GenRepositoryImpl_846_
) : GenUseCase_846_<Unit, List<GenModel_846_>> {
    override suspend fun invoke(params: Unit): List<GenModel_846_> = repository.getAll()
}

class GenSaveUseCase_846_ @Inject constructor(
    private val repository: GenRepositoryImpl_846_
) : GenUseCase_846_<GenModel_846_, GenModel_846_> {
    override suspend fun invoke(params: GenModel_846_): GenModel_846_ = repository.save(params)
}

class GenDeleteUseCase_846_ @Inject constructor(
    private val repository: GenRepositoryImpl_846_
) : GenUseCase_846_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_846_ @Inject constructor(
    private val repository: GenRepositoryImpl_846_
) : GenUseCase_846_<String, List<GenModel_846_>> {
    override suspend fun invoke(params: String): List<GenModel_846_> = repository.search(params)
}

abstract class GenMapper_846_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_846_ : GenMapper_846_<GenModel_846_, String>() {
    override fun map(input: GenModel_846_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_846_ : GenMapper_846_<String, GenModel_846_>() {
    override fun map(input: String): GenModel_846_ {
        val parts = input.split(":")
        return GenModel_846_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_846_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_846_,
    private val saveUseCase: GenSaveUseCase_846_,
    private val deleteUseCase: GenDeleteUseCase_846_,
    private val searchUseCase: GenSearchUseCase_846_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_846_>(GenState_846_.Idle)
    val state: StateFlow<GenState_846_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_846_) {
        when (event) {
            is GenEvent_846_.Load -> loadAll()
            is GenEvent_846_.Update -> save(event.model)
            is GenEvent_846_.Delete -> delete(event.id)
            is GenEvent_846_.Refresh -> loadAll()
            is GenEvent_846_.Search -> search(event.query)
            is GenEvent_846_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_846_.Loading; _state.value = GenState_846_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_846_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_846_.Success(searchUseCase(query)) } }
}
