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

data class GenModel_540_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_540_ {
    data class Load(val id: Long) : GenEvent_540_()
    data class Update(val model: GenModel_540_) : GenEvent_540_()
    data class Delete(val id: Long) : GenEvent_540_()
    data object Refresh : GenEvent_540_()
    data class Search(val query: String) : GenEvent_540_()
    data class Filter(val predicate: String) : GenEvent_540_()
}

sealed class GenState_540_ {
    data object Idle : GenState_540_()
    data object Loading : GenState_540_()
    data class Success(val items: List<GenModel_540_>) : GenState_540_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_540_()
    data class Partial(val items: List<GenModel_540_>, val hasMore: Boolean) : GenState_540_()
}

interface GenRepository_540_ {
    suspend fun getAll(): List<GenModel_540_>
    suspend fun getById(id: Long): GenModel_540_?
    suspend fun save(model: GenModel_540_): GenModel_540_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_540_>
}

@Singleton
class GenRepositoryImpl_540_ @Inject constructor() : GenRepository_540_ {
    private val store = mutableMapOf<Long, GenModel_540_>()
    override suspend fun getAll(): List<GenModel_540_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_540_? = store[id]
    override suspend fun save(model: GenModel_540_): GenModel_540_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_540_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_540_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_540_ @Inject constructor(
    private val repository: GenRepositoryImpl_540_
) : GenUseCase_540_<Unit, List<GenModel_540_>> {
    override suspend fun invoke(params: Unit): List<GenModel_540_> = repository.getAll()
}

class GenSaveUseCase_540_ @Inject constructor(
    private val repository: GenRepositoryImpl_540_
) : GenUseCase_540_<GenModel_540_, GenModel_540_> {
    override suspend fun invoke(params: GenModel_540_): GenModel_540_ = repository.save(params)
}

class GenDeleteUseCase_540_ @Inject constructor(
    private val repository: GenRepositoryImpl_540_
) : GenUseCase_540_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_540_ @Inject constructor(
    private val repository: GenRepositoryImpl_540_
) : GenUseCase_540_<String, List<GenModel_540_>> {
    override suspend fun invoke(params: String): List<GenModel_540_> = repository.search(params)
}

abstract class GenMapper_540_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_540_ : GenMapper_540_<GenModel_540_, String>() {
    override fun map(input: GenModel_540_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_540_ : GenMapper_540_<String, GenModel_540_>() {
    override fun map(input: String): GenModel_540_ {
        val parts = input.split(":")
        return GenModel_540_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_540_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_540_,
    private val saveUseCase: GenSaveUseCase_540_,
    private val deleteUseCase: GenDeleteUseCase_540_,
    private val searchUseCase: GenSearchUseCase_540_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_540_>(GenState_540_.Idle)
    val state: StateFlow<GenState_540_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_540_) {
        when (event) {
            is GenEvent_540_.Load -> loadAll()
            is GenEvent_540_.Update -> save(event.model)
            is GenEvent_540_.Delete -> delete(event.id)
            is GenEvent_540_.Refresh -> loadAll()
            is GenEvent_540_.Search -> search(event.query)
            is GenEvent_540_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_540_.Loading; _state.value = GenState_540_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_540_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_540_.Success(searchUseCase(query)) } }
}
