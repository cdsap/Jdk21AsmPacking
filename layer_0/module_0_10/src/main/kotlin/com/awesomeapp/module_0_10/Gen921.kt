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

data class GenModel_921_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_921_ {
    data class Load(val id: Long) : GenEvent_921_()
    data class Update(val model: GenModel_921_) : GenEvent_921_()
    data class Delete(val id: Long) : GenEvent_921_()
    data object Refresh : GenEvent_921_()
    data class Search(val query: String) : GenEvent_921_()
    data class Filter(val predicate: String) : GenEvent_921_()
}

sealed class GenState_921_ {
    data object Idle : GenState_921_()
    data object Loading : GenState_921_()
    data class Success(val items: List<GenModel_921_>) : GenState_921_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_921_()
    data class Partial(val items: List<GenModel_921_>, val hasMore: Boolean) : GenState_921_()
}

interface GenRepository_921_ {
    suspend fun getAll(): List<GenModel_921_>
    suspend fun getById(id: Long): GenModel_921_?
    suspend fun save(model: GenModel_921_): GenModel_921_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_921_>
}

@Singleton
class GenRepositoryImpl_921_ @Inject constructor() : GenRepository_921_ {
    private val store = mutableMapOf<Long, GenModel_921_>()
    override suspend fun getAll(): List<GenModel_921_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_921_? = store[id]
    override suspend fun save(model: GenModel_921_): GenModel_921_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_921_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_921_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_921_ @Inject constructor(
    private val repository: GenRepositoryImpl_921_
) : GenUseCase_921_<Unit, List<GenModel_921_>> {
    override suspend fun invoke(params: Unit): List<GenModel_921_> = repository.getAll()
}

class GenSaveUseCase_921_ @Inject constructor(
    private val repository: GenRepositoryImpl_921_
) : GenUseCase_921_<GenModel_921_, GenModel_921_> {
    override suspend fun invoke(params: GenModel_921_): GenModel_921_ = repository.save(params)
}

class GenDeleteUseCase_921_ @Inject constructor(
    private val repository: GenRepositoryImpl_921_
) : GenUseCase_921_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_921_ @Inject constructor(
    private val repository: GenRepositoryImpl_921_
) : GenUseCase_921_<String, List<GenModel_921_>> {
    override suspend fun invoke(params: String): List<GenModel_921_> = repository.search(params)
}

abstract class GenMapper_921_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_921_ : GenMapper_921_<GenModel_921_, String>() {
    override fun map(input: GenModel_921_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_921_ : GenMapper_921_<String, GenModel_921_>() {
    override fun map(input: String): GenModel_921_ {
        val parts = input.split(":")
        return GenModel_921_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_921_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_921_,
    private val saveUseCase: GenSaveUseCase_921_,
    private val deleteUseCase: GenDeleteUseCase_921_,
    private val searchUseCase: GenSearchUseCase_921_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_921_>(GenState_921_.Idle)
    val state: StateFlow<GenState_921_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_921_) {
        when (event) {
            is GenEvent_921_.Load -> loadAll()
            is GenEvent_921_.Update -> save(event.model)
            is GenEvent_921_.Delete -> delete(event.id)
            is GenEvent_921_.Refresh -> loadAll()
            is GenEvent_921_.Search -> search(event.query)
            is GenEvent_921_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_921_.Loading; _state.value = GenState_921_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_921_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_921_.Success(searchUseCase(query)) } }
}
