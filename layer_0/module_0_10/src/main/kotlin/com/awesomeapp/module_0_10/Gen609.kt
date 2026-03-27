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

data class GenModel_609_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_609_ {
    data class Load(val id: Long) : GenEvent_609_()
    data class Update(val model: GenModel_609_) : GenEvent_609_()
    data class Delete(val id: Long) : GenEvent_609_()
    data object Refresh : GenEvent_609_()
    data class Search(val query: String) : GenEvent_609_()
    data class Filter(val predicate: String) : GenEvent_609_()
}

sealed class GenState_609_ {
    data object Idle : GenState_609_()
    data object Loading : GenState_609_()
    data class Success(val items: List<GenModel_609_>) : GenState_609_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_609_()
    data class Partial(val items: List<GenModel_609_>, val hasMore: Boolean) : GenState_609_()
}

interface GenRepository_609_ {
    suspend fun getAll(): List<GenModel_609_>
    suspend fun getById(id: Long): GenModel_609_?
    suspend fun save(model: GenModel_609_): GenModel_609_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_609_>
}

@Singleton
class GenRepositoryImpl_609_ @Inject constructor() : GenRepository_609_ {
    private val store = mutableMapOf<Long, GenModel_609_>()
    override suspend fun getAll(): List<GenModel_609_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_609_? = store[id]
    override suspend fun save(model: GenModel_609_): GenModel_609_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_609_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_609_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_609_ @Inject constructor(
    private val repository: GenRepositoryImpl_609_
) : GenUseCase_609_<Unit, List<GenModel_609_>> {
    override suspend fun invoke(params: Unit): List<GenModel_609_> = repository.getAll()
}

class GenSaveUseCase_609_ @Inject constructor(
    private val repository: GenRepositoryImpl_609_
) : GenUseCase_609_<GenModel_609_, GenModel_609_> {
    override suspend fun invoke(params: GenModel_609_): GenModel_609_ = repository.save(params)
}

class GenDeleteUseCase_609_ @Inject constructor(
    private val repository: GenRepositoryImpl_609_
) : GenUseCase_609_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_609_ @Inject constructor(
    private val repository: GenRepositoryImpl_609_
) : GenUseCase_609_<String, List<GenModel_609_>> {
    override suspend fun invoke(params: String): List<GenModel_609_> = repository.search(params)
}

abstract class GenMapper_609_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_609_ : GenMapper_609_<GenModel_609_, String>() {
    override fun map(input: GenModel_609_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_609_ : GenMapper_609_<String, GenModel_609_>() {
    override fun map(input: String): GenModel_609_ {
        val parts = input.split(":")
        return GenModel_609_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_609_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_609_,
    private val saveUseCase: GenSaveUseCase_609_,
    private val deleteUseCase: GenDeleteUseCase_609_,
    private val searchUseCase: GenSearchUseCase_609_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_609_>(GenState_609_.Idle)
    val state: StateFlow<GenState_609_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_609_) {
        when (event) {
            is GenEvent_609_.Load -> loadAll()
            is GenEvent_609_.Update -> save(event.model)
            is GenEvent_609_.Delete -> delete(event.id)
            is GenEvent_609_.Refresh -> loadAll()
            is GenEvent_609_.Search -> search(event.query)
            is GenEvent_609_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_609_.Loading; _state.value = GenState_609_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_609_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_609_.Success(searchUseCase(query)) } }
}
