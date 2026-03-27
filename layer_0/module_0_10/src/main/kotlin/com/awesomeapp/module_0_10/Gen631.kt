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

data class GenModel_631_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_631_ {
    data class Load(val id: Long) : GenEvent_631_()
    data class Update(val model: GenModel_631_) : GenEvent_631_()
    data class Delete(val id: Long) : GenEvent_631_()
    data object Refresh : GenEvent_631_()
    data class Search(val query: String) : GenEvent_631_()
    data class Filter(val predicate: String) : GenEvent_631_()
}

sealed class GenState_631_ {
    data object Idle : GenState_631_()
    data object Loading : GenState_631_()
    data class Success(val items: List<GenModel_631_>) : GenState_631_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_631_()
    data class Partial(val items: List<GenModel_631_>, val hasMore: Boolean) : GenState_631_()
}

interface GenRepository_631_ {
    suspend fun getAll(): List<GenModel_631_>
    suspend fun getById(id: Long): GenModel_631_?
    suspend fun save(model: GenModel_631_): GenModel_631_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_631_>
}

@Singleton
class GenRepositoryImpl_631_ @Inject constructor() : GenRepository_631_ {
    private val store = mutableMapOf<Long, GenModel_631_>()
    override suspend fun getAll(): List<GenModel_631_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_631_? = store[id]
    override suspend fun save(model: GenModel_631_): GenModel_631_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_631_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_631_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_631_ @Inject constructor(
    private val repository: GenRepositoryImpl_631_
) : GenUseCase_631_<Unit, List<GenModel_631_>> {
    override suspend fun invoke(params: Unit): List<GenModel_631_> = repository.getAll()
}

class GenSaveUseCase_631_ @Inject constructor(
    private val repository: GenRepositoryImpl_631_
) : GenUseCase_631_<GenModel_631_, GenModel_631_> {
    override suspend fun invoke(params: GenModel_631_): GenModel_631_ = repository.save(params)
}

class GenDeleteUseCase_631_ @Inject constructor(
    private val repository: GenRepositoryImpl_631_
) : GenUseCase_631_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_631_ @Inject constructor(
    private val repository: GenRepositoryImpl_631_
) : GenUseCase_631_<String, List<GenModel_631_>> {
    override suspend fun invoke(params: String): List<GenModel_631_> = repository.search(params)
}

abstract class GenMapper_631_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_631_ : GenMapper_631_<GenModel_631_, String>() {
    override fun map(input: GenModel_631_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_631_ : GenMapper_631_<String, GenModel_631_>() {
    override fun map(input: String): GenModel_631_ {
        val parts = input.split(":")
        return GenModel_631_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_631_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_631_,
    private val saveUseCase: GenSaveUseCase_631_,
    private val deleteUseCase: GenDeleteUseCase_631_,
    private val searchUseCase: GenSearchUseCase_631_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_631_>(GenState_631_.Idle)
    val state: StateFlow<GenState_631_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_631_) {
        when (event) {
            is GenEvent_631_.Load -> loadAll()
            is GenEvent_631_.Update -> save(event.model)
            is GenEvent_631_.Delete -> delete(event.id)
            is GenEvent_631_.Refresh -> loadAll()
            is GenEvent_631_.Search -> search(event.query)
            is GenEvent_631_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_631_.Loading; _state.value = GenState_631_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_631_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_631_.Success(searchUseCase(query)) } }
}
