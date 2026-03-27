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

data class GenModel_702_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_702_ {
    data class Load(val id: Long) : GenEvent_702_()
    data class Update(val model: GenModel_702_) : GenEvent_702_()
    data class Delete(val id: Long) : GenEvent_702_()
    data object Refresh : GenEvent_702_()
    data class Search(val query: String) : GenEvent_702_()
    data class Filter(val predicate: String) : GenEvent_702_()
}

sealed class GenState_702_ {
    data object Idle : GenState_702_()
    data object Loading : GenState_702_()
    data class Success(val items: List<GenModel_702_>) : GenState_702_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_702_()
    data class Partial(val items: List<GenModel_702_>, val hasMore: Boolean) : GenState_702_()
}

interface GenRepository_702_ {
    suspend fun getAll(): List<GenModel_702_>
    suspend fun getById(id: Long): GenModel_702_?
    suspend fun save(model: GenModel_702_): GenModel_702_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_702_>
}

@Singleton
class GenRepositoryImpl_702_ @Inject constructor() : GenRepository_702_ {
    private val store = mutableMapOf<Long, GenModel_702_>()
    override suspend fun getAll(): List<GenModel_702_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_702_? = store[id]
    override suspend fun save(model: GenModel_702_): GenModel_702_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_702_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_702_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_702_ @Inject constructor(
    private val repository: GenRepositoryImpl_702_
) : GenUseCase_702_<Unit, List<GenModel_702_>> {
    override suspend fun invoke(params: Unit): List<GenModel_702_> = repository.getAll()
}

class GenSaveUseCase_702_ @Inject constructor(
    private val repository: GenRepositoryImpl_702_
) : GenUseCase_702_<GenModel_702_, GenModel_702_> {
    override suspend fun invoke(params: GenModel_702_): GenModel_702_ = repository.save(params)
}

class GenDeleteUseCase_702_ @Inject constructor(
    private val repository: GenRepositoryImpl_702_
) : GenUseCase_702_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_702_ @Inject constructor(
    private val repository: GenRepositoryImpl_702_
) : GenUseCase_702_<String, List<GenModel_702_>> {
    override suspend fun invoke(params: String): List<GenModel_702_> = repository.search(params)
}

abstract class GenMapper_702_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_702_ : GenMapper_702_<GenModel_702_, String>() {
    override fun map(input: GenModel_702_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_702_ : GenMapper_702_<String, GenModel_702_>() {
    override fun map(input: String): GenModel_702_ {
        val parts = input.split(":")
        return GenModel_702_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_702_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_702_,
    private val saveUseCase: GenSaveUseCase_702_,
    private val deleteUseCase: GenDeleteUseCase_702_,
    private val searchUseCase: GenSearchUseCase_702_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_702_>(GenState_702_.Idle)
    val state: StateFlow<GenState_702_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_702_) {
        when (event) {
            is GenEvent_702_.Load -> loadAll()
            is GenEvent_702_.Update -> save(event.model)
            is GenEvent_702_.Delete -> delete(event.id)
            is GenEvent_702_.Refresh -> loadAll()
            is GenEvent_702_.Search -> search(event.query)
            is GenEvent_702_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_702_.Loading; _state.value = GenState_702_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_702_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_702_.Success(searchUseCase(query)) } }
}
