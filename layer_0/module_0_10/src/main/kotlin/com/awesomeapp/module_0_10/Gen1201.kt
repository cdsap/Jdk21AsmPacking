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

data class GenModel_1201_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1201_ {
    data class Load(val id: Long) : GenEvent_1201_()
    data class Update(val model: GenModel_1201_) : GenEvent_1201_()
    data class Delete(val id: Long) : GenEvent_1201_()
    data object Refresh : GenEvent_1201_()
    data class Search(val query: String) : GenEvent_1201_()
    data class Filter(val predicate: String) : GenEvent_1201_()
}

sealed class GenState_1201_ {
    data object Idle : GenState_1201_()
    data object Loading : GenState_1201_()
    data class Success(val items: List<GenModel_1201_>) : GenState_1201_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1201_()
    data class Partial(val items: List<GenModel_1201_>, val hasMore: Boolean) : GenState_1201_()
}

interface GenRepository_1201_ {
    suspend fun getAll(): List<GenModel_1201_>
    suspend fun getById(id: Long): GenModel_1201_?
    suspend fun save(model: GenModel_1201_): GenModel_1201_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1201_>
}

@Singleton
class GenRepositoryImpl_1201_ @Inject constructor() : GenRepository_1201_ {
    private val store = mutableMapOf<Long, GenModel_1201_>()
    override suspend fun getAll(): List<GenModel_1201_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1201_? = store[id]
    override suspend fun save(model: GenModel_1201_): GenModel_1201_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1201_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1201_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1201_ @Inject constructor(
    private val repository: GenRepositoryImpl_1201_
) : GenUseCase_1201_<Unit, List<GenModel_1201_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1201_> = repository.getAll()
}

class GenSaveUseCase_1201_ @Inject constructor(
    private val repository: GenRepositoryImpl_1201_
) : GenUseCase_1201_<GenModel_1201_, GenModel_1201_> {
    override suspend fun invoke(params: GenModel_1201_): GenModel_1201_ = repository.save(params)
}

class GenDeleteUseCase_1201_ @Inject constructor(
    private val repository: GenRepositoryImpl_1201_
) : GenUseCase_1201_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1201_ @Inject constructor(
    private val repository: GenRepositoryImpl_1201_
) : GenUseCase_1201_<String, List<GenModel_1201_>> {
    override suspend fun invoke(params: String): List<GenModel_1201_> = repository.search(params)
}

abstract class GenMapper_1201_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1201_ : GenMapper_1201_<GenModel_1201_, String>() {
    override fun map(input: GenModel_1201_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1201_ : GenMapper_1201_<String, GenModel_1201_>() {
    override fun map(input: String): GenModel_1201_ {
        val parts = input.split(":")
        return GenModel_1201_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1201_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1201_,
    private val saveUseCase: GenSaveUseCase_1201_,
    private val deleteUseCase: GenDeleteUseCase_1201_,
    private val searchUseCase: GenSearchUseCase_1201_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1201_>(GenState_1201_.Idle)
    val state: StateFlow<GenState_1201_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1201_) {
        when (event) {
            is GenEvent_1201_.Load -> loadAll()
            is GenEvent_1201_.Update -> save(event.model)
            is GenEvent_1201_.Delete -> delete(event.id)
            is GenEvent_1201_.Refresh -> loadAll()
            is GenEvent_1201_.Search -> search(event.query)
            is GenEvent_1201_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1201_.Loading; _state.value = GenState_1201_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1201_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1201_.Success(searchUseCase(query)) } }
}
