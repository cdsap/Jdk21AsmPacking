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

data class GenModel_249_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_249_ {
    data class Load(val id: Long) : GenEvent_249_()
    data class Update(val model: GenModel_249_) : GenEvent_249_()
    data class Delete(val id: Long) : GenEvent_249_()
    data object Refresh : GenEvent_249_()
    data class Search(val query: String) : GenEvent_249_()
    data class Filter(val predicate: String) : GenEvent_249_()
}

sealed class GenState_249_ {
    data object Idle : GenState_249_()
    data object Loading : GenState_249_()
    data class Success(val items: List<GenModel_249_>) : GenState_249_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_249_()
    data class Partial(val items: List<GenModel_249_>, val hasMore: Boolean) : GenState_249_()
}

interface GenRepository_249_ {
    suspend fun getAll(): List<GenModel_249_>
    suspend fun getById(id: Long): GenModel_249_?
    suspend fun save(model: GenModel_249_): GenModel_249_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_249_>
}

@Singleton
class GenRepositoryImpl_249_ @Inject constructor() : GenRepository_249_ {
    private val store = mutableMapOf<Long, GenModel_249_>()
    override suspend fun getAll(): List<GenModel_249_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_249_? = store[id]
    override suspend fun save(model: GenModel_249_): GenModel_249_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_249_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_249_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_249_ @Inject constructor(
    private val repository: GenRepositoryImpl_249_
) : GenUseCase_249_<Unit, List<GenModel_249_>> {
    override suspend fun invoke(params: Unit): List<GenModel_249_> = repository.getAll()
}

class GenSaveUseCase_249_ @Inject constructor(
    private val repository: GenRepositoryImpl_249_
) : GenUseCase_249_<GenModel_249_, GenModel_249_> {
    override suspend fun invoke(params: GenModel_249_): GenModel_249_ = repository.save(params)
}

class GenDeleteUseCase_249_ @Inject constructor(
    private val repository: GenRepositoryImpl_249_
) : GenUseCase_249_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_249_ @Inject constructor(
    private val repository: GenRepositoryImpl_249_
) : GenUseCase_249_<String, List<GenModel_249_>> {
    override suspend fun invoke(params: String): List<GenModel_249_> = repository.search(params)
}

abstract class GenMapper_249_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_249_ : GenMapper_249_<GenModel_249_, String>() {
    override fun map(input: GenModel_249_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_249_ : GenMapper_249_<String, GenModel_249_>() {
    override fun map(input: String): GenModel_249_ {
        val parts = input.split(":")
        return GenModel_249_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_249_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_249_,
    private val saveUseCase: GenSaveUseCase_249_,
    private val deleteUseCase: GenDeleteUseCase_249_,
    private val searchUseCase: GenSearchUseCase_249_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_249_>(GenState_249_.Idle)
    val state: StateFlow<GenState_249_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_249_) {
        when (event) {
            is GenEvent_249_.Load -> loadAll()
            is GenEvent_249_.Update -> save(event.model)
            is GenEvent_249_.Delete -> delete(event.id)
            is GenEvent_249_.Refresh -> loadAll()
            is GenEvent_249_.Search -> search(event.query)
            is GenEvent_249_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_249_.Loading; _state.value = GenState_249_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_249_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_249_.Success(searchUseCase(query)) } }
}
