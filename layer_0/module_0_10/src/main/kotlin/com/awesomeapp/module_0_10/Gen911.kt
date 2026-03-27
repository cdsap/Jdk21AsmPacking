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

data class GenModel_911_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_911_ {
    data class Load(val id: Long) : GenEvent_911_()
    data class Update(val model: GenModel_911_) : GenEvent_911_()
    data class Delete(val id: Long) : GenEvent_911_()
    data object Refresh : GenEvent_911_()
    data class Search(val query: String) : GenEvent_911_()
    data class Filter(val predicate: String) : GenEvent_911_()
}

sealed class GenState_911_ {
    data object Idle : GenState_911_()
    data object Loading : GenState_911_()
    data class Success(val items: List<GenModel_911_>) : GenState_911_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_911_()
    data class Partial(val items: List<GenModel_911_>, val hasMore: Boolean) : GenState_911_()
}

interface GenRepository_911_ {
    suspend fun getAll(): List<GenModel_911_>
    suspend fun getById(id: Long): GenModel_911_?
    suspend fun save(model: GenModel_911_): GenModel_911_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_911_>
}

@Singleton
class GenRepositoryImpl_911_ @Inject constructor() : GenRepository_911_ {
    private val store = mutableMapOf<Long, GenModel_911_>()
    override suspend fun getAll(): List<GenModel_911_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_911_? = store[id]
    override suspend fun save(model: GenModel_911_): GenModel_911_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_911_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_911_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_911_ @Inject constructor(
    private val repository: GenRepositoryImpl_911_
) : GenUseCase_911_<Unit, List<GenModel_911_>> {
    override suspend fun invoke(params: Unit): List<GenModel_911_> = repository.getAll()
}

class GenSaveUseCase_911_ @Inject constructor(
    private val repository: GenRepositoryImpl_911_
) : GenUseCase_911_<GenModel_911_, GenModel_911_> {
    override suspend fun invoke(params: GenModel_911_): GenModel_911_ = repository.save(params)
}

class GenDeleteUseCase_911_ @Inject constructor(
    private val repository: GenRepositoryImpl_911_
) : GenUseCase_911_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_911_ @Inject constructor(
    private val repository: GenRepositoryImpl_911_
) : GenUseCase_911_<String, List<GenModel_911_>> {
    override suspend fun invoke(params: String): List<GenModel_911_> = repository.search(params)
}

abstract class GenMapper_911_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_911_ : GenMapper_911_<GenModel_911_, String>() {
    override fun map(input: GenModel_911_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_911_ : GenMapper_911_<String, GenModel_911_>() {
    override fun map(input: String): GenModel_911_ {
        val parts = input.split(":")
        return GenModel_911_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_911_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_911_,
    private val saveUseCase: GenSaveUseCase_911_,
    private val deleteUseCase: GenDeleteUseCase_911_,
    private val searchUseCase: GenSearchUseCase_911_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_911_>(GenState_911_.Idle)
    val state: StateFlow<GenState_911_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_911_) {
        when (event) {
            is GenEvent_911_.Load -> loadAll()
            is GenEvent_911_.Update -> save(event.model)
            is GenEvent_911_.Delete -> delete(event.id)
            is GenEvent_911_.Refresh -> loadAll()
            is GenEvent_911_.Search -> search(event.query)
            is GenEvent_911_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_911_.Loading; _state.value = GenState_911_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_911_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_911_.Success(searchUseCase(query)) } }
}
