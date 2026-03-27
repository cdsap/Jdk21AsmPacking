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

data class GenModel_2702_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2702_ {
    data class Load(val id: Long) : GenEvent_2702_()
    data class Update(val model: GenModel_2702_) : GenEvent_2702_()
    data class Delete(val id: Long) : GenEvent_2702_()
    data object Refresh : GenEvent_2702_()
    data class Search(val query: String) : GenEvent_2702_()
    data class Filter(val predicate: String) : GenEvent_2702_()
}

sealed class GenState_2702_ {
    data object Idle : GenState_2702_()
    data object Loading : GenState_2702_()
    data class Success(val items: List<GenModel_2702_>) : GenState_2702_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2702_()
    data class Partial(val items: List<GenModel_2702_>, val hasMore: Boolean) : GenState_2702_()
}

interface GenRepository_2702_ {
    suspend fun getAll(): List<GenModel_2702_>
    suspend fun getById(id: Long): GenModel_2702_?
    suspend fun save(model: GenModel_2702_): GenModel_2702_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2702_>
}

@Singleton
class GenRepositoryImpl_2702_ @Inject constructor() : GenRepository_2702_ {
    private val store = mutableMapOf<Long, GenModel_2702_>()
    override suspend fun getAll(): List<GenModel_2702_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2702_? = store[id]
    override suspend fun save(model: GenModel_2702_): GenModel_2702_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2702_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2702_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2702_ @Inject constructor(
    private val repository: GenRepositoryImpl_2702_
) : GenUseCase_2702_<Unit, List<GenModel_2702_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2702_> = repository.getAll()
}

class GenSaveUseCase_2702_ @Inject constructor(
    private val repository: GenRepositoryImpl_2702_
) : GenUseCase_2702_<GenModel_2702_, GenModel_2702_> {
    override suspend fun invoke(params: GenModel_2702_): GenModel_2702_ = repository.save(params)
}

class GenDeleteUseCase_2702_ @Inject constructor(
    private val repository: GenRepositoryImpl_2702_
) : GenUseCase_2702_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2702_ @Inject constructor(
    private val repository: GenRepositoryImpl_2702_
) : GenUseCase_2702_<String, List<GenModel_2702_>> {
    override suspend fun invoke(params: String): List<GenModel_2702_> = repository.search(params)
}

abstract class GenMapper_2702_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2702_ : GenMapper_2702_<GenModel_2702_, String>() {
    override fun map(input: GenModel_2702_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2702_ : GenMapper_2702_<String, GenModel_2702_>() {
    override fun map(input: String): GenModel_2702_ {
        val parts = input.split(":")
        return GenModel_2702_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2702_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2702_,
    private val saveUseCase: GenSaveUseCase_2702_,
    private val deleteUseCase: GenDeleteUseCase_2702_,
    private val searchUseCase: GenSearchUseCase_2702_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2702_>(GenState_2702_.Idle)
    val state: StateFlow<GenState_2702_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2702_) {
        when (event) {
            is GenEvent_2702_.Load -> loadAll()
            is GenEvent_2702_.Update -> save(event.model)
            is GenEvent_2702_.Delete -> delete(event.id)
            is GenEvent_2702_.Refresh -> loadAll()
            is GenEvent_2702_.Search -> search(event.query)
            is GenEvent_2702_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2702_.Loading; _state.value = GenState_2702_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2702_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2702_.Success(searchUseCase(query)) } }
}
