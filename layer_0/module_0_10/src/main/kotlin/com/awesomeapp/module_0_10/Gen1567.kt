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

data class GenModel_1567_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1567_ {
    data class Load(val id: Long) : GenEvent_1567_()
    data class Update(val model: GenModel_1567_) : GenEvent_1567_()
    data class Delete(val id: Long) : GenEvent_1567_()
    data object Refresh : GenEvent_1567_()
    data class Search(val query: String) : GenEvent_1567_()
    data class Filter(val predicate: String) : GenEvent_1567_()
}

sealed class GenState_1567_ {
    data object Idle : GenState_1567_()
    data object Loading : GenState_1567_()
    data class Success(val items: List<GenModel_1567_>) : GenState_1567_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1567_()
    data class Partial(val items: List<GenModel_1567_>, val hasMore: Boolean) : GenState_1567_()
}

interface GenRepository_1567_ {
    suspend fun getAll(): List<GenModel_1567_>
    suspend fun getById(id: Long): GenModel_1567_?
    suspend fun save(model: GenModel_1567_): GenModel_1567_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1567_>
}

@Singleton
class GenRepositoryImpl_1567_ @Inject constructor() : GenRepository_1567_ {
    private val store = mutableMapOf<Long, GenModel_1567_>()
    override suspend fun getAll(): List<GenModel_1567_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1567_? = store[id]
    override suspend fun save(model: GenModel_1567_): GenModel_1567_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1567_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1567_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1567_ @Inject constructor(
    private val repository: GenRepositoryImpl_1567_
) : GenUseCase_1567_<Unit, List<GenModel_1567_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1567_> = repository.getAll()
}

class GenSaveUseCase_1567_ @Inject constructor(
    private val repository: GenRepositoryImpl_1567_
) : GenUseCase_1567_<GenModel_1567_, GenModel_1567_> {
    override suspend fun invoke(params: GenModel_1567_): GenModel_1567_ = repository.save(params)
}

class GenDeleteUseCase_1567_ @Inject constructor(
    private val repository: GenRepositoryImpl_1567_
) : GenUseCase_1567_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1567_ @Inject constructor(
    private val repository: GenRepositoryImpl_1567_
) : GenUseCase_1567_<String, List<GenModel_1567_>> {
    override suspend fun invoke(params: String): List<GenModel_1567_> = repository.search(params)
}

abstract class GenMapper_1567_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1567_ : GenMapper_1567_<GenModel_1567_, String>() {
    override fun map(input: GenModel_1567_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1567_ : GenMapper_1567_<String, GenModel_1567_>() {
    override fun map(input: String): GenModel_1567_ {
        val parts = input.split(":")
        return GenModel_1567_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1567_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1567_,
    private val saveUseCase: GenSaveUseCase_1567_,
    private val deleteUseCase: GenDeleteUseCase_1567_,
    private val searchUseCase: GenSearchUseCase_1567_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1567_>(GenState_1567_.Idle)
    val state: StateFlow<GenState_1567_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1567_) {
        when (event) {
            is GenEvent_1567_.Load -> loadAll()
            is GenEvent_1567_.Update -> save(event.model)
            is GenEvent_1567_.Delete -> delete(event.id)
            is GenEvent_1567_.Refresh -> loadAll()
            is GenEvent_1567_.Search -> search(event.query)
            is GenEvent_1567_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1567_.Loading; _state.value = GenState_1567_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1567_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1567_.Success(searchUseCase(query)) } }
}
