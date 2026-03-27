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

data class GenModel_713_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_713_ {
    data class Load(val id: Long) : GenEvent_713_()
    data class Update(val model: GenModel_713_) : GenEvent_713_()
    data class Delete(val id: Long) : GenEvent_713_()
    data object Refresh : GenEvent_713_()
    data class Search(val query: String) : GenEvent_713_()
    data class Filter(val predicate: String) : GenEvent_713_()
}

sealed class GenState_713_ {
    data object Idle : GenState_713_()
    data object Loading : GenState_713_()
    data class Success(val items: List<GenModel_713_>) : GenState_713_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_713_()
    data class Partial(val items: List<GenModel_713_>, val hasMore: Boolean) : GenState_713_()
}

interface GenRepository_713_ {
    suspend fun getAll(): List<GenModel_713_>
    suspend fun getById(id: Long): GenModel_713_?
    suspend fun save(model: GenModel_713_): GenModel_713_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_713_>
}

@Singleton
class GenRepositoryImpl_713_ @Inject constructor() : GenRepository_713_ {
    private val store = mutableMapOf<Long, GenModel_713_>()
    override suspend fun getAll(): List<GenModel_713_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_713_? = store[id]
    override suspend fun save(model: GenModel_713_): GenModel_713_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_713_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_713_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_713_ @Inject constructor(
    private val repository: GenRepositoryImpl_713_
) : GenUseCase_713_<Unit, List<GenModel_713_>> {
    override suspend fun invoke(params: Unit): List<GenModel_713_> = repository.getAll()
}

class GenSaveUseCase_713_ @Inject constructor(
    private val repository: GenRepositoryImpl_713_
) : GenUseCase_713_<GenModel_713_, GenModel_713_> {
    override suspend fun invoke(params: GenModel_713_): GenModel_713_ = repository.save(params)
}

class GenDeleteUseCase_713_ @Inject constructor(
    private val repository: GenRepositoryImpl_713_
) : GenUseCase_713_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_713_ @Inject constructor(
    private val repository: GenRepositoryImpl_713_
) : GenUseCase_713_<String, List<GenModel_713_>> {
    override suspend fun invoke(params: String): List<GenModel_713_> = repository.search(params)
}

abstract class GenMapper_713_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_713_ : GenMapper_713_<GenModel_713_, String>() {
    override fun map(input: GenModel_713_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_713_ : GenMapper_713_<String, GenModel_713_>() {
    override fun map(input: String): GenModel_713_ {
        val parts = input.split(":")
        return GenModel_713_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_713_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_713_,
    private val saveUseCase: GenSaveUseCase_713_,
    private val deleteUseCase: GenDeleteUseCase_713_,
    private val searchUseCase: GenSearchUseCase_713_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_713_>(GenState_713_.Idle)
    val state: StateFlow<GenState_713_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_713_) {
        when (event) {
            is GenEvent_713_.Load -> loadAll()
            is GenEvent_713_.Update -> save(event.model)
            is GenEvent_713_.Delete -> delete(event.id)
            is GenEvent_713_.Refresh -> loadAll()
            is GenEvent_713_.Search -> search(event.query)
            is GenEvent_713_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_713_.Loading; _state.value = GenState_713_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_713_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_713_.Success(searchUseCase(query)) } }
}
