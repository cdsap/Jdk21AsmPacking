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

data class GenModel_201_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_201_ {
    data class Load(val id: Long) : GenEvent_201_()
    data class Update(val model: GenModel_201_) : GenEvent_201_()
    data class Delete(val id: Long) : GenEvent_201_()
    data object Refresh : GenEvent_201_()
    data class Search(val query: String) : GenEvent_201_()
    data class Filter(val predicate: String) : GenEvent_201_()
}

sealed class GenState_201_ {
    data object Idle : GenState_201_()
    data object Loading : GenState_201_()
    data class Success(val items: List<GenModel_201_>) : GenState_201_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_201_()
    data class Partial(val items: List<GenModel_201_>, val hasMore: Boolean) : GenState_201_()
}

interface GenRepository_201_ {
    suspend fun getAll(): List<GenModel_201_>
    suspend fun getById(id: Long): GenModel_201_?
    suspend fun save(model: GenModel_201_): GenModel_201_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_201_>
}

@Singleton
class GenRepositoryImpl_201_ @Inject constructor() : GenRepository_201_ {
    private val store = mutableMapOf<Long, GenModel_201_>()
    override suspend fun getAll(): List<GenModel_201_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_201_? = store[id]
    override suspend fun save(model: GenModel_201_): GenModel_201_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_201_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_201_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_201_ @Inject constructor(
    private val repository: GenRepositoryImpl_201_
) : GenUseCase_201_<Unit, List<GenModel_201_>> {
    override suspend fun invoke(params: Unit): List<GenModel_201_> = repository.getAll()
}

class GenSaveUseCase_201_ @Inject constructor(
    private val repository: GenRepositoryImpl_201_
) : GenUseCase_201_<GenModel_201_, GenModel_201_> {
    override suspend fun invoke(params: GenModel_201_): GenModel_201_ = repository.save(params)
}

class GenDeleteUseCase_201_ @Inject constructor(
    private val repository: GenRepositoryImpl_201_
) : GenUseCase_201_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_201_ @Inject constructor(
    private val repository: GenRepositoryImpl_201_
) : GenUseCase_201_<String, List<GenModel_201_>> {
    override suspend fun invoke(params: String): List<GenModel_201_> = repository.search(params)
}

abstract class GenMapper_201_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_201_ : GenMapper_201_<GenModel_201_, String>() {
    override fun map(input: GenModel_201_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_201_ : GenMapper_201_<String, GenModel_201_>() {
    override fun map(input: String): GenModel_201_ {
        val parts = input.split(":")
        return GenModel_201_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_201_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_201_,
    private val saveUseCase: GenSaveUseCase_201_,
    private val deleteUseCase: GenDeleteUseCase_201_,
    private val searchUseCase: GenSearchUseCase_201_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_201_>(GenState_201_.Idle)
    val state: StateFlow<GenState_201_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_201_) {
        when (event) {
            is GenEvent_201_.Load -> loadAll()
            is GenEvent_201_.Update -> save(event.model)
            is GenEvent_201_.Delete -> delete(event.id)
            is GenEvent_201_.Refresh -> loadAll()
            is GenEvent_201_.Search -> search(event.query)
            is GenEvent_201_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_201_.Loading; _state.value = GenState_201_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_201_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_201_.Success(searchUseCase(query)) } }
}
