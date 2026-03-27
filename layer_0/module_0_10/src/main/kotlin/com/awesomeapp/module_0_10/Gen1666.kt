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

data class GenModel_1666_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1666_ {
    data class Load(val id: Long) : GenEvent_1666_()
    data class Update(val model: GenModel_1666_) : GenEvent_1666_()
    data class Delete(val id: Long) : GenEvent_1666_()
    data object Refresh : GenEvent_1666_()
    data class Search(val query: String) : GenEvent_1666_()
    data class Filter(val predicate: String) : GenEvent_1666_()
}

sealed class GenState_1666_ {
    data object Idle : GenState_1666_()
    data object Loading : GenState_1666_()
    data class Success(val items: List<GenModel_1666_>) : GenState_1666_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1666_()
    data class Partial(val items: List<GenModel_1666_>, val hasMore: Boolean) : GenState_1666_()
}

interface GenRepository_1666_ {
    suspend fun getAll(): List<GenModel_1666_>
    suspend fun getById(id: Long): GenModel_1666_?
    suspend fun save(model: GenModel_1666_): GenModel_1666_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1666_>
}

@Singleton
class GenRepositoryImpl_1666_ @Inject constructor() : GenRepository_1666_ {
    private val store = mutableMapOf<Long, GenModel_1666_>()
    override suspend fun getAll(): List<GenModel_1666_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1666_? = store[id]
    override suspend fun save(model: GenModel_1666_): GenModel_1666_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1666_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1666_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1666_ @Inject constructor(
    private val repository: GenRepositoryImpl_1666_
) : GenUseCase_1666_<Unit, List<GenModel_1666_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1666_> = repository.getAll()
}

class GenSaveUseCase_1666_ @Inject constructor(
    private val repository: GenRepositoryImpl_1666_
) : GenUseCase_1666_<GenModel_1666_, GenModel_1666_> {
    override suspend fun invoke(params: GenModel_1666_): GenModel_1666_ = repository.save(params)
}

class GenDeleteUseCase_1666_ @Inject constructor(
    private val repository: GenRepositoryImpl_1666_
) : GenUseCase_1666_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1666_ @Inject constructor(
    private val repository: GenRepositoryImpl_1666_
) : GenUseCase_1666_<String, List<GenModel_1666_>> {
    override suspend fun invoke(params: String): List<GenModel_1666_> = repository.search(params)
}

abstract class GenMapper_1666_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1666_ : GenMapper_1666_<GenModel_1666_, String>() {
    override fun map(input: GenModel_1666_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1666_ : GenMapper_1666_<String, GenModel_1666_>() {
    override fun map(input: String): GenModel_1666_ {
        val parts = input.split(":")
        return GenModel_1666_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1666_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1666_,
    private val saveUseCase: GenSaveUseCase_1666_,
    private val deleteUseCase: GenDeleteUseCase_1666_,
    private val searchUseCase: GenSearchUseCase_1666_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1666_>(GenState_1666_.Idle)
    val state: StateFlow<GenState_1666_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1666_) {
        when (event) {
            is GenEvent_1666_.Load -> loadAll()
            is GenEvent_1666_.Update -> save(event.model)
            is GenEvent_1666_.Delete -> delete(event.id)
            is GenEvent_1666_.Refresh -> loadAll()
            is GenEvent_1666_.Search -> search(event.query)
            is GenEvent_1666_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1666_.Loading; _state.value = GenState_1666_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1666_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1666_.Success(searchUseCase(query)) } }
}
