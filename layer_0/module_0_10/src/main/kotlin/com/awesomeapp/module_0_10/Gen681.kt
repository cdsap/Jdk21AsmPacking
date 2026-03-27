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

data class GenModel_681_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_681_ {
    data class Load(val id: Long) : GenEvent_681_()
    data class Update(val model: GenModel_681_) : GenEvent_681_()
    data class Delete(val id: Long) : GenEvent_681_()
    data object Refresh : GenEvent_681_()
    data class Search(val query: String) : GenEvent_681_()
    data class Filter(val predicate: String) : GenEvent_681_()
}

sealed class GenState_681_ {
    data object Idle : GenState_681_()
    data object Loading : GenState_681_()
    data class Success(val items: List<GenModel_681_>) : GenState_681_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_681_()
    data class Partial(val items: List<GenModel_681_>, val hasMore: Boolean) : GenState_681_()
}

interface GenRepository_681_ {
    suspend fun getAll(): List<GenModel_681_>
    suspend fun getById(id: Long): GenModel_681_?
    suspend fun save(model: GenModel_681_): GenModel_681_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_681_>
}

@Singleton
class GenRepositoryImpl_681_ @Inject constructor() : GenRepository_681_ {
    private val store = mutableMapOf<Long, GenModel_681_>()
    override suspend fun getAll(): List<GenModel_681_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_681_? = store[id]
    override suspend fun save(model: GenModel_681_): GenModel_681_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_681_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_681_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_681_ @Inject constructor(
    private val repository: GenRepositoryImpl_681_
) : GenUseCase_681_<Unit, List<GenModel_681_>> {
    override suspend fun invoke(params: Unit): List<GenModel_681_> = repository.getAll()
}

class GenSaveUseCase_681_ @Inject constructor(
    private val repository: GenRepositoryImpl_681_
) : GenUseCase_681_<GenModel_681_, GenModel_681_> {
    override suspend fun invoke(params: GenModel_681_): GenModel_681_ = repository.save(params)
}

class GenDeleteUseCase_681_ @Inject constructor(
    private val repository: GenRepositoryImpl_681_
) : GenUseCase_681_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_681_ @Inject constructor(
    private val repository: GenRepositoryImpl_681_
) : GenUseCase_681_<String, List<GenModel_681_>> {
    override suspend fun invoke(params: String): List<GenModel_681_> = repository.search(params)
}

abstract class GenMapper_681_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_681_ : GenMapper_681_<GenModel_681_, String>() {
    override fun map(input: GenModel_681_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_681_ : GenMapper_681_<String, GenModel_681_>() {
    override fun map(input: String): GenModel_681_ {
        val parts = input.split(":")
        return GenModel_681_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_681_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_681_,
    private val saveUseCase: GenSaveUseCase_681_,
    private val deleteUseCase: GenDeleteUseCase_681_,
    private val searchUseCase: GenSearchUseCase_681_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_681_>(GenState_681_.Idle)
    val state: StateFlow<GenState_681_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_681_) {
        when (event) {
            is GenEvent_681_.Load -> loadAll()
            is GenEvent_681_.Update -> save(event.model)
            is GenEvent_681_.Delete -> delete(event.id)
            is GenEvent_681_.Refresh -> loadAll()
            is GenEvent_681_.Search -> search(event.query)
            is GenEvent_681_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_681_.Loading; _state.value = GenState_681_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_681_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_681_.Success(searchUseCase(query)) } }
}
