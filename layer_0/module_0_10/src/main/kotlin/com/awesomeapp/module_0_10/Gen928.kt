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

data class GenModel_928_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_928_ {
    data class Load(val id: Long) : GenEvent_928_()
    data class Update(val model: GenModel_928_) : GenEvent_928_()
    data class Delete(val id: Long) : GenEvent_928_()
    data object Refresh : GenEvent_928_()
    data class Search(val query: String) : GenEvent_928_()
    data class Filter(val predicate: String) : GenEvent_928_()
}

sealed class GenState_928_ {
    data object Idle : GenState_928_()
    data object Loading : GenState_928_()
    data class Success(val items: List<GenModel_928_>) : GenState_928_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_928_()
    data class Partial(val items: List<GenModel_928_>, val hasMore: Boolean) : GenState_928_()
}

interface GenRepository_928_ {
    suspend fun getAll(): List<GenModel_928_>
    suspend fun getById(id: Long): GenModel_928_?
    suspend fun save(model: GenModel_928_): GenModel_928_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_928_>
}

@Singleton
class GenRepositoryImpl_928_ @Inject constructor() : GenRepository_928_ {
    private val store = mutableMapOf<Long, GenModel_928_>()
    override suspend fun getAll(): List<GenModel_928_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_928_? = store[id]
    override suspend fun save(model: GenModel_928_): GenModel_928_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_928_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_928_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_928_ @Inject constructor(
    private val repository: GenRepositoryImpl_928_
) : GenUseCase_928_<Unit, List<GenModel_928_>> {
    override suspend fun invoke(params: Unit): List<GenModel_928_> = repository.getAll()
}

class GenSaveUseCase_928_ @Inject constructor(
    private val repository: GenRepositoryImpl_928_
) : GenUseCase_928_<GenModel_928_, GenModel_928_> {
    override suspend fun invoke(params: GenModel_928_): GenModel_928_ = repository.save(params)
}

class GenDeleteUseCase_928_ @Inject constructor(
    private val repository: GenRepositoryImpl_928_
) : GenUseCase_928_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_928_ @Inject constructor(
    private val repository: GenRepositoryImpl_928_
) : GenUseCase_928_<String, List<GenModel_928_>> {
    override suspend fun invoke(params: String): List<GenModel_928_> = repository.search(params)
}

abstract class GenMapper_928_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_928_ : GenMapper_928_<GenModel_928_, String>() {
    override fun map(input: GenModel_928_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_928_ : GenMapper_928_<String, GenModel_928_>() {
    override fun map(input: String): GenModel_928_ {
        val parts = input.split(":")
        return GenModel_928_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_928_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_928_,
    private val saveUseCase: GenSaveUseCase_928_,
    private val deleteUseCase: GenDeleteUseCase_928_,
    private val searchUseCase: GenSearchUseCase_928_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_928_>(GenState_928_.Idle)
    val state: StateFlow<GenState_928_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_928_) {
        when (event) {
            is GenEvent_928_.Load -> loadAll()
            is GenEvent_928_.Update -> save(event.model)
            is GenEvent_928_.Delete -> delete(event.id)
            is GenEvent_928_.Refresh -> loadAll()
            is GenEvent_928_.Search -> search(event.query)
            is GenEvent_928_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_928_.Loading; _state.value = GenState_928_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_928_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_928_.Success(searchUseCase(query)) } }
}
