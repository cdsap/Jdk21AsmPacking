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

data class GenModel_367_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_367_ {
    data class Load(val id: Long) : GenEvent_367_()
    data class Update(val model: GenModel_367_) : GenEvent_367_()
    data class Delete(val id: Long) : GenEvent_367_()
    data object Refresh : GenEvent_367_()
    data class Search(val query: String) : GenEvent_367_()
    data class Filter(val predicate: String) : GenEvent_367_()
}

sealed class GenState_367_ {
    data object Idle : GenState_367_()
    data object Loading : GenState_367_()
    data class Success(val items: List<GenModel_367_>) : GenState_367_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_367_()
    data class Partial(val items: List<GenModel_367_>, val hasMore: Boolean) : GenState_367_()
}

interface GenRepository_367_ {
    suspend fun getAll(): List<GenModel_367_>
    suspend fun getById(id: Long): GenModel_367_?
    suspend fun save(model: GenModel_367_): GenModel_367_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_367_>
}

@Singleton
class GenRepositoryImpl_367_ @Inject constructor() : GenRepository_367_ {
    private val store = mutableMapOf<Long, GenModel_367_>()
    override suspend fun getAll(): List<GenModel_367_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_367_? = store[id]
    override suspend fun save(model: GenModel_367_): GenModel_367_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_367_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_367_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_367_ @Inject constructor(
    private val repository: GenRepositoryImpl_367_
) : GenUseCase_367_<Unit, List<GenModel_367_>> {
    override suspend fun invoke(params: Unit): List<GenModel_367_> = repository.getAll()
}

class GenSaveUseCase_367_ @Inject constructor(
    private val repository: GenRepositoryImpl_367_
) : GenUseCase_367_<GenModel_367_, GenModel_367_> {
    override suspend fun invoke(params: GenModel_367_): GenModel_367_ = repository.save(params)
}

class GenDeleteUseCase_367_ @Inject constructor(
    private val repository: GenRepositoryImpl_367_
) : GenUseCase_367_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_367_ @Inject constructor(
    private val repository: GenRepositoryImpl_367_
) : GenUseCase_367_<String, List<GenModel_367_>> {
    override suspend fun invoke(params: String): List<GenModel_367_> = repository.search(params)
}

abstract class GenMapper_367_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_367_ : GenMapper_367_<GenModel_367_, String>() {
    override fun map(input: GenModel_367_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_367_ : GenMapper_367_<String, GenModel_367_>() {
    override fun map(input: String): GenModel_367_ {
        val parts = input.split(":")
        return GenModel_367_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_367_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_367_,
    private val saveUseCase: GenSaveUseCase_367_,
    private val deleteUseCase: GenDeleteUseCase_367_,
    private val searchUseCase: GenSearchUseCase_367_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_367_>(GenState_367_.Idle)
    val state: StateFlow<GenState_367_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_367_) {
        when (event) {
            is GenEvent_367_.Load -> loadAll()
            is GenEvent_367_.Update -> save(event.model)
            is GenEvent_367_.Delete -> delete(event.id)
            is GenEvent_367_.Refresh -> loadAll()
            is GenEvent_367_.Search -> search(event.query)
            is GenEvent_367_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_367_.Loading; _state.value = GenState_367_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_367_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_367_.Success(searchUseCase(query)) } }
}
