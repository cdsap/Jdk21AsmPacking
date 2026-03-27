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

data class GenModel_147_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_147_ {
    data class Load(val id: Long) : GenEvent_147_()
    data class Update(val model: GenModel_147_) : GenEvent_147_()
    data class Delete(val id: Long) : GenEvent_147_()
    data object Refresh : GenEvent_147_()
    data class Search(val query: String) : GenEvent_147_()
    data class Filter(val predicate: String) : GenEvent_147_()
}

sealed class GenState_147_ {
    data object Idle : GenState_147_()
    data object Loading : GenState_147_()
    data class Success(val items: List<GenModel_147_>) : GenState_147_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_147_()
    data class Partial(val items: List<GenModel_147_>, val hasMore: Boolean) : GenState_147_()
}

interface GenRepository_147_ {
    suspend fun getAll(): List<GenModel_147_>
    suspend fun getById(id: Long): GenModel_147_?
    suspend fun save(model: GenModel_147_): GenModel_147_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_147_>
}

@Singleton
class GenRepositoryImpl_147_ @Inject constructor() : GenRepository_147_ {
    private val store = mutableMapOf<Long, GenModel_147_>()
    override suspend fun getAll(): List<GenModel_147_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_147_? = store[id]
    override suspend fun save(model: GenModel_147_): GenModel_147_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_147_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_147_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_147_ @Inject constructor(
    private val repository: GenRepositoryImpl_147_
) : GenUseCase_147_<Unit, List<GenModel_147_>> {
    override suspend fun invoke(params: Unit): List<GenModel_147_> = repository.getAll()
}

class GenSaveUseCase_147_ @Inject constructor(
    private val repository: GenRepositoryImpl_147_
) : GenUseCase_147_<GenModel_147_, GenModel_147_> {
    override suspend fun invoke(params: GenModel_147_): GenModel_147_ = repository.save(params)
}

class GenDeleteUseCase_147_ @Inject constructor(
    private val repository: GenRepositoryImpl_147_
) : GenUseCase_147_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_147_ @Inject constructor(
    private val repository: GenRepositoryImpl_147_
) : GenUseCase_147_<String, List<GenModel_147_>> {
    override suspend fun invoke(params: String): List<GenModel_147_> = repository.search(params)
}

abstract class GenMapper_147_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_147_ : GenMapper_147_<GenModel_147_, String>() {
    override fun map(input: GenModel_147_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_147_ : GenMapper_147_<String, GenModel_147_>() {
    override fun map(input: String): GenModel_147_ {
        val parts = input.split(":")
        return GenModel_147_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_147_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_147_,
    private val saveUseCase: GenSaveUseCase_147_,
    private val deleteUseCase: GenDeleteUseCase_147_,
    private val searchUseCase: GenSearchUseCase_147_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_147_>(GenState_147_.Idle)
    val state: StateFlow<GenState_147_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_147_) {
        when (event) {
            is GenEvent_147_.Load -> loadAll()
            is GenEvent_147_.Update -> save(event.model)
            is GenEvent_147_.Delete -> delete(event.id)
            is GenEvent_147_.Refresh -> loadAll()
            is GenEvent_147_.Search -> search(event.query)
            is GenEvent_147_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_147_.Loading; _state.value = GenState_147_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_147_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_147_.Success(searchUseCase(query)) } }
}
