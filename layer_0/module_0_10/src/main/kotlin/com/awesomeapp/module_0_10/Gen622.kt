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

data class GenModel_622_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_622_ {
    data class Load(val id: Long) : GenEvent_622_()
    data class Update(val model: GenModel_622_) : GenEvent_622_()
    data class Delete(val id: Long) : GenEvent_622_()
    data object Refresh : GenEvent_622_()
    data class Search(val query: String) : GenEvent_622_()
    data class Filter(val predicate: String) : GenEvent_622_()
}

sealed class GenState_622_ {
    data object Idle : GenState_622_()
    data object Loading : GenState_622_()
    data class Success(val items: List<GenModel_622_>) : GenState_622_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_622_()
    data class Partial(val items: List<GenModel_622_>, val hasMore: Boolean) : GenState_622_()
}

interface GenRepository_622_ {
    suspend fun getAll(): List<GenModel_622_>
    suspend fun getById(id: Long): GenModel_622_?
    suspend fun save(model: GenModel_622_): GenModel_622_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_622_>
}

@Singleton
class GenRepositoryImpl_622_ @Inject constructor() : GenRepository_622_ {
    private val store = mutableMapOf<Long, GenModel_622_>()
    override suspend fun getAll(): List<GenModel_622_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_622_? = store[id]
    override suspend fun save(model: GenModel_622_): GenModel_622_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_622_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_622_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_622_ @Inject constructor(
    private val repository: GenRepositoryImpl_622_
) : GenUseCase_622_<Unit, List<GenModel_622_>> {
    override suspend fun invoke(params: Unit): List<GenModel_622_> = repository.getAll()
}

class GenSaveUseCase_622_ @Inject constructor(
    private val repository: GenRepositoryImpl_622_
) : GenUseCase_622_<GenModel_622_, GenModel_622_> {
    override suspend fun invoke(params: GenModel_622_): GenModel_622_ = repository.save(params)
}

class GenDeleteUseCase_622_ @Inject constructor(
    private val repository: GenRepositoryImpl_622_
) : GenUseCase_622_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_622_ @Inject constructor(
    private val repository: GenRepositoryImpl_622_
) : GenUseCase_622_<String, List<GenModel_622_>> {
    override suspend fun invoke(params: String): List<GenModel_622_> = repository.search(params)
}

abstract class GenMapper_622_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_622_ : GenMapper_622_<GenModel_622_, String>() {
    override fun map(input: GenModel_622_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_622_ : GenMapper_622_<String, GenModel_622_>() {
    override fun map(input: String): GenModel_622_ {
        val parts = input.split(":")
        return GenModel_622_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_622_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_622_,
    private val saveUseCase: GenSaveUseCase_622_,
    private val deleteUseCase: GenDeleteUseCase_622_,
    private val searchUseCase: GenSearchUseCase_622_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_622_>(GenState_622_.Idle)
    val state: StateFlow<GenState_622_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_622_) {
        when (event) {
            is GenEvent_622_.Load -> loadAll()
            is GenEvent_622_.Update -> save(event.model)
            is GenEvent_622_.Delete -> delete(event.id)
            is GenEvent_622_.Refresh -> loadAll()
            is GenEvent_622_.Search -> search(event.query)
            is GenEvent_622_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_622_.Loading; _state.value = GenState_622_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_622_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_622_.Success(searchUseCase(query)) } }
}
