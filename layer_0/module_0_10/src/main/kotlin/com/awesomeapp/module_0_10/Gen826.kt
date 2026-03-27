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

data class GenModel_826_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_826_ {
    data class Load(val id: Long) : GenEvent_826_()
    data class Update(val model: GenModel_826_) : GenEvent_826_()
    data class Delete(val id: Long) : GenEvent_826_()
    data object Refresh : GenEvent_826_()
    data class Search(val query: String) : GenEvent_826_()
    data class Filter(val predicate: String) : GenEvent_826_()
}

sealed class GenState_826_ {
    data object Idle : GenState_826_()
    data object Loading : GenState_826_()
    data class Success(val items: List<GenModel_826_>) : GenState_826_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_826_()
    data class Partial(val items: List<GenModel_826_>, val hasMore: Boolean) : GenState_826_()
}

interface GenRepository_826_ {
    suspend fun getAll(): List<GenModel_826_>
    suspend fun getById(id: Long): GenModel_826_?
    suspend fun save(model: GenModel_826_): GenModel_826_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_826_>
}

@Singleton
class GenRepositoryImpl_826_ @Inject constructor() : GenRepository_826_ {
    private val store = mutableMapOf<Long, GenModel_826_>()
    override suspend fun getAll(): List<GenModel_826_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_826_? = store[id]
    override suspend fun save(model: GenModel_826_): GenModel_826_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_826_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_826_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_826_ @Inject constructor(
    private val repository: GenRepositoryImpl_826_
) : GenUseCase_826_<Unit, List<GenModel_826_>> {
    override suspend fun invoke(params: Unit): List<GenModel_826_> = repository.getAll()
}

class GenSaveUseCase_826_ @Inject constructor(
    private val repository: GenRepositoryImpl_826_
) : GenUseCase_826_<GenModel_826_, GenModel_826_> {
    override suspend fun invoke(params: GenModel_826_): GenModel_826_ = repository.save(params)
}

class GenDeleteUseCase_826_ @Inject constructor(
    private val repository: GenRepositoryImpl_826_
) : GenUseCase_826_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_826_ @Inject constructor(
    private val repository: GenRepositoryImpl_826_
) : GenUseCase_826_<String, List<GenModel_826_>> {
    override suspend fun invoke(params: String): List<GenModel_826_> = repository.search(params)
}

abstract class GenMapper_826_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_826_ : GenMapper_826_<GenModel_826_, String>() {
    override fun map(input: GenModel_826_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_826_ : GenMapper_826_<String, GenModel_826_>() {
    override fun map(input: String): GenModel_826_ {
        val parts = input.split(":")
        return GenModel_826_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_826_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_826_,
    private val saveUseCase: GenSaveUseCase_826_,
    private val deleteUseCase: GenDeleteUseCase_826_,
    private val searchUseCase: GenSearchUseCase_826_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_826_>(GenState_826_.Idle)
    val state: StateFlow<GenState_826_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_826_) {
        when (event) {
            is GenEvent_826_.Load -> loadAll()
            is GenEvent_826_.Update -> save(event.model)
            is GenEvent_826_.Delete -> delete(event.id)
            is GenEvent_826_.Refresh -> loadAll()
            is GenEvent_826_.Search -> search(event.query)
            is GenEvent_826_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_826_.Loading; _state.value = GenState_826_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_826_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_826_.Success(searchUseCase(query)) } }
}
