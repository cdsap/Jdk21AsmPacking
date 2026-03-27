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

data class GenModel_530_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_530_ {
    data class Load(val id: Long) : GenEvent_530_()
    data class Update(val model: GenModel_530_) : GenEvent_530_()
    data class Delete(val id: Long) : GenEvent_530_()
    data object Refresh : GenEvent_530_()
    data class Search(val query: String) : GenEvent_530_()
    data class Filter(val predicate: String) : GenEvent_530_()
}

sealed class GenState_530_ {
    data object Idle : GenState_530_()
    data object Loading : GenState_530_()
    data class Success(val items: List<GenModel_530_>) : GenState_530_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_530_()
    data class Partial(val items: List<GenModel_530_>, val hasMore: Boolean) : GenState_530_()
}

interface GenRepository_530_ {
    suspend fun getAll(): List<GenModel_530_>
    suspend fun getById(id: Long): GenModel_530_?
    suspend fun save(model: GenModel_530_): GenModel_530_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_530_>
}

@Singleton
class GenRepositoryImpl_530_ @Inject constructor() : GenRepository_530_ {
    private val store = mutableMapOf<Long, GenModel_530_>()
    override suspend fun getAll(): List<GenModel_530_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_530_? = store[id]
    override suspend fun save(model: GenModel_530_): GenModel_530_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_530_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_530_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_530_ @Inject constructor(
    private val repository: GenRepositoryImpl_530_
) : GenUseCase_530_<Unit, List<GenModel_530_>> {
    override suspend fun invoke(params: Unit): List<GenModel_530_> = repository.getAll()
}

class GenSaveUseCase_530_ @Inject constructor(
    private val repository: GenRepositoryImpl_530_
) : GenUseCase_530_<GenModel_530_, GenModel_530_> {
    override suspend fun invoke(params: GenModel_530_): GenModel_530_ = repository.save(params)
}

class GenDeleteUseCase_530_ @Inject constructor(
    private val repository: GenRepositoryImpl_530_
) : GenUseCase_530_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_530_ @Inject constructor(
    private val repository: GenRepositoryImpl_530_
) : GenUseCase_530_<String, List<GenModel_530_>> {
    override suspend fun invoke(params: String): List<GenModel_530_> = repository.search(params)
}

abstract class GenMapper_530_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_530_ : GenMapper_530_<GenModel_530_, String>() {
    override fun map(input: GenModel_530_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_530_ : GenMapper_530_<String, GenModel_530_>() {
    override fun map(input: String): GenModel_530_ {
        val parts = input.split(":")
        return GenModel_530_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_530_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_530_,
    private val saveUseCase: GenSaveUseCase_530_,
    private val deleteUseCase: GenDeleteUseCase_530_,
    private val searchUseCase: GenSearchUseCase_530_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_530_>(GenState_530_.Idle)
    val state: StateFlow<GenState_530_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_530_) {
        when (event) {
            is GenEvent_530_.Load -> loadAll()
            is GenEvent_530_.Update -> save(event.model)
            is GenEvent_530_.Delete -> delete(event.id)
            is GenEvent_530_.Refresh -> loadAll()
            is GenEvent_530_.Search -> search(event.query)
            is GenEvent_530_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_530_.Loading; _state.value = GenState_530_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_530_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_530_.Success(searchUseCase(query)) } }
}
