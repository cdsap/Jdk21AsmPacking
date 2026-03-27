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

data class GenModel_366_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_366_ {
    data class Load(val id: Long) : GenEvent_366_()
    data class Update(val model: GenModel_366_) : GenEvent_366_()
    data class Delete(val id: Long) : GenEvent_366_()
    data object Refresh : GenEvent_366_()
    data class Search(val query: String) : GenEvent_366_()
    data class Filter(val predicate: String) : GenEvent_366_()
}

sealed class GenState_366_ {
    data object Idle : GenState_366_()
    data object Loading : GenState_366_()
    data class Success(val items: List<GenModel_366_>) : GenState_366_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_366_()
    data class Partial(val items: List<GenModel_366_>, val hasMore: Boolean) : GenState_366_()
}

interface GenRepository_366_ {
    suspend fun getAll(): List<GenModel_366_>
    suspend fun getById(id: Long): GenModel_366_?
    suspend fun save(model: GenModel_366_): GenModel_366_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_366_>
}

@Singleton
class GenRepositoryImpl_366_ @Inject constructor() : GenRepository_366_ {
    private val store = mutableMapOf<Long, GenModel_366_>()
    override suspend fun getAll(): List<GenModel_366_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_366_? = store[id]
    override suspend fun save(model: GenModel_366_): GenModel_366_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_366_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_366_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_366_ @Inject constructor(
    private val repository: GenRepositoryImpl_366_
) : GenUseCase_366_<Unit, List<GenModel_366_>> {
    override suspend fun invoke(params: Unit): List<GenModel_366_> = repository.getAll()
}

class GenSaveUseCase_366_ @Inject constructor(
    private val repository: GenRepositoryImpl_366_
) : GenUseCase_366_<GenModel_366_, GenModel_366_> {
    override suspend fun invoke(params: GenModel_366_): GenModel_366_ = repository.save(params)
}

class GenDeleteUseCase_366_ @Inject constructor(
    private val repository: GenRepositoryImpl_366_
) : GenUseCase_366_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_366_ @Inject constructor(
    private val repository: GenRepositoryImpl_366_
) : GenUseCase_366_<String, List<GenModel_366_>> {
    override suspend fun invoke(params: String): List<GenModel_366_> = repository.search(params)
}

abstract class GenMapper_366_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_366_ : GenMapper_366_<GenModel_366_, String>() {
    override fun map(input: GenModel_366_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_366_ : GenMapper_366_<String, GenModel_366_>() {
    override fun map(input: String): GenModel_366_ {
        val parts = input.split(":")
        return GenModel_366_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_366_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_366_,
    private val saveUseCase: GenSaveUseCase_366_,
    private val deleteUseCase: GenDeleteUseCase_366_,
    private val searchUseCase: GenSearchUseCase_366_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_366_>(GenState_366_.Idle)
    val state: StateFlow<GenState_366_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_366_) {
        when (event) {
            is GenEvent_366_.Load -> loadAll()
            is GenEvent_366_.Update -> save(event.model)
            is GenEvent_366_.Delete -> delete(event.id)
            is GenEvent_366_.Refresh -> loadAll()
            is GenEvent_366_.Search -> search(event.query)
            is GenEvent_366_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_366_.Loading; _state.value = GenState_366_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_366_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_366_.Success(searchUseCase(query)) } }
}
