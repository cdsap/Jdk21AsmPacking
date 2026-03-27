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

data class GenModel_480_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_480_ {
    data class Load(val id: Long) : GenEvent_480_()
    data class Update(val model: GenModel_480_) : GenEvent_480_()
    data class Delete(val id: Long) : GenEvent_480_()
    data object Refresh : GenEvent_480_()
    data class Search(val query: String) : GenEvent_480_()
    data class Filter(val predicate: String) : GenEvent_480_()
}

sealed class GenState_480_ {
    data object Idle : GenState_480_()
    data object Loading : GenState_480_()
    data class Success(val items: List<GenModel_480_>) : GenState_480_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_480_()
    data class Partial(val items: List<GenModel_480_>, val hasMore: Boolean) : GenState_480_()
}

interface GenRepository_480_ {
    suspend fun getAll(): List<GenModel_480_>
    suspend fun getById(id: Long): GenModel_480_?
    suspend fun save(model: GenModel_480_): GenModel_480_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_480_>
}

@Singleton
class GenRepositoryImpl_480_ @Inject constructor() : GenRepository_480_ {
    private val store = mutableMapOf<Long, GenModel_480_>()
    override suspend fun getAll(): List<GenModel_480_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_480_? = store[id]
    override suspend fun save(model: GenModel_480_): GenModel_480_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_480_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_480_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_480_ @Inject constructor(
    private val repository: GenRepositoryImpl_480_
) : GenUseCase_480_<Unit, List<GenModel_480_>> {
    override suspend fun invoke(params: Unit): List<GenModel_480_> = repository.getAll()
}

class GenSaveUseCase_480_ @Inject constructor(
    private val repository: GenRepositoryImpl_480_
) : GenUseCase_480_<GenModel_480_, GenModel_480_> {
    override suspend fun invoke(params: GenModel_480_): GenModel_480_ = repository.save(params)
}

class GenDeleteUseCase_480_ @Inject constructor(
    private val repository: GenRepositoryImpl_480_
) : GenUseCase_480_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_480_ @Inject constructor(
    private val repository: GenRepositoryImpl_480_
) : GenUseCase_480_<String, List<GenModel_480_>> {
    override suspend fun invoke(params: String): List<GenModel_480_> = repository.search(params)
}

abstract class GenMapper_480_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_480_ : GenMapper_480_<GenModel_480_, String>() {
    override fun map(input: GenModel_480_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_480_ : GenMapper_480_<String, GenModel_480_>() {
    override fun map(input: String): GenModel_480_ {
        val parts = input.split(":")
        return GenModel_480_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_480_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_480_,
    private val saveUseCase: GenSaveUseCase_480_,
    private val deleteUseCase: GenDeleteUseCase_480_,
    private val searchUseCase: GenSearchUseCase_480_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_480_>(GenState_480_.Idle)
    val state: StateFlow<GenState_480_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_480_) {
        when (event) {
            is GenEvent_480_.Load -> loadAll()
            is GenEvent_480_.Update -> save(event.model)
            is GenEvent_480_.Delete -> delete(event.id)
            is GenEvent_480_.Refresh -> loadAll()
            is GenEvent_480_.Search -> search(event.query)
            is GenEvent_480_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_480_.Loading; _state.value = GenState_480_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_480_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_480_.Success(searchUseCase(query)) } }
}
