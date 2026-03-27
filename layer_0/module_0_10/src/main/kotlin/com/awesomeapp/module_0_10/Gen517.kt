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

data class GenModel_517_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_517_ {
    data class Load(val id: Long) : GenEvent_517_()
    data class Update(val model: GenModel_517_) : GenEvent_517_()
    data class Delete(val id: Long) : GenEvent_517_()
    data object Refresh : GenEvent_517_()
    data class Search(val query: String) : GenEvent_517_()
    data class Filter(val predicate: String) : GenEvent_517_()
}

sealed class GenState_517_ {
    data object Idle : GenState_517_()
    data object Loading : GenState_517_()
    data class Success(val items: List<GenModel_517_>) : GenState_517_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_517_()
    data class Partial(val items: List<GenModel_517_>, val hasMore: Boolean) : GenState_517_()
}

interface GenRepository_517_ {
    suspend fun getAll(): List<GenModel_517_>
    suspend fun getById(id: Long): GenModel_517_?
    suspend fun save(model: GenModel_517_): GenModel_517_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_517_>
}

@Singleton
class GenRepositoryImpl_517_ @Inject constructor() : GenRepository_517_ {
    private val store = mutableMapOf<Long, GenModel_517_>()
    override suspend fun getAll(): List<GenModel_517_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_517_? = store[id]
    override suspend fun save(model: GenModel_517_): GenModel_517_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_517_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_517_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_517_ @Inject constructor(
    private val repository: GenRepositoryImpl_517_
) : GenUseCase_517_<Unit, List<GenModel_517_>> {
    override suspend fun invoke(params: Unit): List<GenModel_517_> = repository.getAll()
}

class GenSaveUseCase_517_ @Inject constructor(
    private val repository: GenRepositoryImpl_517_
) : GenUseCase_517_<GenModel_517_, GenModel_517_> {
    override suspend fun invoke(params: GenModel_517_): GenModel_517_ = repository.save(params)
}

class GenDeleteUseCase_517_ @Inject constructor(
    private val repository: GenRepositoryImpl_517_
) : GenUseCase_517_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_517_ @Inject constructor(
    private val repository: GenRepositoryImpl_517_
) : GenUseCase_517_<String, List<GenModel_517_>> {
    override suspend fun invoke(params: String): List<GenModel_517_> = repository.search(params)
}

abstract class GenMapper_517_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_517_ : GenMapper_517_<GenModel_517_, String>() {
    override fun map(input: GenModel_517_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_517_ : GenMapper_517_<String, GenModel_517_>() {
    override fun map(input: String): GenModel_517_ {
        val parts = input.split(":")
        return GenModel_517_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_517_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_517_,
    private val saveUseCase: GenSaveUseCase_517_,
    private val deleteUseCase: GenDeleteUseCase_517_,
    private val searchUseCase: GenSearchUseCase_517_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_517_>(GenState_517_.Idle)
    val state: StateFlow<GenState_517_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_517_) {
        when (event) {
            is GenEvent_517_.Load -> loadAll()
            is GenEvent_517_.Update -> save(event.model)
            is GenEvent_517_.Delete -> delete(event.id)
            is GenEvent_517_.Refresh -> loadAll()
            is GenEvent_517_.Search -> search(event.query)
            is GenEvent_517_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_517_.Loading; _state.value = GenState_517_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_517_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_517_.Success(searchUseCase(query)) } }
}
