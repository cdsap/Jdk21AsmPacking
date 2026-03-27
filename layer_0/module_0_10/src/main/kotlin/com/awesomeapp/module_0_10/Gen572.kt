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

data class GenModel_572_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_572_ {
    data class Load(val id: Long) : GenEvent_572_()
    data class Update(val model: GenModel_572_) : GenEvent_572_()
    data class Delete(val id: Long) : GenEvent_572_()
    data object Refresh : GenEvent_572_()
    data class Search(val query: String) : GenEvent_572_()
    data class Filter(val predicate: String) : GenEvent_572_()
}

sealed class GenState_572_ {
    data object Idle : GenState_572_()
    data object Loading : GenState_572_()
    data class Success(val items: List<GenModel_572_>) : GenState_572_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_572_()
    data class Partial(val items: List<GenModel_572_>, val hasMore: Boolean) : GenState_572_()
}

interface GenRepository_572_ {
    suspend fun getAll(): List<GenModel_572_>
    suspend fun getById(id: Long): GenModel_572_?
    suspend fun save(model: GenModel_572_): GenModel_572_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_572_>
}

@Singleton
class GenRepositoryImpl_572_ @Inject constructor() : GenRepository_572_ {
    private val store = mutableMapOf<Long, GenModel_572_>()
    override suspend fun getAll(): List<GenModel_572_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_572_? = store[id]
    override suspend fun save(model: GenModel_572_): GenModel_572_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_572_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_572_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_572_ @Inject constructor(
    private val repository: GenRepositoryImpl_572_
) : GenUseCase_572_<Unit, List<GenModel_572_>> {
    override suspend fun invoke(params: Unit): List<GenModel_572_> = repository.getAll()
}

class GenSaveUseCase_572_ @Inject constructor(
    private val repository: GenRepositoryImpl_572_
) : GenUseCase_572_<GenModel_572_, GenModel_572_> {
    override suspend fun invoke(params: GenModel_572_): GenModel_572_ = repository.save(params)
}

class GenDeleteUseCase_572_ @Inject constructor(
    private val repository: GenRepositoryImpl_572_
) : GenUseCase_572_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_572_ @Inject constructor(
    private val repository: GenRepositoryImpl_572_
) : GenUseCase_572_<String, List<GenModel_572_>> {
    override suspend fun invoke(params: String): List<GenModel_572_> = repository.search(params)
}

abstract class GenMapper_572_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_572_ : GenMapper_572_<GenModel_572_, String>() {
    override fun map(input: GenModel_572_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_572_ : GenMapper_572_<String, GenModel_572_>() {
    override fun map(input: String): GenModel_572_ {
        val parts = input.split(":")
        return GenModel_572_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_572_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_572_,
    private val saveUseCase: GenSaveUseCase_572_,
    private val deleteUseCase: GenDeleteUseCase_572_,
    private val searchUseCase: GenSearchUseCase_572_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_572_>(GenState_572_.Idle)
    val state: StateFlow<GenState_572_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_572_) {
        when (event) {
            is GenEvent_572_.Load -> loadAll()
            is GenEvent_572_.Update -> save(event.model)
            is GenEvent_572_.Delete -> delete(event.id)
            is GenEvent_572_.Refresh -> loadAll()
            is GenEvent_572_.Search -> search(event.query)
            is GenEvent_572_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_572_.Loading; _state.value = GenState_572_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_572_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_572_.Success(searchUseCase(query)) } }
}
