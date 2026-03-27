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

data class GenModel_801_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_801_ {
    data class Load(val id: Long) : GenEvent_801_()
    data class Update(val model: GenModel_801_) : GenEvent_801_()
    data class Delete(val id: Long) : GenEvent_801_()
    data object Refresh : GenEvent_801_()
    data class Search(val query: String) : GenEvent_801_()
    data class Filter(val predicate: String) : GenEvent_801_()
}

sealed class GenState_801_ {
    data object Idle : GenState_801_()
    data object Loading : GenState_801_()
    data class Success(val items: List<GenModel_801_>) : GenState_801_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_801_()
    data class Partial(val items: List<GenModel_801_>, val hasMore: Boolean) : GenState_801_()
}

interface GenRepository_801_ {
    suspend fun getAll(): List<GenModel_801_>
    suspend fun getById(id: Long): GenModel_801_?
    suspend fun save(model: GenModel_801_): GenModel_801_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_801_>
}

@Singleton
class GenRepositoryImpl_801_ @Inject constructor() : GenRepository_801_ {
    private val store = mutableMapOf<Long, GenModel_801_>()
    override suspend fun getAll(): List<GenModel_801_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_801_? = store[id]
    override suspend fun save(model: GenModel_801_): GenModel_801_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_801_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_801_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_801_ @Inject constructor(
    private val repository: GenRepositoryImpl_801_
) : GenUseCase_801_<Unit, List<GenModel_801_>> {
    override suspend fun invoke(params: Unit): List<GenModel_801_> = repository.getAll()
}

class GenSaveUseCase_801_ @Inject constructor(
    private val repository: GenRepositoryImpl_801_
) : GenUseCase_801_<GenModel_801_, GenModel_801_> {
    override suspend fun invoke(params: GenModel_801_): GenModel_801_ = repository.save(params)
}

class GenDeleteUseCase_801_ @Inject constructor(
    private val repository: GenRepositoryImpl_801_
) : GenUseCase_801_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_801_ @Inject constructor(
    private val repository: GenRepositoryImpl_801_
) : GenUseCase_801_<String, List<GenModel_801_>> {
    override suspend fun invoke(params: String): List<GenModel_801_> = repository.search(params)
}

abstract class GenMapper_801_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_801_ : GenMapper_801_<GenModel_801_, String>() {
    override fun map(input: GenModel_801_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_801_ : GenMapper_801_<String, GenModel_801_>() {
    override fun map(input: String): GenModel_801_ {
        val parts = input.split(":")
        return GenModel_801_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_801_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_801_,
    private val saveUseCase: GenSaveUseCase_801_,
    private val deleteUseCase: GenDeleteUseCase_801_,
    private val searchUseCase: GenSearchUseCase_801_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_801_>(GenState_801_.Idle)
    val state: StateFlow<GenState_801_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_801_) {
        when (event) {
            is GenEvent_801_.Load -> loadAll()
            is GenEvent_801_.Update -> save(event.model)
            is GenEvent_801_.Delete -> delete(event.id)
            is GenEvent_801_.Refresh -> loadAll()
            is GenEvent_801_.Search -> search(event.query)
            is GenEvent_801_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_801_.Loading; _state.value = GenState_801_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_801_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_801_.Success(searchUseCase(query)) } }
}
