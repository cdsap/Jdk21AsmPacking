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

data class GenModel_485_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_485_ {
    data class Load(val id: Long) : GenEvent_485_()
    data class Update(val model: GenModel_485_) : GenEvent_485_()
    data class Delete(val id: Long) : GenEvent_485_()
    data object Refresh : GenEvent_485_()
    data class Search(val query: String) : GenEvent_485_()
    data class Filter(val predicate: String) : GenEvent_485_()
}

sealed class GenState_485_ {
    data object Idle : GenState_485_()
    data object Loading : GenState_485_()
    data class Success(val items: List<GenModel_485_>) : GenState_485_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_485_()
    data class Partial(val items: List<GenModel_485_>, val hasMore: Boolean) : GenState_485_()
}

interface GenRepository_485_ {
    suspend fun getAll(): List<GenModel_485_>
    suspend fun getById(id: Long): GenModel_485_?
    suspend fun save(model: GenModel_485_): GenModel_485_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_485_>
}

@Singleton
class GenRepositoryImpl_485_ @Inject constructor() : GenRepository_485_ {
    private val store = mutableMapOf<Long, GenModel_485_>()
    override suspend fun getAll(): List<GenModel_485_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_485_? = store[id]
    override suspend fun save(model: GenModel_485_): GenModel_485_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_485_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_485_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_485_ @Inject constructor(
    private val repository: GenRepositoryImpl_485_
) : GenUseCase_485_<Unit, List<GenModel_485_>> {
    override suspend fun invoke(params: Unit): List<GenModel_485_> = repository.getAll()
}

class GenSaveUseCase_485_ @Inject constructor(
    private val repository: GenRepositoryImpl_485_
) : GenUseCase_485_<GenModel_485_, GenModel_485_> {
    override suspend fun invoke(params: GenModel_485_): GenModel_485_ = repository.save(params)
}

class GenDeleteUseCase_485_ @Inject constructor(
    private val repository: GenRepositoryImpl_485_
) : GenUseCase_485_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_485_ @Inject constructor(
    private val repository: GenRepositoryImpl_485_
) : GenUseCase_485_<String, List<GenModel_485_>> {
    override suspend fun invoke(params: String): List<GenModel_485_> = repository.search(params)
}

abstract class GenMapper_485_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_485_ : GenMapper_485_<GenModel_485_, String>() {
    override fun map(input: GenModel_485_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_485_ : GenMapper_485_<String, GenModel_485_>() {
    override fun map(input: String): GenModel_485_ {
        val parts = input.split(":")
        return GenModel_485_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_485_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_485_,
    private val saveUseCase: GenSaveUseCase_485_,
    private val deleteUseCase: GenDeleteUseCase_485_,
    private val searchUseCase: GenSearchUseCase_485_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_485_>(GenState_485_.Idle)
    val state: StateFlow<GenState_485_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_485_) {
        when (event) {
            is GenEvent_485_.Load -> loadAll()
            is GenEvent_485_.Update -> save(event.model)
            is GenEvent_485_.Delete -> delete(event.id)
            is GenEvent_485_.Refresh -> loadAll()
            is GenEvent_485_.Search -> search(event.query)
            is GenEvent_485_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_485_.Loading; _state.value = GenState_485_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_485_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_485_.Success(searchUseCase(query)) } }
}
