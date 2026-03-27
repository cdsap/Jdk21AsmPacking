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

data class GenModel_1375_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1375_ {
    data class Load(val id: Long) : GenEvent_1375_()
    data class Update(val model: GenModel_1375_) : GenEvent_1375_()
    data class Delete(val id: Long) : GenEvent_1375_()
    data object Refresh : GenEvent_1375_()
    data class Search(val query: String) : GenEvent_1375_()
    data class Filter(val predicate: String) : GenEvent_1375_()
}

sealed class GenState_1375_ {
    data object Idle : GenState_1375_()
    data object Loading : GenState_1375_()
    data class Success(val items: List<GenModel_1375_>) : GenState_1375_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1375_()
    data class Partial(val items: List<GenModel_1375_>, val hasMore: Boolean) : GenState_1375_()
}

interface GenRepository_1375_ {
    suspend fun getAll(): List<GenModel_1375_>
    suspend fun getById(id: Long): GenModel_1375_?
    suspend fun save(model: GenModel_1375_): GenModel_1375_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1375_>
}

@Singleton
class GenRepositoryImpl_1375_ @Inject constructor() : GenRepository_1375_ {
    private val store = mutableMapOf<Long, GenModel_1375_>()
    override suspend fun getAll(): List<GenModel_1375_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1375_? = store[id]
    override suspend fun save(model: GenModel_1375_): GenModel_1375_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1375_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1375_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1375_ @Inject constructor(
    private val repository: GenRepositoryImpl_1375_
) : GenUseCase_1375_<Unit, List<GenModel_1375_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1375_> = repository.getAll()
}

class GenSaveUseCase_1375_ @Inject constructor(
    private val repository: GenRepositoryImpl_1375_
) : GenUseCase_1375_<GenModel_1375_, GenModel_1375_> {
    override suspend fun invoke(params: GenModel_1375_): GenModel_1375_ = repository.save(params)
}

class GenDeleteUseCase_1375_ @Inject constructor(
    private val repository: GenRepositoryImpl_1375_
) : GenUseCase_1375_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1375_ @Inject constructor(
    private val repository: GenRepositoryImpl_1375_
) : GenUseCase_1375_<String, List<GenModel_1375_>> {
    override suspend fun invoke(params: String): List<GenModel_1375_> = repository.search(params)
}

abstract class GenMapper_1375_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1375_ : GenMapper_1375_<GenModel_1375_, String>() {
    override fun map(input: GenModel_1375_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1375_ : GenMapper_1375_<String, GenModel_1375_>() {
    override fun map(input: String): GenModel_1375_ {
        val parts = input.split(":")
        return GenModel_1375_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1375_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1375_,
    private val saveUseCase: GenSaveUseCase_1375_,
    private val deleteUseCase: GenDeleteUseCase_1375_,
    private val searchUseCase: GenSearchUseCase_1375_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1375_>(GenState_1375_.Idle)
    val state: StateFlow<GenState_1375_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1375_) {
        when (event) {
            is GenEvent_1375_.Load -> loadAll()
            is GenEvent_1375_.Update -> save(event.model)
            is GenEvent_1375_.Delete -> delete(event.id)
            is GenEvent_1375_.Refresh -> loadAll()
            is GenEvent_1375_.Search -> search(event.query)
            is GenEvent_1375_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1375_.Loading; _state.value = GenState_1375_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1375_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1375_.Success(searchUseCase(query)) } }
}
