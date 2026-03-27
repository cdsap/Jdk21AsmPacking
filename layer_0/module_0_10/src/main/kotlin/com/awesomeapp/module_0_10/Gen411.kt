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

data class GenModel_411_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_411_ {
    data class Load(val id: Long) : GenEvent_411_()
    data class Update(val model: GenModel_411_) : GenEvent_411_()
    data class Delete(val id: Long) : GenEvent_411_()
    data object Refresh : GenEvent_411_()
    data class Search(val query: String) : GenEvent_411_()
    data class Filter(val predicate: String) : GenEvent_411_()
}

sealed class GenState_411_ {
    data object Idle : GenState_411_()
    data object Loading : GenState_411_()
    data class Success(val items: List<GenModel_411_>) : GenState_411_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_411_()
    data class Partial(val items: List<GenModel_411_>, val hasMore: Boolean) : GenState_411_()
}

interface GenRepository_411_ {
    suspend fun getAll(): List<GenModel_411_>
    suspend fun getById(id: Long): GenModel_411_?
    suspend fun save(model: GenModel_411_): GenModel_411_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_411_>
}

@Singleton
class GenRepositoryImpl_411_ @Inject constructor() : GenRepository_411_ {
    private val store = mutableMapOf<Long, GenModel_411_>()
    override suspend fun getAll(): List<GenModel_411_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_411_? = store[id]
    override suspend fun save(model: GenModel_411_): GenModel_411_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_411_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_411_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_411_ @Inject constructor(
    private val repository: GenRepositoryImpl_411_
) : GenUseCase_411_<Unit, List<GenModel_411_>> {
    override suspend fun invoke(params: Unit): List<GenModel_411_> = repository.getAll()
}

class GenSaveUseCase_411_ @Inject constructor(
    private val repository: GenRepositoryImpl_411_
) : GenUseCase_411_<GenModel_411_, GenModel_411_> {
    override suspend fun invoke(params: GenModel_411_): GenModel_411_ = repository.save(params)
}

class GenDeleteUseCase_411_ @Inject constructor(
    private val repository: GenRepositoryImpl_411_
) : GenUseCase_411_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_411_ @Inject constructor(
    private val repository: GenRepositoryImpl_411_
) : GenUseCase_411_<String, List<GenModel_411_>> {
    override suspend fun invoke(params: String): List<GenModel_411_> = repository.search(params)
}

abstract class GenMapper_411_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_411_ : GenMapper_411_<GenModel_411_, String>() {
    override fun map(input: GenModel_411_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_411_ : GenMapper_411_<String, GenModel_411_>() {
    override fun map(input: String): GenModel_411_ {
        val parts = input.split(":")
        return GenModel_411_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_411_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_411_,
    private val saveUseCase: GenSaveUseCase_411_,
    private val deleteUseCase: GenDeleteUseCase_411_,
    private val searchUseCase: GenSearchUseCase_411_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_411_>(GenState_411_.Idle)
    val state: StateFlow<GenState_411_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_411_) {
        when (event) {
            is GenEvent_411_.Load -> loadAll()
            is GenEvent_411_.Update -> save(event.model)
            is GenEvent_411_.Delete -> delete(event.id)
            is GenEvent_411_.Refresh -> loadAll()
            is GenEvent_411_.Search -> search(event.query)
            is GenEvent_411_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_411_.Loading; _state.value = GenState_411_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_411_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_411_.Success(searchUseCase(query)) } }
}
