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

data class GenModel_2717_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2717_ {
    data class Load(val id: Long) : GenEvent_2717_()
    data class Update(val model: GenModel_2717_) : GenEvent_2717_()
    data class Delete(val id: Long) : GenEvent_2717_()
    data object Refresh : GenEvent_2717_()
    data class Search(val query: String) : GenEvent_2717_()
    data class Filter(val predicate: String) : GenEvent_2717_()
}

sealed class GenState_2717_ {
    data object Idle : GenState_2717_()
    data object Loading : GenState_2717_()
    data class Success(val items: List<GenModel_2717_>) : GenState_2717_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2717_()
    data class Partial(val items: List<GenModel_2717_>, val hasMore: Boolean) : GenState_2717_()
}

interface GenRepository_2717_ {
    suspend fun getAll(): List<GenModel_2717_>
    suspend fun getById(id: Long): GenModel_2717_?
    suspend fun save(model: GenModel_2717_): GenModel_2717_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2717_>
}

@Singleton
class GenRepositoryImpl_2717_ @Inject constructor() : GenRepository_2717_ {
    private val store = mutableMapOf<Long, GenModel_2717_>()
    override suspend fun getAll(): List<GenModel_2717_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2717_? = store[id]
    override suspend fun save(model: GenModel_2717_): GenModel_2717_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2717_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2717_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2717_ @Inject constructor(
    private val repository: GenRepositoryImpl_2717_
) : GenUseCase_2717_<Unit, List<GenModel_2717_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2717_> = repository.getAll()
}

class GenSaveUseCase_2717_ @Inject constructor(
    private val repository: GenRepositoryImpl_2717_
) : GenUseCase_2717_<GenModel_2717_, GenModel_2717_> {
    override suspend fun invoke(params: GenModel_2717_): GenModel_2717_ = repository.save(params)
}

class GenDeleteUseCase_2717_ @Inject constructor(
    private val repository: GenRepositoryImpl_2717_
) : GenUseCase_2717_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2717_ @Inject constructor(
    private val repository: GenRepositoryImpl_2717_
) : GenUseCase_2717_<String, List<GenModel_2717_>> {
    override suspend fun invoke(params: String): List<GenModel_2717_> = repository.search(params)
}

abstract class GenMapper_2717_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2717_ : GenMapper_2717_<GenModel_2717_, String>() {
    override fun map(input: GenModel_2717_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2717_ : GenMapper_2717_<String, GenModel_2717_>() {
    override fun map(input: String): GenModel_2717_ {
        val parts = input.split(":")
        return GenModel_2717_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2717_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2717_,
    private val saveUseCase: GenSaveUseCase_2717_,
    private val deleteUseCase: GenDeleteUseCase_2717_,
    private val searchUseCase: GenSearchUseCase_2717_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2717_>(GenState_2717_.Idle)
    val state: StateFlow<GenState_2717_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2717_) {
        when (event) {
            is GenEvent_2717_.Load -> loadAll()
            is GenEvent_2717_.Update -> save(event.model)
            is GenEvent_2717_.Delete -> delete(event.id)
            is GenEvent_2717_.Refresh -> loadAll()
            is GenEvent_2717_.Search -> search(event.query)
            is GenEvent_2717_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2717_.Loading; _state.value = GenState_2717_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2717_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2717_.Success(searchUseCase(query)) } }
}
