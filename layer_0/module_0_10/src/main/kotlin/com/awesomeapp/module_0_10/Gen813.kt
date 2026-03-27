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

data class GenModel_813_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_813_ {
    data class Load(val id: Long) : GenEvent_813_()
    data class Update(val model: GenModel_813_) : GenEvent_813_()
    data class Delete(val id: Long) : GenEvent_813_()
    data object Refresh : GenEvent_813_()
    data class Search(val query: String) : GenEvent_813_()
    data class Filter(val predicate: String) : GenEvent_813_()
}

sealed class GenState_813_ {
    data object Idle : GenState_813_()
    data object Loading : GenState_813_()
    data class Success(val items: List<GenModel_813_>) : GenState_813_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_813_()
    data class Partial(val items: List<GenModel_813_>, val hasMore: Boolean) : GenState_813_()
}

interface GenRepository_813_ {
    suspend fun getAll(): List<GenModel_813_>
    suspend fun getById(id: Long): GenModel_813_?
    suspend fun save(model: GenModel_813_): GenModel_813_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_813_>
}

@Singleton
class GenRepositoryImpl_813_ @Inject constructor() : GenRepository_813_ {
    private val store = mutableMapOf<Long, GenModel_813_>()
    override suspend fun getAll(): List<GenModel_813_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_813_? = store[id]
    override suspend fun save(model: GenModel_813_): GenModel_813_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_813_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_813_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_813_ @Inject constructor(
    private val repository: GenRepositoryImpl_813_
) : GenUseCase_813_<Unit, List<GenModel_813_>> {
    override suspend fun invoke(params: Unit): List<GenModel_813_> = repository.getAll()
}

class GenSaveUseCase_813_ @Inject constructor(
    private val repository: GenRepositoryImpl_813_
) : GenUseCase_813_<GenModel_813_, GenModel_813_> {
    override suspend fun invoke(params: GenModel_813_): GenModel_813_ = repository.save(params)
}

class GenDeleteUseCase_813_ @Inject constructor(
    private val repository: GenRepositoryImpl_813_
) : GenUseCase_813_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_813_ @Inject constructor(
    private val repository: GenRepositoryImpl_813_
) : GenUseCase_813_<String, List<GenModel_813_>> {
    override suspend fun invoke(params: String): List<GenModel_813_> = repository.search(params)
}

abstract class GenMapper_813_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_813_ : GenMapper_813_<GenModel_813_, String>() {
    override fun map(input: GenModel_813_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_813_ : GenMapper_813_<String, GenModel_813_>() {
    override fun map(input: String): GenModel_813_ {
        val parts = input.split(":")
        return GenModel_813_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_813_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_813_,
    private val saveUseCase: GenSaveUseCase_813_,
    private val deleteUseCase: GenDeleteUseCase_813_,
    private val searchUseCase: GenSearchUseCase_813_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_813_>(GenState_813_.Idle)
    val state: StateFlow<GenState_813_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_813_) {
        when (event) {
            is GenEvent_813_.Load -> loadAll()
            is GenEvent_813_.Update -> save(event.model)
            is GenEvent_813_.Delete -> delete(event.id)
            is GenEvent_813_.Refresh -> loadAll()
            is GenEvent_813_.Search -> search(event.query)
            is GenEvent_813_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_813_.Loading; _state.value = GenState_813_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_813_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_813_.Success(searchUseCase(query)) } }
}
