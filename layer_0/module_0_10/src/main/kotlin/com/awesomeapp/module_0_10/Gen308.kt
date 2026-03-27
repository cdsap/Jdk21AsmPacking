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

data class GenModel_308_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_308_ {
    data class Load(val id: Long) : GenEvent_308_()
    data class Update(val model: GenModel_308_) : GenEvent_308_()
    data class Delete(val id: Long) : GenEvent_308_()
    data object Refresh : GenEvent_308_()
    data class Search(val query: String) : GenEvent_308_()
    data class Filter(val predicate: String) : GenEvent_308_()
}

sealed class GenState_308_ {
    data object Idle : GenState_308_()
    data object Loading : GenState_308_()
    data class Success(val items: List<GenModel_308_>) : GenState_308_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_308_()
    data class Partial(val items: List<GenModel_308_>, val hasMore: Boolean) : GenState_308_()
}

interface GenRepository_308_ {
    suspend fun getAll(): List<GenModel_308_>
    suspend fun getById(id: Long): GenModel_308_?
    suspend fun save(model: GenModel_308_): GenModel_308_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_308_>
}

@Singleton
class GenRepositoryImpl_308_ @Inject constructor() : GenRepository_308_ {
    private val store = mutableMapOf<Long, GenModel_308_>()
    override suspend fun getAll(): List<GenModel_308_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_308_? = store[id]
    override suspend fun save(model: GenModel_308_): GenModel_308_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_308_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_308_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_308_ @Inject constructor(
    private val repository: GenRepositoryImpl_308_
) : GenUseCase_308_<Unit, List<GenModel_308_>> {
    override suspend fun invoke(params: Unit): List<GenModel_308_> = repository.getAll()
}

class GenSaveUseCase_308_ @Inject constructor(
    private val repository: GenRepositoryImpl_308_
) : GenUseCase_308_<GenModel_308_, GenModel_308_> {
    override suspend fun invoke(params: GenModel_308_): GenModel_308_ = repository.save(params)
}

class GenDeleteUseCase_308_ @Inject constructor(
    private val repository: GenRepositoryImpl_308_
) : GenUseCase_308_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_308_ @Inject constructor(
    private val repository: GenRepositoryImpl_308_
) : GenUseCase_308_<String, List<GenModel_308_>> {
    override suspend fun invoke(params: String): List<GenModel_308_> = repository.search(params)
}

abstract class GenMapper_308_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_308_ : GenMapper_308_<GenModel_308_, String>() {
    override fun map(input: GenModel_308_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_308_ : GenMapper_308_<String, GenModel_308_>() {
    override fun map(input: String): GenModel_308_ {
        val parts = input.split(":")
        return GenModel_308_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_308_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_308_,
    private val saveUseCase: GenSaveUseCase_308_,
    private val deleteUseCase: GenDeleteUseCase_308_,
    private val searchUseCase: GenSearchUseCase_308_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_308_>(GenState_308_.Idle)
    val state: StateFlow<GenState_308_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_308_) {
        when (event) {
            is GenEvent_308_.Load -> loadAll()
            is GenEvent_308_.Update -> save(event.model)
            is GenEvent_308_.Delete -> delete(event.id)
            is GenEvent_308_.Refresh -> loadAll()
            is GenEvent_308_.Search -> search(event.query)
            is GenEvent_308_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_308_.Loading; _state.value = GenState_308_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_308_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_308_.Success(searchUseCase(query)) } }
}
