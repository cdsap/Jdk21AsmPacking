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

data class GenModel_531_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_531_ {
    data class Load(val id: Long) : GenEvent_531_()
    data class Update(val model: GenModel_531_) : GenEvent_531_()
    data class Delete(val id: Long) : GenEvent_531_()
    data object Refresh : GenEvent_531_()
    data class Search(val query: String) : GenEvent_531_()
    data class Filter(val predicate: String) : GenEvent_531_()
}

sealed class GenState_531_ {
    data object Idle : GenState_531_()
    data object Loading : GenState_531_()
    data class Success(val items: List<GenModel_531_>) : GenState_531_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_531_()
    data class Partial(val items: List<GenModel_531_>, val hasMore: Boolean) : GenState_531_()
}

interface GenRepository_531_ {
    suspend fun getAll(): List<GenModel_531_>
    suspend fun getById(id: Long): GenModel_531_?
    suspend fun save(model: GenModel_531_): GenModel_531_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_531_>
}

@Singleton
class GenRepositoryImpl_531_ @Inject constructor() : GenRepository_531_ {
    private val store = mutableMapOf<Long, GenModel_531_>()
    override suspend fun getAll(): List<GenModel_531_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_531_? = store[id]
    override suspend fun save(model: GenModel_531_): GenModel_531_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_531_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_531_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_531_ @Inject constructor(
    private val repository: GenRepositoryImpl_531_
) : GenUseCase_531_<Unit, List<GenModel_531_>> {
    override suspend fun invoke(params: Unit): List<GenModel_531_> = repository.getAll()
}

class GenSaveUseCase_531_ @Inject constructor(
    private val repository: GenRepositoryImpl_531_
) : GenUseCase_531_<GenModel_531_, GenModel_531_> {
    override suspend fun invoke(params: GenModel_531_): GenModel_531_ = repository.save(params)
}

class GenDeleteUseCase_531_ @Inject constructor(
    private val repository: GenRepositoryImpl_531_
) : GenUseCase_531_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_531_ @Inject constructor(
    private val repository: GenRepositoryImpl_531_
) : GenUseCase_531_<String, List<GenModel_531_>> {
    override suspend fun invoke(params: String): List<GenModel_531_> = repository.search(params)
}

abstract class GenMapper_531_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_531_ : GenMapper_531_<GenModel_531_, String>() {
    override fun map(input: GenModel_531_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_531_ : GenMapper_531_<String, GenModel_531_>() {
    override fun map(input: String): GenModel_531_ {
        val parts = input.split(":")
        return GenModel_531_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_531_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_531_,
    private val saveUseCase: GenSaveUseCase_531_,
    private val deleteUseCase: GenDeleteUseCase_531_,
    private val searchUseCase: GenSearchUseCase_531_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_531_>(GenState_531_.Idle)
    val state: StateFlow<GenState_531_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_531_) {
        when (event) {
            is GenEvent_531_.Load -> loadAll()
            is GenEvent_531_.Update -> save(event.model)
            is GenEvent_531_.Delete -> delete(event.id)
            is GenEvent_531_.Refresh -> loadAll()
            is GenEvent_531_.Search -> search(event.query)
            is GenEvent_531_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_531_.Loading; _state.value = GenState_531_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_531_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_531_.Success(searchUseCase(query)) } }
}
