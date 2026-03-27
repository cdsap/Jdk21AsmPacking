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

data class GenModel_812_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_812_ {
    data class Load(val id: Long) : GenEvent_812_()
    data class Update(val model: GenModel_812_) : GenEvent_812_()
    data class Delete(val id: Long) : GenEvent_812_()
    data object Refresh : GenEvent_812_()
    data class Search(val query: String) : GenEvent_812_()
    data class Filter(val predicate: String) : GenEvent_812_()
}

sealed class GenState_812_ {
    data object Idle : GenState_812_()
    data object Loading : GenState_812_()
    data class Success(val items: List<GenModel_812_>) : GenState_812_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_812_()
    data class Partial(val items: List<GenModel_812_>, val hasMore: Boolean) : GenState_812_()
}

interface GenRepository_812_ {
    suspend fun getAll(): List<GenModel_812_>
    suspend fun getById(id: Long): GenModel_812_?
    suspend fun save(model: GenModel_812_): GenModel_812_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_812_>
}

@Singleton
class GenRepositoryImpl_812_ @Inject constructor() : GenRepository_812_ {
    private val store = mutableMapOf<Long, GenModel_812_>()
    override suspend fun getAll(): List<GenModel_812_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_812_? = store[id]
    override suspend fun save(model: GenModel_812_): GenModel_812_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_812_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_812_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_812_ @Inject constructor(
    private val repository: GenRepositoryImpl_812_
) : GenUseCase_812_<Unit, List<GenModel_812_>> {
    override suspend fun invoke(params: Unit): List<GenModel_812_> = repository.getAll()
}

class GenSaveUseCase_812_ @Inject constructor(
    private val repository: GenRepositoryImpl_812_
) : GenUseCase_812_<GenModel_812_, GenModel_812_> {
    override suspend fun invoke(params: GenModel_812_): GenModel_812_ = repository.save(params)
}

class GenDeleteUseCase_812_ @Inject constructor(
    private val repository: GenRepositoryImpl_812_
) : GenUseCase_812_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_812_ @Inject constructor(
    private val repository: GenRepositoryImpl_812_
) : GenUseCase_812_<String, List<GenModel_812_>> {
    override suspend fun invoke(params: String): List<GenModel_812_> = repository.search(params)
}

abstract class GenMapper_812_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_812_ : GenMapper_812_<GenModel_812_, String>() {
    override fun map(input: GenModel_812_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_812_ : GenMapper_812_<String, GenModel_812_>() {
    override fun map(input: String): GenModel_812_ {
        val parts = input.split(":")
        return GenModel_812_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_812_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_812_,
    private val saveUseCase: GenSaveUseCase_812_,
    private val deleteUseCase: GenDeleteUseCase_812_,
    private val searchUseCase: GenSearchUseCase_812_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_812_>(GenState_812_.Idle)
    val state: StateFlow<GenState_812_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_812_) {
        when (event) {
            is GenEvent_812_.Load -> loadAll()
            is GenEvent_812_.Update -> save(event.model)
            is GenEvent_812_.Delete -> delete(event.id)
            is GenEvent_812_.Refresh -> loadAll()
            is GenEvent_812_.Search -> search(event.query)
            is GenEvent_812_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_812_.Loading; _state.value = GenState_812_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_812_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_812_.Success(searchUseCase(query)) } }
}
