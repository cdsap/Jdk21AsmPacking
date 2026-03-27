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

data class GenModel_2694_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2694_ {
    data class Load(val id: Long) : GenEvent_2694_()
    data class Update(val model: GenModel_2694_) : GenEvent_2694_()
    data class Delete(val id: Long) : GenEvent_2694_()
    data object Refresh : GenEvent_2694_()
    data class Search(val query: String) : GenEvent_2694_()
    data class Filter(val predicate: String) : GenEvent_2694_()
}

sealed class GenState_2694_ {
    data object Idle : GenState_2694_()
    data object Loading : GenState_2694_()
    data class Success(val items: List<GenModel_2694_>) : GenState_2694_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2694_()
    data class Partial(val items: List<GenModel_2694_>, val hasMore: Boolean) : GenState_2694_()
}

interface GenRepository_2694_ {
    suspend fun getAll(): List<GenModel_2694_>
    suspend fun getById(id: Long): GenModel_2694_?
    suspend fun save(model: GenModel_2694_): GenModel_2694_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2694_>
}

@Singleton
class GenRepositoryImpl_2694_ @Inject constructor() : GenRepository_2694_ {
    private val store = mutableMapOf<Long, GenModel_2694_>()
    override suspend fun getAll(): List<GenModel_2694_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2694_? = store[id]
    override suspend fun save(model: GenModel_2694_): GenModel_2694_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2694_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2694_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2694_ @Inject constructor(
    private val repository: GenRepositoryImpl_2694_
) : GenUseCase_2694_<Unit, List<GenModel_2694_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2694_> = repository.getAll()
}

class GenSaveUseCase_2694_ @Inject constructor(
    private val repository: GenRepositoryImpl_2694_
) : GenUseCase_2694_<GenModel_2694_, GenModel_2694_> {
    override suspend fun invoke(params: GenModel_2694_): GenModel_2694_ = repository.save(params)
}

class GenDeleteUseCase_2694_ @Inject constructor(
    private val repository: GenRepositoryImpl_2694_
) : GenUseCase_2694_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2694_ @Inject constructor(
    private val repository: GenRepositoryImpl_2694_
) : GenUseCase_2694_<String, List<GenModel_2694_>> {
    override suspend fun invoke(params: String): List<GenModel_2694_> = repository.search(params)
}

abstract class GenMapper_2694_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2694_ : GenMapper_2694_<GenModel_2694_, String>() {
    override fun map(input: GenModel_2694_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2694_ : GenMapper_2694_<String, GenModel_2694_>() {
    override fun map(input: String): GenModel_2694_ {
        val parts = input.split(":")
        return GenModel_2694_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2694_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2694_,
    private val saveUseCase: GenSaveUseCase_2694_,
    private val deleteUseCase: GenDeleteUseCase_2694_,
    private val searchUseCase: GenSearchUseCase_2694_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2694_>(GenState_2694_.Idle)
    val state: StateFlow<GenState_2694_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2694_) {
        when (event) {
            is GenEvent_2694_.Load -> loadAll()
            is GenEvent_2694_.Update -> save(event.model)
            is GenEvent_2694_.Delete -> delete(event.id)
            is GenEvent_2694_.Refresh -> loadAll()
            is GenEvent_2694_.Search -> search(event.query)
            is GenEvent_2694_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2694_.Loading; _state.value = GenState_2694_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2694_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2694_.Success(searchUseCase(query)) } }
}
