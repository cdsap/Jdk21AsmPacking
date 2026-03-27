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

data class GenModel_3026_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3026_ {
    data class Load(val id: Long) : GenEvent_3026_()
    data class Update(val model: GenModel_3026_) : GenEvent_3026_()
    data class Delete(val id: Long) : GenEvent_3026_()
    data object Refresh : GenEvent_3026_()
    data class Search(val query: String) : GenEvent_3026_()
    data class Filter(val predicate: String) : GenEvent_3026_()
}

sealed class GenState_3026_ {
    data object Idle : GenState_3026_()
    data object Loading : GenState_3026_()
    data class Success(val items: List<GenModel_3026_>) : GenState_3026_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3026_()
    data class Partial(val items: List<GenModel_3026_>, val hasMore: Boolean) : GenState_3026_()
}

interface GenRepository_3026_ {
    suspend fun getAll(): List<GenModel_3026_>
    suspend fun getById(id: Long): GenModel_3026_?
    suspend fun save(model: GenModel_3026_): GenModel_3026_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3026_>
}

@Singleton
class GenRepositoryImpl_3026_ @Inject constructor() : GenRepository_3026_ {
    private val store = mutableMapOf<Long, GenModel_3026_>()
    override suspend fun getAll(): List<GenModel_3026_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3026_? = store[id]
    override suspend fun save(model: GenModel_3026_): GenModel_3026_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3026_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3026_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3026_ @Inject constructor(
    private val repository: GenRepositoryImpl_3026_
) : GenUseCase_3026_<Unit, List<GenModel_3026_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3026_> = repository.getAll()
}

class GenSaveUseCase_3026_ @Inject constructor(
    private val repository: GenRepositoryImpl_3026_
) : GenUseCase_3026_<GenModel_3026_, GenModel_3026_> {
    override suspend fun invoke(params: GenModel_3026_): GenModel_3026_ = repository.save(params)
}

class GenDeleteUseCase_3026_ @Inject constructor(
    private val repository: GenRepositoryImpl_3026_
) : GenUseCase_3026_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3026_ @Inject constructor(
    private val repository: GenRepositoryImpl_3026_
) : GenUseCase_3026_<String, List<GenModel_3026_>> {
    override suspend fun invoke(params: String): List<GenModel_3026_> = repository.search(params)
}

abstract class GenMapper_3026_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3026_ : GenMapper_3026_<GenModel_3026_, String>() {
    override fun map(input: GenModel_3026_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3026_ : GenMapper_3026_<String, GenModel_3026_>() {
    override fun map(input: String): GenModel_3026_ {
        val parts = input.split(":")
        return GenModel_3026_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3026_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3026_,
    private val saveUseCase: GenSaveUseCase_3026_,
    private val deleteUseCase: GenDeleteUseCase_3026_,
    private val searchUseCase: GenSearchUseCase_3026_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3026_>(GenState_3026_.Idle)
    val state: StateFlow<GenState_3026_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3026_) {
        when (event) {
            is GenEvent_3026_.Load -> loadAll()
            is GenEvent_3026_.Update -> save(event.model)
            is GenEvent_3026_.Delete -> delete(event.id)
            is GenEvent_3026_.Refresh -> loadAll()
            is GenEvent_3026_.Search -> search(event.query)
            is GenEvent_3026_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3026_.Loading; _state.value = GenState_3026_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3026_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3026_.Success(searchUseCase(query)) } }
}
