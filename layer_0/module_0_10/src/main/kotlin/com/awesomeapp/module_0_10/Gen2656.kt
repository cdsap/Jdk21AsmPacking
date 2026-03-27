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

data class GenModel_2656_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2656_ {
    data class Load(val id: Long) : GenEvent_2656_()
    data class Update(val model: GenModel_2656_) : GenEvent_2656_()
    data class Delete(val id: Long) : GenEvent_2656_()
    data object Refresh : GenEvent_2656_()
    data class Search(val query: String) : GenEvent_2656_()
    data class Filter(val predicate: String) : GenEvent_2656_()
}

sealed class GenState_2656_ {
    data object Idle : GenState_2656_()
    data object Loading : GenState_2656_()
    data class Success(val items: List<GenModel_2656_>) : GenState_2656_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2656_()
    data class Partial(val items: List<GenModel_2656_>, val hasMore: Boolean) : GenState_2656_()
}

interface GenRepository_2656_ {
    suspend fun getAll(): List<GenModel_2656_>
    suspend fun getById(id: Long): GenModel_2656_?
    suspend fun save(model: GenModel_2656_): GenModel_2656_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2656_>
}

@Singleton
class GenRepositoryImpl_2656_ @Inject constructor() : GenRepository_2656_ {
    private val store = mutableMapOf<Long, GenModel_2656_>()
    override suspend fun getAll(): List<GenModel_2656_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2656_? = store[id]
    override suspend fun save(model: GenModel_2656_): GenModel_2656_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2656_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2656_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2656_ @Inject constructor(
    private val repository: GenRepositoryImpl_2656_
) : GenUseCase_2656_<Unit, List<GenModel_2656_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2656_> = repository.getAll()
}

class GenSaveUseCase_2656_ @Inject constructor(
    private val repository: GenRepositoryImpl_2656_
) : GenUseCase_2656_<GenModel_2656_, GenModel_2656_> {
    override suspend fun invoke(params: GenModel_2656_): GenModel_2656_ = repository.save(params)
}

class GenDeleteUseCase_2656_ @Inject constructor(
    private val repository: GenRepositoryImpl_2656_
) : GenUseCase_2656_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2656_ @Inject constructor(
    private val repository: GenRepositoryImpl_2656_
) : GenUseCase_2656_<String, List<GenModel_2656_>> {
    override suspend fun invoke(params: String): List<GenModel_2656_> = repository.search(params)
}

abstract class GenMapper_2656_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2656_ : GenMapper_2656_<GenModel_2656_, String>() {
    override fun map(input: GenModel_2656_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2656_ : GenMapper_2656_<String, GenModel_2656_>() {
    override fun map(input: String): GenModel_2656_ {
        val parts = input.split(":")
        return GenModel_2656_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2656_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2656_,
    private val saveUseCase: GenSaveUseCase_2656_,
    private val deleteUseCase: GenDeleteUseCase_2656_,
    private val searchUseCase: GenSearchUseCase_2656_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2656_>(GenState_2656_.Idle)
    val state: StateFlow<GenState_2656_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2656_) {
        when (event) {
            is GenEvent_2656_.Load -> loadAll()
            is GenEvent_2656_.Update -> save(event.model)
            is GenEvent_2656_.Delete -> delete(event.id)
            is GenEvent_2656_.Refresh -> loadAll()
            is GenEvent_2656_.Search -> search(event.query)
            is GenEvent_2656_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2656_.Loading; _state.value = GenState_2656_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2656_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2656_.Success(searchUseCase(query)) } }
}
