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

data class GenModel_1874_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1874_ {
    data class Load(val id: Long) : GenEvent_1874_()
    data class Update(val model: GenModel_1874_) : GenEvent_1874_()
    data class Delete(val id: Long) : GenEvent_1874_()
    data object Refresh : GenEvent_1874_()
    data class Search(val query: String) : GenEvent_1874_()
    data class Filter(val predicate: String) : GenEvent_1874_()
}

sealed class GenState_1874_ {
    data object Idle : GenState_1874_()
    data object Loading : GenState_1874_()
    data class Success(val items: List<GenModel_1874_>) : GenState_1874_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1874_()
    data class Partial(val items: List<GenModel_1874_>, val hasMore: Boolean) : GenState_1874_()
}

interface GenRepository_1874_ {
    suspend fun getAll(): List<GenModel_1874_>
    suspend fun getById(id: Long): GenModel_1874_?
    suspend fun save(model: GenModel_1874_): GenModel_1874_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1874_>
}

@Singleton
class GenRepositoryImpl_1874_ @Inject constructor() : GenRepository_1874_ {
    private val store = mutableMapOf<Long, GenModel_1874_>()
    override suspend fun getAll(): List<GenModel_1874_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1874_? = store[id]
    override suspend fun save(model: GenModel_1874_): GenModel_1874_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1874_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1874_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1874_ @Inject constructor(
    private val repository: GenRepositoryImpl_1874_
) : GenUseCase_1874_<Unit, List<GenModel_1874_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1874_> = repository.getAll()
}

class GenSaveUseCase_1874_ @Inject constructor(
    private val repository: GenRepositoryImpl_1874_
) : GenUseCase_1874_<GenModel_1874_, GenModel_1874_> {
    override suspend fun invoke(params: GenModel_1874_): GenModel_1874_ = repository.save(params)
}

class GenDeleteUseCase_1874_ @Inject constructor(
    private val repository: GenRepositoryImpl_1874_
) : GenUseCase_1874_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1874_ @Inject constructor(
    private val repository: GenRepositoryImpl_1874_
) : GenUseCase_1874_<String, List<GenModel_1874_>> {
    override suspend fun invoke(params: String): List<GenModel_1874_> = repository.search(params)
}

abstract class GenMapper_1874_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1874_ : GenMapper_1874_<GenModel_1874_, String>() {
    override fun map(input: GenModel_1874_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1874_ : GenMapper_1874_<String, GenModel_1874_>() {
    override fun map(input: String): GenModel_1874_ {
        val parts = input.split(":")
        return GenModel_1874_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1874_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1874_,
    private val saveUseCase: GenSaveUseCase_1874_,
    private val deleteUseCase: GenDeleteUseCase_1874_,
    private val searchUseCase: GenSearchUseCase_1874_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1874_>(GenState_1874_.Idle)
    val state: StateFlow<GenState_1874_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1874_) {
        when (event) {
            is GenEvent_1874_.Load -> loadAll()
            is GenEvent_1874_.Update -> save(event.model)
            is GenEvent_1874_.Delete -> delete(event.id)
            is GenEvent_1874_.Refresh -> loadAll()
            is GenEvent_1874_.Search -> search(event.query)
            is GenEvent_1874_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1874_.Loading; _state.value = GenState_1874_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1874_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1874_.Success(searchUseCase(query)) } }
}
