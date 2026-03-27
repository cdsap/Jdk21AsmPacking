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

data class GenModel_918_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_918_ {
    data class Load(val id: Long) : GenEvent_918_()
    data class Update(val model: GenModel_918_) : GenEvent_918_()
    data class Delete(val id: Long) : GenEvent_918_()
    data object Refresh : GenEvent_918_()
    data class Search(val query: String) : GenEvent_918_()
    data class Filter(val predicate: String) : GenEvent_918_()
}

sealed class GenState_918_ {
    data object Idle : GenState_918_()
    data object Loading : GenState_918_()
    data class Success(val items: List<GenModel_918_>) : GenState_918_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_918_()
    data class Partial(val items: List<GenModel_918_>, val hasMore: Boolean) : GenState_918_()
}

interface GenRepository_918_ {
    suspend fun getAll(): List<GenModel_918_>
    suspend fun getById(id: Long): GenModel_918_?
    suspend fun save(model: GenModel_918_): GenModel_918_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_918_>
}

@Singleton
class GenRepositoryImpl_918_ @Inject constructor() : GenRepository_918_ {
    private val store = mutableMapOf<Long, GenModel_918_>()
    override suspend fun getAll(): List<GenModel_918_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_918_? = store[id]
    override suspend fun save(model: GenModel_918_): GenModel_918_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_918_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_918_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_918_ @Inject constructor(
    private val repository: GenRepositoryImpl_918_
) : GenUseCase_918_<Unit, List<GenModel_918_>> {
    override suspend fun invoke(params: Unit): List<GenModel_918_> = repository.getAll()
}

class GenSaveUseCase_918_ @Inject constructor(
    private val repository: GenRepositoryImpl_918_
) : GenUseCase_918_<GenModel_918_, GenModel_918_> {
    override suspend fun invoke(params: GenModel_918_): GenModel_918_ = repository.save(params)
}

class GenDeleteUseCase_918_ @Inject constructor(
    private val repository: GenRepositoryImpl_918_
) : GenUseCase_918_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_918_ @Inject constructor(
    private val repository: GenRepositoryImpl_918_
) : GenUseCase_918_<String, List<GenModel_918_>> {
    override suspend fun invoke(params: String): List<GenModel_918_> = repository.search(params)
}

abstract class GenMapper_918_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_918_ : GenMapper_918_<GenModel_918_, String>() {
    override fun map(input: GenModel_918_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_918_ : GenMapper_918_<String, GenModel_918_>() {
    override fun map(input: String): GenModel_918_ {
        val parts = input.split(":")
        return GenModel_918_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_918_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_918_,
    private val saveUseCase: GenSaveUseCase_918_,
    private val deleteUseCase: GenDeleteUseCase_918_,
    private val searchUseCase: GenSearchUseCase_918_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_918_>(GenState_918_.Idle)
    val state: StateFlow<GenState_918_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_918_) {
        when (event) {
            is GenEvent_918_.Load -> loadAll()
            is GenEvent_918_.Update -> save(event.model)
            is GenEvent_918_.Delete -> delete(event.id)
            is GenEvent_918_.Refresh -> loadAll()
            is GenEvent_918_.Search -> search(event.query)
            is GenEvent_918_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_918_.Loading; _state.value = GenState_918_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_918_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_918_.Success(searchUseCase(query)) } }
}
