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

data class GenModel_733_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_733_ {
    data class Load(val id: Long) : GenEvent_733_()
    data class Update(val model: GenModel_733_) : GenEvent_733_()
    data class Delete(val id: Long) : GenEvent_733_()
    data object Refresh : GenEvent_733_()
    data class Search(val query: String) : GenEvent_733_()
    data class Filter(val predicate: String) : GenEvent_733_()
}

sealed class GenState_733_ {
    data object Idle : GenState_733_()
    data object Loading : GenState_733_()
    data class Success(val items: List<GenModel_733_>) : GenState_733_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_733_()
    data class Partial(val items: List<GenModel_733_>, val hasMore: Boolean) : GenState_733_()
}

interface GenRepository_733_ {
    suspend fun getAll(): List<GenModel_733_>
    suspend fun getById(id: Long): GenModel_733_?
    suspend fun save(model: GenModel_733_): GenModel_733_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_733_>
}

@Singleton
class GenRepositoryImpl_733_ @Inject constructor() : GenRepository_733_ {
    private val store = mutableMapOf<Long, GenModel_733_>()
    override suspend fun getAll(): List<GenModel_733_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_733_? = store[id]
    override suspend fun save(model: GenModel_733_): GenModel_733_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_733_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_733_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_733_ @Inject constructor(
    private val repository: GenRepositoryImpl_733_
) : GenUseCase_733_<Unit, List<GenModel_733_>> {
    override suspend fun invoke(params: Unit): List<GenModel_733_> = repository.getAll()
}

class GenSaveUseCase_733_ @Inject constructor(
    private val repository: GenRepositoryImpl_733_
) : GenUseCase_733_<GenModel_733_, GenModel_733_> {
    override suspend fun invoke(params: GenModel_733_): GenModel_733_ = repository.save(params)
}

class GenDeleteUseCase_733_ @Inject constructor(
    private val repository: GenRepositoryImpl_733_
) : GenUseCase_733_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_733_ @Inject constructor(
    private val repository: GenRepositoryImpl_733_
) : GenUseCase_733_<String, List<GenModel_733_>> {
    override suspend fun invoke(params: String): List<GenModel_733_> = repository.search(params)
}

abstract class GenMapper_733_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_733_ : GenMapper_733_<GenModel_733_, String>() {
    override fun map(input: GenModel_733_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_733_ : GenMapper_733_<String, GenModel_733_>() {
    override fun map(input: String): GenModel_733_ {
        val parts = input.split(":")
        return GenModel_733_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_733_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_733_,
    private val saveUseCase: GenSaveUseCase_733_,
    private val deleteUseCase: GenDeleteUseCase_733_,
    private val searchUseCase: GenSearchUseCase_733_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_733_>(GenState_733_.Idle)
    val state: StateFlow<GenState_733_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_733_) {
        when (event) {
            is GenEvent_733_.Load -> loadAll()
            is GenEvent_733_.Update -> save(event.model)
            is GenEvent_733_.Delete -> delete(event.id)
            is GenEvent_733_.Refresh -> loadAll()
            is GenEvent_733_.Search -> search(event.query)
            is GenEvent_733_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_733_.Loading; _state.value = GenState_733_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_733_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_733_.Success(searchUseCase(query)) } }
}
