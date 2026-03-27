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

data class GenModel_2625_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2625_ {
    data class Load(val id: Long) : GenEvent_2625_()
    data class Update(val model: GenModel_2625_) : GenEvent_2625_()
    data class Delete(val id: Long) : GenEvent_2625_()
    data object Refresh : GenEvent_2625_()
    data class Search(val query: String) : GenEvent_2625_()
    data class Filter(val predicate: String) : GenEvent_2625_()
}

sealed class GenState_2625_ {
    data object Idle : GenState_2625_()
    data object Loading : GenState_2625_()
    data class Success(val items: List<GenModel_2625_>) : GenState_2625_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2625_()
    data class Partial(val items: List<GenModel_2625_>, val hasMore: Boolean) : GenState_2625_()
}

interface GenRepository_2625_ {
    suspend fun getAll(): List<GenModel_2625_>
    suspend fun getById(id: Long): GenModel_2625_?
    suspend fun save(model: GenModel_2625_): GenModel_2625_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2625_>
}

@Singleton
class GenRepositoryImpl_2625_ @Inject constructor() : GenRepository_2625_ {
    private val store = mutableMapOf<Long, GenModel_2625_>()
    override suspend fun getAll(): List<GenModel_2625_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2625_? = store[id]
    override suspend fun save(model: GenModel_2625_): GenModel_2625_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2625_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2625_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2625_ @Inject constructor(
    private val repository: GenRepositoryImpl_2625_
) : GenUseCase_2625_<Unit, List<GenModel_2625_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2625_> = repository.getAll()
}

class GenSaveUseCase_2625_ @Inject constructor(
    private val repository: GenRepositoryImpl_2625_
) : GenUseCase_2625_<GenModel_2625_, GenModel_2625_> {
    override suspend fun invoke(params: GenModel_2625_): GenModel_2625_ = repository.save(params)
}

class GenDeleteUseCase_2625_ @Inject constructor(
    private val repository: GenRepositoryImpl_2625_
) : GenUseCase_2625_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2625_ @Inject constructor(
    private val repository: GenRepositoryImpl_2625_
) : GenUseCase_2625_<String, List<GenModel_2625_>> {
    override suspend fun invoke(params: String): List<GenModel_2625_> = repository.search(params)
}

abstract class GenMapper_2625_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2625_ : GenMapper_2625_<GenModel_2625_, String>() {
    override fun map(input: GenModel_2625_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2625_ : GenMapper_2625_<String, GenModel_2625_>() {
    override fun map(input: String): GenModel_2625_ {
        val parts = input.split(":")
        return GenModel_2625_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2625_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2625_,
    private val saveUseCase: GenSaveUseCase_2625_,
    private val deleteUseCase: GenDeleteUseCase_2625_,
    private val searchUseCase: GenSearchUseCase_2625_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2625_>(GenState_2625_.Idle)
    val state: StateFlow<GenState_2625_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2625_) {
        when (event) {
            is GenEvent_2625_.Load -> loadAll()
            is GenEvent_2625_.Update -> save(event.model)
            is GenEvent_2625_.Delete -> delete(event.id)
            is GenEvent_2625_.Refresh -> loadAll()
            is GenEvent_2625_.Search -> search(event.query)
            is GenEvent_2625_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2625_.Loading; _state.value = GenState_2625_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2625_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2625_.Success(searchUseCase(query)) } }
}
