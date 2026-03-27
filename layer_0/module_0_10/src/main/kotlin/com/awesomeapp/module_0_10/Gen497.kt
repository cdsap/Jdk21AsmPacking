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

data class GenModel_497_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_497_ {
    data class Load(val id: Long) : GenEvent_497_()
    data class Update(val model: GenModel_497_) : GenEvent_497_()
    data class Delete(val id: Long) : GenEvent_497_()
    data object Refresh : GenEvent_497_()
    data class Search(val query: String) : GenEvent_497_()
    data class Filter(val predicate: String) : GenEvent_497_()
}

sealed class GenState_497_ {
    data object Idle : GenState_497_()
    data object Loading : GenState_497_()
    data class Success(val items: List<GenModel_497_>) : GenState_497_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_497_()
    data class Partial(val items: List<GenModel_497_>, val hasMore: Boolean) : GenState_497_()
}

interface GenRepository_497_ {
    suspend fun getAll(): List<GenModel_497_>
    suspend fun getById(id: Long): GenModel_497_?
    suspend fun save(model: GenModel_497_): GenModel_497_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_497_>
}

@Singleton
class GenRepositoryImpl_497_ @Inject constructor() : GenRepository_497_ {
    private val store = mutableMapOf<Long, GenModel_497_>()
    override suspend fun getAll(): List<GenModel_497_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_497_? = store[id]
    override suspend fun save(model: GenModel_497_): GenModel_497_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_497_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_497_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_497_ @Inject constructor(
    private val repository: GenRepositoryImpl_497_
) : GenUseCase_497_<Unit, List<GenModel_497_>> {
    override suspend fun invoke(params: Unit): List<GenModel_497_> = repository.getAll()
}

class GenSaveUseCase_497_ @Inject constructor(
    private val repository: GenRepositoryImpl_497_
) : GenUseCase_497_<GenModel_497_, GenModel_497_> {
    override suspend fun invoke(params: GenModel_497_): GenModel_497_ = repository.save(params)
}

class GenDeleteUseCase_497_ @Inject constructor(
    private val repository: GenRepositoryImpl_497_
) : GenUseCase_497_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_497_ @Inject constructor(
    private val repository: GenRepositoryImpl_497_
) : GenUseCase_497_<String, List<GenModel_497_>> {
    override suspend fun invoke(params: String): List<GenModel_497_> = repository.search(params)
}

abstract class GenMapper_497_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_497_ : GenMapper_497_<GenModel_497_, String>() {
    override fun map(input: GenModel_497_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_497_ : GenMapper_497_<String, GenModel_497_>() {
    override fun map(input: String): GenModel_497_ {
        val parts = input.split(":")
        return GenModel_497_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_497_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_497_,
    private val saveUseCase: GenSaveUseCase_497_,
    private val deleteUseCase: GenDeleteUseCase_497_,
    private val searchUseCase: GenSearchUseCase_497_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_497_>(GenState_497_.Idle)
    val state: StateFlow<GenState_497_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_497_) {
        when (event) {
            is GenEvent_497_.Load -> loadAll()
            is GenEvent_497_.Update -> save(event.model)
            is GenEvent_497_.Delete -> delete(event.id)
            is GenEvent_497_.Refresh -> loadAll()
            is GenEvent_497_.Search -> search(event.query)
            is GenEvent_497_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_497_.Loading; _state.value = GenState_497_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_497_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_497_.Success(searchUseCase(query)) } }
}
