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

data class GenModel_614_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_614_ {
    data class Load(val id: Long) : GenEvent_614_()
    data class Update(val model: GenModel_614_) : GenEvent_614_()
    data class Delete(val id: Long) : GenEvent_614_()
    data object Refresh : GenEvent_614_()
    data class Search(val query: String) : GenEvent_614_()
    data class Filter(val predicate: String) : GenEvent_614_()
}

sealed class GenState_614_ {
    data object Idle : GenState_614_()
    data object Loading : GenState_614_()
    data class Success(val items: List<GenModel_614_>) : GenState_614_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_614_()
    data class Partial(val items: List<GenModel_614_>, val hasMore: Boolean) : GenState_614_()
}

interface GenRepository_614_ {
    suspend fun getAll(): List<GenModel_614_>
    suspend fun getById(id: Long): GenModel_614_?
    suspend fun save(model: GenModel_614_): GenModel_614_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_614_>
}

@Singleton
class GenRepositoryImpl_614_ @Inject constructor() : GenRepository_614_ {
    private val store = mutableMapOf<Long, GenModel_614_>()
    override suspend fun getAll(): List<GenModel_614_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_614_? = store[id]
    override suspend fun save(model: GenModel_614_): GenModel_614_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_614_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_614_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_614_ @Inject constructor(
    private val repository: GenRepositoryImpl_614_
) : GenUseCase_614_<Unit, List<GenModel_614_>> {
    override suspend fun invoke(params: Unit): List<GenModel_614_> = repository.getAll()
}

class GenSaveUseCase_614_ @Inject constructor(
    private val repository: GenRepositoryImpl_614_
) : GenUseCase_614_<GenModel_614_, GenModel_614_> {
    override suspend fun invoke(params: GenModel_614_): GenModel_614_ = repository.save(params)
}

class GenDeleteUseCase_614_ @Inject constructor(
    private val repository: GenRepositoryImpl_614_
) : GenUseCase_614_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_614_ @Inject constructor(
    private val repository: GenRepositoryImpl_614_
) : GenUseCase_614_<String, List<GenModel_614_>> {
    override suspend fun invoke(params: String): List<GenModel_614_> = repository.search(params)
}

abstract class GenMapper_614_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_614_ : GenMapper_614_<GenModel_614_, String>() {
    override fun map(input: GenModel_614_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_614_ : GenMapper_614_<String, GenModel_614_>() {
    override fun map(input: String): GenModel_614_ {
        val parts = input.split(":")
        return GenModel_614_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_614_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_614_,
    private val saveUseCase: GenSaveUseCase_614_,
    private val deleteUseCase: GenDeleteUseCase_614_,
    private val searchUseCase: GenSearchUseCase_614_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_614_>(GenState_614_.Idle)
    val state: StateFlow<GenState_614_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_614_) {
        when (event) {
            is GenEvent_614_.Load -> loadAll()
            is GenEvent_614_.Update -> save(event.model)
            is GenEvent_614_.Delete -> delete(event.id)
            is GenEvent_614_.Refresh -> loadAll()
            is GenEvent_614_.Search -> search(event.query)
            is GenEvent_614_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_614_.Loading; _state.value = GenState_614_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_614_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_614_.Success(searchUseCase(query)) } }
}
