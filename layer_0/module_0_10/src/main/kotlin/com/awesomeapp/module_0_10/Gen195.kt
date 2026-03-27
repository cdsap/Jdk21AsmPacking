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

data class GenModel_195_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_195_ {
    data class Load(val id: Long) : GenEvent_195_()
    data class Update(val model: GenModel_195_) : GenEvent_195_()
    data class Delete(val id: Long) : GenEvent_195_()
    data object Refresh : GenEvent_195_()
    data class Search(val query: String) : GenEvent_195_()
    data class Filter(val predicate: String) : GenEvent_195_()
}

sealed class GenState_195_ {
    data object Idle : GenState_195_()
    data object Loading : GenState_195_()
    data class Success(val items: List<GenModel_195_>) : GenState_195_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_195_()
    data class Partial(val items: List<GenModel_195_>, val hasMore: Boolean) : GenState_195_()
}

interface GenRepository_195_ {
    suspend fun getAll(): List<GenModel_195_>
    suspend fun getById(id: Long): GenModel_195_?
    suspend fun save(model: GenModel_195_): GenModel_195_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_195_>
}

@Singleton
class GenRepositoryImpl_195_ @Inject constructor() : GenRepository_195_ {
    private val store = mutableMapOf<Long, GenModel_195_>()
    override suspend fun getAll(): List<GenModel_195_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_195_? = store[id]
    override suspend fun save(model: GenModel_195_): GenModel_195_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_195_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_195_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_195_ @Inject constructor(
    private val repository: GenRepositoryImpl_195_
) : GenUseCase_195_<Unit, List<GenModel_195_>> {
    override suspend fun invoke(params: Unit): List<GenModel_195_> = repository.getAll()
}

class GenSaveUseCase_195_ @Inject constructor(
    private val repository: GenRepositoryImpl_195_
) : GenUseCase_195_<GenModel_195_, GenModel_195_> {
    override suspend fun invoke(params: GenModel_195_): GenModel_195_ = repository.save(params)
}

class GenDeleteUseCase_195_ @Inject constructor(
    private val repository: GenRepositoryImpl_195_
) : GenUseCase_195_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_195_ @Inject constructor(
    private val repository: GenRepositoryImpl_195_
) : GenUseCase_195_<String, List<GenModel_195_>> {
    override suspend fun invoke(params: String): List<GenModel_195_> = repository.search(params)
}

abstract class GenMapper_195_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_195_ : GenMapper_195_<GenModel_195_, String>() {
    override fun map(input: GenModel_195_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_195_ : GenMapper_195_<String, GenModel_195_>() {
    override fun map(input: String): GenModel_195_ {
        val parts = input.split(":")
        return GenModel_195_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_195_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_195_,
    private val saveUseCase: GenSaveUseCase_195_,
    private val deleteUseCase: GenDeleteUseCase_195_,
    private val searchUseCase: GenSearchUseCase_195_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_195_>(GenState_195_.Idle)
    val state: StateFlow<GenState_195_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_195_) {
        when (event) {
            is GenEvent_195_.Load -> loadAll()
            is GenEvent_195_.Update -> save(event.model)
            is GenEvent_195_.Delete -> delete(event.id)
            is GenEvent_195_.Refresh -> loadAll()
            is GenEvent_195_.Search -> search(event.query)
            is GenEvent_195_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_195_.Loading; _state.value = GenState_195_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_195_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_195_.Success(searchUseCase(query)) } }
}
