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

data class GenModel_2503_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2503_ {
    data class Load(val id: Long) : GenEvent_2503_()
    data class Update(val model: GenModel_2503_) : GenEvent_2503_()
    data class Delete(val id: Long) : GenEvent_2503_()
    data object Refresh : GenEvent_2503_()
    data class Search(val query: String) : GenEvent_2503_()
    data class Filter(val predicate: String) : GenEvent_2503_()
}

sealed class GenState_2503_ {
    data object Idle : GenState_2503_()
    data object Loading : GenState_2503_()
    data class Success(val items: List<GenModel_2503_>) : GenState_2503_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2503_()
    data class Partial(val items: List<GenModel_2503_>, val hasMore: Boolean) : GenState_2503_()
}

interface GenRepository_2503_ {
    suspend fun getAll(): List<GenModel_2503_>
    suspend fun getById(id: Long): GenModel_2503_?
    suspend fun save(model: GenModel_2503_): GenModel_2503_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2503_>
}

@Singleton
class GenRepositoryImpl_2503_ @Inject constructor() : GenRepository_2503_ {
    private val store = mutableMapOf<Long, GenModel_2503_>()
    override suspend fun getAll(): List<GenModel_2503_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2503_? = store[id]
    override suspend fun save(model: GenModel_2503_): GenModel_2503_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2503_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2503_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2503_ @Inject constructor(
    private val repository: GenRepositoryImpl_2503_
) : GenUseCase_2503_<Unit, List<GenModel_2503_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2503_> = repository.getAll()
}

class GenSaveUseCase_2503_ @Inject constructor(
    private val repository: GenRepositoryImpl_2503_
) : GenUseCase_2503_<GenModel_2503_, GenModel_2503_> {
    override suspend fun invoke(params: GenModel_2503_): GenModel_2503_ = repository.save(params)
}

class GenDeleteUseCase_2503_ @Inject constructor(
    private val repository: GenRepositoryImpl_2503_
) : GenUseCase_2503_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2503_ @Inject constructor(
    private val repository: GenRepositoryImpl_2503_
) : GenUseCase_2503_<String, List<GenModel_2503_>> {
    override suspend fun invoke(params: String): List<GenModel_2503_> = repository.search(params)
}

abstract class GenMapper_2503_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2503_ : GenMapper_2503_<GenModel_2503_, String>() {
    override fun map(input: GenModel_2503_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2503_ : GenMapper_2503_<String, GenModel_2503_>() {
    override fun map(input: String): GenModel_2503_ {
        val parts = input.split(":")
        return GenModel_2503_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2503_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2503_,
    private val saveUseCase: GenSaveUseCase_2503_,
    private val deleteUseCase: GenDeleteUseCase_2503_,
    private val searchUseCase: GenSearchUseCase_2503_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2503_>(GenState_2503_.Idle)
    val state: StateFlow<GenState_2503_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2503_) {
        when (event) {
            is GenEvent_2503_.Load -> loadAll()
            is GenEvent_2503_.Update -> save(event.model)
            is GenEvent_2503_.Delete -> delete(event.id)
            is GenEvent_2503_.Refresh -> loadAll()
            is GenEvent_2503_.Search -> search(event.query)
            is GenEvent_2503_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2503_.Loading; _state.value = GenState_2503_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2503_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2503_.Success(searchUseCase(query)) } }
}
