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

data class GenModel_22_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_22_ {
    data class Load(val id: Long) : GenEvent_22_()
    data class Update(val model: GenModel_22_) : GenEvent_22_()
    data class Delete(val id: Long) : GenEvent_22_()
    data object Refresh : GenEvent_22_()
    data class Search(val query: String) : GenEvent_22_()
    data class Filter(val predicate: String) : GenEvent_22_()
}

sealed class GenState_22_ {
    data object Idle : GenState_22_()
    data object Loading : GenState_22_()
    data class Success(val items: List<GenModel_22_>) : GenState_22_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_22_()
    data class Partial(val items: List<GenModel_22_>, val hasMore: Boolean) : GenState_22_()
}

interface GenRepository_22_ {
    suspend fun getAll(): List<GenModel_22_>
    suspend fun getById(id: Long): GenModel_22_?
    suspend fun save(model: GenModel_22_): GenModel_22_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_22_>
}

@Singleton
class GenRepositoryImpl_22_ @Inject constructor() : GenRepository_22_ {
    private val store = mutableMapOf<Long, GenModel_22_>()
    override suspend fun getAll(): List<GenModel_22_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_22_? = store[id]
    override suspend fun save(model: GenModel_22_): GenModel_22_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_22_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_22_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_22_ @Inject constructor(
    private val repository: GenRepositoryImpl_22_
) : GenUseCase_22_<Unit, List<GenModel_22_>> {
    override suspend fun invoke(params: Unit): List<GenModel_22_> = repository.getAll()
}

class GenSaveUseCase_22_ @Inject constructor(
    private val repository: GenRepositoryImpl_22_
) : GenUseCase_22_<GenModel_22_, GenModel_22_> {
    override suspend fun invoke(params: GenModel_22_): GenModel_22_ = repository.save(params)
}

class GenDeleteUseCase_22_ @Inject constructor(
    private val repository: GenRepositoryImpl_22_
) : GenUseCase_22_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_22_ @Inject constructor(
    private val repository: GenRepositoryImpl_22_
) : GenUseCase_22_<String, List<GenModel_22_>> {
    override suspend fun invoke(params: String): List<GenModel_22_> = repository.search(params)
}

abstract class GenMapper_22_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_22_ : GenMapper_22_<GenModel_22_, String>() {
    override fun map(input: GenModel_22_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_22_ : GenMapper_22_<String, GenModel_22_>() {
    override fun map(input: String): GenModel_22_ {
        val parts = input.split(":")
        return GenModel_22_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_22_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_22_,
    private val saveUseCase: GenSaveUseCase_22_,
    private val deleteUseCase: GenDeleteUseCase_22_,
    private val searchUseCase: GenSearchUseCase_22_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_22_>(GenState_22_.Idle)
    val state: StateFlow<GenState_22_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_22_) {
        when (event) {
            is GenEvent_22_.Load -> loadAll()
            is GenEvent_22_.Update -> save(event.model)
            is GenEvent_22_.Delete -> delete(event.id)
            is GenEvent_22_.Refresh -> loadAll()
            is GenEvent_22_.Search -> search(event.query)
            is GenEvent_22_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_22_.Loading; _state.value = GenState_22_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_22_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_22_.Success(searchUseCase(query)) } }
}
