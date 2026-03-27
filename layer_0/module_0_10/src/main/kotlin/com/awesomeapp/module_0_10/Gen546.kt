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

data class GenModel_546_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_546_ {
    data class Load(val id: Long) : GenEvent_546_()
    data class Update(val model: GenModel_546_) : GenEvent_546_()
    data class Delete(val id: Long) : GenEvent_546_()
    data object Refresh : GenEvent_546_()
    data class Search(val query: String) : GenEvent_546_()
    data class Filter(val predicate: String) : GenEvent_546_()
}

sealed class GenState_546_ {
    data object Idle : GenState_546_()
    data object Loading : GenState_546_()
    data class Success(val items: List<GenModel_546_>) : GenState_546_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_546_()
    data class Partial(val items: List<GenModel_546_>, val hasMore: Boolean) : GenState_546_()
}

interface GenRepository_546_ {
    suspend fun getAll(): List<GenModel_546_>
    suspend fun getById(id: Long): GenModel_546_?
    suspend fun save(model: GenModel_546_): GenModel_546_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_546_>
}

@Singleton
class GenRepositoryImpl_546_ @Inject constructor() : GenRepository_546_ {
    private val store = mutableMapOf<Long, GenModel_546_>()
    override suspend fun getAll(): List<GenModel_546_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_546_? = store[id]
    override suspend fun save(model: GenModel_546_): GenModel_546_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_546_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_546_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_546_ @Inject constructor(
    private val repository: GenRepositoryImpl_546_
) : GenUseCase_546_<Unit, List<GenModel_546_>> {
    override suspend fun invoke(params: Unit): List<GenModel_546_> = repository.getAll()
}

class GenSaveUseCase_546_ @Inject constructor(
    private val repository: GenRepositoryImpl_546_
) : GenUseCase_546_<GenModel_546_, GenModel_546_> {
    override suspend fun invoke(params: GenModel_546_): GenModel_546_ = repository.save(params)
}

class GenDeleteUseCase_546_ @Inject constructor(
    private val repository: GenRepositoryImpl_546_
) : GenUseCase_546_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_546_ @Inject constructor(
    private val repository: GenRepositoryImpl_546_
) : GenUseCase_546_<String, List<GenModel_546_>> {
    override suspend fun invoke(params: String): List<GenModel_546_> = repository.search(params)
}

abstract class GenMapper_546_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_546_ : GenMapper_546_<GenModel_546_, String>() {
    override fun map(input: GenModel_546_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_546_ : GenMapper_546_<String, GenModel_546_>() {
    override fun map(input: String): GenModel_546_ {
        val parts = input.split(":")
        return GenModel_546_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_546_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_546_,
    private val saveUseCase: GenSaveUseCase_546_,
    private val deleteUseCase: GenDeleteUseCase_546_,
    private val searchUseCase: GenSearchUseCase_546_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_546_>(GenState_546_.Idle)
    val state: StateFlow<GenState_546_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_546_) {
        when (event) {
            is GenEvent_546_.Load -> loadAll()
            is GenEvent_546_.Update -> save(event.model)
            is GenEvent_546_.Delete -> delete(event.id)
            is GenEvent_546_.Refresh -> loadAll()
            is GenEvent_546_.Search -> search(event.query)
            is GenEvent_546_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_546_.Loading; _state.value = GenState_546_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_546_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_546_.Success(searchUseCase(query)) } }
}
