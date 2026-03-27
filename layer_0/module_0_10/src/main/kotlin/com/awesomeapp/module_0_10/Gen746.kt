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

data class GenModel_746_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_746_ {
    data class Load(val id: Long) : GenEvent_746_()
    data class Update(val model: GenModel_746_) : GenEvent_746_()
    data class Delete(val id: Long) : GenEvent_746_()
    data object Refresh : GenEvent_746_()
    data class Search(val query: String) : GenEvent_746_()
    data class Filter(val predicate: String) : GenEvent_746_()
}

sealed class GenState_746_ {
    data object Idle : GenState_746_()
    data object Loading : GenState_746_()
    data class Success(val items: List<GenModel_746_>) : GenState_746_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_746_()
    data class Partial(val items: List<GenModel_746_>, val hasMore: Boolean) : GenState_746_()
}

interface GenRepository_746_ {
    suspend fun getAll(): List<GenModel_746_>
    suspend fun getById(id: Long): GenModel_746_?
    suspend fun save(model: GenModel_746_): GenModel_746_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_746_>
}

@Singleton
class GenRepositoryImpl_746_ @Inject constructor() : GenRepository_746_ {
    private val store = mutableMapOf<Long, GenModel_746_>()
    override suspend fun getAll(): List<GenModel_746_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_746_? = store[id]
    override suspend fun save(model: GenModel_746_): GenModel_746_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_746_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_746_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_746_ @Inject constructor(
    private val repository: GenRepositoryImpl_746_
) : GenUseCase_746_<Unit, List<GenModel_746_>> {
    override suspend fun invoke(params: Unit): List<GenModel_746_> = repository.getAll()
}

class GenSaveUseCase_746_ @Inject constructor(
    private val repository: GenRepositoryImpl_746_
) : GenUseCase_746_<GenModel_746_, GenModel_746_> {
    override suspend fun invoke(params: GenModel_746_): GenModel_746_ = repository.save(params)
}

class GenDeleteUseCase_746_ @Inject constructor(
    private val repository: GenRepositoryImpl_746_
) : GenUseCase_746_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_746_ @Inject constructor(
    private val repository: GenRepositoryImpl_746_
) : GenUseCase_746_<String, List<GenModel_746_>> {
    override suspend fun invoke(params: String): List<GenModel_746_> = repository.search(params)
}

abstract class GenMapper_746_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_746_ : GenMapper_746_<GenModel_746_, String>() {
    override fun map(input: GenModel_746_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_746_ : GenMapper_746_<String, GenModel_746_>() {
    override fun map(input: String): GenModel_746_ {
        val parts = input.split(":")
        return GenModel_746_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_746_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_746_,
    private val saveUseCase: GenSaveUseCase_746_,
    private val deleteUseCase: GenDeleteUseCase_746_,
    private val searchUseCase: GenSearchUseCase_746_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_746_>(GenState_746_.Idle)
    val state: StateFlow<GenState_746_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_746_) {
        when (event) {
            is GenEvent_746_.Load -> loadAll()
            is GenEvent_746_.Update -> save(event.model)
            is GenEvent_746_.Delete -> delete(event.id)
            is GenEvent_746_.Refresh -> loadAll()
            is GenEvent_746_.Search -> search(event.query)
            is GenEvent_746_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_746_.Loading; _state.value = GenState_746_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_746_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_746_.Success(searchUseCase(query)) } }
}
