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

data class GenModel_616_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_616_ {
    data class Load(val id: Long) : GenEvent_616_()
    data class Update(val model: GenModel_616_) : GenEvent_616_()
    data class Delete(val id: Long) : GenEvent_616_()
    data object Refresh : GenEvent_616_()
    data class Search(val query: String) : GenEvent_616_()
    data class Filter(val predicate: String) : GenEvent_616_()
}

sealed class GenState_616_ {
    data object Idle : GenState_616_()
    data object Loading : GenState_616_()
    data class Success(val items: List<GenModel_616_>) : GenState_616_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_616_()
    data class Partial(val items: List<GenModel_616_>, val hasMore: Boolean) : GenState_616_()
}

interface GenRepository_616_ {
    suspend fun getAll(): List<GenModel_616_>
    suspend fun getById(id: Long): GenModel_616_?
    suspend fun save(model: GenModel_616_): GenModel_616_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_616_>
}

@Singleton
class GenRepositoryImpl_616_ @Inject constructor() : GenRepository_616_ {
    private val store = mutableMapOf<Long, GenModel_616_>()
    override suspend fun getAll(): List<GenModel_616_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_616_? = store[id]
    override suspend fun save(model: GenModel_616_): GenModel_616_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_616_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_616_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_616_ @Inject constructor(
    private val repository: GenRepositoryImpl_616_
) : GenUseCase_616_<Unit, List<GenModel_616_>> {
    override suspend fun invoke(params: Unit): List<GenModel_616_> = repository.getAll()
}

class GenSaveUseCase_616_ @Inject constructor(
    private val repository: GenRepositoryImpl_616_
) : GenUseCase_616_<GenModel_616_, GenModel_616_> {
    override suspend fun invoke(params: GenModel_616_): GenModel_616_ = repository.save(params)
}

class GenDeleteUseCase_616_ @Inject constructor(
    private val repository: GenRepositoryImpl_616_
) : GenUseCase_616_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_616_ @Inject constructor(
    private val repository: GenRepositoryImpl_616_
) : GenUseCase_616_<String, List<GenModel_616_>> {
    override suspend fun invoke(params: String): List<GenModel_616_> = repository.search(params)
}

abstract class GenMapper_616_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_616_ : GenMapper_616_<GenModel_616_, String>() {
    override fun map(input: GenModel_616_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_616_ : GenMapper_616_<String, GenModel_616_>() {
    override fun map(input: String): GenModel_616_ {
        val parts = input.split(":")
        return GenModel_616_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_616_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_616_,
    private val saveUseCase: GenSaveUseCase_616_,
    private val deleteUseCase: GenDeleteUseCase_616_,
    private val searchUseCase: GenSearchUseCase_616_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_616_>(GenState_616_.Idle)
    val state: StateFlow<GenState_616_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_616_) {
        when (event) {
            is GenEvent_616_.Load -> loadAll()
            is GenEvent_616_.Update -> save(event.model)
            is GenEvent_616_.Delete -> delete(event.id)
            is GenEvent_616_.Refresh -> loadAll()
            is GenEvent_616_.Search -> search(event.query)
            is GenEvent_616_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_616_.Loading; _state.value = GenState_616_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_616_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_616_.Success(searchUseCase(query)) } }
}
