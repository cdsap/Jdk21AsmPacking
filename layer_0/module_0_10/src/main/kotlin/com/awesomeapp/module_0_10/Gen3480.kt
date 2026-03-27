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

data class GenModel_3480_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3480_ {
    data class Load(val id: Long) : GenEvent_3480_()
    data class Update(val model: GenModel_3480_) : GenEvent_3480_()
    data class Delete(val id: Long) : GenEvent_3480_()
    data object Refresh : GenEvent_3480_()
    data class Search(val query: String) : GenEvent_3480_()
    data class Filter(val predicate: String) : GenEvent_3480_()
}

sealed class GenState_3480_ {
    data object Idle : GenState_3480_()
    data object Loading : GenState_3480_()
    data class Success(val items: List<GenModel_3480_>) : GenState_3480_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3480_()
    data class Partial(val items: List<GenModel_3480_>, val hasMore: Boolean) : GenState_3480_()
}

interface GenRepository_3480_ {
    suspend fun getAll(): List<GenModel_3480_>
    suspend fun getById(id: Long): GenModel_3480_?
    suspend fun save(model: GenModel_3480_): GenModel_3480_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3480_>
}

@Singleton
class GenRepositoryImpl_3480_ @Inject constructor() : GenRepository_3480_ {
    private val store = mutableMapOf<Long, GenModel_3480_>()
    override suspend fun getAll(): List<GenModel_3480_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3480_? = store[id]
    override suspend fun save(model: GenModel_3480_): GenModel_3480_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3480_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3480_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3480_ @Inject constructor(
    private val repository: GenRepositoryImpl_3480_
) : GenUseCase_3480_<Unit, List<GenModel_3480_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3480_> = repository.getAll()
}

class GenSaveUseCase_3480_ @Inject constructor(
    private val repository: GenRepositoryImpl_3480_
) : GenUseCase_3480_<GenModel_3480_, GenModel_3480_> {
    override suspend fun invoke(params: GenModel_3480_): GenModel_3480_ = repository.save(params)
}

class GenDeleteUseCase_3480_ @Inject constructor(
    private val repository: GenRepositoryImpl_3480_
) : GenUseCase_3480_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3480_ @Inject constructor(
    private val repository: GenRepositoryImpl_3480_
) : GenUseCase_3480_<String, List<GenModel_3480_>> {
    override suspend fun invoke(params: String): List<GenModel_3480_> = repository.search(params)
}

abstract class GenMapper_3480_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3480_ : GenMapper_3480_<GenModel_3480_, String>() {
    override fun map(input: GenModel_3480_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3480_ : GenMapper_3480_<String, GenModel_3480_>() {
    override fun map(input: String): GenModel_3480_ {
        val parts = input.split(":")
        return GenModel_3480_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3480_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3480_,
    private val saveUseCase: GenSaveUseCase_3480_,
    private val deleteUseCase: GenDeleteUseCase_3480_,
    private val searchUseCase: GenSearchUseCase_3480_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3480_>(GenState_3480_.Idle)
    val state: StateFlow<GenState_3480_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3480_) {
        when (event) {
            is GenEvent_3480_.Load -> loadAll()
            is GenEvent_3480_.Update -> save(event.model)
            is GenEvent_3480_.Delete -> delete(event.id)
            is GenEvent_3480_.Refresh -> loadAll()
            is GenEvent_3480_.Search -> search(event.query)
            is GenEvent_3480_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3480_.Loading; _state.value = GenState_3480_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3480_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3480_.Success(searchUseCase(query)) } }
}
