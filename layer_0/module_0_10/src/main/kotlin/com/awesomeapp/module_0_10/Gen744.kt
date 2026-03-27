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

data class GenModel_744_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_744_ {
    data class Load(val id: Long) : GenEvent_744_()
    data class Update(val model: GenModel_744_) : GenEvent_744_()
    data class Delete(val id: Long) : GenEvent_744_()
    data object Refresh : GenEvent_744_()
    data class Search(val query: String) : GenEvent_744_()
    data class Filter(val predicate: String) : GenEvent_744_()
}

sealed class GenState_744_ {
    data object Idle : GenState_744_()
    data object Loading : GenState_744_()
    data class Success(val items: List<GenModel_744_>) : GenState_744_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_744_()
    data class Partial(val items: List<GenModel_744_>, val hasMore: Boolean) : GenState_744_()
}

interface GenRepository_744_ {
    suspend fun getAll(): List<GenModel_744_>
    suspend fun getById(id: Long): GenModel_744_?
    suspend fun save(model: GenModel_744_): GenModel_744_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_744_>
}

@Singleton
class GenRepositoryImpl_744_ @Inject constructor() : GenRepository_744_ {
    private val store = mutableMapOf<Long, GenModel_744_>()
    override suspend fun getAll(): List<GenModel_744_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_744_? = store[id]
    override suspend fun save(model: GenModel_744_): GenModel_744_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_744_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_744_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_744_ @Inject constructor(
    private val repository: GenRepositoryImpl_744_
) : GenUseCase_744_<Unit, List<GenModel_744_>> {
    override suspend fun invoke(params: Unit): List<GenModel_744_> = repository.getAll()
}

class GenSaveUseCase_744_ @Inject constructor(
    private val repository: GenRepositoryImpl_744_
) : GenUseCase_744_<GenModel_744_, GenModel_744_> {
    override suspend fun invoke(params: GenModel_744_): GenModel_744_ = repository.save(params)
}

class GenDeleteUseCase_744_ @Inject constructor(
    private val repository: GenRepositoryImpl_744_
) : GenUseCase_744_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_744_ @Inject constructor(
    private val repository: GenRepositoryImpl_744_
) : GenUseCase_744_<String, List<GenModel_744_>> {
    override suspend fun invoke(params: String): List<GenModel_744_> = repository.search(params)
}

abstract class GenMapper_744_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_744_ : GenMapper_744_<GenModel_744_, String>() {
    override fun map(input: GenModel_744_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_744_ : GenMapper_744_<String, GenModel_744_>() {
    override fun map(input: String): GenModel_744_ {
        val parts = input.split(":")
        return GenModel_744_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_744_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_744_,
    private val saveUseCase: GenSaveUseCase_744_,
    private val deleteUseCase: GenDeleteUseCase_744_,
    private val searchUseCase: GenSearchUseCase_744_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_744_>(GenState_744_.Idle)
    val state: StateFlow<GenState_744_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_744_) {
        when (event) {
            is GenEvent_744_.Load -> loadAll()
            is GenEvent_744_.Update -> save(event.model)
            is GenEvent_744_.Delete -> delete(event.id)
            is GenEvent_744_.Refresh -> loadAll()
            is GenEvent_744_.Search -> search(event.query)
            is GenEvent_744_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_744_.Loading; _state.value = GenState_744_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_744_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_744_.Success(searchUseCase(query)) } }
}
