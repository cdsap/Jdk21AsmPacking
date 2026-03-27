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

data class GenModel_3383_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3383_ {
    data class Load(val id: Long) : GenEvent_3383_()
    data class Update(val model: GenModel_3383_) : GenEvent_3383_()
    data class Delete(val id: Long) : GenEvent_3383_()
    data object Refresh : GenEvent_3383_()
    data class Search(val query: String) : GenEvent_3383_()
    data class Filter(val predicate: String) : GenEvent_3383_()
}

sealed class GenState_3383_ {
    data object Idle : GenState_3383_()
    data object Loading : GenState_3383_()
    data class Success(val items: List<GenModel_3383_>) : GenState_3383_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3383_()
    data class Partial(val items: List<GenModel_3383_>, val hasMore: Boolean) : GenState_3383_()
}

interface GenRepository_3383_ {
    suspend fun getAll(): List<GenModel_3383_>
    suspend fun getById(id: Long): GenModel_3383_?
    suspend fun save(model: GenModel_3383_): GenModel_3383_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3383_>
}

@Singleton
class GenRepositoryImpl_3383_ @Inject constructor() : GenRepository_3383_ {
    private val store = mutableMapOf<Long, GenModel_3383_>()
    override suspend fun getAll(): List<GenModel_3383_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3383_? = store[id]
    override suspend fun save(model: GenModel_3383_): GenModel_3383_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3383_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3383_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3383_ @Inject constructor(
    private val repository: GenRepositoryImpl_3383_
) : GenUseCase_3383_<Unit, List<GenModel_3383_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3383_> = repository.getAll()
}

class GenSaveUseCase_3383_ @Inject constructor(
    private val repository: GenRepositoryImpl_3383_
) : GenUseCase_3383_<GenModel_3383_, GenModel_3383_> {
    override suspend fun invoke(params: GenModel_3383_): GenModel_3383_ = repository.save(params)
}

class GenDeleteUseCase_3383_ @Inject constructor(
    private val repository: GenRepositoryImpl_3383_
) : GenUseCase_3383_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3383_ @Inject constructor(
    private val repository: GenRepositoryImpl_3383_
) : GenUseCase_3383_<String, List<GenModel_3383_>> {
    override suspend fun invoke(params: String): List<GenModel_3383_> = repository.search(params)
}

abstract class GenMapper_3383_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3383_ : GenMapper_3383_<GenModel_3383_, String>() {
    override fun map(input: GenModel_3383_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3383_ : GenMapper_3383_<String, GenModel_3383_>() {
    override fun map(input: String): GenModel_3383_ {
        val parts = input.split(":")
        return GenModel_3383_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3383_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3383_,
    private val saveUseCase: GenSaveUseCase_3383_,
    private val deleteUseCase: GenDeleteUseCase_3383_,
    private val searchUseCase: GenSearchUseCase_3383_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3383_>(GenState_3383_.Idle)
    val state: StateFlow<GenState_3383_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3383_) {
        when (event) {
            is GenEvent_3383_.Load -> loadAll()
            is GenEvent_3383_.Update -> save(event.model)
            is GenEvent_3383_.Delete -> delete(event.id)
            is GenEvent_3383_.Refresh -> loadAll()
            is GenEvent_3383_.Search -> search(event.query)
            is GenEvent_3383_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3383_.Loading; _state.value = GenState_3383_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3383_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3383_.Success(searchUseCase(query)) } }
}
