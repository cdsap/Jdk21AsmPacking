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

data class GenModel_242_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_242_ {
    data class Load(val id: Long) : GenEvent_242_()
    data class Update(val model: GenModel_242_) : GenEvent_242_()
    data class Delete(val id: Long) : GenEvent_242_()
    data object Refresh : GenEvent_242_()
    data class Search(val query: String) : GenEvent_242_()
    data class Filter(val predicate: String) : GenEvent_242_()
}

sealed class GenState_242_ {
    data object Idle : GenState_242_()
    data object Loading : GenState_242_()
    data class Success(val items: List<GenModel_242_>) : GenState_242_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_242_()
    data class Partial(val items: List<GenModel_242_>, val hasMore: Boolean) : GenState_242_()
}

interface GenRepository_242_ {
    suspend fun getAll(): List<GenModel_242_>
    suspend fun getById(id: Long): GenModel_242_?
    suspend fun save(model: GenModel_242_): GenModel_242_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_242_>
}

@Singleton
class GenRepositoryImpl_242_ @Inject constructor() : GenRepository_242_ {
    private val store = mutableMapOf<Long, GenModel_242_>()
    override suspend fun getAll(): List<GenModel_242_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_242_? = store[id]
    override suspend fun save(model: GenModel_242_): GenModel_242_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_242_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_242_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_242_ @Inject constructor(
    private val repository: GenRepositoryImpl_242_
) : GenUseCase_242_<Unit, List<GenModel_242_>> {
    override suspend fun invoke(params: Unit): List<GenModel_242_> = repository.getAll()
}

class GenSaveUseCase_242_ @Inject constructor(
    private val repository: GenRepositoryImpl_242_
) : GenUseCase_242_<GenModel_242_, GenModel_242_> {
    override suspend fun invoke(params: GenModel_242_): GenModel_242_ = repository.save(params)
}

class GenDeleteUseCase_242_ @Inject constructor(
    private val repository: GenRepositoryImpl_242_
) : GenUseCase_242_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_242_ @Inject constructor(
    private val repository: GenRepositoryImpl_242_
) : GenUseCase_242_<String, List<GenModel_242_>> {
    override suspend fun invoke(params: String): List<GenModel_242_> = repository.search(params)
}

abstract class GenMapper_242_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_242_ : GenMapper_242_<GenModel_242_, String>() {
    override fun map(input: GenModel_242_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_242_ : GenMapper_242_<String, GenModel_242_>() {
    override fun map(input: String): GenModel_242_ {
        val parts = input.split(":")
        return GenModel_242_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_242_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_242_,
    private val saveUseCase: GenSaveUseCase_242_,
    private val deleteUseCase: GenDeleteUseCase_242_,
    private val searchUseCase: GenSearchUseCase_242_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_242_>(GenState_242_.Idle)
    val state: StateFlow<GenState_242_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_242_) {
        when (event) {
            is GenEvent_242_.Load -> loadAll()
            is GenEvent_242_.Update -> save(event.model)
            is GenEvent_242_.Delete -> delete(event.id)
            is GenEvent_242_.Refresh -> loadAll()
            is GenEvent_242_.Search -> search(event.query)
            is GenEvent_242_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_242_.Loading; _state.value = GenState_242_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_242_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_242_.Success(searchUseCase(query)) } }
}
