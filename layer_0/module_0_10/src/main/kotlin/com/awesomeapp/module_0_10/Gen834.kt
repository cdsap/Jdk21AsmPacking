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

data class GenModel_834_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_834_ {
    data class Load(val id: Long) : GenEvent_834_()
    data class Update(val model: GenModel_834_) : GenEvent_834_()
    data class Delete(val id: Long) : GenEvent_834_()
    data object Refresh : GenEvent_834_()
    data class Search(val query: String) : GenEvent_834_()
    data class Filter(val predicate: String) : GenEvent_834_()
}

sealed class GenState_834_ {
    data object Idle : GenState_834_()
    data object Loading : GenState_834_()
    data class Success(val items: List<GenModel_834_>) : GenState_834_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_834_()
    data class Partial(val items: List<GenModel_834_>, val hasMore: Boolean) : GenState_834_()
}

interface GenRepository_834_ {
    suspend fun getAll(): List<GenModel_834_>
    suspend fun getById(id: Long): GenModel_834_?
    suspend fun save(model: GenModel_834_): GenModel_834_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_834_>
}

@Singleton
class GenRepositoryImpl_834_ @Inject constructor() : GenRepository_834_ {
    private val store = mutableMapOf<Long, GenModel_834_>()
    override suspend fun getAll(): List<GenModel_834_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_834_? = store[id]
    override suspend fun save(model: GenModel_834_): GenModel_834_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_834_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_834_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_834_ @Inject constructor(
    private val repository: GenRepositoryImpl_834_
) : GenUseCase_834_<Unit, List<GenModel_834_>> {
    override suspend fun invoke(params: Unit): List<GenModel_834_> = repository.getAll()
}

class GenSaveUseCase_834_ @Inject constructor(
    private val repository: GenRepositoryImpl_834_
) : GenUseCase_834_<GenModel_834_, GenModel_834_> {
    override suspend fun invoke(params: GenModel_834_): GenModel_834_ = repository.save(params)
}

class GenDeleteUseCase_834_ @Inject constructor(
    private val repository: GenRepositoryImpl_834_
) : GenUseCase_834_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_834_ @Inject constructor(
    private val repository: GenRepositoryImpl_834_
) : GenUseCase_834_<String, List<GenModel_834_>> {
    override suspend fun invoke(params: String): List<GenModel_834_> = repository.search(params)
}

abstract class GenMapper_834_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_834_ : GenMapper_834_<GenModel_834_, String>() {
    override fun map(input: GenModel_834_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_834_ : GenMapper_834_<String, GenModel_834_>() {
    override fun map(input: String): GenModel_834_ {
        val parts = input.split(":")
        return GenModel_834_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_834_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_834_,
    private val saveUseCase: GenSaveUseCase_834_,
    private val deleteUseCase: GenDeleteUseCase_834_,
    private val searchUseCase: GenSearchUseCase_834_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_834_>(GenState_834_.Idle)
    val state: StateFlow<GenState_834_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_834_) {
        when (event) {
            is GenEvent_834_.Load -> loadAll()
            is GenEvent_834_.Update -> save(event.model)
            is GenEvent_834_.Delete -> delete(event.id)
            is GenEvent_834_.Refresh -> loadAll()
            is GenEvent_834_.Search -> search(event.query)
            is GenEvent_834_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_834_.Loading; _state.value = GenState_834_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_834_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_834_.Success(searchUseCase(query)) } }
}
