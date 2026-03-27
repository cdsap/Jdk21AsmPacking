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

data class GenModel_2874_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2874_ {
    data class Load(val id: Long) : GenEvent_2874_()
    data class Update(val model: GenModel_2874_) : GenEvent_2874_()
    data class Delete(val id: Long) : GenEvent_2874_()
    data object Refresh : GenEvent_2874_()
    data class Search(val query: String) : GenEvent_2874_()
    data class Filter(val predicate: String) : GenEvent_2874_()
}

sealed class GenState_2874_ {
    data object Idle : GenState_2874_()
    data object Loading : GenState_2874_()
    data class Success(val items: List<GenModel_2874_>) : GenState_2874_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2874_()
    data class Partial(val items: List<GenModel_2874_>, val hasMore: Boolean) : GenState_2874_()
}

interface GenRepository_2874_ {
    suspend fun getAll(): List<GenModel_2874_>
    suspend fun getById(id: Long): GenModel_2874_?
    suspend fun save(model: GenModel_2874_): GenModel_2874_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2874_>
}

@Singleton
class GenRepositoryImpl_2874_ @Inject constructor() : GenRepository_2874_ {
    private val store = mutableMapOf<Long, GenModel_2874_>()
    override suspend fun getAll(): List<GenModel_2874_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2874_? = store[id]
    override suspend fun save(model: GenModel_2874_): GenModel_2874_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2874_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2874_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2874_ @Inject constructor(
    private val repository: GenRepositoryImpl_2874_
) : GenUseCase_2874_<Unit, List<GenModel_2874_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2874_> = repository.getAll()
}

class GenSaveUseCase_2874_ @Inject constructor(
    private val repository: GenRepositoryImpl_2874_
) : GenUseCase_2874_<GenModel_2874_, GenModel_2874_> {
    override suspend fun invoke(params: GenModel_2874_): GenModel_2874_ = repository.save(params)
}

class GenDeleteUseCase_2874_ @Inject constructor(
    private val repository: GenRepositoryImpl_2874_
) : GenUseCase_2874_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2874_ @Inject constructor(
    private val repository: GenRepositoryImpl_2874_
) : GenUseCase_2874_<String, List<GenModel_2874_>> {
    override suspend fun invoke(params: String): List<GenModel_2874_> = repository.search(params)
}

abstract class GenMapper_2874_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2874_ : GenMapper_2874_<GenModel_2874_, String>() {
    override fun map(input: GenModel_2874_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2874_ : GenMapper_2874_<String, GenModel_2874_>() {
    override fun map(input: String): GenModel_2874_ {
        val parts = input.split(":")
        return GenModel_2874_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2874_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2874_,
    private val saveUseCase: GenSaveUseCase_2874_,
    private val deleteUseCase: GenDeleteUseCase_2874_,
    private val searchUseCase: GenSearchUseCase_2874_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2874_>(GenState_2874_.Idle)
    val state: StateFlow<GenState_2874_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2874_) {
        when (event) {
            is GenEvent_2874_.Load -> loadAll()
            is GenEvent_2874_.Update -> save(event.model)
            is GenEvent_2874_.Delete -> delete(event.id)
            is GenEvent_2874_.Refresh -> loadAll()
            is GenEvent_2874_.Search -> search(event.query)
            is GenEvent_2874_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2874_.Loading; _state.value = GenState_2874_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2874_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2874_.Success(searchUseCase(query)) } }
}
