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

data class GenModel_973_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_973_ {
    data class Load(val id: Long) : GenEvent_973_()
    data class Update(val model: GenModel_973_) : GenEvent_973_()
    data class Delete(val id: Long) : GenEvent_973_()
    data object Refresh : GenEvent_973_()
    data class Search(val query: String) : GenEvent_973_()
    data class Filter(val predicate: String) : GenEvent_973_()
}

sealed class GenState_973_ {
    data object Idle : GenState_973_()
    data object Loading : GenState_973_()
    data class Success(val items: List<GenModel_973_>) : GenState_973_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_973_()
    data class Partial(val items: List<GenModel_973_>, val hasMore: Boolean) : GenState_973_()
}

interface GenRepository_973_ {
    suspend fun getAll(): List<GenModel_973_>
    suspend fun getById(id: Long): GenModel_973_?
    suspend fun save(model: GenModel_973_): GenModel_973_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_973_>
}

@Singleton
class GenRepositoryImpl_973_ @Inject constructor() : GenRepository_973_ {
    private val store = mutableMapOf<Long, GenModel_973_>()
    override suspend fun getAll(): List<GenModel_973_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_973_? = store[id]
    override suspend fun save(model: GenModel_973_): GenModel_973_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_973_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_973_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_973_ @Inject constructor(
    private val repository: GenRepositoryImpl_973_
) : GenUseCase_973_<Unit, List<GenModel_973_>> {
    override suspend fun invoke(params: Unit): List<GenModel_973_> = repository.getAll()
}

class GenSaveUseCase_973_ @Inject constructor(
    private val repository: GenRepositoryImpl_973_
) : GenUseCase_973_<GenModel_973_, GenModel_973_> {
    override suspend fun invoke(params: GenModel_973_): GenModel_973_ = repository.save(params)
}

class GenDeleteUseCase_973_ @Inject constructor(
    private val repository: GenRepositoryImpl_973_
) : GenUseCase_973_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_973_ @Inject constructor(
    private val repository: GenRepositoryImpl_973_
) : GenUseCase_973_<String, List<GenModel_973_>> {
    override suspend fun invoke(params: String): List<GenModel_973_> = repository.search(params)
}

abstract class GenMapper_973_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_973_ : GenMapper_973_<GenModel_973_, String>() {
    override fun map(input: GenModel_973_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_973_ : GenMapper_973_<String, GenModel_973_>() {
    override fun map(input: String): GenModel_973_ {
        val parts = input.split(":")
        return GenModel_973_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_973_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_973_,
    private val saveUseCase: GenSaveUseCase_973_,
    private val deleteUseCase: GenDeleteUseCase_973_,
    private val searchUseCase: GenSearchUseCase_973_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_973_>(GenState_973_.Idle)
    val state: StateFlow<GenState_973_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_973_) {
        when (event) {
            is GenEvent_973_.Load -> loadAll()
            is GenEvent_973_.Update -> save(event.model)
            is GenEvent_973_.Delete -> delete(event.id)
            is GenEvent_973_.Refresh -> loadAll()
            is GenEvent_973_.Search -> search(event.query)
            is GenEvent_973_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_973_.Loading; _state.value = GenState_973_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_973_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_973_.Success(searchUseCase(query)) } }
}
