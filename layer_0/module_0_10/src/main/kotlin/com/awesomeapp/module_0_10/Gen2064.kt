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

data class GenModel_2064_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2064_ {
    data class Load(val id: Long) : GenEvent_2064_()
    data class Update(val model: GenModel_2064_) : GenEvent_2064_()
    data class Delete(val id: Long) : GenEvent_2064_()
    data object Refresh : GenEvent_2064_()
    data class Search(val query: String) : GenEvent_2064_()
    data class Filter(val predicate: String) : GenEvent_2064_()
}

sealed class GenState_2064_ {
    data object Idle : GenState_2064_()
    data object Loading : GenState_2064_()
    data class Success(val items: List<GenModel_2064_>) : GenState_2064_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2064_()
    data class Partial(val items: List<GenModel_2064_>, val hasMore: Boolean) : GenState_2064_()
}

interface GenRepository_2064_ {
    suspend fun getAll(): List<GenModel_2064_>
    suspend fun getById(id: Long): GenModel_2064_?
    suspend fun save(model: GenModel_2064_): GenModel_2064_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2064_>
}

@Singleton
class GenRepositoryImpl_2064_ @Inject constructor() : GenRepository_2064_ {
    private val store = mutableMapOf<Long, GenModel_2064_>()
    override suspend fun getAll(): List<GenModel_2064_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2064_? = store[id]
    override suspend fun save(model: GenModel_2064_): GenModel_2064_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2064_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2064_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2064_ @Inject constructor(
    private val repository: GenRepositoryImpl_2064_
) : GenUseCase_2064_<Unit, List<GenModel_2064_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2064_> = repository.getAll()
}

class GenSaveUseCase_2064_ @Inject constructor(
    private val repository: GenRepositoryImpl_2064_
) : GenUseCase_2064_<GenModel_2064_, GenModel_2064_> {
    override suspend fun invoke(params: GenModel_2064_): GenModel_2064_ = repository.save(params)
}

class GenDeleteUseCase_2064_ @Inject constructor(
    private val repository: GenRepositoryImpl_2064_
) : GenUseCase_2064_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2064_ @Inject constructor(
    private val repository: GenRepositoryImpl_2064_
) : GenUseCase_2064_<String, List<GenModel_2064_>> {
    override suspend fun invoke(params: String): List<GenModel_2064_> = repository.search(params)
}

abstract class GenMapper_2064_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2064_ : GenMapper_2064_<GenModel_2064_, String>() {
    override fun map(input: GenModel_2064_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2064_ : GenMapper_2064_<String, GenModel_2064_>() {
    override fun map(input: String): GenModel_2064_ {
        val parts = input.split(":")
        return GenModel_2064_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2064_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2064_,
    private val saveUseCase: GenSaveUseCase_2064_,
    private val deleteUseCase: GenDeleteUseCase_2064_,
    private val searchUseCase: GenSearchUseCase_2064_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2064_>(GenState_2064_.Idle)
    val state: StateFlow<GenState_2064_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2064_) {
        when (event) {
            is GenEvent_2064_.Load -> loadAll()
            is GenEvent_2064_.Update -> save(event.model)
            is GenEvent_2064_.Delete -> delete(event.id)
            is GenEvent_2064_.Refresh -> loadAll()
            is GenEvent_2064_.Search -> search(event.query)
            is GenEvent_2064_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2064_.Loading; _state.value = GenState_2064_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2064_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2064_.Success(searchUseCase(query)) } }
}
