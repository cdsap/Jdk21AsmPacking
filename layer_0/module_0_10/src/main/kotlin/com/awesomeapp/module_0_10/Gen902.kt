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

data class GenModel_902_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_902_ {
    data class Load(val id: Long) : GenEvent_902_()
    data class Update(val model: GenModel_902_) : GenEvent_902_()
    data class Delete(val id: Long) : GenEvent_902_()
    data object Refresh : GenEvent_902_()
    data class Search(val query: String) : GenEvent_902_()
    data class Filter(val predicate: String) : GenEvent_902_()
}

sealed class GenState_902_ {
    data object Idle : GenState_902_()
    data object Loading : GenState_902_()
    data class Success(val items: List<GenModel_902_>) : GenState_902_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_902_()
    data class Partial(val items: List<GenModel_902_>, val hasMore: Boolean) : GenState_902_()
}

interface GenRepository_902_ {
    suspend fun getAll(): List<GenModel_902_>
    suspend fun getById(id: Long): GenModel_902_?
    suspend fun save(model: GenModel_902_): GenModel_902_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_902_>
}

@Singleton
class GenRepositoryImpl_902_ @Inject constructor() : GenRepository_902_ {
    private val store = mutableMapOf<Long, GenModel_902_>()
    override suspend fun getAll(): List<GenModel_902_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_902_? = store[id]
    override suspend fun save(model: GenModel_902_): GenModel_902_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_902_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_902_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_902_ @Inject constructor(
    private val repository: GenRepositoryImpl_902_
) : GenUseCase_902_<Unit, List<GenModel_902_>> {
    override suspend fun invoke(params: Unit): List<GenModel_902_> = repository.getAll()
}

class GenSaveUseCase_902_ @Inject constructor(
    private val repository: GenRepositoryImpl_902_
) : GenUseCase_902_<GenModel_902_, GenModel_902_> {
    override suspend fun invoke(params: GenModel_902_): GenModel_902_ = repository.save(params)
}

class GenDeleteUseCase_902_ @Inject constructor(
    private val repository: GenRepositoryImpl_902_
) : GenUseCase_902_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_902_ @Inject constructor(
    private val repository: GenRepositoryImpl_902_
) : GenUseCase_902_<String, List<GenModel_902_>> {
    override suspend fun invoke(params: String): List<GenModel_902_> = repository.search(params)
}

abstract class GenMapper_902_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_902_ : GenMapper_902_<GenModel_902_, String>() {
    override fun map(input: GenModel_902_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_902_ : GenMapper_902_<String, GenModel_902_>() {
    override fun map(input: String): GenModel_902_ {
        val parts = input.split(":")
        return GenModel_902_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_902_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_902_,
    private val saveUseCase: GenSaveUseCase_902_,
    private val deleteUseCase: GenDeleteUseCase_902_,
    private val searchUseCase: GenSearchUseCase_902_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_902_>(GenState_902_.Idle)
    val state: StateFlow<GenState_902_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_902_) {
        when (event) {
            is GenEvent_902_.Load -> loadAll()
            is GenEvent_902_.Update -> save(event.model)
            is GenEvent_902_.Delete -> delete(event.id)
            is GenEvent_902_.Refresh -> loadAll()
            is GenEvent_902_.Search -> search(event.query)
            is GenEvent_902_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_902_.Loading; _state.value = GenState_902_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_902_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_902_.Success(searchUseCase(query)) } }
}
