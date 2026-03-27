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

data class GenModel_2394_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2394_ {
    data class Load(val id: Long) : GenEvent_2394_()
    data class Update(val model: GenModel_2394_) : GenEvent_2394_()
    data class Delete(val id: Long) : GenEvent_2394_()
    data object Refresh : GenEvent_2394_()
    data class Search(val query: String) : GenEvent_2394_()
    data class Filter(val predicate: String) : GenEvent_2394_()
}

sealed class GenState_2394_ {
    data object Idle : GenState_2394_()
    data object Loading : GenState_2394_()
    data class Success(val items: List<GenModel_2394_>) : GenState_2394_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2394_()
    data class Partial(val items: List<GenModel_2394_>, val hasMore: Boolean) : GenState_2394_()
}

interface GenRepository_2394_ {
    suspend fun getAll(): List<GenModel_2394_>
    suspend fun getById(id: Long): GenModel_2394_?
    suspend fun save(model: GenModel_2394_): GenModel_2394_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2394_>
}

@Singleton
class GenRepositoryImpl_2394_ @Inject constructor() : GenRepository_2394_ {
    private val store = mutableMapOf<Long, GenModel_2394_>()
    override suspend fun getAll(): List<GenModel_2394_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2394_? = store[id]
    override suspend fun save(model: GenModel_2394_): GenModel_2394_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2394_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2394_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2394_ @Inject constructor(
    private val repository: GenRepositoryImpl_2394_
) : GenUseCase_2394_<Unit, List<GenModel_2394_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2394_> = repository.getAll()
}

class GenSaveUseCase_2394_ @Inject constructor(
    private val repository: GenRepositoryImpl_2394_
) : GenUseCase_2394_<GenModel_2394_, GenModel_2394_> {
    override suspend fun invoke(params: GenModel_2394_): GenModel_2394_ = repository.save(params)
}

class GenDeleteUseCase_2394_ @Inject constructor(
    private val repository: GenRepositoryImpl_2394_
) : GenUseCase_2394_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2394_ @Inject constructor(
    private val repository: GenRepositoryImpl_2394_
) : GenUseCase_2394_<String, List<GenModel_2394_>> {
    override suspend fun invoke(params: String): List<GenModel_2394_> = repository.search(params)
}

abstract class GenMapper_2394_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2394_ : GenMapper_2394_<GenModel_2394_, String>() {
    override fun map(input: GenModel_2394_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2394_ : GenMapper_2394_<String, GenModel_2394_>() {
    override fun map(input: String): GenModel_2394_ {
        val parts = input.split(":")
        return GenModel_2394_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2394_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2394_,
    private val saveUseCase: GenSaveUseCase_2394_,
    private val deleteUseCase: GenDeleteUseCase_2394_,
    private val searchUseCase: GenSearchUseCase_2394_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2394_>(GenState_2394_.Idle)
    val state: StateFlow<GenState_2394_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2394_) {
        when (event) {
            is GenEvent_2394_.Load -> loadAll()
            is GenEvent_2394_.Update -> save(event.model)
            is GenEvent_2394_.Delete -> delete(event.id)
            is GenEvent_2394_.Refresh -> loadAll()
            is GenEvent_2394_.Search -> search(event.query)
            is GenEvent_2394_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2394_.Loading; _state.value = GenState_2394_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2394_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2394_.Success(searchUseCase(query)) } }
}
