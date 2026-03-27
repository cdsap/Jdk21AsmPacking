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

data class GenModel_979_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_979_ {
    data class Load(val id: Long) : GenEvent_979_()
    data class Update(val model: GenModel_979_) : GenEvent_979_()
    data class Delete(val id: Long) : GenEvent_979_()
    data object Refresh : GenEvent_979_()
    data class Search(val query: String) : GenEvent_979_()
    data class Filter(val predicate: String) : GenEvent_979_()
}

sealed class GenState_979_ {
    data object Idle : GenState_979_()
    data object Loading : GenState_979_()
    data class Success(val items: List<GenModel_979_>) : GenState_979_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_979_()
    data class Partial(val items: List<GenModel_979_>, val hasMore: Boolean) : GenState_979_()
}

interface GenRepository_979_ {
    suspend fun getAll(): List<GenModel_979_>
    suspend fun getById(id: Long): GenModel_979_?
    suspend fun save(model: GenModel_979_): GenModel_979_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_979_>
}

@Singleton
class GenRepositoryImpl_979_ @Inject constructor() : GenRepository_979_ {
    private val store = mutableMapOf<Long, GenModel_979_>()
    override suspend fun getAll(): List<GenModel_979_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_979_? = store[id]
    override suspend fun save(model: GenModel_979_): GenModel_979_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_979_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_979_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_979_ @Inject constructor(
    private val repository: GenRepositoryImpl_979_
) : GenUseCase_979_<Unit, List<GenModel_979_>> {
    override suspend fun invoke(params: Unit): List<GenModel_979_> = repository.getAll()
}

class GenSaveUseCase_979_ @Inject constructor(
    private val repository: GenRepositoryImpl_979_
) : GenUseCase_979_<GenModel_979_, GenModel_979_> {
    override suspend fun invoke(params: GenModel_979_): GenModel_979_ = repository.save(params)
}

class GenDeleteUseCase_979_ @Inject constructor(
    private val repository: GenRepositoryImpl_979_
) : GenUseCase_979_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_979_ @Inject constructor(
    private val repository: GenRepositoryImpl_979_
) : GenUseCase_979_<String, List<GenModel_979_>> {
    override suspend fun invoke(params: String): List<GenModel_979_> = repository.search(params)
}

abstract class GenMapper_979_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_979_ : GenMapper_979_<GenModel_979_, String>() {
    override fun map(input: GenModel_979_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_979_ : GenMapper_979_<String, GenModel_979_>() {
    override fun map(input: String): GenModel_979_ {
        val parts = input.split(":")
        return GenModel_979_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_979_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_979_,
    private val saveUseCase: GenSaveUseCase_979_,
    private val deleteUseCase: GenDeleteUseCase_979_,
    private val searchUseCase: GenSearchUseCase_979_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_979_>(GenState_979_.Idle)
    val state: StateFlow<GenState_979_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_979_) {
        when (event) {
            is GenEvent_979_.Load -> loadAll()
            is GenEvent_979_.Update -> save(event.model)
            is GenEvent_979_.Delete -> delete(event.id)
            is GenEvent_979_.Refresh -> loadAll()
            is GenEvent_979_.Search -> search(event.query)
            is GenEvent_979_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_979_.Loading; _state.value = GenState_979_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_979_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_979_.Success(searchUseCase(query)) } }
}
