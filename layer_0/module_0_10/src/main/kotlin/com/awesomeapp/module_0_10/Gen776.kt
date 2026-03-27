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

data class GenModel_776_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_776_ {
    data class Load(val id: Long) : GenEvent_776_()
    data class Update(val model: GenModel_776_) : GenEvent_776_()
    data class Delete(val id: Long) : GenEvent_776_()
    data object Refresh : GenEvent_776_()
    data class Search(val query: String) : GenEvent_776_()
    data class Filter(val predicate: String) : GenEvent_776_()
}

sealed class GenState_776_ {
    data object Idle : GenState_776_()
    data object Loading : GenState_776_()
    data class Success(val items: List<GenModel_776_>) : GenState_776_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_776_()
    data class Partial(val items: List<GenModel_776_>, val hasMore: Boolean) : GenState_776_()
}

interface GenRepository_776_ {
    suspend fun getAll(): List<GenModel_776_>
    suspend fun getById(id: Long): GenModel_776_?
    suspend fun save(model: GenModel_776_): GenModel_776_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_776_>
}

@Singleton
class GenRepositoryImpl_776_ @Inject constructor() : GenRepository_776_ {
    private val store = mutableMapOf<Long, GenModel_776_>()
    override suspend fun getAll(): List<GenModel_776_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_776_? = store[id]
    override suspend fun save(model: GenModel_776_): GenModel_776_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_776_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_776_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_776_ @Inject constructor(
    private val repository: GenRepositoryImpl_776_
) : GenUseCase_776_<Unit, List<GenModel_776_>> {
    override suspend fun invoke(params: Unit): List<GenModel_776_> = repository.getAll()
}

class GenSaveUseCase_776_ @Inject constructor(
    private val repository: GenRepositoryImpl_776_
) : GenUseCase_776_<GenModel_776_, GenModel_776_> {
    override suspend fun invoke(params: GenModel_776_): GenModel_776_ = repository.save(params)
}

class GenDeleteUseCase_776_ @Inject constructor(
    private val repository: GenRepositoryImpl_776_
) : GenUseCase_776_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_776_ @Inject constructor(
    private val repository: GenRepositoryImpl_776_
) : GenUseCase_776_<String, List<GenModel_776_>> {
    override suspend fun invoke(params: String): List<GenModel_776_> = repository.search(params)
}

abstract class GenMapper_776_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_776_ : GenMapper_776_<GenModel_776_, String>() {
    override fun map(input: GenModel_776_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_776_ : GenMapper_776_<String, GenModel_776_>() {
    override fun map(input: String): GenModel_776_ {
        val parts = input.split(":")
        return GenModel_776_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_776_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_776_,
    private val saveUseCase: GenSaveUseCase_776_,
    private val deleteUseCase: GenDeleteUseCase_776_,
    private val searchUseCase: GenSearchUseCase_776_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_776_>(GenState_776_.Idle)
    val state: StateFlow<GenState_776_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_776_) {
        when (event) {
            is GenEvent_776_.Load -> loadAll()
            is GenEvent_776_.Update -> save(event.model)
            is GenEvent_776_.Delete -> delete(event.id)
            is GenEvent_776_.Refresh -> loadAll()
            is GenEvent_776_.Search -> search(event.query)
            is GenEvent_776_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_776_.Loading; _state.value = GenState_776_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_776_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_776_.Success(searchUseCase(query)) } }
}
