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

data class GenModel_415_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_415_ {
    data class Load(val id: Long) : GenEvent_415_()
    data class Update(val model: GenModel_415_) : GenEvent_415_()
    data class Delete(val id: Long) : GenEvent_415_()
    data object Refresh : GenEvent_415_()
    data class Search(val query: String) : GenEvent_415_()
    data class Filter(val predicate: String) : GenEvent_415_()
}

sealed class GenState_415_ {
    data object Idle : GenState_415_()
    data object Loading : GenState_415_()
    data class Success(val items: List<GenModel_415_>) : GenState_415_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_415_()
    data class Partial(val items: List<GenModel_415_>, val hasMore: Boolean) : GenState_415_()
}

interface GenRepository_415_ {
    suspend fun getAll(): List<GenModel_415_>
    suspend fun getById(id: Long): GenModel_415_?
    suspend fun save(model: GenModel_415_): GenModel_415_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_415_>
}

@Singleton
class GenRepositoryImpl_415_ @Inject constructor() : GenRepository_415_ {
    private val store = mutableMapOf<Long, GenModel_415_>()
    override suspend fun getAll(): List<GenModel_415_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_415_? = store[id]
    override suspend fun save(model: GenModel_415_): GenModel_415_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_415_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_415_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_415_ @Inject constructor(
    private val repository: GenRepositoryImpl_415_
) : GenUseCase_415_<Unit, List<GenModel_415_>> {
    override suspend fun invoke(params: Unit): List<GenModel_415_> = repository.getAll()
}

class GenSaveUseCase_415_ @Inject constructor(
    private val repository: GenRepositoryImpl_415_
) : GenUseCase_415_<GenModel_415_, GenModel_415_> {
    override suspend fun invoke(params: GenModel_415_): GenModel_415_ = repository.save(params)
}

class GenDeleteUseCase_415_ @Inject constructor(
    private val repository: GenRepositoryImpl_415_
) : GenUseCase_415_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_415_ @Inject constructor(
    private val repository: GenRepositoryImpl_415_
) : GenUseCase_415_<String, List<GenModel_415_>> {
    override suspend fun invoke(params: String): List<GenModel_415_> = repository.search(params)
}

abstract class GenMapper_415_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_415_ : GenMapper_415_<GenModel_415_, String>() {
    override fun map(input: GenModel_415_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_415_ : GenMapper_415_<String, GenModel_415_>() {
    override fun map(input: String): GenModel_415_ {
        val parts = input.split(":")
        return GenModel_415_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_415_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_415_,
    private val saveUseCase: GenSaveUseCase_415_,
    private val deleteUseCase: GenDeleteUseCase_415_,
    private val searchUseCase: GenSearchUseCase_415_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_415_>(GenState_415_.Idle)
    val state: StateFlow<GenState_415_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_415_) {
        when (event) {
            is GenEvent_415_.Load -> loadAll()
            is GenEvent_415_.Update -> save(event.model)
            is GenEvent_415_.Delete -> delete(event.id)
            is GenEvent_415_.Refresh -> loadAll()
            is GenEvent_415_.Search -> search(event.query)
            is GenEvent_415_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_415_.Loading; _state.value = GenState_415_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_415_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_415_.Success(searchUseCase(query)) } }
}
