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

data class GenModel_1572_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1572_ {
    data class Load(val id: Long) : GenEvent_1572_()
    data class Update(val model: GenModel_1572_) : GenEvent_1572_()
    data class Delete(val id: Long) : GenEvent_1572_()
    data object Refresh : GenEvent_1572_()
    data class Search(val query: String) : GenEvent_1572_()
    data class Filter(val predicate: String) : GenEvent_1572_()
}

sealed class GenState_1572_ {
    data object Idle : GenState_1572_()
    data object Loading : GenState_1572_()
    data class Success(val items: List<GenModel_1572_>) : GenState_1572_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1572_()
    data class Partial(val items: List<GenModel_1572_>, val hasMore: Boolean) : GenState_1572_()
}

interface GenRepository_1572_ {
    suspend fun getAll(): List<GenModel_1572_>
    suspend fun getById(id: Long): GenModel_1572_?
    suspend fun save(model: GenModel_1572_): GenModel_1572_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1572_>
}

@Singleton
class GenRepositoryImpl_1572_ @Inject constructor() : GenRepository_1572_ {
    private val store = mutableMapOf<Long, GenModel_1572_>()
    override suspend fun getAll(): List<GenModel_1572_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1572_? = store[id]
    override suspend fun save(model: GenModel_1572_): GenModel_1572_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1572_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1572_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1572_ @Inject constructor(
    private val repository: GenRepositoryImpl_1572_
) : GenUseCase_1572_<Unit, List<GenModel_1572_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1572_> = repository.getAll()
}

class GenSaveUseCase_1572_ @Inject constructor(
    private val repository: GenRepositoryImpl_1572_
) : GenUseCase_1572_<GenModel_1572_, GenModel_1572_> {
    override suspend fun invoke(params: GenModel_1572_): GenModel_1572_ = repository.save(params)
}

class GenDeleteUseCase_1572_ @Inject constructor(
    private val repository: GenRepositoryImpl_1572_
) : GenUseCase_1572_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1572_ @Inject constructor(
    private val repository: GenRepositoryImpl_1572_
) : GenUseCase_1572_<String, List<GenModel_1572_>> {
    override suspend fun invoke(params: String): List<GenModel_1572_> = repository.search(params)
}

abstract class GenMapper_1572_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1572_ : GenMapper_1572_<GenModel_1572_, String>() {
    override fun map(input: GenModel_1572_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1572_ : GenMapper_1572_<String, GenModel_1572_>() {
    override fun map(input: String): GenModel_1572_ {
        val parts = input.split(":")
        return GenModel_1572_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1572_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1572_,
    private val saveUseCase: GenSaveUseCase_1572_,
    private val deleteUseCase: GenDeleteUseCase_1572_,
    private val searchUseCase: GenSearchUseCase_1572_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1572_>(GenState_1572_.Idle)
    val state: StateFlow<GenState_1572_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1572_) {
        when (event) {
            is GenEvent_1572_.Load -> loadAll()
            is GenEvent_1572_.Update -> save(event.model)
            is GenEvent_1572_.Delete -> delete(event.id)
            is GenEvent_1572_.Refresh -> loadAll()
            is GenEvent_1572_.Search -> search(event.query)
            is GenEvent_1572_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1572_.Loading; _state.value = GenState_1572_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1572_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1572_.Success(searchUseCase(query)) } }
}
