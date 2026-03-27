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

data class GenModel_323_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_323_ {
    data class Load(val id: Long) : GenEvent_323_()
    data class Update(val model: GenModel_323_) : GenEvent_323_()
    data class Delete(val id: Long) : GenEvent_323_()
    data object Refresh : GenEvent_323_()
    data class Search(val query: String) : GenEvent_323_()
    data class Filter(val predicate: String) : GenEvent_323_()
}

sealed class GenState_323_ {
    data object Idle : GenState_323_()
    data object Loading : GenState_323_()
    data class Success(val items: List<GenModel_323_>) : GenState_323_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_323_()
    data class Partial(val items: List<GenModel_323_>, val hasMore: Boolean) : GenState_323_()
}

interface GenRepository_323_ {
    suspend fun getAll(): List<GenModel_323_>
    suspend fun getById(id: Long): GenModel_323_?
    suspend fun save(model: GenModel_323_): GenModel_323_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_323_>
}

@Singleton
class GenRepositoryImpl_323_ @Inject constructor() : GenRepository_323_ {
    private val store = mutableMapOf<Long, GenModel_323_>()
    override suspend fun getAll(): List<GenModel_323_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_323_? = store[id]
    override suspend fun save(model: GenModel_323_): GenModel_323_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_323_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_323_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_323_ @Inject constructor(
    private val repository: GenRepositoryImpl_323_
) : GenUseCase_323_<Unit, List<GenModel_323_>> {
    override suspend fun invoke(params: Unit): List<GenModel_323_> = repository.getAll()
}

class GenSaveUseCase_323_ @Inject constructor(
    private val repository: GenRepositoryImpl_323_
) : GenUseCase_323_<GenModel_323_, GenModel_323_> {
    override suspend fun invoke(params: GenModel_323_): GenModel_323_ = repository.save(params)
}

class GenDeleteUseCase_323_ @Inject constructor(
    private val repository: GenRepositoryImpl_323_
) : GenUseCase_323_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_323_ @Inject constructor(
    private val repository: GenRepositoryImpl_323_
) : GenUseCase_323_<String, List<GenModel_323_>> {
    override suspend fun invoke(params: String): List<GenModel_323_> = repository.search(params)
}

abstract class GenMapper_323_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_323_ : GenMapper_323_<GenModel_323_, String>() {
    override fun map(input: GenModel_323_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_323_ : GenMapper_323_<String, GenModel_323_>() {
    override fun map(input: String): GenModel_323_ {
        val parts = input.split(":")
        return GenModel_323_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_323_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_323_,
    private val saveUseCase: GenSaveUseCase_323_,
    private val deleteUseCase: GenDeleteUseCase_323_,
    private val searchUseCase: GenSearchUseCase_323_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_323_>(GenState_323_.Idle)
    val state: StateFlow<GenState_323_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_323_) {
        when (event) {
            is GenEvent_323_.Load -> loadAll()
            is GenEvent_323_.Update -> save(event.model)
            is GenEvent_323_.Delete -> delete(event.id)
            is GenEvent_323_.Refresh -> loadAll()
            is GenEvent_323_.Search -> search(event.query)
            is GenEvent_323_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_323_.Loading; _state.value = GenState_323_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_323_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_323_.Success(searchUseCase(query)) } }
}
