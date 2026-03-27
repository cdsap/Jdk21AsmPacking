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

data class GenModel_1118_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1118_ {
    data class Load(val id: Long) : GenEvent_1118_()
    data class Update(val model: GenModel_1118_) : GenEvent_1118_()
    data class Delete(val id: Long) : GenEvent_1118_()
    data object Refresh : GenEvent_1118_()
    data class Search(val query: String) : GenEvent_1118_()
    data class Filter(val predicate: String) : GenEvent_1118_()
}

sealed class GenState_1118_ {
    data object Idle : GenState_1118_()
    data object Loading : GenState_1118_()
    data class Success(val items: List<GenModel_1118_>) : GenState_1118_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1118_()
    data class Partial(val items: List<GenModel_1118_>, val hasMore: Boolean) : GenState_1118_()
}

interface GenRepository_1118_ {
    suspend fun getAll(): List<GenModel_1118_>
    suspend fun getById(id: Long): GenModel_1118_?
    suspend fun save(model: GenModel_1118_): GenModel_1118_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1118_>
}

@Singleton
class GenRepositoryImpl_1118_ @Inject constructor() : GenRepository_1118_ {
    private val store = mutableMapOf<Long, GenModel_1118_>()
    override suspend fun getAll(): List<GenModel_1118_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1118_? = store[id]
    override suspend fun save(model: GenModel_1118_): GenModel_1118_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1118_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1118_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1118_ @Inject constructor(
    private val repository: GenRepositoryImpl_1118_
) : GenUseCase_1118_<Unit, List<GenModel_1118_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1118_> = repository.getAll()
}

class GenSaveUseCase_1118_ @Inject constructor(
    private val repository: GenRepositoryImpl_1118_
) : GenUseCase_1118_<GenModel_1118_, GenModel_1118_> {
    override suspend fun invoke(params: GenModel_1118_): GenModel_1118_ = repository.save(params)
}

class GenDeleteUseCase_1118_ @Inject constructor(
    private val repository: GenRepositoryImpl_1118_
) : GenUseCase_1118_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1118_ @Inject constructor(
    private val repository: GenRepositoryImpl_1118_
) : GenUseCase_1118_<String, List<GenModel_1118_>> {
    override suspend fun invoke(params: String): List<GenModel_1118_> = repository.search(params)
}

abstract class GenMapper_1118_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1118_ : GenMapper_1118_<GenModel_1118_, String>() {
    override fun map(input: GenModel_1118_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1118_ : GenMapper_1118_<String, GenModel_1118_>() {
    override fun map(input: String): GenModel_1118_ {
        val parts = input.split(":")
        return GenModel_1118_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1118_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1118_,
    private val saveUseCase: GenSaveUseCase_1118_,
    private val deleteUseCase: GenDeleteUseCase_1118_,
    private val searchUseCase: GenSearchUseCase_1118_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1118_>(GenState_1118_.Idle)
    val state: StateFlow<GenState_1118_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1118_) {
        when (event) {
            is GenEvent_1118_.Load -> loadAll()
            is GenEvent_1118_.Update -> save(event.model)
            is GenEvent_1118_.Delete -> delete(event.id)
            is GenEvent_1118_.Refresh -> loadAll()
            is GenEvent_1118_.Search -> search(event.query)
            is GenEvent_1118_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1118_.Loading; _state.value = GenState_1118_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1118_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1118_.Success(searchUseCase(query)) } }
}
