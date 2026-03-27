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

data class GenModel_3015_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3015_ {
    data class Load(val id: Long) : GenEvent_3015_()
    data class Update(val model: GenModel_3015_) : GenEvent_3015_()
    data class Delete(val id: Long) : GenEvent_3015_()
    data object Refresh : GenEvent_3015_()
    data class Search(val query: String) : GenEvent_3015_()
    data class Filter(val predicate: String) : GenEvent_3015_()
}

sealed class GenState_3015_ {
    data object Idle : GenState_3015_()
    data object Loading : GenState_3015_()
    data class Success(val items: List<GenModel_3015_>) : GenState_3015_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3015_()
    data class Partial(val items: List<GenModel_3015_>, val hasMore: Boolean) : GenState_3015_()
}

interface GenRepository_3015_ {
    suspend fun getAll(): List<GenModel_3015_>
    suspend fun getById(id: Long): GenModel_3015_?
    suspend fun save(model: GenModel_3015_): GenModel_3015_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3015_>
}

@Singleton
class GenRepositoryImpl_3015_ @Inject constructor() : GenRepository_3015_ {
    private val store = mutableMapOf<Long, GenModel_3015_>()
    override suspend fun getAll(): List<GenModel_3015_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3015_? = store[id]
    override suspend fun save(model: GenModel_3015_): GenModel_3015_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3015_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3015_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3015_ @Inject constructor(
    private val repository: GenRepositoryImpl_3015_
) : GenUseCase_3015_<Unit, List<GenModel_3015_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3015_> = repository.getAll()
}

class GenSaveUseCase_3015_ @Inject constructor(
    private val repository: GenRepositoryImpl_3015_
) : GenUseCase_3015_<GenModel_3015_, GenModel_3015_> {
    override suspend fun invoke(params: GenModel_3015_): GenModel_3015_ = repository.save(params)
}

class GenDeleteUseCase_3015_ @Inject constructor(
    private val repository: GenRepositoryImpl_3015_
) : GenUseCase_3015_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3015_ @Inject constructor(
    private val repository: GenRepositoryImpl_3015_
) : GenUseCase_3015_<String, List<GenModel_3015_>> {
    override suspend fun invoke(params: String): List<GenModel_3015_> = repository.search(params)
}

abstract class GenMapper_3015_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3015_ : GenMapper_3015_<GenModel_3015_, String>() {
    override fun map(input: GenModel_3015_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3015_ : GenMapper_3015_<String, GenModel_3015_>() {
    override fun map(input: String): GenModel_3015_ {
        val parts = input.split(":")
        return GenModel_3015_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3015_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3015_,
    private val saveUseCase: GenSaveUseCase_3015_,
    private val deleteUseCase: GenDeleteUseCase_3015_,
    private val searchUseCase: GenSearchUseCase_3015_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3015_>(GenState_3015_.Idle)
    val state: StateFlow<GenState_3015_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3015_) {
        when (event) {
            is GenEvent_3015_.Load -> loadAll()
            is GenEvent_3015_.Update -> save(event.model)
            is GenEvent_3015_.Delete -> delete(event.id)
            is GenEvent_3015_.Refresh -> loadAll()
            is GenEvent_3015_.Search -> search(event.query)
            is GenEvent_3015_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3015_.Loading; _state.value = GenState_3015_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3015_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3015_.Success(searchUseCase(query)) } }
}
