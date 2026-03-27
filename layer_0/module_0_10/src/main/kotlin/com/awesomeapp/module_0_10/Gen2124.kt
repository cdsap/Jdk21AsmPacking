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

data class GenModel_2124_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2124_ {
    data class Load(val id: Long) : GenEvent_2124_()
    data class Update(val model: GenModel_2124_) : GenEvent_2124_()
    data class Delete(val id: Long) : GenEvent_2124_()
    data object Refresh : GenEvent_2124_()
    data class Search(val query: String) : GenEvent_2124_()
    data class Filter(val predicate: String) : GenEvent_2124_()
}

sealed class GenState_2124_ {
    data object Idle : GenState_2124_()
    data object Loading : GenState_2124_()
    data class Success(val items: List<GenModel_2124_>) : GenState_2124_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2124_()
    data class Partial(val items: List<GenModel_2124_>, val hasMore: Boolean) : GenState_2124_()
}

interface GenRepository_2124_ {
    suspend fun getAll(): List<GenModel_2124_>
    suspend fun getById(id: Long): GenModel_2124_?
    suspend fun save(model: GenModel_2124_): GenModel_2124_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2124_>
}

@Singleton
class GenRepositoryImpl_2124_ @Inject constructor() : GenRepository_2124_ {
    private val store = mutableMapOf<Long, GenModel_2124_>()
    override suspend fun getAll(): List<GenModel_2124_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2124_? = store[id]
    override suspend fun save(model: GenModel_2124_): GenModel_2124_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2124_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2124_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2124_ @Inject constructor(
    private val repository: GenRepositoryImpl_2124_
) : GenUseCase_2124_<Unit, List<GenModel_2124_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2124_> = repository.getAll()
}

class GenSaveUseCase_2124_ @Inject constructor(
    private val repository: GenRepositoryImpl_2124_
) : GenUseCase_2124_<GenModel_2124_, GenModel_2124_> {
    override suspend fun invoke(params: GenModel_2124_): GenModel_2124_ = repository.save(params)
}

class GenDeleteUseCase_2124_ @Inject constructor(
    private val repository: GenRepositoryImpl_2124_
) : GenUseCase_2124_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2124_ @Inject constructor(
    private val repository: GenRepositoryImpl_2124_
) : GenUseCase_2124_<String, List<GenModel_2124_>> {
    override suspend fun invoke(params: String): List<GenModel_2124_> = repository.search(params)
}

abstract class GenMapper_2124_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2124_ : GenMapper_2124_<GenModel_2124_, String>() {
    override fun map(input: GenModel_2124_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2124_ : GenMapper_2124_<String, GenModel_2124_>() {
    override fun map(input: String): GenModel_2124_ {
        val parts = input.split(":")
        return GenModel_2124_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2124_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2124_,
    private val saveUseCase: GenSaveUseCase_2124_,
    private val deleteUseCase: GenDeleteUseCase_2124_,
    private val searchUseCase: GenSearchUseCase_2124_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2124_>(GenState_2124_.Idle)
    val state: StateFlow<GenState_2124_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2124_) {
        when (event) {
            is GenEvent_2124_.Load -> loadAll()
            is GenEvent_2124_.Update -> save(event.model)
            is GenEvent_2124_.Delete -> delete(event.id)
            is GenEvent_2124_.Refresh -> loadAll()
            is GenEvent_2124_.Search -> search(event.query)
            is GenEvent_2124_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2124_.Loading; _state.value = GenState_2124_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2124_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2124_.Success(searchUseCase(query)) } }
}
