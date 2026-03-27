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

data class GenModel_750_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_750_ {
    data class Load(val id: Long) : GenEvent_750_()
    data class Update(val model: GenModel_750_) : GenEvent_750_()
    data class Delete(val id: Long) : GenEvent_750_()
    data object Refresh : GenEvent_750_()
    data class Search(val query: String) : GenEvent_750_()
    data class Filter(val predicate: String) : GenEvent_750_()
}

sealed class GenState_750_ {
    data object Idle : GenState_750_()
    data object Loading : GenState_750_()
    data class Success(val items: List<GenModel_750_>) : GenState_750_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_750_()
    data class Partial(val items: List<GenModel_750_>, val hasMore: Boolean) : GenState_750_()
}

interface GenRepository_750_ {
    suspend fun getAll(): List<GenModel_750_>
    suspend fun getById(id: Long): GenModel_750_?
    suspend fun save(model: GenModel_750_): GenModel_750_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_750_>
}

@Singleton
class GenRepositoryImpl_750_ @Inject constructor() : GenRepository_750_ {
    private val store = mutableMapOf<Long, GenModel_750_>()
    override suspend fun getAll(): List<GenModel_750_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_750_? = store[id]
    override suspend fun save(model: GenModel_750_): GenModel_750_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_750_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_750_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_750_ @Inject constructor(
    private val repository: GenRepositoryImpl_750_
) : GenUseCase_750_<Unit, List<GenModel_750_>> {
    override suspend fun invoke(params: Unit): List<GenModel_750_> = repository.getAll()
}

class GenSaveUseCase_750_ @Inject constructor(
    private val repository: GenRepositoryImpl_750_
) : GenUseCase_750_<GenModel_750_, GenModel_750_> {
    override suspend fun invoke(params: GenModel_750_): GenModel_750_ = repository.save(params)
}

class GenDeleteUseCase_750_ @Inject constructor(
    private val repository: GenRepositoryImpl_750_
) : GenUseCase_750_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_750_ @Inject constructor(
    private val repository: GenRepositoryImpl_750_
) : GenUseCase_750_<String, List<GenModel_750_>> {
    override suspend fun invoke(params: String): List<GenModel_750_> = repository.search(params)
}

abstract class GenMapper_750_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_750_ : GenMapper_750_<GenModel_750_, String>() {
    override fun map(input: GenModel_750_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_750_ : GenMapper_750_<String, GenModel_750_>() {
    override fun map(input: String): GenModel_750_ {
        val parts = input.split(":")
        return GenModel_750_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_750_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_750_,
    private val saveUseCase: GenSaveUseCase_750_,
    private val deleteUseCase: GenDeleteUseCase_750_,
    private val searchUseCase: GenSearchUseCase_750_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_750_>(GenState_750_.Idle)
    val state: StateFlow<GenState_750_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_750_) {
        when (event) {
            is GenEvent_750_.Load -> loadAll()
            is GenEvent_750_.Update -> save(event.model)
            is GenEvent_750_.Delete -> delete(event.id)
            is GenEvent_750_.Refresh -> loadAll()
            is GenEvent_750_.Search -> search(event.query)
            is GenEvent_750_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_750_.Loading; _state.value = GenState_750_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_750_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_750_.Success(searchUseCase(query)) } }
}
