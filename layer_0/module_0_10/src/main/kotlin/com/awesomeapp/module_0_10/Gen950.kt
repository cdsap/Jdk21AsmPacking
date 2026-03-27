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

data class GenModel_950_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_950_ {
    data class Load(val id: Long) : GenEvent_950_()
    data class Update(val model: GenModel_950_) : GenEvent_950_()
    data class Delete(val id: Long) : GenEvent_950_()
    data object Refresh : GenEvent_950_()
    data class Search(val query: String) : GenEvent_950_()
    data class Filter(val predicate: String) : GenEvent_950_()
}

sealed class GenState_950_ {
    data object Idle : GenState_950_()
    data object Loading : GenState_950_()
    data class Success(val items: List<GenModel_950_>) : GenState_950_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_950_()
    data class Partial(val items: List<GenModel_950_>, val hasMore: Boolean) : GenState_950_()
}

interface GenRepository_950_ {
    suspend fun getAll(): List<GenModel_950_>
    suspend fun getById(id: Long): GenModel_950_?
    suspend fun save(model: GenModel_950_): GenModel_950_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_950_>
}

@Singleton
class GenRepositoryImpl_950_ @Inject constructor() : GenRepository_950_ {
    private val store = mutableMapOf<Long, GenModel_950_>()
    override suspend fun getAll(): List<GenModel_950_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_950_? = store[id]
    override suspend fun save(model: GenModel_950_): GenModel_950_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_950_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_950_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_950_ @Inject constructor(
    private val repository: GenRepositoryImpl_950_
) : GenUseCase_950_<Unit, List<GenModel_950_>> {
    override suspend fun invoke(params: Unit): List<GenModel_950_> = repository.getAll()
}

class GenSaveUseCase_950_ @Inject constructor(
    private val repository: GenRepositoryImpl_950_
) : GenUseCase_950_<GenModel_950_, GenModel_950_> {
    override suspend fun invoke(params: GenModel_950_): GenModel_950_ = repository.save(params)
}

class GenDeleteUseCase_950_ @Inject constructor(
    private val repository: GenRepositoryImpl_950_
) : GenUseCase_950_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_950_ @Inject constructor(
    private val repository: GenRepositoryImpl_950_
) : GenUseCase_950_<String, List<GenModel_950_>> {
    override suspend fun invoke(params: String): List<GenModel_950_> = repository.search(params)
}

abstract class GenMapper_950_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_950_ : GenMapper_950_<GenModel_950_, String>() {
    override fun map(input: GenModel_950_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_950_ : GenMapper_950_<String, GenModel_950_>() {
    override fun map(input: String): GenModel_950_ {
        val parts = input.split(":")
        return GenModel_950_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_950_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_950_,
    private val saveUseCase: GenSaveUseCase_950_,
    private val deleteUseCase: GenDeleteUseCase_950_,
    private val searchUseCase: GenSearchUseCase_950_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_950_>(GenState_950_.Idle)
    val state: StateFlow<GenState_950_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_950_) {
        when (event) {
            is GenEvent_950_.Load -> loadAll()
            is GenEvent_950_.Update -> save(event.model)
            is GenEvent_950_.Delete -> delete(event.id)
            is GenEvent_950_.Refresh -> loadAll()
            is GenEvent_950_.Search -> search(event.query)
            is GenEvent_950_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_950_.Loading; _state.value = GenState_950_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_950_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_950_.Success(searchUseCase(query)) } }
}
