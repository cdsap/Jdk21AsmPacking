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

data class GenModel_832_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_832_ {
    data class Load(val id: Long) : GenEvent_832_()
    data class Update(val model: GenModel_832_) : GenEvent_832_()
    data class Delete(val id: Long) : GenEvent_832_()
    data object Refresh : GenEvent_832_()
    data class Search(val query: String) : GenEvent_832_()
    data class Filter(val predicate: String) : GenEvent_832_()
}

sealed class GenState_832_ {
    data object Idle : GenState_832_()
    data object Loading : GenState_832_()
    data class Success(val items: List<GenModel_832_>) : GenState_832_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_832_()
    data class Partial(val items: List<GenModel_832_>, val hasMore: Boolean) : GenState_832_()
}

interface GenRepository_832_ {
    suspend fun getAll(): List<GenModel_832_>
    suspend fun getById(id: Long): GenModel_832_?
    suspend fun save(model: GenModel_832_): GenModel_832_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_832_>
}

@Singleton
class GenRepositoryImpl_832_ @Inject constructor() : GenRepository_832_ {
    private val store = mutableMapOf<Long, GenModel_832_>()
    override suspend fun getAll(): List<GenModel_832_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_832_? = store[id]
    override suspend fun save(model: GenModel_832_): GenModel_832_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_832_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_832_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_832_ @Inject constructor(
    private val repository: GenRepositoryImpl_832_
) : GenUseCase_832_<Unit, List<GenModel_832_>> {
    override suspend fun invoke(params: Unit): List<GenModel_832_> = repository.getAll()
}

class GenSaveUseCase_832_ @Inject constructor(
    private val repository: GenRepositoryImpl_832_
) : GenUseCase_832_<GenModel_832_, GenModel_832_> {
    override suspend fun invoke(params: GenModel_832_): GenModel_832_ = repository.save(params)
}

class GenDeleteUseCase_832_ @Inject constructor(
    private val repository: GenRepositoryImpl_832_
) : GenUseCase_832_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_832_ @Inject constructor(
    private val repository: GenRepositoryImpl_832_
) : GenUseCase_832_<String, List<GenModel_832_>> {
    override suspend fun invoke(params: String): List<GenModel_832_> = repository.search(params)
}

abstract class GenMapper_832_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_832_ : GenMapper_832_<GenModel_832_, String>() {
    override fun map(input: GenModel_832_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_832_ : GenMapper_832_<String, GenModel_832_>() {
    override fun map(input: String): GenModel_832_ {
        val parts = input.split(":")
        return GenModel_832_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_832_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_832_,
    private val saveUseCase: GenSaveUseCase_832_,
    private val deleteUseCase: GenDeleteUseCase_832_,
    private val searchUseCase: GenSearchUseCase_832_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_832_>(GenState_832_.Idle)
    val state: StateFlow<GenState_832_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_832_) {
        when (event) {
            is GenEvent_832_.Load -> loadAll()
            is GenEvent_832_.Update -> save(event.model)
            is GenEvent_832_.Delete -> delete(event.id)
            is GenEvent_832_.Refresh -> loadAll()
            is GenEvent_832_.Search -> search(event.query)
            is GenEvent_832_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_832_.Loading; _state.value = GenState_832_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_832_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_832_.Success(searchUseCase(query)) } }
}
