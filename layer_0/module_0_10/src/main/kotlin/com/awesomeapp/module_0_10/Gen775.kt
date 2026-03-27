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

data class GenModel_775_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_775_ {
    data class Load(val id: Long) : GenEvent_775_()
    data class Update(val model: GenModel_775_) : GenEvent_775_()
    data class Delete(val id: Long) : GenEvent_775_()
    data object Refresh : GenEvent_775_()
    data class Search(val query: String) : GenEvent_775_()
    data class Filter(val predicate: String) : GenEvent_775_()
}

sealed class GenState_775_ {
    data object Idle : GenState_775_()
    data object Loading : GenState_775_()
    data class Success(val items: List<GenModel_775_>) : GenState_775_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_775_()
    data class Partial(val items: List<GenModel_775_>, val hasMore: Boolean) : GenState_775_()
}

interface GenRepository_775_ {
    suspend fun getAll(): List<GenModel_775_>
    suspend fun getById(id: Long): GenModel_775_?
    suspend fun save(model: GenModel_775_): GenModel_775_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_775_>
}

@Singleton
class GenRepositoryImpl_775_ @Inject constructor() : GenRepository_775_ {
    private val store = mutableMapOf<Long, GenModel_775_>()
    override suspend fun getAll(): List<GenModel_775_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_775_? = store[id]
    override suspend fun save(model: GenModel_775_): GenModel_775_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_775_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_775_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_775_ @Inject constructor(
    private val repository: GenRepositoryImpl_775_
) : GenUseCase_775_<Unit, List<GenModel_775_>> {
    override suspend fun invoke(params: Unit): List<GenModel_775_> = repository.getAll()
}

class GenSaveUseCase_775_ @Inject constructor(
    private val repository: GenRepositoryImpl_775_
) : GenUseCase_775_<GenModel_775_, GenModel_775_> {
    override suspend fun invoke(params: GenModel_775_): GenModel_775_ = repository.save(params)
}

class GenDeleteUseCase_775_ @Inject constructor(
    private val repository: GenRepositoryImpl_775_
) : GenUseCase_775_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_775_ @Inject constructor(
    private val repository: GenRepositoryImpl_775_
) : GenUseCase_775_<String, List<GenModel_775_>> {
    override suspend fun invoke(params: String): List<GenModel_775_> = repository.search(params)
}

abstract class GenMapper_775_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_775_ : GenMapper_775_<GenModel_775_, String>() {
    override fun map(input: GenModel_775_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_775_ : GenMapper_775_<String, GenModel_775_>() {
    override fun map(input: String): GenModel_775_ {
        val parts = input.split(":")
        return GenModel_775_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_775_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_775_,
    private val saveUseCase: GenSaveUseCase_775_,
    private val deleteUseCase: GenDeleteUseCase_775_,
    private val searchUseCase: GenSearchUseCase_775_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_775_>(GenState_775_.Idle)
    val state: StateFlow<GenState_775_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_775_) {
        when (event) {
            is GenEvent_775_.Load -> loadAll()
            is GenEvent_775_.Update -> save(event.model)
            is GenEvent_775_.Delete -> delete(event.id)
            is GenEvent_775_.Refresh -> loadAll()
            is GenEvent_775_.Search -> search(event.query)
            is GenEvent_775_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_775_.Loading; _state.value = GenState_775_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_775_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_775_.Success(searchUseCase(query)) } }
}
