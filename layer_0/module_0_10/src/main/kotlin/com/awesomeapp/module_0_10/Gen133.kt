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

data class GenModel_133_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_133_ {
    data class Load(val id: Long) : GenEvent_133_()
    data class Update(val model: GenModel_133_) : GenEvent_133_()
    data class Delete(val id: Long) : GenEvent_133_()
    data object Refresh : GenEvent_133_()
    data class Search(val query: String) : GenEvent_133_()
    data class Filter(val predicate: String) : GenEvent_133_()
}

sealed class GenState_133_ {
    data object Idle : GenState_133_()
    data object Loading : GenState_133_()
    data class Success(val items: List<GenModel_133_>) : GenState_133_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_133_()
    data class Partial(val items: List<GenModel_133_>, val hasMore: Boolean) : GenState_133_()
}

interface GenRepository_133_ {
    suspend fun getAll(): List<GenModel_133_>
    suspend fun getById(id: Long): GenModel_133_?
    suspend fun save(model: GenModel_133_): GenModel_133_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_133_>
}

@Singleton
class GenRepositoryImpl_133_ @Inject constructor() : GenRepository_133_ {
    private val store = mutableMapOf<Long, GenModel_133_>()
    override suspend fun getAll(): List<GenModel_133_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_133_? = store[id]
    override suspend fun save(model: GenModel_133_): GenModel_133_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_133_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_133_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_133_ @Inject constructor(
    private val repository: GenRepositoryImpl_133_
) : GenUseCase_133_<Unit, List<GenModel_133_>> {
    override suspend fun invoke(params: Unit): List<GenModel_133_> = repository.getAll()
}

class GenSaveUseCase_133_ @Inject constructor(
    private val repository: GenRepositoryImpl_133_
) : GenUseCase_133_<GenModel_133_, GenModel_133_> {
    override suspend fun invoke(params: GenModel_133_): GenModel_133_ = repository.save(params)
}

class GenDeleteUseCase_133_ @Inject constructor(
    private val repository: GenRepositoryImpl_133_
) : GenUseCase_133_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_133_ @Inject constructor(
    private val repository: GenRepositoryImpl_133_
) : GenUseCase_133_<String, List<GenModel_133_>> {
    override suspend fun invoke(params: String): List<GenModel_133_> = repository.search(params)
}

abstract class GenMapper_133_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_133_ : GenMapper_133_<GenModel_133_, String>() {
    override fun map(input: GenModel_133_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_133_ : GenMapper_133_<String, GenModel_133_>() {
    override fun map(input: String): GenModel_133_ {
        val parts = input.split(":")
        return GenModel_133_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_133_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_133_,
    private val saveUseCase: GenSaveUseCase_133_,
    private val deleteUseCase: GenDeleteUseCase_133_,
    private val searchUseCase: GenSearchUseCase_133_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_133_>(GenState_133_.Idle)
    val state: StateFlow<GenState_133_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_133_) {
        when (event) {
            is GenEvent_133_.Load -> loadAll()
            is GenEvent_133_.Update -> save(event.model)
            is GenEvent_133_.Delete -> delete(event.id)
            is GenEvent_133_.Refresh -> loadAll()
            is GenEvent_133_.Search -> search(event.query)
            is GenEvent_133_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_133_.Loading; _state.value = GenState_133_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_133_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_133_.Success(searchUseCase(query)) } }
}
