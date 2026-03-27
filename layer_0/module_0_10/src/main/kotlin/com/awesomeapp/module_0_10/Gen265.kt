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

data class GenModel_265_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_265_ {
    data class Load(val id: Long) : GenEvent_265_()
    data class Update(val model: GenModel_265_) : GenEvent_265_()
    data class Delete(val id: Long) : GenEvent_265_()
    data object Refresh : GenEvent_265_()
    data class Search(val query: String) : GenEvent_265_()
    data class Filter(val predicate: String) : GenEvent_265_()
}

sealed class GenState_265_ {
    data object Idle : GenState_265_()
    data object Loading : GenState_265_()
    data class Success(val items: List<GenModel_265_>) : GenState_265_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_265_()
    data class Partial(val items: List<GenModel_265_>, val hasMore: Boolean) : GenState_265_()
}

interface GenRepository_265_ {
    suspend fun getAll(): List<GenModel_265_>
    suspend fun getById(id: Long): GenModel_265_?
    suspend fun save(model: GenModel_265_): GenModel_265_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_265_>
}

@Singleton
class GenRepositoryImpl_265_ @Inject constructor() : GenRepository_265_ {
    private val store = mutableMapOf<Long, GenModel_265_>()
    override suspend fun getAll(): List<GenModel_265_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_265_? = store[id]
    override suspend fun save(model: GenModel_265_): GenModel_265_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_265_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_265_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_265_ @Inject constructor(
    private val repository: GenRepositoryImpl_265_
) : GenUseCase_265_<Unit, List<GenModel_265_>> {
    override suspend fun invoke(params: Unit): List<GenModel_265_> = repository.getAll()
}

class GenSaveUseCase_265_ @Inject constructor(
    private val repository: GenRepositoryImpl_265_
) : GenUseCase_265_<GenModel_265_, GenModel_265_> {
    override suspend fun invoke(params: GenModel_265_): GenModel_265_ = repository.save(params)
}

class GenDeleteUseCase_265_ @Inject constructor(
    private val repository: GenRepositoryImpl_265_
) : GenUseCase_265_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_265_ @Inject constructor(
    private val repository: GenRepositoryImpl_265_
) : GenUseCase_265_<String, List<GenModel_265_>> {
    override suspend fun invoke(params: String): List<GenModel_265_> = repository.search(params)
}

abstract class GenMapper_265_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_265_ : GenMapper_265_<GenModel_265_, String>() {
    override fun map(input: GenModel_265_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_265_ : GenMapper_265_<String, GenModel_265_>() {
    override fun map(input: String): GenModel_265_ {
        val parts = input.split(":")
        return GenModel_265_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_265_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_265_,
    private val saveUseCase: GenSaveUseCase_265_,
    private val deleteUseCase: GenDeleteUseCase_265_,
    private val searchUseCase: GenSearchUseCase_265_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_265_>(GenState_265_.Idle)
    val state: StateFlow<GenState_265_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_265_) {
        when (event) {
            is GenEvent_265_.Load -> loadAll()
            is GenEvent_265_.Update -> save(event.model)
            is GenEvent_265_.Delete -> delete(event.id)
            is GenEvent_265_.Refresh -> loadAll()
            is GenEvent_265_.Search -> search(event.query)
            is GenEvent_265_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_265_.Loading; _state.value = GenState_265_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_265_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_265_.Success(searchUseCase(query)) } }
}
