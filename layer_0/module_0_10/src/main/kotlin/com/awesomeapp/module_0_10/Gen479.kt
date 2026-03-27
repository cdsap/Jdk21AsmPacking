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

data class GenModel_479_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_479_ {
    data class Load(val id: Long) : GenEvent_479_()
    data class Update(val model: GenModel_479_) : GenEvent_479_()
    data class Delete(val id: Long) : GenEvent_479_()
    data object Refresh : GenEvent_479_()
    data class Search(val query: String) : GenEvent_479_()
    data class Filter(val predicate: String) : GenEvent_479_()
}

sealed class GenState_479_ {
    data object Idle : GenState_479_()
    data object Loading : GenState_479_()
    data class Success(val items: List<GenModel_479_>) : GenState_479_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_479_()
    data class Partial(val items: List<GenModel_479_>, val hasMore: Boolean) : GenState_479_()
}

interface GenRepository_479_ {
    suspend fun getAll(): List<GenModel_479_>
    suspend fun getById(id: Long): GenModel_479_?
    suspend fun save(model: GenModel_479_): GenModel_479_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_479_>
}

@Singleton
class GenRepositoryImpl_479_ @Inject constructor() : GenRepository_479_ {
    private val store = mutableMapOf<Long, GenModel_479_>()
    override suspend fun getAll(): List<GenModel_479_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_479_? = store[id]
    override suspend fun save(model: GenModel_479_): GenModel_479_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_479_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_479_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_479_ @Inject constructor(
    private val repository: GenRepositoryImpl_479_
) : GenUseCase_479_<Unit, List<GenModel_479_>> {
    override suspend fun invoke(params: Unit): List<GenModel_479_> = repository.getAll()
}

class GenSaveUseCase_479_ @Inject constructor(
    private val repository: GenRepositoryImpl_479_
) : GenUseCase_479_<GenModel_479_, GenModel_479_> {
    override suspend fun invoke(params: GenModel_479_): GenModel_479_ = repository.save(params)
}

class GenDeleteUseCase_479_ @Inject constructor(
    private val repository: GenRepositoryImpl_479_
) : GenUseCase_479_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_479_ @Inject constructor(
    private val repository: GenRepositoryImpl_479_
) : GenUseCase_479_<String, List<GenModel_479_>> {
    override suspend fun invoke(params: String): List<GenModel_479_> = repository.search(params)
}

abstract class GenMapper_479_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_479_ : GenMapper_479_<GenModel_479_, String>() {
    override fun map(input: GenModel_479_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_479_ : GenMapper_479_<String, GenModel_479_>() {
    override fun map(input: String): GenModel_479_ {
        val parts = input.split(":")
        return GenModel_479_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_479_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_479_,
    private val saveUseCase: GenSaveUseCase_479_,
    private val deleteUseCase: GenDeleteUseCase_479_,
    private val searchUseCase: GenSearchUseCase_479_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_479_>(GenState_479_.Idle)
    val state: StateFlow<GenState_479_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_479_) {
        when (event) {
            is GenEvent_479_.Load -> loadAll()
            is GenEvent_479_.Update -> save(event.model)
            is GenEvent_479_.Delete -> delete(event.id)
            is GenEvent_479_.Refresh -> loadAll()
            is GenEvent_479_.Search -> search(event.query)
            is GenEvent_479_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_479_.Loading; _state.value = GenState_479_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_479_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_479_.Success(searchUseCase(query)) } }
}
