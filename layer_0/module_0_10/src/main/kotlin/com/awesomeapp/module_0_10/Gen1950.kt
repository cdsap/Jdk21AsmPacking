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

data class GenModel_1950_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1950_ {
    data class Load(val id: Long) : GenEvent_1950_()
    data class Update(val model: GenModel_1950_) : GenEvent_1950_()
    data class Delete(val id: Long) : GenEvent_1950_()
    data object Refresh : GenEvent_1950_()
    data class Search(val query: String) : GenEvent_1950_()
    data class Filter(val predicate: String) : GenEvent_1950_()
}

sealed class GenState_1950_ {
    data object Idle : GenState_1950_()
    data object Loading : GenState_1950_()
    data class Success(val items: List<GenModel_1950_>) : GenState_1950_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1950_()
    data class Partial(val items: List<GenModel_1950_>, val hasMore: Boolean) : GenState_1950_()
}

interface GenRepository_1950_ {
    suspend fun getAll(): List<GenModel_1950_>
    suspend fun getById(id: Long): GenModel_1950_?
    suspend fun save(model: GenModel_1950_): GenModel_1950_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1950_>
}

@Singleton
class GenRepositoryImpl_1950_ @Inject constructor() : GenRepository_1950_ {
    private val store = mutableMapOf<Long, GenModel_1950_>()
    override suspend fun getAll(): List<GenModel_1950_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1950_? = store[id]
    override suspend fun save(model: GenModel_1950_): GenModel_1950_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1950_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1950_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1950_ @Inject constructor(
    private val repository: GenRepositoryImpl_1950_
) : GenUseCase_1950_<Unit, List<GenModel_1950_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1950_> = repository.getAll()
}

class GenSaveUseCase_1950_ @Inject constructor(
    private val repository: GenRepositoryImpl_1950_
) : GenUseCase_1950_<GenModel_1950_, GenModel_1950_> {
    override suspend fun invoke(params: GenModel_1950_): GenModel_1950_ = repository.save(params)
}

class GenDeleteUseCase_1950_ @Inject constructor(
    private val repository: GenRepositoryImpl_1950_
) : GenUseCase_1950_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1950_ @Inject constructor(
    private val repository: GenRepositoryImpl_1950_
) : GenUseCase_1950_<String, List<GenModel_1950_>> {
    override suspend fun invoke(params: String): List<GenModel_1950_> = repository.search(params)
}

abstract class GenMapper_1950_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1950_ : GenMapper_1950_<GenModel_1950_, String>() {
    override fun map(input: GenModel_1950_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1950_ : GenMapper_1950_<String, GenModel_1950_>() {
    override fun map(input: String): GenModel_1950_ {
        val parts = input.split(":")
        return GenModel_1950_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1950_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1950_,
    private val saveUseCase: GenSaveUseCase_1950_,
    private val deleteUseCase: GenDeleteUseCase_1950_,
    private val searchUseCase: GenSearchUseCase_1950_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1950_>(GenState_1950_.Idle)
    val state: StateFlow<GenState_1950_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1950_) {
        when (event) {
            is GenEvent_1950_.Load -> loadAll()
            is GenEvent_1950_.Update -> save(event.model)
            is GenEvent_1950_.Delete -> delete(event.id)
            is GenEvent_1950_.Refresh -> loadAll()
            is GenEvent_1950_.Search -> search(event.query)
            is GenEvent_1950_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1950_.Loading; _state.value = GenState_1950_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1950_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1950_.Success(searchUseCase(query)) } }
}
