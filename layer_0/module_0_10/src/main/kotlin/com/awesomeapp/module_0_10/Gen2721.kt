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

data class GenModel_2721_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2721_ {
    data class Load(val id: Long) : GenEvent_2721_()
    data class Update(val model: GenModel_2721_) : GenEvent_2721_()
    data class Delete(val id: Long) : GenEvent_2721_()
    data object Refresh : GenEvent_2721_()
    data class Search(val query: String) : GenEvent_2721_()
    data class Filter(val predicate: String) : GenEvent_2721_()
}

sealed class GenState_2721_ {
    data object Idle : GenState_2721_()
    data object Loading : GenState_2721_()
    data class Success(val items: List<GenModel_2721_>) : GenState_2721_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2721_()
    data class Partial(val items: List<GenModel_2721_>, val hasMore: Boolean) : GenState_2721_()
}

interface GenRepository_2721_ {
    suspend fun getAll(): List<GenModel_2721_>
    suspend fun getById(id: Long): GenModel_2721_?
    suspend fun save(model: GenModel_2721_): GenModel_2721_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2721_>
}

@Singleton
class GenRepositoryImpl_2721_ @Inject constructor() : GenRepository_2721_ {
    private val store = mutableMapOf<Long, GenModel_2721_>()
    override suspend fun getAll(): List<GenModel_2721_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2721_? = store[id]
    override suspend fun save(model: GenModel_2721_): GenModel_2721_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2721_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2721_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2721_ @Inject constructor(
    private val repository: GenRepositoryImpl_2721_
) : GenUseCase_2721_<Unit, List<GenModel_2721_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2721_> = repository.getAll()
}

class GenSaveUseCase_2721_ @Inject constructor(
    private val repository: GenRepositoryImpl_2721_
) : GenUseCase_2721_<GenModel_2721_, GenModel_2721_> {
    override suspend fun invoke(params: GenModel_2721_): GenModel_2721_ = repository.save(params)
}

class GenDeleteUseCase_2721_ @Inject constructor(
    private val repository: GenRepositoryImpl_2721_
) : GenUseCase_2721_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2721_ @Inject constructor(
    private val repository: GenRepositoryImpl_2721_
) : GenUseCase_2721_<String, List<GenModel_2721_>> {
    override suspend fun invoke(params: String): List<GenModel_2721_> = repository.search(params)
}

abstract class GenMapper_2721_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2721_ : GenMapper_2721_<GenModel_2721_, String>() {
    override fun map(input: GenModel_2721_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2721_ : GenMapper_2721_<String, GenModel_2721_>() {
    override fun map(input: String): GenModel_2721_ {
        val parts = input.split(":")
        return GenModel_2721_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2721_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2721_,
    private val saveUseCase: GenSaveUseCase_2721_,
    private val deleteUseCase: GenDeleteUseCase_2721_,
    private val searchUseCase: GenSearchUseCase_2721_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2721_>(GenState_2721_.Idle)
    val state: StateFlow<GenState_2721_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2721_) {
        when (event) {
            is GenEvent_2721_.Load -> loadAll()
            is GenEvent_2721_.Update -> save(event.model)
            is GenEvent_2721_.Delete -> delete(event.id)
            is GenEvent_2721_.Refresh -> loadAll()
            is GenEvent_2721_.Search -> search(event.query)
            is GenEvent_2721_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2721_.Loading; _state.value = GenState_2721_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2721_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2721_.Success(searchUseCase(query)) } }
}
