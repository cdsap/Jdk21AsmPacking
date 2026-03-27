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

data class GenModel_553_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_553_ {
    data class Load(val id: Long) : GenEvent_553_()
    data class Update(val model: GenModel_553_) : GenEvent_553_()
    data class Delete(val id: Long) : GenEvent_553_()
    data object Refresh : GenEvent_553_()
    data class Search(val query: String) : GenEvent_553_()
    data class Filter(val predicate: String) : GenEvent_553_()
}

sealed class GenState_553_ {
    data object Idle : GenState_553_()
    data object Loading : GenState_553_()
    data class Success(val items: List<GenModel_553_>) : GenState_553_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_553_()
    data class Partial(val items: List<GenModel_553_>, val hasMore: Boolean) : GenState_553_()
}

interface GenRepository_553_ {
    suspend fun getAll(): List<GenModel_553_>
    suspend fun getById(id: Long): GenModel_553_?
    suspend fun save(model: GenModel_553_): GenModel_553_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_553_>
}

@Singleton
class GenRepositoryImpl_553_ @Inject constructor() : GenRepository_553_ {
    private val store = mutableMapOf<Long, GenModel_553_>()
    override suspend fun getAll(): List<GenModel_553_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_553_? = store[id]
    override suspend fun save(model: GenModel_553_): GenModel_553_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_553_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_553_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_553_ @Inject constructor(
    private val repository: GenRepositoryImpl_553_
) : GenUseCase_553_<Unit, List<GenModel_553_>> {
    override suspend fun invoke(params: Unit): List<GenModel_553_> = repository.getAll()
}

class GenSaveUseCase_553_ @Inject constructor(
    private val repository: GenRepositoryImpl_553_
) : GenUseCase_553_<GenModel_553_, GenModel_553_> {
    override suspend fun invoke(params: GenModel_553_): GenModel_553_ = repository.save(params)
}

class GenDeleteUseCase_553_ @Inject constructor(
    private val repository: GenRepositoryImpl_553_
) : GenUseCase_553_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_553_ @Inject constructor(
    private val repository: GenRepositoryImpl_553_
) : GenUseCase_553_<String, List<GenModel_553_>> {
    override suspend fun invoke(params: String): List<GenModel_553_> = repository.search(params)
}

abstract class GenMapper_553_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_553_ : GenMapper_553_<GenModel_553_, String>() {
    override fun map(input: GenModel_553_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_553_ : GenMapper_553_<String, GenModel_553_>() {
    override fun map(input: String): GenModel_553_ {
        val parts = input.split(":")
        return GenModel_553_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_553_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_553_,
    private val saveUseCase: GenSaveUseCase_553_,
    private val deleteUseCase: GenDeleteUseCase_553_,
    private val searchUseCase: GenSearchUseCase_553_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_553_>(GenState_553_.Idle)
    val state: StateFlow<GenState_553_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_553_) {
        when (event) {
            is GenEvent_553_.Load -> loadAll()
            is GenEvent_553_.Update -> save(event.model)
            is GenEvent_553_.Delete -> delete(event.id)
            is GenEvent_553_.Refresh -> loadAll()
            is GenEvent_553_.Search -> search(event.query)
            is GenEvent_553_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_553_.Loading; _state.value = GenState_553_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_553_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_553_.Success(searchUseCase(query)) } }
}
