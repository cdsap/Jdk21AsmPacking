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

data class GenModel_842_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_842_ {
    data class Load(val id: Long) : GenEvent_842_()
    data class Update(val model: GenModel_842_) : GenEvent_842_()
    data class Delete(val id: Long) : GenEvent_842_()
    data object Refresh : GenEvent_842_()
    data class Search(val query: String) : GenEvent_842_()
    data class Filter(val predicate: String) : GenEvent_842_()
}

sealed class GenState_842_ {
    data object Idle : GenState_842_()
    data object Loading : GenState_842_()
    data class Success(val items: List<GenModel_842_>) : GenState_842_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_842_()
    data class Partial(val items: List<GenModel_842_>, val hasMore: Boolean) : GenState_842_()
}

interface GenRepository_842_ {
    suspend fun getAll(): List<GenModel_842_>
    suspend fun getById(id: Long): GenModel_842_?
    suspend fun save(model: GenModel_842_): GenModel_842_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_842_>
}

@Singleton
class GenRepositoryImpl_842_ @Inject constructor() : GenRepository_842_ {
    private val store = mutableMapOf<Long, GenModel_842_>()
    override suspend fun getAll(): List<GenModel_842_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_842_? = store[id]
    override suspend fun save(model: GenModel_842_): GenModel_842_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_842_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_842_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_842_ @Inject constructor(
    private val repository: GenRepositoryImpl_842_
) : GenUseCase_842_<Unit, List<GenModel_842_>> {
    override suspend fun invoke(params: Unit): List<GenModel_842_> = repository.getAll()
}

class GenSaveUseCase_842_ @Inject constructor(
    private val repository: GenRepositoryImpl_842_
) : GenUseCase_842_<GenModel_842_, GenModel_842_> {
    override suspend fun invoke(params: GenModel_842_): GenModel_842_ = repository.save(params)
}

class GenDeleteUseCase_842_ @Inject constructor(
    private val repository: GenRepositoryImpl_842_
) : GenUseCase_842_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_842_ @Inject constructor(
    private val repository: GenRepositoryImpl_842_
) : GenUseCase_842_<String, List<GenModel_842_>> {
    override suspend fun invoke(params: String): List<GenModel_842_> = repository.search(params)
}

abstract class GenMapper_842_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_842_ : GenMapper_842_<GenModel_842_, String>() {
    override fun map(input: GenModel_842_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_842_ : GenMapper_842_<String, GenModel_842_>() {
    override fun map(input: String): GenModel_842_ {
        val parts = input.split(":")
        return GenModel_842_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_842_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_842_,
    private val saveUseCase: GenSaveUseCase_842_,
    private val deleteUseCase: GenDeleteUseCase_842_,
    private val searchUseCase: GenSearchUseCase_842_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_842_>(GenState_842_.Idle)
    val state: StateFlow<GenState_842_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_842_) {
        when (event) {
            is GenEvent_842_.Load -> loadAll()
            is GenEvent_842_.Update -> save(event.model)
            is GenEvent_842_.Delete -> delete(event.id)
            is GenEvent_842_.Refresh -> loadAll()
            is GenEvent_842_.Search -> search(event.query)
            is GenEvent_842_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_842_.Loading; _state.value = GenState_842_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_842_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_842_.Success(searchUseCase(query)) } }
}
