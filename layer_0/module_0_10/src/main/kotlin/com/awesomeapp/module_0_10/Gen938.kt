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

data class GenModel_938_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_938_ {
    data class Load(val id: Long) : GenEvent_938_()
    data class Update(val model: GenModel_938_) : GenEvent_938_()
    data class Delete(val id: Long) : GenEvent_938_()
    data object Refresh : GenEvent_938_()
    data class Search(val query: String) : GenEvent_938_()
    data class Filter(val predicate: String) : GenEvent_938_()
}

sealed class GenState_938_ {
    data object Idle : GenState_938_()
    data object Loading : GenState_938_()
    data class Success(val items: List<GenModel_938_>) : GenState_938_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_938_()
    data class Partial(val items: List<GenModel_938_>, val hasMore: Boolean) : GenState_938_()
}

interface GenRepository_938_ {
    suspend fun getAll(): List<GenModel_938_>
    suspend fun getById(id: Long): GenModel_938_?
    suspend fun save(model: GenModel_938_): GenModel_938_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_938_>
}

@Singleton
class GenRepositoryImpl_938_ @Inject constructor() : GenRepository_938_ {
    private val store = mutableMapOf<Long, GenModel_938_>()
    override suspend fun getAll(): List<GenModel_938_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_938_? = store[id]
    override suspend fun save(model: GenModel_938_): GenModel_938_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_938_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_938_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_938_ @Inject constructor(
    private val repository: GenRepositoryImpl_938_
) : GenUseCase_938_<Unit, List<GenModel_938_>> {
    override suspend fun invoke(params: Unit): List<GenModel_938_> = repository.getAll()
}

class GenSaveUseCase_938_ @Inject constructor(
    private val repository: GenRepositoryImpl_938_
) : GenUseCase_938_<GenModel_938_, GenModel_938_> {
    override suspend fun invoke(params: GenModel_938_): GenModel_938_ = repository.save(params)
}

class GenDeleteUseCase_938_ @Inject constructor(
    private val repository: GenRepositoryImpl_938_
) : GenUseCase_938_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_938_ @Inject constructor(
    private val repository: GenRepositoryImpl_938_
) : GenUseCase_938_<String, List<GenModel_938_>> {
    override suspend fun invoke(params: String): List<GenModel_938_> = repository.search(params)
}

abstract class GenMapper_938_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_938_ : GenMapper_938_<GenModel_938_, String>() {
    override fun map(input: GenModel_938_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_938_ : GenMapper_938_<String, GenModel_938_>() {
    override fun map(input: String): GenModel_938_ {
        val parts = input.split(":")
        return GenModel_938_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_938_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_938_,
    private val saveUseCase: GenSaveUseCase_938_,
    private val deleteUseCase: GenDeleteUseCase_938_,
    private val searchUseCase: GenSearchUseCase_938_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_938_>(GenState_938_.Idle)
    val state: StateFlow<GenState_938_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_938_) {
        when (event) {
            is GenEvent_938_.Load -> loadAll()
            is GenEvent_938_.Update -> save(event.model)
            is GenEvent_938_.Delete -> delete(event.id)
            is GenEvent_938_.Refresh -> loadAll()
            is GenEvent_938_.Search -> search(event.query)
            is GenEvent_938_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_938_.Loading; _state.value = GenState_938_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_938_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_938_.Success(searchUseCase(query)) } }
}
