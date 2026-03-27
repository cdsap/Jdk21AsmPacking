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

data class GenModel_426_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_426_ {
    data class Load(val id: Long) : GenEvent_426_()
    data class Update(val model: GenModel_426_) : GenEvent_426_()
    data class Delete(val id: Long) : GenEvent_426_()
    data object Refresh : GenEvent_426_()
    data class Search(val query: String) : GenEvent_426_()
    data class Filter(val predicate: String) : GenEvent_426_()
}

sealed class GenState_426_ {
    data object Idle : GenState_426_()
    data object Loading : GenState_426_()
    data class Success(val items: List<GenModel_426_>) : GenState_426_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_426_()
    data class Partial(val items: List<GenModel_426_>, val hasMore: Boolean) : GenState_426_()
}

interface GenRepository_426_ {
    suspend fun getAll(): List<GenModel_426_>
    suspend fun getById(id: Long): GenModel_426_?
    suspend fun save(model: GenModel_426_): GenModel_426_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_426_>
}

@Singleton
class GenRepositoryImpl_426_ @Inject constructor() : GenRepository_426_ {
    private val store = mutableMapOf<Long, GenModel_426_>()
    override suspend fun getAll(): List<GenModel_426_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_426_? = store[id]
    override suspend fun save(model: GenModel_426_): GenModel_426_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_426_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_426_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_426_ @Inject constructor(
    private val repository: GenRepositoryImpl_426_
) : GenUseCase_426_<Unit, List<GenModel_426_>> {
    override suspend fun invoke(params: Unit): List<GenModel_426_> = repository.getAll()
}

class GenSaveUseCase_426_ @Inject constructor(
    private val repository: GenRepositoryImpl_426_
) : GenUseCase_426_<GenModel_426_, GenModel_426_> {
    override suspend fun invoke(params: GenModel_426_): GenModel_426_ = repository.save(params)
}

class GenDeleteUseCase_426_ @Inject constructor(
    private val repository: GenRepositoryImpl_426_
) : GenUseCase_426_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_426_ @Inject constructor(
    private val repository: GenRepositoryImpl_426_
) : GenUseCase_426_<String, List<GenModel_426_>> {
    override suspend fun invoke(params: String): List<GenModel_426_> = repository.search(params)
}

abstract class GenMapper_426_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_426_ : GenMapper_426_<GenModel_426_, String>() {
    override fun map(input: GenModel_426_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_426_ : GenMapper_426_<String, GenModel_426_>() {
    override fun map(input: String): GenModel_426_ {
        val parts = input.split(":")
        return GenModel_426_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_426_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_426_,
    private val saveUseCase: GenSaveUseCase_426_,
    private val deleteUseCase: GenDeleteUseCase_426_,
    private val searchUseCase: GenSearchUseCase_426_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_426_>(GenState_426_.Idle)
    val state: StateFlow<GenState_426_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_426_) {
        when (event) {
            is GenEvent_426_.Load -> loadAll()
            is GenEvent_426_.Update -> save(event.model)
            is GenEvent_426_.Delete -> delete(event.id)
            is GenEvent_426_.Refresh -> loadAll()
            is GenEvent_426_.Search -> search(event.query)
            is GenEvent_426_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_426_.Loading; _state.value = GenState_426_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_426_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_426_.Success(searchUseCase(query)) } }
}
