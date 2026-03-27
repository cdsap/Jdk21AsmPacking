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

data class GenModel_640_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_640_ {
    data class Load(val id: Long) : GenEvent_640_()
    data class Update(val model: GenModel_640_) : GenEvent_640_()
    data class Delete(val id: Long) : GenEvent_640_()
    data object Refresh : GenEvent_640_()
    data class Search(val query: String) : GenEvent_640_()
    data class Filter(val predicate: String) : GenEvent_640_()
}

sealed class GenState_640_ {
    data object Idle : GenState_640_()
    data object Loading : GenState_640_()
    data class Success(val items: List<GenModel_640_>) : GenState_640_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_640_()
    data class Partial(val items: List<GenModel_640_>, val hasMore: Boolean) : GenState_640_()
}

interface GenRepository_640_ {
    suspend fun getAll(): List<GenModel_640_>
    suspend fun getById(id: Long): GenModel_640_?
    suspend fun save(model: GenModel_640_): GenModel_640_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_640_>
}

@Singleton
class GenRepositoryImpl_640_ @Inject constructor() : GenRepository_640_ {
    private val store = mutableMapOf<Long, GenModel_640_>()
    override suspend fun getAll(): List<GenModel_640_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_640_? = store[id]
    override suspend fun save(model: GenModel_640_): GenModel_640_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_640_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_640_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_640_ @Inject constructor(
    private val repository: GenRepositoryImpl_640_
) : GenUseCase_640_<Unit, List<GenModel_640_>> {
    override suspend fun invoke(params: Unit): List<GenModel_640_> = repository.getAll()
}

class GenSaveUseCase_640_ @Inject constructor(
    private val repository: GenRepositoryImpl_640_
) : GenUseCase_640_<GenModel_640_, GenModel_640_> {
    override suspend fun invoke(params: GenModel_640_): GenModel_640_ = repository.save(params)
}

class GenDeleteUseCase_640_ @Inject constructor(
    private val repository: GenRepositoryImpl_640_
) : GenUseCase_640_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_640_ @Inject constructor(
    private val repository: GenRepositoryImpl_640_
) : GenUseCase_640_<String, List<GenModel_640_>> {
    override suspend fun invoke(params: String): List<GenModel_640_> = repository.search(params)
}

abstract class GenMapper_640_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_640_ : GenMapper_640_<GenModel_640_, String>() {
    override fun map(input: GenModel_640_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_640_ : GenMapper_640_<String, GenModel_640_>() {
    override fun map(input: String): GenModel_640_ {
        val parts = input.split(":")
        return GenModel_640_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_640_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_640_,
    private val saveUseCase: GenSaveUseCase_640_,
    private val deleteUseCase: GenDeleteUseCase_640_,
    private val searchUseCase: GenSearchUseCase_640_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_640_>(GenState_640_.Idle)
    val state: StateFlow<GenState_640_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_640_) {
        when (event) {
            is GenEvent_640_.Load -> loadAll()
            is GenEvent_640_.Update -> save(event.model)
            is GenEvent_640_.Delete -> delete(event.id)
            is GenEvent_640_.Refresh -> loadAll()
            is GenEvent_640_.Search -> search(event.query)
            is GenEvent_640_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_640_.Loading; _state.value = GenState_640_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_640_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_640_.Success(searchUseCase(query)) } }
}
