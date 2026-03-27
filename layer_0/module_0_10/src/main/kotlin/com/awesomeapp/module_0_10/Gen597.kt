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

data class GenModel_597_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_597_ {
    data class Load(val id: Long) : GenEvent_597_()
    data class Update(val model: GenModel_597_) : GenEvent_597_()
    data class Delete(val id: Long) : GenEvent_597_()
    data object Refresh : GenEvent_597_()
    data class Search(val query: String) : GenEvent_597_()
    data class Filter(val predicate: String) : GenEvent_597_()
}

sealed class GenState_597_ {
    data object Idle : GenState_597_()
    data object Loading : GenState_597_()
    data class Success(val items: List<GenModel_597_>) : GenState_597_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_597_()
    data class Partial(val items: List<GenModel_597_>, val hasMore: Boolean) : GenState_597_()
}

interface GenRepository_597_ {
    suspend fun getAll(): List<GenModel_597_>
    suspend fun getById(id: Long): GenModel_597_?
    suspend fun save(model: GenModel_597_): GenModel_597_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_597_>
}

@Singleton
class GenRepositoryImpl_597_ @Inject constructor() : GenRepository_597_ {
    private val store = mutableMapOf<Long, GenModel_597_>()
    override suspend fun getAll(): List<GenModel_597_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_597_? = store[id]
    override suspend fun save(model: GenModel_597_): GenModel_597_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_597_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_597_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_597_ @Inject constructor(
    private val repository: GenRepositoryImpl_597_
) : GenUseCase_597_<Unit, List<GenModel_597_>> {
    override suspend fun invoke(params: Unit): List<GenModel_597_> = repository.getAll()
}

class GenSaveUseCase_597_ @Inject constructor(
    private val repository: GenRepositoryImpl_597_
) : GenUseCase_597_<GenModel_597_, GenModel_597_> {
    override suspend fun invoke(params: GenModel_597_): GenModel_597_ = repository.save(params)
}

class GenDeleteUseCase_597_ @Inject constructor(
    private val repository: GenRepositoryImpl_597_
) : GenUseCase_597_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_597_ @Inject constructor(
    private val repository: GenRepositoryImpl_597_
) : GenUseCase_597_<String, List<GenModel_597_>> {
    override suspend fun invoke(params: String): List<GenModel_597_> = repository.search(params)
}

abstract class GenMapper_597_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_597_ : GenMapper_597_<GenModel_597_, String>() {
    override fun map(input: GenModel_597_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_597_ : GenMapper_597_<String, GenModel_597_>() {
    override fun map(input: String): GenModel_597_ {
        val parts = input.split(":")
        return GenModel_597_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_597_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_597_,
    private val saveUseCase: GenSaveUseCase_597_,
    private val deleteUseCase: GenDeleteUseCase_597_,
    private val searchUseCase: GenSearchUseCase_597_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_597_>(GenState_597_.Idle)
    val state: StateFlow<GenState_597_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_597_) {
        when (event) {
            is GenEvent_597_.Load -> loadAll()
            is GenEvent_597_.Update -> save(event.model)
            is GenEvent_597_.Delete -> delete(event.id)
            is GenEvent_597_.Refresh -> loadAll()
            is GenEvent_597_.Search -> search(event.query)
            is GenEvent_597_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_597_.Loading; _state.value = GenState_597_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_597_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_597_.Success(searchUseCase(query)) } }
}
