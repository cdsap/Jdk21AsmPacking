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

data class GenModel_2010_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2010_ {
    data class Load(val id: Long) : GenEvent_2010_()
    data class Update(val model: GenModel_2010_) : GenEvent_2010_()
    data class Delete(val id: Long) : GenEvent_2010_()
    data object Refresh : GenEvent_2010_()
    data class Search(val query: String) : GenEvent_2010_()
    data class Filter(val predicate: String) : GenEvent_2010_()
}

sealed class GenState_2010_ {
    data object Idle : GenState_2010_()
    data object Loading : GenState_2010_()
    data class Success(val items: List<GenModel_2010_>) : GenState_2010_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2010_()
    data class Partial(val items: List<GenModel_2010_>, val hasMore: Boolean) : GenState_2010_()
}

interface GenRepository_2010_ {
    suspend fun getAll(): List<GenModel_2010_>
    suspend fun getById(id: Long): GenModel_2010_?
    suspend fun save(model: GenModel_2010_): GenModel_2010_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2010_>
}

@Singleton
class GenRepositoryImpl_2010_ @Inject constructor() : GenRepository_2010_ {
    private val store = mutableMapOf<Long, GenModel_2010_>()
    override suspend fun getAll(): List<GenModel_2010_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2010_? = store[id]
    override suspend fun save(model: GenModel_2010_): GenModel_2010_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2010_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2010_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2010_ @Inject constructor(
    private val repository: GenRepositoryImpl_2010_
) : GenUseCase_2010_<Unit, List<GenModel_2010_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2010_> = repository.getAll()
}

class GenSaveUseCase_2010_ @Inject constructor(
    private val repository: GenRepositoryImpl_2010_
) : GenUseCase_2010_<GenModel_2010_, GenModel_2010_> {
    override suspend fun invoke(params: GenModel_2010_): GenModel_2010_ = repository.save(params)
}

class GenDeleteUseCase_2010_ @Inject constructor(
    private val repository: GenRepositoryImpl_2010_
) : GenUseCase_2010_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2010_ @Inject constructor(
    private val repository: GenRepositoryImpl_2010_
) : GenUseCase_2010_<String, List<GenModel_2010_>> {
    override suspend fun invoke(params: String): List<GenModel_2010_> = repository.search(params)
}

abstract class GenMapper_2010_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2010_ : GenMapper_2010_<GenModel_2010_, String>() {
    override fun map(input: GenModel_2010_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2010_ : GenMapper_2010_<String, GenModel_2010_>() {
    override fun map(input: String): GenModel_2010_ {
        val parts = input.split(":")
        return GenModel_2010_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2010_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2010_,
    private val saveUseCase: GenSaveUseCase_2010_,
    private val deleteUseCase: GenDeleteUseCase_2010_,
    private val searchUseCase: GenSearchUseCase_2010_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2010_>(GenState_2010_.Idle)
    val state: StateFlow<GenState_2010_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2010_) {
        when (event) {
            is GenEvent_2010_.Load -> loadAll()
            is GenEvent_2010_.Update -> save(event.model)
            is GenEvent_2010_.Delete -> delete(event.id)
            is GenEvent_2010_.Refresh -> loadAll()
            is GenEvent_2010_.Search -> search(event.query)
            is GenEvent_2010_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2010_.Loading; _state.value = GenState_2010_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2010_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2010_.Success(searchUseCase(query)) } }
}
