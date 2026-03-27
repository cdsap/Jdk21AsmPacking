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

data class GenModel_3497_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3497_ {
    data class Load(val id: Long) : GenEvent_3497_()
    data class Update(val model: GenModel_3497_) : GenEvent_3497_()
    data class Delete(val id: Long) : GenEvent_3497_()
    data object Refresh : GenEvent_3497_()
    data class Search(val query: String) : GenEvent_3497_()
    data class Filter(val predicate: String) : GenEvent_3497_()
}

sealed class GenState_3497_ {
    data object Idle : GenState_3497_()
    data object Loading : GenState_3497_()
    data class Success(val items: List<GenModel_3497_>) : GenState_3497_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3497_()
    data class Partial(val items: List<GenModel_3497_>, val hasMore: Boolean) : GenState_3497_()
}

interface GenRepository_3497_ {
    suspend fun getAll(): List<GenModel_3497_>
    suspend fun getById(id: Long): GenModel_3497_?
    suspend fun save(model: GenModel_3497_): GenModel_3497_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3497_>
}

@Singleton
class GenRepositoryImpl_3497_ @Inject constructor() : GenRepository_3497_ {
    private val store = mutableMapOf<Long, GenModel_3497_>()
    override suspend fun getAll(): List<GenModel_3497_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3497_? = store[id]
    override suspend fun save(model: GenModel_3497_): GenModel_3497_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3497_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3497_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3497_ @Inject constructor(
    private val repository: GenRepositoryImpl_3497_
) : GenUseCase_3497_<Unit, List<GenModel_3497_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3497_> = repository.getAll()
}

class GenSaveUseCase_3497_ @Inject constructor(
    private val repository: GenRepositoryImpl_3497_
) : GenUseCase_3497_<GenModel_3497_, GenModel_3497_> {
    override suspend fun invoke(params: GenModel_3497_): GenModel_3497_ = repository.save(params)
}

class GenDeleteUseCase_3497_ @Inject constructor(
    private val repository: GenRepositoryImpl_3497_
) : GenUseCase_3497_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3497_ @Inject constructor(
    private val repository: GenRepositoryImpl_3497_
) : GenUseCase_3497_<String, List<GenModel_3497_>> {
    override suspend fun invoke(params: String): List<GenModel_3497_> = repository.search(params)
}

abstract class GenMapper_3497_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3497_ : GenMapper_3497_<GenModel_3497_, String>() {
    override fun map(input: GenModel_3497_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3497_ : GenMapper_3497_<String, GenModel_3497_>() {
    override fun map(input: String): GenModel_3497_ {
        val parts = input.split(":")
        return GenModel_3497_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3497_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3497_,
    private val saveUseCase: GenSaveUseCase_3497_,
    private val deleteUseCase: GenDeleteUseCase_3497_,
    private val searchUseCase: GenSearchUseCase_3497_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3497_>(GenState_3497_.Idle)
    val state: StateFlow<GenState_3497_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3497_) {
        when (event) {
            is GenEvent_3497_.Load -> loadAll()
            is GenEvent_3497_.Update -> save(event.model)
            is GenEvent_3497_.Delete -> delete(event.id)
            is GenEvent_3497_.Refresh -> loadAll()
            is GenEvent_3497_.Search -> search(event.query)
            is GenEvent_3497_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3497_.Loading; _state.value = GenState_3497_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3497_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3497_.Success(searchUseCase(query)) } }
}
