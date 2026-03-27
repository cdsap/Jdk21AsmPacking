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

data class GenModel_637_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_637_ {
    data class Load(val id: Long) : GenEvent_637_()
    data class Update(val model: GenModel_637_) : GenEvent_637_()
    data class Delete(val id: Long) : GenEvent_637_()
    data object Refresh : GenEvent_637_()
    data class Search(val query: String) : GenEvent_637_()
    data class Filter(val predicate: String) : GenEvent_637_()
}

sealed class GenState_637_ {
    data object Idle : GenState_637_()
    data object Loading : GenState_637_()
    data class Success(val items: List<GenModel_637_>) : GenState_637_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_637_()
    data class Partial(val items: List<GenModel_637_>, val hasMore: Boolean) : GenState_637_()
}

interface GenRepository_637_ {
    suspend fun getAll(): List<GenModel_637_>
    suspend fun getById(id: Long): GenModel_637_?
    suspend fun save(model: GenModel_637_): GenModel_637_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_637_>
}

@Singleton
class GenRepositoryImpl_637_ @Inject constructor() : GenRepository_637_ {
    private val store = mutableMapOf<Long, GenModel_637_>()
    override suspend fun getAll(): List<GenModel_637_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_637_? = store[id]
    override suspend fun save(model: GenModel_637_): GenModel_637_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_637_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_637_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_637_ @Inject constructor(
    private val repository: GenRepositoryImpl_637_
) : GenUseCase_637_<Unit, List<GenModel_637_>> {
    override suspend fun invoke(params: Unit): List<GenModel_637_> = repository.getAll()
}

class GenSaveUseCase_637_ @Inject constructor(
    private val repository: GenRepositoryImpl_637_
) : GenUseCase_637_<GenModel_637_, GenModel_637_> {
    override suspend fun invoke(params: GenModel_637_): GenModel_637_ = repository.save(params)
}

class GenDeleteUseCase_637_ @Inject constructor(
    private val repository: GenRepositoryImpl_637_
) : GenUseCase_637_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_637_ @Inject constructor(
    private val repository: GenRepositoryImpl_637_
) : GenUseCase_637_<String, List<GenModel_637_>> {
    override suspend fun invoke(params: String): List<GenModel_637_> = repository.search(params)
}

abstract class GenMapper_637_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_637_ : GenMapper_637_<GenModel_637_, String>() {
    override fun map(input: GenModel_637_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_637_ : GenMapper_637_<String, GenModel_637_>() {
    override fun map(input: String): GenModel_637_ {
        val parts = input.split(":")
        return GenModel_637_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_637_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_637_,
    private val saveUseCase: GenSaveUseCase_637_,
    private val deleteUseCase: GenDeleteUseCase_637_,
    private val searchUseCase: GenSearchUseCase_637_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_637_>(GenState_637_.Idle)
    val state: StateFlow<GenState_637_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_637_) {
        when (event) {
            is GenEvent_637_.Load -> loadAll()
            is GenEvent_637_.Update -> save(event.model)
            is GenEvent_637_.Delete -> delete(event.id)
            is GenEvent_637_.Refresh -> loadAll()
            is GenEvent_637_.Search -> search(event.query)
            is GenEvent_637_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_637_.Loading; _state.value = GenState_637_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_637_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_637_.Success(searchUseCase(query)) } }
}
