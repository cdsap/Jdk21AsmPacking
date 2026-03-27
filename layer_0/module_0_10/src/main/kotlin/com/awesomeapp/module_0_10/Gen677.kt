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

data class GenModel_677_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_677_ {
    data class Load(val id: Long) : GenEvent_677_()
    data class Update(val model: GenModel_677_) : GenEvent_677_()
    data class Delete(val id: Long) : GenEvent_677_()
    data object Refresh : GenEvent_677_()
    data class Search(val query: String) : GenEvent_677_()
    data class Filter(val predicate: String) : GenEvent_677_()
}

sealed class GenState_677_ {
    data object Idle : GenState_677_()
    data object Loading : GenState_677_()
    data class Success(val items: List<GenModel_677_>) : GenState_677_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_677_()
    data class Partial(val items: List<GenModel_677_>, val hasMore: Boolean) : GenState_677_()
}

interface GenRepository_677_ {
    suspend fun getAll(): List<GenModel_677_>
    suspend fun getById(id: Long): GenModel_677_?
    suspend fun save(model: GenModel_677_): GenModel_677_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_677_>
}

@Singleton
class GenRepositoryImpl_677_ @Inject constructor() : GenRepository_677_ {
    private val store = mutableMapOf<Long, GenModel_677_>()
    override suspend fun getAll(): List<GenModel_677_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_677_? = store[id]
    override suspend fun save(model: GenModel_677_): GenModel_677_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_677_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_677_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_677_ @Inject constructor(
    private val repository: GenRepositoryImpl_677_
) : GenUseCase_677_<Unit, List<GenModel_677_>> {
    override suspend fun invoke(params: Unit): List<GenModel_677_> = repository.getAll()
}

class GenSaveUseCase_677_ @Inject constructor(
    private val repository: GenRepositoryImpl_677_
) : GenUseCase_677_<GenModel_677_, GenModel_677_> {
    override suspend fun invoke(params: GenModel_677_): GenModel_677_ = repository.save(params)
}

class GenDeleteUseCase_677_ @Inject constructor(
    private val repository: GenRepositoryImpl_677_
) : GenUseCase_677_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_677_ @Inject constructor(
    private val repository: GenRepositoryImpl_677_
) : GenUseCase_677_<String, List<GenModel_677_>> {
    override suspend fun invoke(params: String): List<GenModel_677_> = repository.search(params)
}

abstract class GenMapper_677_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_677_ : GenMapper_677_<GenModel_677_, String>() {
    override fun map(input: GenModel_677_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_677_ : GenMapper_677_<String, GenModel_677_>() {
    override fun map(input: String): GenModel_677_ {
        val parts = input.split(":")
        return GenModel_677_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_677_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_677_,
    private val saveUseCase: GenSaveUseCase_677_,
    private val deleteUseCase: GenDeleteUseCase_677_,
    private val searchUseCase: GenSearchUseCase_677_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_677_>(GenState_677_.Idle)
    val state: StateFlow<GenState_677_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_677_) {
        when (event) {
            is GenEvent_677_.Load -> loadAll()
            is GenEvent_677_.Update -> save(event.model)
            is GenEvent_677_.Delete -> delete(event.id)
            is GenEvent_677_.Refresh -> loadAll()
            is GenEvent_677_.Search -> search(event.query)
            is GenEvent_677_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_677_.Loading; _state.value = GenState_677_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_677_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_677_.Success(searchUseCase(query)) } }
}
