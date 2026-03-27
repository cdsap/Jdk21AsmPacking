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

data class GenModel_1348_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1348_ {
    data class Load(val id: Long) : GenEvent_1348_()
    data class Update(val model: GenModel_1348_) : GenEvent_1348_()
    data class Delete(val id: Long) : GenEvent_1348_()
    data object Refresh : GenEvent_1348_()
    data class Search(val query: String) : GenEvent_1348_()
    data class Filter(val predicate: String) : GenEvent_1348_()
}

sealed class GenState_1348_ {
    data object Idle : GenState_1348_()
    data object Loading : GenState_1348_()
    data class Success(val items: List<GenModel_1348_>) : GenState_1348_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1348_()
    data class Partial(val items: List<GenModel_1348_>, val hasMore: Boolean) : GenState_1348_()
}

interface GenRepository_1348_ {
    suspend fun getAll(): List<GenModel_1348_>
    suspend fun getById(id: Long): GenModel_1348_?
    suspend fun save(model: GenModel_1348_): GenModel_1348_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1348_>
}

@Singleton
class GenRepositoryImpl_1348_ @Inject constructor() : GenRepository_1348_ {
    private val store = mutableMapOf<Long, GenModel_1348_>()
    override suspend fun getAll(): List<GenModel_1348_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1348_? = store[id]
    override suspend fun save(model: GenModel_1348_): GenModel_1348_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1348_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1348_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1348_ @Inject constructor(
    private val repository: GenRepositoryImpl_1348_
) : GenUseCase_1348_<Unit, List<GenModel_1348_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1348_> = repository.getAll()
}

class GenSaveUseCase_1348_ @Inject constructor(
    private val repository: GenRepositoryImpl_1348_
) : GenUseCase_1348_<GenModel_1348_, GenModel_1348_> {
    override suspend fun invoke(params: GenModel_1348_): GenModel_1348_ = repository.save(params)
}

class GenDeleteUseCase_1348_ @Inject constructor(
    private val repository: GenRepositoryImpl_1348_
) : GenUseCase_1348_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1348_ @Inject constructor(
    private val repository: GenRepositoryImpl_1348_
) : GenUseCase_1348_<String, List<GenModel_1348_>> {
    override suspend fun invoke(params: String): List<GenModel_1348_> = repository.search(params)
}

abstract class GenMapper_1348_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1348_ : GenMapper_1348_<GenModel_1348_, String>() {
    override fun map(input: GenModel_1348_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1348_ : GenMapper_1348_<String, GenModel_1348_>() {
    override fun map(input: String): GenModel_1348_ {
        val parts = input.split(":")
        return GenModel_1348_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1348_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1348_,
    private val saveUseCase: GenSaveUseCase_1348_,
    private val deleteUseCase: GenDeleteUseCase_1348_,
    private val searchUseCase: GenSearchUseCase_1348_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1348_>(GenState_1348_.Idle)
    val state: StateFlow<GenState_1348_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1348_) {
        when (event) {
            is GenEvent_1348_.Load -> loadAll()
            is GenEvent_1348_.Update -> save(event.model)
            is GenEvent_1348_.Delete -> delete(event.id)
            is GenEvent_1348_.Refresh -> loadAll()
            is GenEvent_1348_.Search -> search(event.query)
            is GenEvent_1348_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1348_.Loading; _state.value = GenState_1348_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1348_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1348_.Success(searchUseCase(query)) } }
}
