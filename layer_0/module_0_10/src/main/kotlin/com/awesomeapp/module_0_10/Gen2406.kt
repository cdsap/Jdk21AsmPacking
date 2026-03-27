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

data class GenModel_2406_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2406_ {
    data class Load(val id: Long) : GenEvent_2406_()
    data class Update(val model: GenModel_2406_) : GenEvent_2406_()
    data class Delete(val id: Long) : GenEvent_2406_()
    data object Refresh : GenEvent_2406_()
    data class Search(val query: String) : GenEvent_2406_()
    data class Filter(val predicate: String) : GenEvent_2406_()
}

sealed class GenState_2406_ {
    data object Idle : GenState_2406_()
    data object Loading : GenState_2406_()
    data class Success(val items: List<GenModel_2406_>) : GenState_2406_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2406_()
    data class Partial(val items: List<GenModel_2406_>, val hasMore: Boolean) : GenState_2406_()
}

interface GenRepository_2406_ {
    suspend fun getAll(): List<GenModel_2406_>
    suspend fun getById(id: Long): GenModel_2406_?
    suspend fun save(model: GenModel_2406_): GenModel_2406_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2406_>
}

@Singleton
class GenRepositoryImpl_2406_ @Inject constructor() : GenRepository_2406_ {
    private val store = mutableMapOf<Long, GenModel_2406_>()
    override suspend fun getAll(): List<GenModel_2406_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2406_? = store[id]
    override suspend fun save(model: GenModel_2406_): GenModel_2406_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2406_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2406_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2406_ @Inject constructor(
    private val repository: GenRepositoryImpl_2406_
) : GenUseCase_2406_<Unit, List<GenModel_2406_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2406_> = repository.getAll()
}

class GenSaveUseCase_2406_ @Inject constructor(
    private val repository: GenRepositoryImpl_2406_
) : GenUseCase_2406_<GenModel_2406_, GenModel_2406_> {
    override suspend fun invoke(params: GenModel_2406_): GenModel_2406_ = repository.save(params)
}

class GenDeleteUseCase_2406_ @Inject constructor(
    private val repository: GenRepositoryImpl_2406_
) : GenUseCase_2406_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2406_ @Inject constructor(
    private val repository: GenRepositoryImpl_2406_
) : GenUseCase_2406_<String, List<GenModel_2406_>> {
    override suspend fun invoke(params: String): List<GenModel_2406_> = repository.search(params)
}

abstract class GenMapper_2406_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2406_ : GenMapper_2406_<GenModel_2406_, String>() {
    override fun map(input: GenModel_2406_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2406_ : GenMapper_2406_<String, GenModel_2406_>() {
    override fun map(input: String): GenModel_2406_ {
        val parts = input.split(":")
        return GenModel_2406_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2406_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2406_,
    private val saveUseCase: GenSaveUseCase_2406_,
    private val deleteUseCase: GenDeleteUseCase_2406_,
    private val searchUseCase: GenSearchUseCase_2406_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2406_>(GenState_2406_.Idle)
    val state: StateFlow<GenState_2406_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2406_) {
        when (event) {
            is GenEvent_2406_.Load -> loadAll()
            is GenEvent_2406_.Update -> save(event.model)
            is GenEvent_2406_.Delete -> delete(event.id)
            is GenEvent_2406_.Refresh -> loadAll()
            is GenEvent_2406_.Search -> search(event.query)
            is GenEvent_2406_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2406_.Loading; _state.value = GenState_2406_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2406_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2406_.Success(searchUseCase(query)) } }
}
