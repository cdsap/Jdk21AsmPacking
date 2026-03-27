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

data class GenModel_1525_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1525_ {
    data class Load(val id: Long) : GenEvent_1525_()
    data class Update(val model: GenModel_1525_) : GenEvent_1525_()
    data class Delete(val id: Long) : GenEvent_1525_()
    data object Refresh : GenEvent_1525_()
    data class Search(val query: String) : GenEvent_1525_()
    data class Filter(val predicate: String) : GenEvent_1525_()
}

sealed class GenState_1525_ {
    data object Idle : GenState_1525_()
    data object Loading : GenState_1525_()
    data class Success(val items: List<GenModel_1525_>) : GenState_1525_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1525_()
    data class Partial(val items: List<GenModel_1525_>, val hasMore: Boolean) : GenState_1525_()
}

interface GenRepository_1525_ {
    suspend fun getAll(): List<GenModel_1525_>
    suspend fun getById(id: Long): GenModel_1525_?
    suspend fun save(model: GenModel_1525_): GenModel_1525_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1525_>
}

@Singleton
class GenRepositoryImpl_1525_ @Inject constructor() : GenRepository_1525_ {
    private val store = mutableMapOf<Long, GenModel_1525_>()
    override suspend fun getAll(): List<GenModel_1525_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1525_? = store[id]
    override suspend fun save(model: GenModel_1525_): GenModel_1525_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1525_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1525_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1525_ @Inject constructor(
    private val repository: GenRepositoryImpl_1525_
) : GenUseCase_1525_<Unit, List<GenModel_1525_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1525_> = repository.getAll()
}

class GenSaveUseCase_1525_ @Inject constructor(
    private val repository: GenRepositoryImpl_1525_
) : GenUseCase_1525_<GenModel_1525_, GenModel_1525_> {
    override suspend fun invoke(params: GenModel_1525_): GenModel_1525_ = repository.save(params)
}

class GenDeleteUseCase_1525_ @Inject constructor(
    private val repository: GenRepositoryImpl_1525_
) : GenUseCase_1525_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1525_ @Inject constructor(
    private val repository: GenRepositoryImpl_1525_
) : GenUseCase_1525_<String, List<GenModel_1525_>> {
    override suspend fun invoke(params: String): List<GenModel_1525_> = repository.search(params)
}

abstract class GenMapper_1525_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1525_ : GenMapper_1525_<GenModel_1525_, String>() {
    override fun map(input: GenModel_1525_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1525_ : GenMapper_1525_<String, GenModel_1525_>() {
    override fun map(input: String): GenModel_1525_ {
        val parts = input.split(":")
        return GenModel_1525_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1525_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1525_,
    private val saveUseCase: GenSaveUseCase_1525_,
    private val deleteUseCase: GenDeleteUseCase_1525_,
    private val searchUseCase: GenSearchUseCase_1525_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1525_>(GenState_1525_.Idle)
    val state: StateFlow<GenState_1525_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1525_) {
        when (event) {
            is GenEvent_1525_.Load -> loadAll()
            is GenEvent_1525_.Update -> save(event.model)
            is GenEvent_1525_.Delete -> delete(event.id)
            is GenEvent_1525_.Refresh -> loadAll()
            is GenEvent_1525_.Search -> search(event.query)
            is GenEvent_1525_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1525_.Loading; _state.value = GenState_1525_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1525_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1525_.Success(searchUseCase(query)) } }
}
