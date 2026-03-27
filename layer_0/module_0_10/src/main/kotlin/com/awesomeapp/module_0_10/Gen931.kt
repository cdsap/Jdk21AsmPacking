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

data class GenModel_931_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_931_ {
    data class Load(val id: Long) : GenEvent_931_()
    data class Update(val model: GenModel_931_) : GenEvent_931_()
    data class Delete(val id: Long) : GenEvent_931_()
    data object Refresh : GenEvent_931_()
    data class Search(val query: String) : GenEvent_931_()
    data class Filter(val predicate: String) : GenEvent_931_()
}

sealed class GenState_931_ {
    data object Idle : GenState_931_()
    data object Loading : GenState_931_()
    data class Success(val items: List<GenModel_931_>) : GenState_931_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_931_()
    data class Partial(val items: List<GenModel_931_>, val hasMore: Boolean) : GenState_931_()
}

interface GenRepository_931_ {
    suspend fun getAll(): List<GenModel_931_>
    suspend fun getById(id: Long): GenModel_931_?
    suspend fun save(model: GenModel_931_): GenModel_931_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_931_>
}

@Singleton
class GenRepositoryImpl_931_ @Inject constructor() : GenRepository_931_ {
    private val store = mutableMapOf<Long, GenModel_931_>()
    override suspend fun getAll(): List<GenModel_931_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_931_? = store[id]
    override suspend fun save(model: GenModel_931_): GenModel_931_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_931_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_931_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_931_ @Inject constructor(
    private val repository: GenRepositoryImpl_931_
) : GenUseCase_931_<Unit, List<GenModel_931_>> {
    override suspend fun invoke(params: Unit): List<GenModel_931_> = repository.getAll()
}

class GenSaveUseCase_931_ @Inject constructor(
    private val repository: GenRepositoryImpl_931_
) : GenUseCase_931_<GenModel_931_, GenModel_931_> {
    override suspend fun invoke(params: GenModel_931_): GenModel_931_ = repository.save(params)
}

class GenDeleteUseCase_931_ @Inject constructor(
    private val repository: GenRepositoryImpl_931_
) : GenUseCase_931_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_931_ @Inject constructor(
    private val repository: GenRepositoryImpl_931_
) : GenUseCase_931_<String, List<GenModel_931_>> {
    override suspend fun invoke(params: String): List<GenModel_931_> = repository.search(params)
}

abstract class GenMapper_931_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_931_ : GenMapper_931_<GenModel_931_, String>() {
    override fun map(input: GenModel_931_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_931_ : GenMapper_931_<String, GenModel_931_>() {
    override fun map(input: String): GenModel_931_ {
        val parts = input.split(":")
        return GenModel_931_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_931_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_931_,
    private val saveUseCase: GenSaveUseCase_931_,
    private val deleteUseCase: GenDeleteUseCase_931_,
    private val searchUseCase: GenSearchUseCase_931_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_931_>(GenState_931_.Idle)
    val state: StateFlow<GenState_931_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_931_) {
        when (event) {
            is GenEvent_931_.Load -> loadAll()
            is GenEvent_931_.Update -> save(event.model)
            is GenEvent_931_.Delete -> delete(event.id)
            is GenEvent_931_.Refresh -> loadAll()
            is GenEvent_931_.Search -> search(event.query)
            is GenEvent_931_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_931_.Loading; _state.value = GenState_931_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_931_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_931_.Success(searchUseCase(query)) } }
}
