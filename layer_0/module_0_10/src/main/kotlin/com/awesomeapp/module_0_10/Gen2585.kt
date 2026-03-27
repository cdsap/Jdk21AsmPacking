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

data class GenModel_2585_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2585_ {
    data class Load(val id: Long) : GenEvent_2585_()
    data class Update(val model: GenModel_2585_) : GenEvent_2585_()
    data class Delete(val id: Long) : GenEvent_2585_()
    data object Refresh : GenEvent_2585_()
    data class Search(val query: String) : GenEvent_2585_()
    data class Filter(val predicate: String) : GenEvent_2585_()
}

sealed class GenState_2585_ {
    data object Idle : GenState_2585_()
    data object Loading : GenState_2585_()
    data class Success(val items: List<GenModel_2585_>) : GenState_2585_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2585_()
    data class Partial(val items: List<GenModel_2585_>, val hasMore: Boolean) : GenState_2585_()
}

interface GenRepository_2585_ {
    suspend fun getAll(): List<GenModel_2585_>
    suspend fun getById(id: Long): GenModel_2585_?
    suspend fun save(model: GenModel_2585_): GenModel_2585_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2585_>
}

@Singleton
class GenRepositoryImpl_2585_ @Inject constructor() : GenRepository_2585_ {
    private val store = mutableMapOf<Long, GenModel_2585_>()
    override suspend fun getAll(): List<GenModel_2585_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2585_? = store[id]
    override suspend fun save(model: GenModel_2585_): GenModel_2585_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2585_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2585_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2585_ @Inject constructor(
    private val repository: GenRepositoryImpl_2585_
) : GenUseCase_2585_<Unit, List<GenModel_2585_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2585_> = repository.getAll()
}

class GenSaveUseCase_2585_ @Inject constructor(
    private val repository: GenRepositoryImpl_2585_
) : GenUseCase_2585_<GenModel_2585_, GenModel_2585_> {
    override suspend fun invoke(params: GenModel_2585_): GenModel_2585_ = repository.save(params)
}

class GenDeleteUseCase_2585_ @Inject constructor(
    private val repository: GenRepositoryImpl_2585_
) : GenUseCase_2585_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2585_ @Inject constructor(
    private val repository: GenRepositoryImpl_2585_
) : GenUseCase_2585_<String, List<GenModel_2585_>> {
    override suspend fun invoke(params: String): List<GenModel_2585_> = repository.search(params)
}

abstract class GenMapper_2585_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2585_ : GenMapper_2585_<GenModel_2585_, String>() {
    override fun map(input: GenModel_2585_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2585_ : GenMapper_2585_<String, GenModel_2585_>() {
    override fun map(input: String): GenModel_2585_ {
        val parts = input.split(":")
        return GenModel_2585_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2585_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2585_,
    private val saveUseCase: GenSaveUseCase_2585_,
    private val deleteUseCase: GenDeleteUseCase_2585_,
    private val searchUseCase: GenSearchUseCase_2585_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2585_>(GenState_2585_.Idle)
    val state: StateFlow<GenState_2585_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2585_) {
        when (event) {
            is GenEvent_2585_.Load -> loadAll()
            is GenEvent_2585_.Update -> save(event.model)
            is GenEvent_2585_.Delete -> delete(event.id)
            is GenEvent_2585_.Refresh -> loadAll()
            is GenEvent_2585_.Search -> search(event.query)
            is GenEvent_2585_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2585_.Loading; _state.value = GenState_2585_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2585_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2585_.Success(searchUseCase(query)) } }
}
