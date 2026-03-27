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

data class GenModel_12_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_12_ {
    data class Load(val id: Long) : GenEvent_12_()
    data class Update(val model: GenModel_12_) : GenEvent_12_()
    data class Delete(val id: Long) : GenEvent_12_()
    data object Refresh : GenEvent_12_()
    data class Search(val query: String) : GenEvent_12_()
    data class Filter(val predicate: String) : GenEvent_12_()
}

sealed class GenState_12_ {
    data object Idle : GenState_12_()
    data object Loading : GenState_12_()
    data class Success(val items: List<GenModel_12_>) : GenState_12_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_12_()
    data class Partial(val items: List<GenModel_12_>, val hasMore: Boolean) : GenState_12_()
}

interface GenRepository_12_ {
    suspend fun getAll(): List<GenModel_12_>
    suspend fun getById(id: Long): GenModel_12_?
    suspend fun save(model: GenModel_12_): GenModel_12_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_12_>
}

@Singleton
class GenRepositoryImpl_12_ @Inject constructor() : GenRepository_12_ {
    private val store = mutableMapOf<Long, GenModel_12_>()
    override suspend fun getAll(): List<GenModel_12_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_12_? = store[id]
    override suspend fun save(model: GenModel_12_): GenModel_12_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_12_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_12_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_12_ @Inject constructor(
    private val repository: GenRepositoryImpl_12_
) : GenUseCase_12_<Unit, List<GenModel_12_>> {
    override suspend fun invoke(params: Unit): List<GenModel_12_> = repository.getAll()
}

class GenSaveUseCase_12_ @Inject constructor(
    private val repository: GenRepositoryImpl_12_
) : GenUseCase_12_<GenModel_12_, GenModel_12_> {
    override suspend fun invoke(params: GenModel_12_): GenModel_12_ = repository.save(params)
}

class GenDeleteUseCase_12_ @Inject constructor(
    private val repository: GenRepositoryImpl_12_
) : GenUseCase_12_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_12_ @Inject constructor(
    private val repository: GenRepositoryImpl_12_
) : GenUseCase_12_<String, List<GenModel_12_>> {
    override suspend fun invoke(params: String): List<GenModel_12_> = repository.search(params)
}

abstract class GenMapper_12_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_12_ : GenMapper_12_<GenModel_12_, String>() {
    override fun map(input: GenModel_12_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_12_ : GenMapper_12_<String, GenModel_12_>() {
    override fun map(input: String): GenModel_12_ {
        val parts = input.split(":")
        return GenModel_12_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_12_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_12_,
    private val saveUseCase: GenSaveUseCase_12_,
    private val deleteUseCase: GenDeleteUseCase_12_,
    private val searchUseCase: GenSearchUseCase_12_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_12_>(GenState_12_.Idle)
    val state: StateFlow<GenState_12_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_12_) {
        when (event) {
            is GenEvent_12_.Load -> loadAll()
            is GenEvent_12_.Update -> save(event.model)
            is GenEvent_12_.Delete -> delete(event.id)
            is GenEvent_12_.Refresh -> loadAll()
            is GenEvent_12_.Search -> search(event.query)
            is GenEvent_12_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_12_.Loading; _state.value = GenState_12_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_12_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_12_.Success(searchUseCase(query)) } }
}
