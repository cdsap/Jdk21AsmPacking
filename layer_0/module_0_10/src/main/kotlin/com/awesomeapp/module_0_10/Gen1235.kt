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

data class GenModel_1235_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1235_ {
    data class Load(val id: Long) : GenEvent_1235_()
    data class Update(val model: GenModel_1235_) : GenEvent_1235_()
    data class Delete(val id: Long) : GenEvent_1235_()
    data object Refresh : GenEvent_1235_()
    data class Search(val query: String) : GenEvent_1235_()
    data class Filter(val predicate: String) : GenEvent_1235_()
}

sealed class GenState_1235_ {
    data object Idle : GenState_1235_()
    data object Loading : GenState_1235_()
    data class Success(val items: List<GenModel_1235_>) : GenState_1235_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1235_()
    data class Partial(val items: List<GenModel_1235_>, val hasMore: Boolean) : GenState_1235_()
}

interface GenRepository_1235_ {
    suspend fun getAll(): List<GenModel_1235_>
    suspend fun getById(id: Long): GenModel_1235_?
    suspend fun save(model: GenModel_1235_): GenModel_1235_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1235_>
}

@Singleton
class GenRepositoryImpl_1235_ @Inject constructor() : GenRepository_1235_ {
    private val store = mutableMapOf<Long, GenModel_1235_>()
    override suspend fun getAll(): List<GenModel_1235_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1235_? = store[id]
    override suspend fun save(model: GenModel_1235_): GenModel_1235_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1235_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1235_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1235_ @Inject constructor(
    private val repository: GenRepositoryImpl_1235_
) : GenUseCase_1235_<Unit, List<GenModel_1235_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1235_> = repository.getAll()
}

class GenSaveUseCase_1235_ @Inject constructor(
    private val repository: GenRepositoryImpl_1235_
) : GenUseCase_1235_<GenModel_1235_, GenModel_1235_> {
    override suspend fun invoke(params: GenModel_1235_): GenModel_1235_ = repository.save(params)
}

class GenDeleteUseCase_1235_ @Inject constructor(
    private val repository: GenRepositoryImpl_1235_
) : GenUseCase_1235_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1235_ @Inject constructor(
    private val repository: GenRepositoryImpl_1235_
) : GenUseCase_1235_<String, List<GenModel_1235_>> {
    override suspend fun invoke(params: String): List<GenModel_1235_> = repository.search(params)
}

abstract class GenMapper_1235_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1235_ : GenMapper_1235_<GenModel_1235_, String>() {
    override fun map(input: GenModel_1235_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1235_ : GenMapper_1235_<String, GenModel_1235_>() {
    override fun map(input: String): GenModel_1235_ {
        val parts = input.split(":")
        return GenModel_1235_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1235_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1235_,
    private val saveUseCase: GenSaveUseCase_1235_,
    private val deleteUseCase: GenDeleteUseCase_1235_,
    private val searchUseCase: GenSearchUseCase_1235_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1235_>(GenState_1235_.Idle)
    val state: StateFlow<GenState_1235_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1235_) {
        when (event) {
            is GenEvent_1235_.Load -> loadAll()
            is GenEvent_1235_.Update -> save(event.model)
            is GenEvent_1235_.Delete -> delete(event.id)
            is GenEvent_1235_.Refresh -> loadAll()
            is GenEvent_1235_.Search -> search(event.query)
            is GenEvent_1235_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1235_.Loading; _state.value = GenState_1235_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1235_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1235_.Success(searchUseCase(query)) } }
}
