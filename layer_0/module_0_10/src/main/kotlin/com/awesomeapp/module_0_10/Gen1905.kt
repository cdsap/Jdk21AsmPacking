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

data class GenModel_1905_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1905_ {
    data class Load(val id: Long) : GenEvent_1905_()
    data class Update(val model: GenModel_1905_) : GenEvent_1905_()
    data class Delete(val id: Long) : GenEvent_1905_()
    data object Refresh : GenEvent_1905_()
    data class Search(val query: String) : GenEvent_1905_()
    data class Filter(val predicate: String) : GenEvent_1905_()
}

sealed class GenState_1905_ {
    data object Idle : GenState_1905_()
    data object Loading : GenState_1905_()
    data class Success(val items: List<GenModel_1905_>) : GenState_1905_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1905_()
    data class Partial(val items: List<GenModel_1905_>, val hasMore: Boolean) : GenState_1905_()
}

interface GenRepository_1905_ {
    suspend fun getAll(): List<GenModel_1905_>
    suspend fun getById(id: Long): GenModel_1905_?
    suspend fun save(model: GenModel_1905_): GenModel_1905_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1905_>
}

@Singleton
class GenRepositoryImpl_1905_ @Inject constructor() : GenRepository_1905_ {
    private val store = mutableMapOf<Long, GenModel_1905_>()
    override suspend fun getAll(): List<GenModel_1905_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1905_? = store[id]
    override suspend fun save(model: GenModel_1905_): GenModel_1905_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1905_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1905_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1905_ @Inject constructor(
    private val repository: GenRepositoryImpl_1905_
) : GenUseCase_1905_<Unit, List<GenModel_1905_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1905_> = repository.getAll()
}

class GenSaveUseCase_1905_ @Inject constructor(
    private val repository: GenRepositoryImpl_1905_
) : GenUseCase_1905_<GenModel_1905_, GenModel_1905_> {
    override suspend fun invoke(params: GenModel_1905_): GenModel_1905_ = repository.save(params)
}

class GenDeleteUseCase_1905_ @Inject constructor(
    private val repository: GenRepositoryImpl_1905_
) : GenUseCase_1905_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1905_ @Inject constructor(
    private val repository: GenRepositoryImpl_1905_
) : GenUseCase_1905_<String, List<GenModel_1905_>> {
    override suspend fun invoke(params: String): List<GenModel_1905_> = repository.search(params)
}

abstract class GenMapper_1905_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1905_ : GenMapper_1905_<GenModel_1905_, String>() {
    override fun map(input: GenModel_1905_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1905_ : GenMapper_1905_<String, GenModel_1905_>() {
    override fun map(input: String): GenModel_1905_ {
        val parts = input.split(":")
        return GenModel_1905_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1905_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1905_,
    private val saveUseCase: GenSaveUseCase_1905_,
    private val deleteUseCase: GenDeleteUseCase_1905_,
    private val searchUseCase: GenSearchUseCase_1905_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1905_>(GenState_1905_.Idle)
    val state: StateFlow<GenState_1905_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1905_) {
        when (event) {
            is GenEvent_1905_.Load -> loadAll()
            is GenEvent_1905_.Update -> save(event.model)
            is GenEvent_1905_.Delete -> delete(event.id)
            is GenEvent_1905_.Refresh -> loadAll()
            is GenEvent_1905_.Search -> search(event.query)
            is GenEvent_1905_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1905_.Loading; _state.value = GenState_1905_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1905_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1905_.Success(searchUseCase(query)) } }
}
