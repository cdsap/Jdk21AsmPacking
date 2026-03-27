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

data class GenModel_682_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_682_ {
    data class Load(val id: Long) : GenEvent_682_()
    data class Update(val model: GenModel_682_) : GenEvent_682_()
    data class Delete(val id: Long) : GenEvent_682_()
    data object Refresh : GenEvent_682_()
    data class Search(val query: String) : GenEvent_682_()
    data class Filter(val predicate: String) : GenEvent_682_()
}

sealed class GenState_682_ {
    data object Idle : GenState_682_()
    data object Loading : GenState_682_()
    data class Success(val items: List<GenModel_682_>) : GenState_682_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_682_()
    data class Partial(val items: List<GenModel_682_>, val hasMore: Boolean) : GenState_682_()
}

interface GenRepository_682_ {
    suspend fun getAll(): List<GenModel_682_>
    suspend fun getById(id: Long): GenModel_682_?
    suspend fun save(model: GenModel_682_): GenModel_682_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_682_>
}

@Singleton
class GenRepositoryImpl_682_ @Inject constructor() : GenRepository_682_ {
    private val store = mutableMapOf<Long, GenModel_682_>()
    override suspend fun getAll(): List<GenModel_682_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_682_? = store[id]
    override suspend fun save(model: GenModel_682_): GenModel_682_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_682_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_682_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_682_ @Inject constructor(
    private val repository: GenRepositoryImpl_682_
) : GenUseCase_682_<Unit, List<GenModel_682_>> {
    override suspend fun invoke(params: Unit): List<GenModel_682_> = repository.getAll()
}

class GenSaveUseCase_682_ @Inject constructor(
    private val repository: GenRepositoryImpl_682_
) : GenUseCase_682_<GenModel_682_, GenModel_682_> {
    override suspend fun invoke(params: GenModel_682_): GenModel_682_ = repository.save(params)
}

class GenDeleteUseCase_682_ @Inject constructor(
    private val repository: GenRepositoryImpl_682_
) : GenUseCase_682_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_682_ @Inject constructor(
    private val repository: GenRepositoryImpl_682_
) : GenUseCase_682_<String, List<GenModel_682_>> {
    override suspend fun invoke(params: String): List<GenModel_682_> = repository.search(params)
}

abstract class GenMapper_682_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_682_ : GenMapper_682_<GenModel_682_, String>() {
    override fun map(input: GenModel_682_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_682_ : GenMapper_682_<String, GenModel_682_>() {
    override fun map(input: String): GenModel_682_ {
        val parts = input.split(":")
        return GenModel_682_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_682_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_682_,
    private val saveUseCase: GenSaveUseCase_682_,
    private val deleteUseCase: GenDeleteUseCase_682_,
    private val searchUseCase: GenSearchUseCase_682_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_682_>(GenState_682_.Idle)
    val state: StateFlow<GenState_682_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_682_) {
        when (event) {
            is GenEvent_682_.Load -> loadAll()
            is GenEvent_682_.Update -> save(event.model)
            is GenEvent_682_.Delete -> delete(event.id)
            is GenEvent_682_.Refresh -> loadAll()
            is GenEvent_682_.Search -> search(event.query)
            is GenEvent_682_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_682_.Loading; _state.value = GenState_682_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_682_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_682_.Success(searchUseCase(query)) } }
}
