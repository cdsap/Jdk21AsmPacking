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

data class GenModel_1015_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1015_ {
    data class Load(val id: Long) : GenEvent_1015_()
    data class Update(val model: GenModel_1015_) : GenEvent_1015_()
    data class Delete(val id: Long) : GenEvent_1015_()
    data object Refresh : GenEvent_1015_()
    data class Search(val query: String) : GenEvent_1015_()
    data class Filter(val predicate: String) : GenEvent_1015_()
}

sealed class GenState_1015_ {
    data object Idle : GenState_1015_()
    data object Loading : GenState_1015_()
    data class Success(val items: List<GenModel_1015_>) : GenState_1015_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1015_()
    data class Partial(val items: List<GenModel_1015_>, val hasMore: Boolean) : GenState_1015_()
}

interface GenRepository_1015_ {
    suspend fun getAll(): List<GenModel_1015_>
    suspend fun getById(id: Long): GenModel_1015_?
    suspend fun save(model: GenModel_1015_): GenModel_1015_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1015_>
}

@Singleton
class GenRepositoryImpl_1015_ @Inject constructor() : GenRepository_1015_ {
    private val store = mutableMapOf<Long, GenModel_1015_>()
    override suspend fun getAll(): List<GenModel_1015_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1015_? = store[id]
    override suspend fun save(model: GenModel_1015_): GenModel_1015_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1015_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1015_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1015_ @Inject constructor(
    private val repository: GenRepositoryImpl_1015_
) : GenUseCase_1015_<Unit, List<GenModel_1015_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1015_> = repository.getAll()
}

class GenSaveUseCase_1015_ @Inject constructor(
    private val repository: GenRepositoryImpl_1015_
) : GenUseCase_1015_<GenModel_1015_, GenModel_1015_> {
    override suspend fun invoke(params: GenModel_1015_): GenModel_1015_ = repository.save(params)
}

class GenDeleteUseCase_1015_ @Inject constructor(
    private val repository: GenRepositoryImpl_1015_
) : GenUseCase_1015_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1015_ @Inject constructor(
    private val repository: GenRepositoryImpl_1015_
) : GenUseCase_1015_<String, List<GenModel_1015_>> {
    override suspend fun invoke(params: String): List<GenModel_1015_> = repository.search(params)
}

abstract class GenMapper_1015_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1015_ : GenMapper_1015_<GenModel_1015_, String>() {
    override fun map(input: GenModel_1015_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1015_ : GenMapper_1015_<String, GenModel_1015_>() {
    override fun map(input: String): GenModel_1015_ {
        val parts = input.split(":")
        return GenModel_1015_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1015_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1015_,
    private val saveUseCase: GenSaveUseCase_1015_,
    private val deleteUseCase: GenDeleteUseCase_1015_,
    private val searchUseCase: GenSearchUseCase_1015_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1015_>(GenState_1015_.Idle)
    val state: StateFlow<GenState_1015_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1015_) {
        when (event) {
            is GenEvent_1015_.Load -> loadAll()
            is GenEvent_1015_.Update -> save(event.model)
            is GenEvent_1015_.Delete -> delete(event.id)
            is GenEvent_1015_.Refresh -> loadAll()
            is GenEvent_1015_.Search -> search(event.query)
            is GenEvent_1015_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1015_.Loading; _state.value = GenState_1015_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1015_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1015_.Success(searchUseCase(query)) } }
}
