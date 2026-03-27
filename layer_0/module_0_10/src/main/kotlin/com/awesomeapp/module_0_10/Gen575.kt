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

data class GenModel_575_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_575_ {
    data class Load(val id: Long) : GenEvent_575_()
    data class Update(val model: GenModel_575_) : GenEvent_575_()
    data class Delete(val id: Long) : GenEvent_575_()
    data object Refresh : GenEvent_575_()
    data class Search(val query: String) : GenEvent_575_()
    data class Filter(val predicate: String) : GenEvent_575_()
}

sealed class GenState_575_ {
    data object Idle : GenState_575_()
    data object Loading : GenState_575_()
    data class Success(val items: List<GenModel_575_>) : GenState_575_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_575_()
    data class Partial(val items: List<GenModel_575_>, val hasMore: Boolean) : GenState_575_()
}

interface GenRepository_575_ {
    suspend fun getAll(): List<GenModel_575_>
    suspend fun getById(id: Long): GenModel_575_?
    suspend fun save(model: GenModel_575_): GenModel_575_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_575_>
}

@Singleton
class GenRepositoryImpl_575_ @Inject constructor() : GenRepository_575_ {
    private val store = mutableMapOf<Long, GenModel_575_>()
    override suspend fun getAll(): List<GenModel_575_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_575_? = store[id]
    override suspend fun save(model: GenModel_575_): GenModel_575_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_575_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_575_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_575_ @Inject constructor(
    private val repository: GenRepositoryImpl_575_
) : GenUseCase_575_<Unit, List<GenModel_575_>> {
    override suspend fun invoke(params: Unit): List<GenModel_575_> = repository.getAll()
}

class GenSaveUseCase_575_ @Inject constructor(
    private val repository: GenRepositoryImpl_575_
) : GenUseCase_575_<GenModel_575_, GenModel_575_> {
    override suspend fun invoke(params: GenModel_575_): GenModel_575_ = repository.save(params)
}

class GenDeleteUseCase_575_ @Inject constructor(
    private val repository: GenRepositoryImpl_575_
) : GenUseCase_575_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_575_ @Inject constructor(
    private val repository: GenRepositoryImpl_575_
) : GenUseCase_575_<String, List<GenModel_575_>> {
    override suspend fun invoke(params: String): List<GenModel_575_> = repository.search(params)
}

abstract class GenMapper_575_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_575_ : GenMapper_575_<GenModel_575_, String>() {
    override fun map(input: GenModel_575_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_575_ : GenMapper_575_<String, GenModel_575_>() {
    override fun map(input: String): GenModel_575_ {
        val parts = input.split(":")
        return GenModel_575_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_575_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_575_,
    private val saveUseCase: GenSaveUseCase_575_,
    private val deleteUseCase: GenDeleteUseCase_575_,
    private val searchUseCase: GenSearchUseCase_575_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_575_>(GenState_575_.Idle)
    val state: StateFlow<GenState_575_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_575_) {
        when (event) {
            is GenEvent_575_.Load -> loadAll()
            is GenEvent_575_.Update -> save(event.model)
            is GenEvent_575_.Delete -> delete(event.id)
            is GenEvent_575_.Refresh -> loadAll()
            is GenEvent_575_.Search -> search(event.query)
            is GenEvent_575_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_575_.Loading; _state.value = GenState_575_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_575_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_575_.Success(searchUseCase(query)) } }
}
