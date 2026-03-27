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

data class GenModel_149_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_149_ {
    data class Load(val id: Long) : GenEvent_149_()
    data class Update(val model: GenModel_149_) : GenEvent_149_()
    data class Delete(val id: Long) : GenEvent_149_()
    data object Refresh : GenEvent_149_()
    data class Search(val query: String) : GenEvent_149_()
    data class Filter(val predicate: String) : GenEvent_149_()
}

sealed class GenState_149_ {
    data object Idle : GenState_149_()
    data object Loading : GenState_149_()
    data class Success(val items: List<GenModel_149_>) : GenState_149_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_149_()
    data class Partial(val items: List<GenModel_149_>, val hasMore: Boolean) : GenState_149_()
}

interface GenRepository_149_ {
    suspend fun getAll(): List<GenModel_149_>
    suspend fun getById(id: Long): GenModel_149_?
    suspend fun save(model: GenModel_149_): GenModel_149_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_149_>
}

@Singleton
class GenRepositoryImpl_149_ @Inject constructor() : GenRepository_149_ {
    private val store = mutableMapOf<Long, GenModel_149_>()
    override suspend fun getAll(): List<GenModel_149_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_149_? = store[id]
    override suspend fun save(model: GenModel_149_): GenModel_149_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_149_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_149_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_149_ @Inject constructor(
    private val repository: GenRepositoryImpl_149_
) : GenUseCase_149_<Unit, List<GenModel_149_>> {
    override suspend fun invoke(params: Unit): List<GenModel_149_> = repository.getAll()
}

class GenSaveUseCase_149_ @Inject constructor(
    private val repository: GenRepositoryImpl_149_
) : GenUseCase_149_<GenModel_149_, GenModel_149_> {
    override suspend fun invoke(params: GenModel_149_): GenModel_149_ = repository.save(params)
}

class GenDeleteUseCase_149_ @Inject constructor(
    private val repository: GenRepositoryImpl_149_
) : GenUseCase_149_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_149_ @Inject constructor(
    private val repository: GenRepositoryImpl_149_
) : GenUseCase_149_<String, List<GenModel_149_>> {
    override suspend fun invoke(params: String): List<GenModel_149_> = repository.search(params)
}

abstract class GenMapper_149_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_149_ : GenMapper_149_<GenModel_149_, String>() {
    override fun map(input: GenModel_149_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_149_ : GenMapper_149_<String, GenModel_149_>() {
    override fun map(input: String): GenModel_149_ {
        val parts = input.split(":")
        return GenModel_149_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_149_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_149_,
    private val saveUseCase: GenSaveUseCase_149_,
    private val deleteUseCase: GenDeleteUseCase_149_,
    private val searchUseCase: GenSearchUseCase_149_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_149_>(GenState_149_.Idle)
    val state: StateFlow<GenState_149_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_149_) {
        when (event) {
            is GenEvent_149_.Load -> loadAll()
            is GenEvent_149_.Update -> save(event.model)
            is GenEvent_149_.Delete -> delete(event.id)
            is GenEvent_149_.Refresh -> loadAll()
            is GenEvent_149_.Search -> search(event.query)
            is GenEvent_149_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_149_.Loading; _state.value = GenState_149_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_149_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_149_.Success(searchUseCase(query)) } }
}
