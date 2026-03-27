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

data class GenModel_319_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_319_ {
    data class Load(val id: Long) : GenEvent_319_()
    data class Update(val model: GenModel_319_) : GenEvent_319_()
    data class Delete(val id: Long) : GenEvent_319_()
    data object Refresh : GenEvent_319_()
    data class Search(val query: String) : GenEvent_319_()
    data class Filter(val predicate: String) : GenEvent_319_()
}

sealed class GenState_319_ {
    data object Idle : GenState_319_()
    data object Loading : GenState_319_()
    data class Success(val items: List<GenModel_319_>) : GenState_319_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_319_()
    data class Partial(val items: List<GenModel_319_>, val hasMore: Boolean) : GenState_319_()
}

interface GenRepository_319_ {
    suspend fun getAll(): List<GenModel_319_>
    suspend fun getById(id: Long): GenModel_319_?
    suspend fun save(model: GenModel_319_): GenModel_319_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_319_>
}

@Singleton
class GenRepositoryImpl_319_ @Inject constructor() : GenRepository_319_ {
    private val store = mutableMapOf<Long, GenModel_319_>()
    override suspend fun getAll(): List<GenModel_319_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_319_? = store[id]
    override suspend fun save(model: GenModel_319_): GenModel_319_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_319_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_319_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_319_ @Inject constructor(
    private val repository: GenRepositoryImpl_319_
) : GenUseCase_319_<Unit, List<GenModel_319_>> {
    override suspend fun invoke(params: Unit): List<GenModel_319_> = repository.getAll()
}

class GenSaveUseCase_319_ @Inject constructor(
    private val repository: GenRepositoryImpl_319_
) : GenUseCase_319_<GenModel_319_, GenModel_319_> {
    override suspend fun invoke(params: GenModel_319_): GenModel_319_ = repository.save(params)
}

class GenDeleteUseCase_319_ @Inject constructor(
    private val repository: GenRepositoryImpl_319_
) : GenUseCase_319_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_319_ @Inject constructor(
    private val repository: GenRepositoryImpl_319_
) : GenUseCase_319_<String, List<GenModel_319_>> {
    override suspend fun invoke(params: String): List<GenModel_319_> = repository.search(params)
}

abstract class GenMapper_319_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_319_ : GenMapper_319_<GenModel_319_, String>() {
    override fun map(input: GenModel_319_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_319_ : GenMapper_319_<String, GenModel_319_>() {
    override fun map(input: String): GenModel_319_ {
        val parts = input.split(":")
        return GenModel_319_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_319_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_319_,
    private val saveUseCase: GenSaveUseCase_319_,
    private val deleteUseCase: GenDeleteUseCase_319_,
    private val searchUseCase: GenSearchUseCase_319_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_319_>(GenState_319_.Idle)
    val state: StateFlow<GenState_319_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_319_) {
        when (event) {
            is GenEvent_319_.Load -> loadAll()
            is GenEvent_319_.Update -> save(event.model)
            is GenEvent_319_.Delete -> delete(event.id)
            is GenEvent_319_.Refresh -> loadAll()
            is GenEvent_319_.Search -> search(event.query)
            is GenEvent_319_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_319_.Loading; _state.value = GenState_319_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_319_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_319_.Success(searchUseCase(query)) } }
}
