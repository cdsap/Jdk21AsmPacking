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

data class GenModel_949_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_949_ {
    data class Load(val id: Long) : GenEvent_949_()
    data class Update(val model: GenModel_949_) : GenEvent_949_()
    data class Delete(val id: Long) : GenEvent_949_()
    data object Refresh : GenEvent_949_()
    data class Search(val query: String) : GenEvent_949_()
    data class Filter(val predicate: String) : GenEvent_949_()
}

sealed class GenState_949_ {
    data object Idle : GenState_949_()
    data object Loading : GenState_949_()
    data class Success(val items: List<GenModel_949_>) : GenState_949_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_949_()
    data class Partial(val items: List<GenModel_949_>, val hasMore: Boolean) : GenState_949_()
}

interface GenRepository_949_ {
    suspend fun getAll(): List<GenModel_949_>
    suspend fun getById(id: Long): GenModel_949_?
    suspend fun save(model: GenModel_949_): GenModel_949_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_949_>
}

@Singleton
class GenRepositoryImpl_949_ @Inject constructor() : GenRepository_949_ {
    private val store = mutableMapOf<Long, GenModel_949_>()
    override suspend fun getAll(): List<GenModel_949_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_949_? = store[id]
    override suspend fun save(model: GenModel_949_): GenModel_949_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_949_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_949_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_949_ @Inject constructor(
    private val repository: GenRepositoryImpl_949_
) : GenUseCase_949_<Unit, List<GenModel_949_>> {
    override suspend fun invoke(params: Unit): List<GenModel_949_> = repository.getAll()
}

class GenSaveUseCase_949_ @Inject constructor(
    private val repository: GenRepositoryImpl_949_
) : GenUseCase_949_<GenModel_949_, GenModel_949_> {
    override suspend fun invoke(params: GenModel_949_): GenModel_949_ = repository.save(params)
}

class GenDeleteUseCase_949_ @Inject constructor(
    private val repository: GenRepositoryImpl_949_
) : GenUseCase_949_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_949_ @Inject constructor(
    private val repository: GenRepositoryImpl_949_
) : GenUseCase_949_<String, List<GenModel_949_>> {
    override suspend fun invoke(params: String): List<GenModel_949_> = repository.search(params)
}

abstract class GenMapper_949_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_949_ : GenMapper_949_<GenModel_949_, String>() {
    override fun map(input: GenModel_949_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_949_ : GenMapper_949_<String, GenModel_949_>() {
    override fun map(input: String): GenModel_949_ {
        val parts = input.split(":")
        return GenModel_949_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_949_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_949_,
    private val saveUseCase: GenSaveUseCase_949_,
    private val deleteUseCase: GenDeleteUseCase_949_,
    private val searchUseCase: GenSearchUseCase_949_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_949_>(GenState_949_.Idle)
    val state: StateFlow<GenState_949_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_949_) {
        when (event) {
            is GenEvent_949_.Load -> loadAll()
            is GenEvent_949_.Update -> save(event.model)
            is GenEvent_949_.Delete -> delete(event.id)
            is GenEvent_949_.Refresh -> loadAll()
            is GenEvent_949_.Search -> search(event.query)
            is GenEvent_949_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_949_.Loading; _state.value = GenState_949_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_949_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_949_.Success(searchUseCase(query)) } }
}
