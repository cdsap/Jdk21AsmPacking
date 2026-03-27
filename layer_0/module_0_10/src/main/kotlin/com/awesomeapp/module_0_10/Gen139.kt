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

data class GenModel_139_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_139_ {
    data class Load(val id: Long) : GenEvent_139_()
    data class Update(val model: GenModel_139_) : GenEvent_139_()
    data class Delete(val id: Long) : GenEvent_139_()
    data object Refresh : GenEvent_139_()
    data class Search(val query: String) : GenEvent_139_()
    data class Filter(val predicate: String) : GenEvent_139_()
}

sealed class GenState_139_ {
    data object Idle : GenState_139_()
    data object Loading : GenState_139_()
    data class Success(val items: List<GenModel_139_>) : GenState_139_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_139_()
    data class Partial(val items: List<GenModel_139_>, val hasMore: Boolean) : GenState_139_()
}

interface GenRepository_139_ {
    suspend fun getAll(): List<GenModel_139_>
    suspend fun getById(id: Long): GenModel_139_?
    suspend fun save(model: GenModel_139_): GenModel_139_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_139_>
}

@Singleton
class GenRepositoryImpl_139_ @Inject constructor() : GenRepository_139_ {
    private val store = mutableMapOf<Long, GenModel_139_>()
    override suspend fun getAll(): List<GenModel_139_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_139_? = store[id]
    override suspend fun save(model: GenModel_139_): GenModel_139_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_139_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_139_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_139_ @Inject constructor(
    private val repository: GenRepositoryImpl_139_
) : GenUseCase_139_<Unit, List<GenModel_139_>> {
    override suspend fun invoke(params: Unit): List<GenModel_139_> = repository.getAll()
}

class GenSaveUseCase_139_ @Inject constructor(
    private val repository: GenRepositoryImpl_139_
) : GenUseCase_139_<GenModel_139_, GenModel_139_> {
    override suspend fun invoke(params: GenModel_139_): GenModel_139_ = repository.save(params)
}

class GenDeleteUseCase_139_ @Inject constructor(
    private val repository: GenRepositoryImpl_139_
) : GenUseCase_139_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_139_ @Inject constructor(
    private val repository: GenRepositoryImpl_139_
) : GenUseCase_139_<String, List<GenModel_139_>> {
    override suspend fun invoke(params: String): List<GenModel_139_> = repository.search(params)
}

abstract class GenMapper_139_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_139_ : GenMapper_139_<GenModel_139_, String>() {
    override fun map(input: GenModel_139_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_139_ : GenMapper_139_<String, GenModel_139_>() {
    override fun map(input: String): GenModel_139_ {
        val parts = input.split(":")
        return GenModel_139_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_139_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_139_,
    private val saveUseCase: GenSaveUseCase_139_,
    private val deleteUseCase: GenDeleteUseCase_139_,
    private val searchUseCase: GenSearchUseCase_139_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_139_>(GenState_139_.Idle)
    val state: StateFlow<GenState_139_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_139_) {
        when (event) {
            is GenEvent_139_.Load -> loadAll()
            is GenEvent_139_.Update -> save(event.model)
            is GenEvent_139_.Delete -> delete(event.id)
            is GenEvent_139_.Refresh -> loadAll()
            is GenEvent_139_.Search -> search(event.query)
            is GenEvent_139_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_139_.Loading; _state.value = GenState_139_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_139_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_139_.Success(searchUseCase(query)) } }
}
