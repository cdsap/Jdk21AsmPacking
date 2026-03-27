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

data class GenModel_448_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_448_ {
    data class Load(val id: Long) : GenEvent_448_()
    data class Update(val model: GenModel_448_) : GenEvent_448_()
    data class Delete(val id: Long) : GenEvent_448_()
    data object Refresh : GenEvent_448_()
    data class Search(val query: String) : GenEvent_448_()
    data class Filter(val predicate: String) : GenEvent_448_()
}

sealed class GenState_448_ {
    data object Idle : GenState_448_()
    data object Loading : GenState_448_()
    data class Success(val items: List<GenModel_448_>) : GenState_448_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_448_()
    data class Partial(val items: List<GenModel_448_>, val hasMore: Boolean) : GenState_448_()
}

interface GenRepository_448_ {
    suspend fun getAll(): List<GenModel_448_>
    suspend fun getById(id: Long): GenModel_448_?
    suspend fun save(model: GenModel_448_): GenModel_448_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_448_>
}

@Singleton
class GenRepositoryImpl_448_ @Inject constructor() : GenRepository_448_ {
    private val store = mutableMapOf<Long, GenModel_448_>()
    override suspend fun getAll(): List<GenModel_448_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_448_? = store[id]
    override suspend fun save(model: GenModel_448_): GenModel_448_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_448_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_448_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_448_ @Inject constructor(
    private val repository: GenRepositoryImpl_448_
) : GenUseCase_448_<Unit, List<GenModel_448_>> {
    override suspend fun invoke(params: Unit): List<GenModel_448_> = repository.getAll()
}

class GenSaveUseCase_448_ @Inject constructor(
    private val repository: GenRepositoryImpl_448_
) : GenUseCase_448_<GenModel_448_, GenModel_448_> {
    override suspend fun invoke(params: GenModel_448_): GenModel_448_ = repository.save(params)
}

class GenDeleteUseCase_448_ @Inject constructor(
    private val repository: GenRepositoryImpl_448_
) : GenUseCase_448_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_448_ @Inject constructor(
    private val repository: GenRepositoryImpl_448_
) : GenUseCase_448_<String, List<GenModel_448_>> {
    override suspend fun invoke(params: String): List<GenModel_448_> = repository.search(params)
}

abstract class GenMapper_448_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_448_ : GenMapper_448_<GenModel_448_, String>() {
    override fun map(input: GenModel_448_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_448_ : GenMapper_448_<String, GenModel_448_>() {
    override fun map(input: String): GenModel_448_ {
        val parts = input.split(":")
        return GenModel_448_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_448_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_448_,
    private val saveUseCase: GenSaveUseCase_448_,
    private val deleteUseCase: GenDeleteUseCase_448_,
    private val searchUseCase: GenSearchUseCase_448_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_448_>(GenState_448_.Idle)
    val state: StateFlow<GenState_448_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_448_) {
        when (event) {
            is GenEvent_448_.Load -> loadAll()
            is GenEvent_448_.Update -> save(event.model)
            is GenEvent_448_.Delete -> delete(event.id)
            is GenEvent_448_.Refresh -> loadAll()
            is GenEvent_448_.Search -> search(event.query)
            is GenEvent_448_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_448_.Loading; _state.value = GenState_448_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_448_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_448_.Success(searchUseCase(query)) } }
}
