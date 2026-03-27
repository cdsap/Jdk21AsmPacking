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

data class GenModel_1616_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1616_ {
    data class Load(val id: Long) : GenEvent_1616_()
    data class Update(val model: GenModel_1616_) : GenEvent_1616_()
    data class Delete(val id: Long) : GenEvent_1616_()
    data object Refresh : GenEvent_1616_()
    data class Search(val query: String) : GenEvent_1616_()
    data class Filter(val predicate: String) : GenEvent_1616_()
}

sealed class GenState_1616_ {
    data object Idle : GenState_1616_()
    data object Loading : GenState_1616_()
    data class Success(val items: List<GenModel_1616_>) : GenState_1616_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1616_()
    data class Partial(val items: List<GenModel_1616_>, val hasMore: Boolean) : GenState_1616_()
}

interface GenRepository_1616_ {
    suspend fun getAll(): List<GenModel_1616_>
    suspend fun getById(id: Long): GenModel_1616_?
    suspend fun save(model: GenModel_1616_): GenModel_1616_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1616_>
}

@Singleton
class GenRepositoryImpl_1616_ @Inject constructor() : GenRepository_1616_ {
    private val store = mutableMapOf<Long, GenModel_1616_>()
    override suspend fun getAll(): List<GenModel_1616_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1616_? = store[id]
    override suspend fun save(model: GenModel_1616_): GenModel_1616_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1616_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1616_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1616_ @Inject constructor(
    private val repository: GenRepositoryImpl_1616_
) : GenUseCase_1616_<Unit, List<GenModel_1616_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1616_> = repository.getAll()
}

class GenSaveUseCase_1616_ @Inject constructor(
    private val repository: GenRepositoryImpl_1616_
) : GenUseCase_1616_<GenModel_1616_, GenModel_1616_> {
    override suspend fun invoke(params: GenModel_1616_): GenModel_1616_ = repository.save(params)
}

class GenDeleteUseCase_1616_ @Inject constructor(
    private val repository: GenRepositoryImpl_1616_
) : GenUseCase_1616_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1616_ @Inject constructor(
    private val repository: GenRepositoryImpl_1616_
) : GenUseCase_1616_<String, List<GenModel_1616_>> {
    override suspend fun invoke(params: String): List<GenModel_1616_> = repository.search(params)
}

abstract class GenMapper_1616_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1616_ : GenMapper_1616_<GenModel_1616_, String>() {
    override fun map(input: GenModel_1616_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1616_ : GenMapper_1616_<String, GenModel_1616_>() {
    override fun map(input: String): GenModel_1616_ {
        val parts = input.split(":")
        return GenModel_1616_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1616_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1616_,
    private val saveUseCase: GenSaveUseCase_1616_,
    private val deleteUseCase: GenDeleteUseCase_1616_,
    private val searchUseCase: GenSearchUseCase_1616_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1616_>(GenState_1616_.Idle)
    val state: StateFlow<GenState_1616_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1616_) {
        when (event) {
            is GenEvent_1616_.Load -> loadAll()
            is GenEvent_1616_.Update -> save(event.model)
            is GenEvent_1616_.Delete -> delete(event.id)
            is GenEvent_1616_.Refresh -> loadAll()
            is GenEvent_1616_.Search -> search(event.query)
            is GenEvent_1616_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1616_.Loading; _state.value = GenState_1616_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1616_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1616_.Success(searchUseCase(query)) } }
}
