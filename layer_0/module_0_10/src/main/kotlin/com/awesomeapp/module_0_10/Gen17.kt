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

data class GenModel_17_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_17_ {
    data class Load(val id: Long) : GenEvent_17_()
    data class Update(val model: GenModel_17_) : GenEvent_17_()
    data class Delete(val id: Long) : GenEvent_17_()
    data object Refresh : GenEvent_17_()
    data class Search(val query: String) : GenEvent_17_()
    data class Filter(val predicate: String) : GenEvent_17_()
}

sealed class GenState_17_ {
    data object Idle : GenState_17_()
    data object Loading : GenState_17_()
    data class Success(val items: List<GenModel_17_>) : GenState_17_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_17_()
    data class Partial(val items: List<GenModel_17_>, val hasMore: Boolean) : GenState_17_()
}

interface GenRepository_17_ {
    suspend fun getAll(): List<GenModel_17_>
    suspend fun getById(id: Long): GenModel_17_?
    suspend fun save(model: GenModel_17_): GenModel_17_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_17_>
}

@Singleton
class GenRepositoryImpl_17_ @Inject constructor() : GenRepository_17_ {
    private val store = mutableMapOf<Long, GenModel_17_>()
    override suspend fun getAll(): List<GenModel_17_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_17_? = store[id]
    override suspend fun save(model: GenModel_17_): GenModel_17_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_17_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_17_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_17_ @Inject constructor(
    private val repository: GenRepositoryImpl_17_
) : GenUseCase_17_<Unit, List<GenModel_17_>> {
    override suspend fun invoke(params: Unit): List<GenModel_17_> = repository.getAll()
}

class GenSaveUseCase_17_ @Inject constructor(
    private val repository: GenRepositoryImpl_17_
) : GenUseCase_17_<GenModel_17_, GenModel_17_> {
    override suspend fun invoke(params: GenModel_17_): GenModel_17_ = repository.save(params)
}

class GenDeleteUseCase_17_ @Inject constructor(
    private val repository: GenRepositoryImpl_17_
) : GenUseCase_17_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_17_ @Inject constructor(
    private val repository: GenRepositoryImpl_17_
) : GenUseCase_17_<String, List<GenModel_17_>> {
    override suspend fun invoke(params: String): List<GenModel_17_> = repository.search(params)
}

abstract class GenMapper_17_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_17_ : GenMapper_17_<GenModel_17_, String>() {
    override fun map(input: GenModel_17_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_17_ : GenMapper_17_<String, GenModel_17_>() {
    override fun map(input: String): GenModel_17_ {
        val parts = input.split(":")
        return GenModel_17_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_17_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_17_,
    private val saveUseCase: GenSaveUseCase_17_,
    private val deleteUseCase: GenDeleteUseCase_17_,
    private val searchUseCase: GenSearchUseCase_17_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_17_>(GenState_17_.Idle)
    val state: StateFlow<GenState_17_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_17_) {
        when (event) {
            is GenEvent_17_.Load -> loadAll()
            is GenEvent_17_.Update -> save(event.model)
            is GenEvent_17_.Delete -> delete(event.id)
            is GenEvent_17_.Refresh -> loadAll()
            is GenEvent_17_.Search -> search(event.query)
            is GenEvent_17_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_17_.Loading; _state.value = GenState_17_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_17_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_17_.Success(searchUseCase(query)) } }
}
