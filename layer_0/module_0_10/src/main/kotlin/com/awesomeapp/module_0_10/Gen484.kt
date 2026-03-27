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

data class GenModel_484_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_484_ {
    data class Load(val id: Long) : GenEvent_484_()
    data class Update(val model: GenModel_484_) : GenEvent_484_()
    data class Delete(val id: Long) : GenEvent_484_()
    data object Refresh : GenEvent_484_()
    data class Search(val query: String) : GenEvent_484_()
    data class Filter(val predicate: String) : GenEvent_484_()
}

sealed class GenState_484_ {
    data object Idle : GenState_484_()
    data object Loading : GenState_484_()
    data class Success(val items: List<GenModel_484_>) : GenState_484_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_484_()
    data class Partial(val items: List<GenModel_484_>, val hasMore: Boolean) : GenState_484_()
}

interface GenRepository_484_ {
    suspend fun getAll(): List<GenModel_484_>
    suspend fun getById(id: Long): GenModel_484_?
    suspend fun save(model: GenModel_484_): GenModel_484_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_484_>
}

@Singleton
class GenRepositoryImpl_484_ @Inject constructor() : GenRepository_484_ {
    private val store = mutableMapOf<Long, GenModel_484_>()
    override suspend fun getAll(): List<GenModel_484_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_484_? = store[id]
    override suspend fun save(model: GenModel_484_): GenModel_484_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_484_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_484_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_484_ @Inject constructor(
    private val repository: GenRepositoryImpl_484_
) : GenUseCase_484_<Unit, List<GenModel_484_>> {
    override suspend fun invoke(params: Unit): List<GenModel_484_> = repository.getAll()
}

class GenSaveUseCase_484_ @Inject constructor(
    private val repository: GenRepositoryImpl_484_
) : GenUseCase_484_<GenModel_484_, GenModel_484_> {
    override suspend fun invoke(params: GenModel_484_): GenModel_484_ = repository.save(params)
}

class GenDeleteUseCase_484_ @Inject constructor(
    private val repository: GenRepositoryImpl_484_
) : GenUseCase_484_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_484_ @Inject constructor(
    private val repository: GenRepositoryImpl_484_
) : GenUseCase_484_<String, List<GenModel_484_>> {
    override suspend fun invoke(params: String): List<GenModel_484_> = repository.search(params)
}

abstract class GenMapper_484_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_484_ : GenMapper_484_<GenModel_484_, String>() {
    override fun map(input: GenModel_484_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_484_ : GenMapper_484_<String, GenModel_484_>() {
    override fun map(input: String): GenModel_484_ {
        val parts = input.split(":")
        return GenModel_484_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_484_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_484_,
    private val saveUseCase: GenSaveUseCase_484_,
    private val deleteUseCase: GenDeleteUseCase_484_,
    private val searchUseCase: GenSearchUseCase_484_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_484_>(GenState_484_.Idle)
    val state: StateFlow<GenState_484_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_484_) {
        when (event) {
            is GenEvent_484_.Load -> loadAll()
            is GenEvent_484_.Update -> save(event.model)
            is GenEvent_484_.Delete -> delete(event.id)
            is GenEvent_484_.Refresh -> loadAll()
            is GenEvent_484_.Search -> search(event.query)
            is GenEvent_484_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_484_.Loading; _state.value = GenState_484_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_484_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_484_.Success(searchUseCase(query)) } }
}
