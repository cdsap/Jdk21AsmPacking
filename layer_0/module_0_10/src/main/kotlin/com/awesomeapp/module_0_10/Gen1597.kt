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

data class GenModel_1597_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1597_ {
    data class Load(val id: Long) : GenEvent_1597_()
    data class Update(val model: GenModel_1597_) : GenEvent_1597_()
    data class Delete(val id: Long) : GenEvent_1597_()
    data object Refresh : GenEvent_1597_()
    data class Search(val query: String) : GenEvent_1597_()
    data class Filter(val predicate: String) : GenEvent_1597_()
}

sealed class GenState_1597_ {
    data object Idle : GenState_1597_()
    data object Loading : GenState_1597_()
    data class Success(val items: List<GenModel_1597_>) : GenState_1597_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1597_()
    data class Partial(val items: List<GenModel_1597_>, val hasMore: Boolean) : GenState_1597_()
}

interface GenRepository_1597_ {
    suspend fun getAll(): List<GenModel_1597_>
    suspend fun getById(id: Long): GenModel_1597_?
    suspend fun save(model: GenModel_1597_): GenModel_1597_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1597_>
}

@Singleton
class GenRepositoryImpl_1597_ @Inject constructor() : GenRepository_1597_ {
    private val store = mutableMapOf<Long, GenModel_1597_>()
    override suspend fun getAll(): List<GenModel_1597_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1597_? = store[id]
    override suspend fun save(model: GenModel_1597_): GenModel_1597_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1597_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1597_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1597_ @Inject constructor(
    private val repository: GenRepositoryImpl_1597_
) : GenUseCase_1597_<Unit, List<GenModel_1597_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1597_> = repository.getAll()
}

class GenSaveUseCase_1597_ @Inject constructor(
    private val repository: GenRepositoryImpl_1597_
) : GenUseCase_1597_<GenModel_1597_, GenModel_1597_> {
    override suspend fun invoke(params: GenModel_1597_): GenModel_1597_ = repository.save(params)
}

class GenDeleteUseCase_1597_ @Inject constructor(
    private val repository: GenRepositoryImpl_1597_
) : GenUseCase_1597_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1597_ @Inject constructor(
    private val repository: GenRepositoryImpl_1597_
) : GenUseCase_1597_<String, List<GenModel_1597_>> {
    override suspend fun invoke(params: String): List<GenModel_1597_> = repository.search(params)
}

abstract class GenMapper_1597_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1597_ : GenMapper_1597_<GenModel_1597_, String>() {
    override fun map(input: GenModel_1597_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1597_ : GenMapper_1597_<String, GenModel_1597_>() {
    override fun map(input: String): GenModel_1597_ {
        val parts = input.split(":")
        return GenModel_1597_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1597_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1597_,
    private val saveUseCase: GenSaveUseCase_1597_,
    private val deleteUseCase: GenDeleteUseCase_1597_,
    private val searchUseCase: GenSearchUseCase_1597_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1597_>(GenState_1597_.Idle)
    val state: StateFlow<GenState_1597_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1597_) {
        when (event) {
            is GenEvent_1597_.Load -> loadAll()
            is GenEvent_1597_.Update -> save(event.model)
            is GenEvent_1597_.Delete -> delete(event.id)
            is GenEvent_1597_.Refresh -> loadAll()
            is GenEvent_1597_.Search -> search(event.query)
            is GenEvent_1597_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1597_.Loading; _state.value = GenState_1597_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1597_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1597_.Success(searchUseCase(query)) } }
}
