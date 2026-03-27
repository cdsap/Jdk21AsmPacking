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

data class GenModel_1299_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1299_ {
    data class Load(val id: Long) : GenEvent_1299_()
    data class Update(val model: GenModel_1299_) : GenEvent_1299_()
    data class Delete(val id: Long) : GenEvent_1299_()
    data object Refresh : GenEvent_1299_()
    data class Search(val query: String) : GenEvent_1299_()
    data class Filter(val predicate: String) : GenEvent_1299_()
}

sealed class GenState_1299_ {
    data object Idle : GenState_1299_()
    data object Loading : GenState_1299_()
    data class Success(val items: List<GenModel_1299_>) : GenState_1299_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1299_()
    data class Partial(val items: List<GenModel_1299_>, val hasMore: Boolean) : GenState_1299_()
}

interface GenRepository_1299_ {
    suspend fun getAll(): List<GenModel_1299_>
    suspend fun getById(id: Long): GenModel_1299_?
    suspend fun save(model: GenModel_1299_): GenModel_1299_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1299_>
}

@Singleton
class GenRepositoryImpl_1299_ @Inject constructor() : GenRepository_1299_ {
    private val store = mutableMapOf<Long, GenModel_1299_>()
    override suspend fun getAll(): List<GenModel_1299_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1299_? = store[id]
    override suspend fun save(model: GenModel_1299_): GenModel_1299_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1299_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1299_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1299_ @Inject constructor(
    private val repository: GenRepositoryImpl_1299_
) : GenUseCase_1299_<Unit, List<GenModel_1299_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1299_> = repository.getAll()
}

class GenSaveUseCase_1299_ @Inject constructor(
    private val repository: GenRepositoryImpl_1299_
) : GenUseCase_1299_<GenModel_1299_, GenModel_1299_> {
    override suspend fun invoke(params: GenModel_1299_): GenModel_1299_ = repository.save(params)
}

class GenDeleteUseCase_1299_ @Inject constructor(
    private val repository: GenRepositoryImpl_1299_
) : GenUseCase_1299_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1299_ @Inject constructor(
    private val repository: GenRepositoryImpl_1299_
) : GenUseCase_1299_<String, List<GenModel_1299_>> {
    override suspend fun invoke(params: String): List<GenModel_1299_> = repository.search(params)
}

abstract class GenMapper_1299_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1299_ : GenMapper_1299_<GenModel_1299_, String>() {
    override fun map(input: GenModel_1299_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1299_ : GenMapper_1299_<String, GenModel_1299_>() {
    override fun map(input: String): GenModel_1299_ {
        val parts = input.split(":")
        return GenModel_1299_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1299_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1299_,
    private val saveUseCase: GenSaveUseCase_1299_,
    private val deleteUseCase: GenDeleteUseCase_1299_,
    private val searchUseCase: GenSearchUseCase_1299_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1299_>(GenState_1299_.Idle)
    val state: StateFlow<GenState_1299_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1299_) {
        when (event) {
            is GenEvent_1299_.Load -> loadAll()
            is GenEvent_1299_.Update -> save(event.model)
            is GenEvent_1299_.Delete -> delete(event.id)
            is GenEvent_1299_.Refresh -> loadAll()
            is GenEvent_1299_.Search -> search(event.query)
            is GenEvent_1299_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1299_.Loading; _state.value = GenState_1299_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1299_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1299_.Success(searchUseCase(query)) } }
}
