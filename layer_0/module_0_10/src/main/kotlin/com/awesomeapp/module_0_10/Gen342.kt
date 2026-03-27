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

data class GenModel_342_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_342_ {
    data class Load(val id: Long) : GenEvent_342_()
    data class Update(val model: GenModel_342_) : GenEvent_342_()
    data class Delete(val id: Long) : GenEvent_342_()
    data object Refresh : GenEvent_342_()
    data class Search(val query: String) : GenEvent_342_()
    data class Filter(val predicate: String) : GenEvent_342_()
}

sealed class GenState_342_ {
    data object Idle : GenState_342_()
    data object Loading : GenState_342_()
    data class Success(val items: List<GenModel_342_>) : GenState_342_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_342_()
    data class Partial(val items: List<GenModel_342_>, val hasMore: Boolean) : GenState_342_()
}

interface GenRepository_342_ {
    suspend fun getAll(): List<GenModel_342_>
    suspend fun getById(id: Long): GenModel_342_?
    suspend fun save(model: GenModel_342_): GenModel_342_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_342_>
}

@Singleton
class GenRepositoryImpl_342_ @Inject constructor() : GenRepository_342_ {
    private val store = mutableMapOf<Long, GenModel_342_>()
    override suspend fun getAll(): List<GenModel_342_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_342_? = store[id]
    override suspend fun save(model: GenModel_342_): GenModel_342_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_342_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_342_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_342_ @Inject constructor(
    private val repository: GenRepositoryImpl_342_
) : GenUseCase_342_<Unit, List<GenModel_342_>> {
    override suspend fun invoke(params: Unit): List<GenModel_342_> = repository.getAll()
}

class GenSaveUseCase_342_ @Inject constructor(
    private val repository: GenRepositoryImpl_342_
) : GenUseCase_342_<GenModel_342_, GenModel_342_> {
    override suspend fun invoke(params: GenModel_342_): GenModel_342_ = repository.save(params)
}

class GenDeleteUseCase_342_ @Inject constructor(
    private val repository: GenRepositoryImpl_342_
) : GenUseCase_342_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_342_ @Inject constructor(
    private val repository: GenRepositoryImpl_342_
) : GenUseCase_342_<String, List<GenModel_342_>> {
    override suspend fun invoke(params: String): List<GenModel_342_> = repository.search(params)
}

abstract class GenMapper_342_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_342_ : GenMapper_342_<GenModel_342_, String>() {
    override fun map(input: GenModel_342_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_342_ : GenMapper_342_<String, GenModel_342_>() {
    override fun map(input: String): GenModel_342_ {
        val parts = input.split(":")
        return GenModel_342_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_342_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_342_,
    private val saveUseCase: GenSaveUseCase_342_,
    private val deleteUseCase: GenDeleteUseCase_342_,
    private val searchUseCase: GenSearchUseCase_342_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_342_>(GenState_342_.Idle)
    val state: StateFlow<GenState_342_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_342_) {
        when (event) {
            is GenEvent_342_.Load -> loadAll()
            is GenEvent_342_.Update -> save(event.model)
            is GenEvent_342_.Delete -> delete(event.id)
            is GenEvent_342_.Refresh -> loadAll()
            is GenEvent_342_.Search -> search(event.query)
            is GenEvent_342_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_342_.Loading; _state.value = GenState_342_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_342_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_342_.Success(searchUseCase(query)) } }
}
