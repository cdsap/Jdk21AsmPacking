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

data class GenModel_11_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_11_ {
    data class Load(val id: Long) : GenEvent_11_()
    data class Update(val model: GenModel_11_) : GenEvent_11_()
    data class Delete(val id: Long) : GenEvent_11_()
    data object Refresh : GenEvent_11_()
    data class Search(val query: String) : GenEvent_11_()
    data class Filter(val predicate: String) : GenEvent_11_()
}

sealed class GenState_11_ {
    data object Idle : GenState_11_()
    data object Loading : GenState_11_()
    data class Success(val items: List<GenModel_11_>) : GenState_11_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_11_()
    data class Partial(val items: List<GenModel_11_>, val hasMore: Boolean) : GenState_11_()
}

interface GenRepository_11_ {
    suspend fun getAll(): List<GenModel_11_>
    suspend fun getById(id: Long): GenModel_11_?
    suspend fun save(model: GenModel_11_): GenModel_11_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_11_>
}

@Singleton
class GenRepositoryImpl_11_ @Inject constructor() : GenRepository_11_ {
    private val store = mutableMapOf<Long, GenModel_11_>()
    override suspend fun getAll(): List<GenModel_11_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_11_? = store[id]
    override suspend fun save(model: GenModel_11_): GenModel_11_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_11_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_11_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_11_ @Inject constructor(
    private val repository: GenRepositoryImpl_11_
) : GenUseCase_11_<Unit, List<GenModel_11_>> {
    override suspend fun invoke(params: Unit): List<GenModel_11_> = repository.getAll()
}

class GenSaveUseCase_11_ @Inject constructor(
    private val repository: GenRepositoryImpl_11_
) : GenUseCase_11_<GenModel_11_, GenModel_11_> {
    override suspend fun invoke(params: GenModel_11_): GenModel_11_ = repository.save(params)
}

class GenDeleteUseCase_11_ @Inject constructor(
    private val repository: GenRepositoryImpl_11_
) : GenUseCase_11_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_11_ @Inject constructor(
    private val repository: GenRepositoryImpl_11_
) : GenUseCase_11_<String, List<GenModel_11_>> {
    override suspend fun invoke(params: String): List<GenModel_11_> = repository.search(params)
}

abstract class GenMapper_11_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_11_ : GenMapper_11_<GenModel_11_, String>() {
    override fun map(input: GenModel_11_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_11_ : GenMapper_11_<String, GenModel_11_>() {
    override fun map(input: String): GenModel_11_ {
        val parts = input.split(":")
        return GenModel_11_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_11_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_11_,
    private val saveUseCase: GenSaveUseCase_11_,
    private val deleteUseCase: GenDeleteUseCase_11_,
    private val searchUseCase: GenSearchUseCase_11_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_11_>(GenState_11_.Idle)
    val state: StateFlow<GenState_11_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_11_) {
        when (event) {
            is GenEvent_11_.Load -> loadAll()
            is GenEvent_11_.Update -> save(event.model)
            is GenEvent_11_.Delete -> delete(event.id)
            is GenEvent_11_.Refresh -> loadAll()
            is GenEvent_11_.Search -> search(event.query)
            is GenEvent_11_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_11_.Loading; _state.value = GenState_11_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_11_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_11_.Success(searchUseCase(query)) } }
}
