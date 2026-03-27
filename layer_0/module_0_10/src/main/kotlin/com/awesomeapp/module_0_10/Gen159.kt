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

data class GenModel_159_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_159_ {
    data class Load(val id: Long) : GenEvent_159_()
    data class Update(val model: GenModel_159_) : GenEvent_159_()
    data class Delete(val id: Long) : GenEvent_159_()
    data object Refresh : GenEvent_159_()
    data class Search(val query: String) : GenEvent_159_()
    data class Filter(val predicate: String) : GenEvent_159_()
}

sealed class GenState_159_ {
    data object Idle : GenState_159_()
    data object Loading : GenState_159_()
    data class Success(val items: List<GenModel_159_>) : GenState_159_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_159_()
    data class Partial(val items: List<GenModel_159_>, val hasMore: Boolean) : GenState_159_()
}

interface GenRepository_159_ {
    suspend fun getAll(): List<GenModel_159_>
    suspend fun getById(id: Long): GenModel_159_?
    suspend fun save(model: GenModel_159_): GenModel_159_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_159_>
}

@Singleton
class GenRepositoryImpl_159_ @Inject constructor() : GenRepository_159_ {
    private val store = mutableMapOf<Long, GenModel_159_>()
    override suspend fun getAll(): List<GenModel_159_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_159_? = store[id]
    override suspend fun save(model: GenModel_159_): GenModel_159_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_159_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_159_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_159_ @Inject constructor(
    private val repository: GenRepositoryImpl_159_
) : GenUseCase_159_<Unit, List<GenModel_159_>> {
    override suspend fun invoke(params: Unit): List<GenModel_159_> = repository.getAll()
}

class GenSaveUseCase_159_ @Inject constructor(
    private val repository: GenRepositoryImpl_159_
) : GenUseCase_159_<GenModel_159_, GenModel_159_> {
    override suspend fun invoke(params: GenModel_159_): GenModel_159_ = repository.save(params)
}

class GenDeleteUseCase_159_ @Inject constructor(
    private val repository: GenRepositoryImpl_159_
) : GenUseCase_159_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_159_ @Inject constructor(
    private val repository: GenRepositoryImpl_159_
) : GenUseCase_159_<String, List<GenModel_159_>> {
    override suspend fun invoke(params: String): List<GenModel_159_> = repository.search(params)
}

abstract class GenMapper_159_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_159_ : GenMapper_159_<GenModel_159_, String>() {
    override fun map(input: GenModel_159_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_159_ : GenMapper_159_<String, GenModel_159_>() {
    override fun map(input: String): GenModel_159_ {
        val parts = input.split(":")
        return GenModel_159_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_159_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_159_,
    private val saveUseCase: GenSaveUseCase_159_,
    private val deleteUseCase: GenDeleteUseCase_159_,
    private val searchUseCase: GenSearchUseCase_159_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_159_>(GenState_159_.Idle)
    val state: StateFlow<GenState_159_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_159_) {
        when (event) {
            is GenEvent_159_.Load -> loadAll()
            is GenEvent_159_.Update -> save(event.model)
            is GenEvent_159_.Delete -> delete(event.id)
            is GenEvent_159_.Refresh -> loadAll()
            is GenEvent_159_.Search -> search(event.query)
            is GenEvent_159_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_159_.Loading; _state.value = GenState_159_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_159_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_159_.Success(searchUseCase(query)) } }
}
