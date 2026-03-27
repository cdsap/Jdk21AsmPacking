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

data class GenModel_624_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_624_ {
    data class Load(val id: Long) : GenEvent_624_()
    data class Update(val model: GenModel_624_) : GenEvent_624_()
    data class Delete(val id: Long) : GenEvent_624_()
    data object Refresh : GenEvent_624_()
    data class Search(val query: String) : GenEvent_624_()
    data class Filter(val predicate: String) : GenEvent_624_()
}

sealed class GenState_624_ {
    data object Idle : GenState_624_()
    data object Loading : GenState_624_()
    data class Success(val items: List<GenModel_624_>) : GenState_624_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_624_()
    data class Partial(val items: List<GenModel_624_>, val hasMore: Boolean) : GenState_624_()
}

interface GenRepository_624_ {
    suspend fun getAll(): List<GenModel_624_>
    suspend fun getById(id: Long): GenModel_624_?
    suspend fun save(model: GenModel_624_): GenModel_624_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_624_>
}

@Singleton
class GenRepositoryImpl_624_ @Inject constructor() : GenRepository_624_ {
    private val store = mutableMapOf<Long, GenModel_624_>()
    override suspend fun getAll(): List<GenModel_624_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_624_? = store[id]
    override suspend fun save(model: GenModel_624_): GenModel_624_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_624_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_624_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_624_ @Inject constructor(
    private val repository: GenRepositoryImpl_624_
) : GenUseCase_624_<Unit, List<GenModel_624_>> {
    override suspend fun invoke(params: Unit): List<GenModel_624_> = repository.getAll()
}

class GenSaveUseCase_624_ @Inject constructor(
    private val repository: GenRepositoryImpl_624_
) : GenUseCase_624_<GenModel_624_, GenModel_624_> {
    override suspend fun invoke(params: GenModel_624_): GenModel_624_ = repository.save(params)
}

class GenDeleteUseCase_624_ @Inject constructor(
    private val repository: GenRepositoryImpl_624_
) : GenUseCase_624_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_624_ @Inject constructor(
    private val repository: GenRepositoryImpl_624_
) : GenUseCase_624_<String, List<GenModel_624_>> {
    override suspend fun invoke(params: String): List<GenModel_624_> = repository.search(params)
}

abstract class GenMapper_624_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_624_ : GenMapper_624_<GenModel_624_, String>() {
    override fun map(input: GenModel_624_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_624_ : GenMapper_624_<String, GenModel_624_>() {
    override fun map(input: String): GenModel_624_ {
        val parts = input.split(":")
        return GenModel_624_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_624_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_624_,
    private val saveUseCase: GenSaveUseCase_624_,
    private val deleteUseCase: GenDeleteUseCase_624_,
    private val searchUseCase: GenSearchUseCase_624_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_624_>(GenState_624_.Idle)
    val state: StateFlow<GenState_624_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_624_) {
        when (event) {
            is GenEvent_624_.Load -> loadAll()
            is GenEvent_624_.Update -> save(event.model)
            is GenEvent_624_.Delete -> delete(event.id)
            is GenEvent_624_.Refresh -> loadAll()
            is GenEvent_624_.Search -> search(event.query)
            is GenEvent_624_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_624_.Loading; _state.value = GenState_624_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_624_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_624_.Success(searchUseCase(query)) } }
}
