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

data class GenModel_364_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_364_ {
    data class Load(val id: Long) : GenEvent_364_()
    data class Update(val model: GenModel_364_) : GenEvent_364_()
    data class Delete(val id: Long) : GenEvent_364_()
    data object Refresh : GenEvent_364_()
    data class Search(val query: String) : GenEvent_364_()
    data class Filter(val predicate: String) : GenEvent_364_()
}

sealed class GenState_364_ {
    data object Idle : GenState_364_()
    data object Loading : GenState_364_()
    data class Success(val items: List<GenModel_364_>) : GenState_364_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_364_()
    data class Partial(val items: List<GenModel_364_>, val hasMore: Boolean) : GenState_364_()
}

interface GenRepository_364_ {
    suspend fun getAll(): List<GenModel_364_>
    suspend fun getById(id: Long): GenModel_364_?
    suspend fun save(model: GenModel_364_): GenModel_364_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_364_>
}

@Singleton
class GenRepositoryImpl_364_ @Inject constructor() : GenRepository_364_ {
    private val store = mutableMapOf<Long, GenModel_364_>()
    override suspend fun getAll(): List<GenModel_364_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_364_? = store[id]
    override suspend fun save(model: GenModel_364_): GenModel_364_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_364_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_364_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_364_ @Inject constructor(
    private val repository: GenRepositoryImpl_364_
) : GenUseCase_364_<Unit, List<GenModel_364_>> {
    override suspend fun invoke(params: Unit): List<GenModel_364_> = repository.getAll()
}

class GenSaveUseCase_364_ @Inject constructor(
    private val repository: GenRepositoryImpl_364_
) : GenUseCase_364_<GenModel_364_, GenModel_364_> {
    override suspend fun invoke(params: GenModel_364_): GenModel_364_ = repository.save(params)
}

class GenDeleteUseCase_364_ @Inject constructor(
    private val repository: GenRepositoryImpl_364_
) : GenUseCase_364_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_364_ @Inject constructor(
    private val repository: GenRepositoryImpl_364_
) : GenUseCase_364_<String, List<GenModel_364_>> {
    override suspend fun invoke(params: String): List<GenModel_364_> = repository.search(params)
}

abstract class GenMapper_364_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_364_ : GenMapper_364_<GenModel_364_, String>() {
    override fun map(input: GenModel_364_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_364_ : GenMapper_364_<String, GenModel_364_>() {
    override fun map(input: String): GenModel_364_ {
        val parts = input.split(":")
        return GenModel_364_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_364_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_364_,
    private val saveUseCase: GenSaveUseCase_364_,
    private val deleteUseCase: GenDeleteUseCase_364_,
    private val searchUseCase: GenSearchUseCase_364_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_364_>(GenState_364_.Idle)
    val state: StateFlow<GenState_364_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_364_) {
        when (event) {
            is GenEvent_364_.Load -> loadAll()
            is GenEvent_364_.Update -> save(event.model)
            is GenEvent_364_.Delete -> delete(event.id)
            is GenEvent_364_.Refresh -> loadAll()
            is GenEvent_364_.Search -> search(event.query)
            is GenEvent_364_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_364_.Loading; _state.value = GenState_364_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_364_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_364_.Success(searchUseCase(query)) } }
}
