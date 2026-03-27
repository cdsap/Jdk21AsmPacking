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

data class GenModel_360_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_360_ {
    data class Load(val id: Long) : GenEvent_360_()
    data class Update(val model: GenModel_360_) : GenEvent_360_()
    data class Delete(val id: Long) : GenEvent_360_()
    data object Refresh : GenEvent_360_()
    data class Search(val query: String) : GenEvent_360_()
    data class Filter(val predicate: String) : GenEvent_360_()
}

sealed class GenState_360_ {
    data object Idle : GenState_360_()
    data object Loading : GenState_360_()
    data class Success(val items: List<GenModel_360_>) : GenState_360_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_360_()
    data class Partial(val items: List<GenModel_360_>, val hasMore: Boolean) : GenState_360_()
}

interface GenRepository_360_ {
    suspend fun getAll(): List<GenModel_360_>
    suspend fun getById(id: Long): GenModel_360_?
    suspend fun save(model: GenModel_360_): GenModel_360_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_360_>
}

@Singleton
class GenRepositoryImpl_360_ @Inject constructor() : GenRepository_360_ {
    private val store = mutableMapOf<Long, GenModel_360_>()
    override suspend fun getAll(): List<GenModel_360_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_360_? = store[id]
    override suspend fun save(model: GenModel_360_): GenModel_360_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_360_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_360_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_360_ @Inject constructor(
    private val repository: GenRepositoryImpl_360_
) : GenUseCase_360_<Unit, List<GenModel_360_>> {
    override suspend fun invoke(params: Unit): List<GenModel_360_> = repository.getAll()
}

class GenSaveUseCase_360_ @Inject constructor(
    private val repository: GenRepositoryImpl_360_
) : GenUseCase_360_<GenModel_360_, GenModel_360_> {
    override suspend fun invoke(params: GenModel_360_): GenModel_360_ = repository.save(params)
}

class GenDeleteUseCase_360_ @Inject constructor(
    private val repository: GenRepositoryImpl_360_
) : GenUseCase_360_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_360_ @Inject constructor(
    private val repository: GenRepositoryImpl_360_
) : GenUseCase_360_<String, List<GenModel_360_>> {
    override suspend fun invoke(params: String): List<GenModel_360_> = repository.search(params)
}

abstract class GenMapper_360_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_360_ : GenMapper_360_<GenModel_360_, String>() {
    override fun map(input: GenModel_360_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_360_ : GenMapper_360_<String, GenModel_360_>() {
    override fun map(input: String): GenModel_360_ {
        val parts = input.split(":")
        return GenModel_360_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_360_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_360_,
    private val saveUseCase: GenSaveUseCase_360_,
    private val deleteUseCase: GenDeleteUseCase_360_,
    private val searchUseCase: GenSearchUseCase_360_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_360_>(GenState_360_.Idle)
    val state: StateFlow<GenState_360_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_360_) {
        when (event) {
            is GenEvent_360_.Load -> loadAll()
            is GenEvent_360_.Update -> save(event.model)
            is GenEvent_360_.Delete -> delete(event.id)
            is GenEvent_360_.Refresh -> loadAll()
            is GenEvent_360_.Search -> search(event.query)
            is GenEvent_360_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_360_.Loading; _state.value = GenState_360_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_360_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_360_.Success(searchUseCase(query)) } }
}
