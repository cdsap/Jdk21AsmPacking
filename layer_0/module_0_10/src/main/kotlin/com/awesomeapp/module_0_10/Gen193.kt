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

data class GenModel_193_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_193_ {
    data class Load(val id: Long) : GenEvent_193_()
    data class Update(val model: GenModel_193_) : GenEvent_193_()
    data class Delete(val id: Long) : GenEvent_193_()
    data object Refresh : GenEvent_193_()
    data class Search(val query: String) : GenEvent_193_()
    data class Filter(val predicate: String) : GenEvent_193_()
}

sealed class GenState_193_ {
    data object Idle : GenState_193_()
    data object Loading : GenState_193_()
    data class Success(val items: List<GenModel_193_>) : GenState_193_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_193_()
    data class Partial(val items: List<GenModel_193_>, val hasMore: Boolean) : GenState_193_()
}

interface GenRepository_193_ {
    suspend fun getAll(): List<GenModel_193_>
    suspend fun getById(id: Long): GenModel_193_?
    suspend fun save(model: GenModel_193_): GenModel_193_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_193_>
}

@Singleton
class GenRepositoryImpl_193_ @Inject constructor() : GenRepository_193_ {
    private val store = mutableMapOf<Long, GenModel_193_>()
    override suspend fun getAll(): List<GenModel_193_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_193_? = store[id]
    override suspend fun save(model: GenModel_193_): GenModel_193_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_193_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_193_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_193_ @Inject constructor(
    private val repository: GenRepositoryImpl_193_
) : GenUseCase_193_<Unit, List<GenModel_193_>> {
    override suspend fun invoke(params: Unit): List<GenModel_193_> = repository.getAll()
}

class GenSaveUseCase_193_ @Inject constructor(
    private val repository: GenRepositoryImpl_193_
) : GenUseCase_193_<GenModel_193_, GenModel_193_> {
    override suspend fun invoke(params: GenModel_193_): GenModel_193_ = repository.save(params)
}

class GenDeleteUseCase_193_ @Inject constructor(
    private val repository: GenRepositoryImpl_193_
) : GenUseCase_193_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_193_ @Inject constructor(
    private val repository: GenRepositoryImpl_193_
) : GenUseCase_193_<String, List<GenModel_193_>> {
    override suspend fun invoke(params: String): List<GenModel_193_> = repository.search(params)
}

abstract class GenMapper_193_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_193_ : GenMapper_193_<GenModel_193_, String>() {
    override fun map(input: GenModel_193_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_193_ : GenMapper_193_<String, GenModel_193_>() {
    override fun map(input: String): GenModel_193_ {
        val parts = input.split(":")
        return GenModel_193_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_193_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_193_,
    private val saveUseCase: GenSaveUseCase_193_,
    private val deleteUseCase: GenDeleteUseCase_193_,
    private val searchUseCase: GenSearchUseCase_193_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_193_>(GenState_193_.Idle)
    val state: StateFlow<GenState_193_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_193_) {
        when (event) {
            is GenEvent_193_.Load -> loadAll()
            is GenEvent_193_.Update -> save(event.model)
            is GenEvent_193_.Delete -> delete(event.id)
            is GenEvent_193_.Refresh -> loadAll()
            is GenEvent_193_.Search -> search(event.query)
            is GenEvent_193_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_193_.Loading; _state.value = GenState_193_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_193_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_193_.Success(searchUseCase(query)) } }
}
