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

data class GenModel_607_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_607_ {
    data class Load(val id: Long) : GenEvent_607_()
    data class Update(val model: GenModel_607_) : GenEvent_607_()
    data class Delete(val id: Long) : GenEvent_607_()
    data object Refresh : GenEvent_607_()
    data class Search(val query: String) : GenEvent_607_()
    data class Filter(val predicate: String) : GenEvent_607_()
}

sealed class GenState_607_ {
    data object Idle : GenState_607_()
    data object Loading : GenState_607_()
    data class Success(val items: List<GenModel_607_>) : GenState_607_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_607_()
    data class Partial(val items: List<GenModel_607_>, val hasMore: Boolean) : GenState_607_()
}

interface GenRepository_607_ {
    suspend fun getAll(): List<GenModel_607_>
    suspend fun getById(id: Long): GenModel_607_?
    suspend fun save(model: GenModel_607_): GenModel_607_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_607_>
}

@Singleton
class GenRepositoryImpl_607_ @Inject constructor() : GenRepository_607_ {
    private val store = mutableMapOf<Long, GenModel_607_>()
    override suspend fun getAll(): List<GenModel_607_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_607_? = store[id]
    override suspend fun save(model: GenModel_607_): GenModel_607_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_607_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_607_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_607_ @Inject constructor(
    private val repository: GenRepositoryImpl_607_
) : GenUseCase_607_<Unit, List<GenModel_607_>> {
    override suspend fun invoke(params: Unit): List<GenModel_607_> = repository.getAll()
}

class GenSaveUseCase_607_ @Inject constructor(
    private val repository: GenRepositoryImpl_607_
) : GenUseCase_607_<GenModel_607_, GenModel_607_> {
    override suspend fun invoke(params: GenModel_607_): GenModel_607_ = repository.save(params)
}

class GenDeleteUseCase_607_ @Inject constructor(
    private val repository: GenRepositoryImpl_607_
) : GenUseCase_607_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_607_ @Inject constructor(
    private val repository: GenRepositoryImpl_607_
) : GenUseCase_607_<String, List<GenModel_607_>> {
    override suspend fun invoke(params: String): List<GenModel_607_> = repository.search(params)
}

abstract class GenMapper_607_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_607_ : GenMapper_607_<GenModel_607_, String>() {
    override fun map(input: GenModel_607_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_607_ : GenMapper_607_<String, GenModel_607_>() {
    override fun map(input: String): GenModel_607_ {
        val parts = input.split(":")
        return GenModel_607_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_607_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_607_,
    private val saveUseCase: GenSaveUseCase_607_,
    private val deleteUseCase: GenDeleteUseCase_607_,
    private val searchUseCase: GenSearchUseCase_607_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_607_>(GenState_607_.Idle)
    val state: StateFlow<GenState_607_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_607_) {
        when (event) {
            is GenEvent_607_.Load -> loadAll()
            is GenEvent_607_.Update -> save(event.model)
            is GenEvent_607_.Delete -> delete(event.id)
            is GenEvent_607_.Refresh -> loadAll()
            is GenEvent_607_.Search -> search(event.query)
            is GenEvent_607_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_607_.Loading; _state.value = GenState_607_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_607_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_607_.Success(searchUseCase(query)) } }
}
