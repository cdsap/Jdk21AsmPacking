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

data class GenModel_326_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_326_ {
    data class Load(val id: Long) : GenEvent_326_()
    data class Update(val model: GenModel_326_) : GenEvent_326_()
    data class Delete(val id: Long) : GenEvent_326_()
    data object Refresh : GenEvent_326_()
    data class Search(val query: String) : GenEvent_326_()
    data class Filter(val predicate: String) : GenEvent_326_()
}

sealed class GenState_326_ {
    data object Idle : GenState_326_()
    data object Loading : GenState_326_()
    data class Success(val items: List<GenModel_326_>) : GenState_326_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_326_()
    data class Partial(val items: List<GenModel_326_>, val hasMore: Boolean) : GenState_326_()
}

interface GenRepository_326_ {
    suspend fun getAll(): List<GenModel_326_>
    suspend fun getById(id: Long): GenModel_326_?
    suspend fun save(model: GenModel_326_): GenModel_326_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_326_>
}

@Singleton
class GenRepositoryImpl_326_ @Inject constructor() : GenRepository_326_ {
    private val store = mutableMapOf<Long, GenModel_326_>()
    override suspend fun getAll(): List<GenModel_326_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_326_? = store[id]
    override suspend fun save(model: GenModel_326_): GenModel_326_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_326_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_326_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_326_ @Inject constructor(
    private val repository: GenRepositoryImpl_326_
) : GenUseCase_326_<Unit, List<GenModel_326_>> {
    override suspend fun invoke(params: Unit): List<GenModel_326_> = repository.getAll()
}

class GenSaveUseCase_326_ @Inject constructor(
    private val repository: GenRepositoryImpl_326_
) : GenUseCase_326_<GenModel_326_, GenModel_326_> {
    override suspend fun invoke(params: GenModel_326_): GenModel_326_ = repository.save(params)
}

class GenDeleteUseCase_326_ @Inject constructor(
    private val repository: GenRepositoryImpl_326_
) : GenUseCase_326_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_326_ @Inject constructor(
    private val repository: GenRepositoryImpl_326_
) : GenUseCase_326_<String, List<GenModel_326_>> {
    override suspend fun invoke(params: String): List<GenModel_326_> = repository.search(params)
}

abstract class GenMapper_326_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_326_ : GenMapper_326_<GenModel_326_, String>() {
    override fun map(input: GenModel_326_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_326_ : GenMapper_326_<String, GenModel_326_>() {
    override fun map(input: String): GenModel_326_ {
        val parts = input.split(":")
        return GenModel_326_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_326_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_326_,
    private val saveUseCase: GenSaveUseCase_326_,
    private val deleteUseCase: GenDeleteUseCase_326_,
    private val searchUseCase: GenSearchUseCase_326_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_326_>(GenState_326_.Idle)
    val state: StateFlow<GenState_326_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_326_) {
        when (event) {
            is GenEvent_326_.Load -> loadAll()
            is GenEvent_326_.Update -> save(event.model)
            is GenEvent_326_.Delete -> delete(event.id)
            is GenEvent_326_.Refresh -> loadAll()
            is GenEvent_326_.Search -> search(event.query)
            is GenEvent_326_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_326_.Loading; _state.value = GenState_326_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_326_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_326_.Success(searchUseCase(query)) } }
}
