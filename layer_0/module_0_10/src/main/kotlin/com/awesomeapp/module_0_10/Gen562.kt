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

data class GenModel_562_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_562_ {
    data class Load(val id: Long) : GenEvent_562_()
    data class Update(val model: GenModel_562_) : GenEvent_562_()
    data class Delete(val id: Long) : GenEvent_562_()
    data object Refresh : GenEvent_562_()
    data class Search(val query: String) : GenEvent_562_()
    data class Filter(val predicate: String) : GenEvent_562_()
}

sealed class GenState_562_ {
    data object Idle : GenState_562_()
    data object Loading : GenState_562_()
    data class Success(val items: List<GenModel_562_>) : GenState_562_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_562_()
    data class Partial(val items: List<GenModel_562_>, val hasMore: Boolean) : GenState_562_()
}

interface GenRepository_562_ {
    suspend fun getAll(): List<GenModel_562_>
    suspend fun getById(id: Long): GenModel_562_?
    suspend fun save(model: GenModel_562_): GenModel_562_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_562_>
}

@Singleton
class GenRepositoryImpl_562_ @Inject constructor() : GenRepository_562_ {
    private val store = mutableMapOf<Long, GenModel_562_>()
    override suspend fun getAll(): List<GenModel_562_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_562_? = store[id]
    override suspend fun save(model: GenModel_562_): GenModel_562_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_562_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_562_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_562_ @Inject constructor(
    private val repository: GenRepositoryImpl_562_
) : GenUseCase_562_<Unit, List<GenModel_562_>> {
    override suspend fun invoke(params: Unit): List<GenModel_562_> = repository.getAll()
}

class GenSaveUseCase_562_ @Inject constructor(
    private val repository: GenRepositoryImpl_562_
) : GenUseCase_562_<GenModel_562_, GenModel_562_> {
    override suspend fun invoke(params: GenModel_562_): GenModel_562_ = repository.save(params)
}

class GenDeleteUseCase_562_ @Inject constructor(
    private val repository: GenRepositoryImpl_562_
) : GenUseCase_562_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_562_ @Inject constructor(
    private val repository: GenRepositoryImpl_562_
) : GenUseCase_562_<String, List<GenModel_562_>> {
    override suspend fun invoke(params: String): List<GenModel_562_> = repository.search(params)
}

abstract class GenMapper_562_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_562_ : GenMapper_562_<GenModel_562_, String>() {
    override fun map(input: GenModel_562_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_562_ : GenMapper_562_<String, GenModel_562_>() {
    override fun map(input: String): GenModel_562_ {
        val parts = input.split(":")
        return GenModel_562_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_562_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_562_,
    private val saveUseCase: GenSaveUseCase_562_,
    private val deleteUseCase: GenDeleteUseCase_562_,
    private val searchUseCase: GenSearchUseCase_562_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_562_>(GenState_562_.Idle)
    val state: StateFlow<GenState_562_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_562_) {
        when (event) {
            is GenEvent_562_.Load -> loadAll()
            is GenEvent_562_.Update -> save(event.model)
            is GenEvent_562_.Delete -> delete(event.id)
            is GenEvent_562_.Refresh -> loadAll()
            is GenEvent_562_.Search -> search(event.query)
            is GenEvent_562_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_562_.Loading; _state.value = GenState_562_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_562_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_562_.Success(searchUseCase(query)) } }
}
