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

data class GenModel_686_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_686_ {
    data class Load(val id: Long) : GenEvent_686_()
    data class Update(val model: GenModel_686_) : GenEvent_686_()
    data class Delete(val id: Long) : GenEvent_686_()
    data object Refresh : GenEvent_686_()
    data class Search(val query: String) : GenEvent_686_()
    data class Filter(val predicate: String) : GenEvent_686_()
}

sealed class GenState_686_ {
    data object Idle : GenState_686_()
    data object Loading : GenState_686_()
    data class Success(val items: List<GenModel_686_>) : GenState_686_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_686_()
    data class Partial(val items: List<GenModel_686_>, val hasMore: Boolean) : GenState_686_()
}

interface GenRepository_686_ {
    suspend fun getAll(): List<GenModel_686_>
    suspend fun getById(id: Long): GenModel_686_?
    suspend fun save(model: GenModel_686_): GenModel_686_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_686_>
}

@Singleton
class GenRepositoryImpl_686_ @Inject constructor() : GenRepository_686_ {
    private val store = mutableMapOf<Long, GenModel_686_>()
    override suspend fun getAll(): List<GenModel_686_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_686_? = store[id]
    override suspend fun save(model: GenModel_686_): GenModel_686_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_686_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_686_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_686_ @Inject constructor(
    private val repository: GenRepositoryImpl_686_
) : GenUseCase_686_<Unit, List<GenModel_686_>> {
    override suspend fun invoke(params: Unit): List<GenModel_686_> = repository.getAll()
}

class GenSaveUseCase_686_ @Inject constructor(
    private val repository: GenRepositoryImpl_686_
) : GenUseCase_686_<GenModel_686_, GenModel_686_> {
    override suspend fun invoke(params: GenModel_686_): GenModel_686_ = repository.save(params)
}

class GenDeleteUseCase_686_ @Inject constructor(
    private val repository: GenRepositoryImpl_686_
) : GenUseCase_686_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_686_ @Inject constructor(
    private val repository: GenRepositoryImpl_686_
) : GenUseCase_686_<String, List<GenModel_686_>> {
    override suspend fun invoke(params: String): List<GenModel_686_> = repository.search(params)
}

abstract class GenMapper_686_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_686_ : GenMapper_686_<GenModel_686_, String>() {
    override fun map(input: GenModel_686_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_686_ : GenMapper_686_<String, GenModel_686_>() {
    override fun map(input: String): GenModel_686_ {
        val parts = input.split(":")
        return GenModel_686_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_686_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_686_,
    private val saveUseCase: GenSaveUseCase_686_,
    private val deleteUseCase: GenDeleteUseCase_686_,
    private val searchUseCase: GenSearchUseCase_686_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_686_>(GenState_686_.Idle)
    val state: StateFlow<GenState_686_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_686_) {
        when (event) {
            is GenEvent_686_.Load -> loadAll()
            is GenEvent_686_.Update -> save(event.model)
            is GenEvent_686_.Delete -> delete(event.id)
            is GenEvent_686_.Refresh -> loadAll()
            is GenEvent_686_.Search -> search(event.query)
            is GenEvent_686_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_686_.Loading; _state.value = GenState_686_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_686_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_686_.Success(searchUseCase(query)) } }
}
