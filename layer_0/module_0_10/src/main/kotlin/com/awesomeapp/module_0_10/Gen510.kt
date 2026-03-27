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

data class GenModel_510_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_510_ {
    data class Load(val id: Long) : GenEvent_510_()
    data class Update(val model: GenModel_510_) : GenEvent_510_()
    data class Delete(val id: Long) : GenEvent_510_()
    data object Refresh : GenEvent_510_()
    data class Search(val query: String) : GenEvent_510_()
    data class Filter(val predicate: String) : GenEvent_510_()
}

sealed class GenState_510_ {
    data object Idle : GenState_510_()
    data object Loading : GenState_510_()
    data class Success(val items: List<GenModel_510_>) : GenState_510_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_510_()
    data class Partial(val items: List<GenModel_510_>, val hasMore: Boolean) : GenState_510_()
}

interface GenRepository_510_ {
    suspend fun getAll(): List<GenModel_510_>
    suspend fun getById(id: Long): GenModel_510_?
    suspend fun save(model: GenModel_510_): GenModel_510_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_510_>
}

@Singleton
class GenRepositoryImpl_510_ @Inject constructor() : GenRepository_510_ {
    private val store = mutableMapOf<Long, GenModel_510_>()
    override suspend fun getAll(): List<GenModel_510_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_510_? = store[id]
    override suspend fun save(model: GenModel_510_): GenModel_510_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_510_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_510_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_510_ @Inject constructor(
    private val repository: GenRepositoryImpl_510_
) : GenUseCase_510_<Unit, List<GenModel_510_>> {
    override suspend fun invoke(params: Unit): List<GenModel_510_> = repository.getAll()
}

class GenSaveUseCase_510_ @Inject constructor(
    private val repository: GenRepositoryImpl_510_
) : GenUseCase_510_<GenModel_510_, GenModel_510_> {
    override suspend fun invoke(params: GenModel_510_): GenModel_510_ = repository.save(params)
}

class GenDeleteUseCase_510_ @Inject constructor(
    private val repository: GenRepositoryImpl_510_
) : GenUseCase_510_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_510_ @Inject constructor(
    private val repository: GenRepositoryImpl_510_
) : GenUseCase_510_<String, List<GenModel_510_>> {
    override suspend fun invoke(params: String): List<GenModel_510_> = repository.search(params)
}

abstract class GenMapper_510_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_510_ : GenMapper_510_<GenModel_510_, String>() {
    override fun map(input: GenModel_510_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_510_ : GenMapper_510_<String, GenModel_510_>() {
    override fun map(input: String): GenModel_510_ {
        val parts = input.split(":")
        return GenModel_510_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_510_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_510_,
    private val saveUseCase: GenSaveUseCase_510_,
    private val deleteUseCase: GenDeleteUseCase_510_,
    private val searchUseCase: GenSearchUseCase_510_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_510_>(GenState_510_.Idle)
    val state: StateFlow<GenState_510_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_510_) {
        when (event) {
            is GenEvent_510_.Load -> loadAll()
            is GenEvent_510_.Update -> save(event.model)
            is GenEvent_510_.Delete -> delete(event.id)
            is GenEvent_510_.Refresh -> loadAll()
            is GenEvent_510_.Search -> search(event.query)
            is GenEvent_510_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_510_.Loading; _state.value = GenState_510_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_510_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_510_.Success(searchUseCase(query)) } }
}
