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

data class GenModel_36_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_36_ {
    data class Load(val id: Long) : GenEvent_36_()
    data class Update(val model: GenModel_36_) : GenEvent_36_()
    data class Delete(val id: Long) : GenEvent_36_()
    data object Refresh : GenEvent_36_()
    data class Search(val query: String) : GenEvent_36_()
    data class Filter(val predicate: String) : GenEvent_36_()
}

sealed class GenState_36_ {
    data object Idle : GenState_36_()
    data object Loading : GenState_36_()
    data class Success(val items: List<GenModel_36_>) : GenState_36_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_36_()
    data class Partial(val items: List<GenModel_36_>, val hasMore: Boolean) : GenState_36_()
}

interface GenRepository_36_ {
    suspend fun getAll(): List<GenModel_36_>
    suspend fun getById(id: Long): GenModel_36_?
    suspend fun save(model: GenModel_36_): GenModel_36_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_36_>
}

@Singleton
class GenRepositoryImpl_36_ @Inject constructor() : GenRepository_36_ {
    private val store = mutableMapOf<Long, GenModel_36_>()
    override suspend fun getAll(): List<GenModel_36_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_36_? = store[id]
    override suspend fun save(model: GenModel_36_): GenModel_36_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_36_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_36_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_36_ @Inject constructor(
    private val repository: GenRepositoryImpl_36_
) : GenUseCase_36_<Unit, List<GenModel_36_>> {
    override suspend fun invoke(params: Unit): List<GenModel_36_> = repository.getAll()
}

class GenSaveUseCase_36_ @Inject constructor(
    private val repository: GenRepositoryImpl_36_
) : GenUseCase_36_<GenModel_36_, GenModel_36_> {
    override suspend fun invoke(params: GenModel_36_): GenModel_36_ = repository.save(params)
}

class GenDeleteUseCase_36_ @Inject constructor(
    private val repository: GenRepositoryImpl_36_
) : GenUseCase_36_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_36_ @Inject constructor(
    private val repository: GenRepositoryImpl_36_
) : GenUseCase_36_<String, List<GenModel_36_>> {
    override suspend fun invoke(params: String): List<GenModel_36_> = repository.search(params)
}

abstract class GenMapper_36_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_36_ : GenMapper_36_<GenModel_36_, String>() {
    override fun map(input: GenModel_36_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_36_ : GenMapper_36_<String, GenModel_36_>() {
    override fun map(input: String): GenModel_36_ {
        val parts = input.split(":")
        return GenModel_36_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_36_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_36_,
    private val saveUseCase: GenSaveUseCase_36_,
    private val deleteUseCase: GenDeleteUseCase_36_,
    private val searchUseCase: GenSearchUseCase_36_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_36_>(GenState_36_.Idle)
    val state: StateFlow<GenState_36_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_36_) {
        when (event) {
            is GenEvent_36_.Load -> loadAll()
            is GenEvent_36_.Update -> save(event.model)
            is GenEvent_36_.Delete -> delete(event.id)
            is GenEvent_36_.Refresh -> loadAll()
            is GenEvent_36_.Search -> search(event.query)
            is GenEvent_36_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_36_.Loading; _state.value = GenState_36_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_36_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_36_.Success(searchUseCase(query)) } }
}
