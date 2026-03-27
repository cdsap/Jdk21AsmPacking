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

data class GenModel_345_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_345_ {
    data class Load(val id: Long) : GenEvent_345_()
    data class Update(val model: GenModel_345_) : GenEvent_345_()
    data class Delete(val id: Long) : GenEvent_345_()
    data object Refresh : GenEvent_345_()
    data class Search(val query: String) : GenEvent_345_()
    data class Filter(val predicate: String) : GenEvent_345_()
}

sealed class GenState_345_ {
    data object Idle : GenState_345_()
    data object Loading : GenState_345_()
    data class Success(val items: List<GenModel_345_>) : GenState_345_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_345_()
    data class Partial(val items: List<GenModel_345_>, val hasMore: Boolean) : GenState_345_()
}

interface GenRepository_345_ {
    suspend fun getAll(): List<GenModel_345_>
    suspend fun getById(id: Long): GenModel_345_?
    suspend fun save(model: GenModel_345_): GenModel_345_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_345_>
}

@Singleton
class GenRepositoryImpl_345_ @Inject constructor() : GenRepository_345_ {
    private val store = mutableMapOf<Long, GenModel_345_>()
    override suspend fun getAll(): List<GenModel_345_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_345_? = store[id]
    override suspend fun save(model: GenModel_345_): GenModel_345_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_345_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_345_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_345_ @Inject constructor(
    private val repository: GenRepositoryImpl_345_
) : GenUseCase_345_<Unit, List<GenModel_345_>> {
    override suspend fun invoke(params: Unit): List<GenModel_345_> = repository.getAll()
}

class GenSaveUseCase_345_ @Inject constructor(
    private val repository: GenRepositoryImpl_345_
) : GenUseCase_345_<GenModel_345_, GenModel_345_> {
    override suspend fun invoke(params: GenModel_345_): GenModel_345_ = repository.save(params)
}

class GenDeleteUseCase_345_ @Inject constructor(
    private val repository: GenRepositoryImpl_345_
) : GenUseCase_345_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_345_ @Inject constructor(
    private val repository: GenRepositoryImpl_345_
) : GenUseCase_345_<String, List<GenModel_345_>> {
    override suspend fun invoke(params: String): List<GenModel_345_> = repository.search(params)
}

abstract class GenMapper_345_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_345_ : GenMapper_345_<GenModel_345_, String>() {
    override fun map(input: GenModel_345_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_345_ : GenMapper_345_<String, GenModel_345_>() {
    override fun map(input: String): GenModel_345_ {
        val parts = input.split(":")
        return GenModel_345_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_345_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_345_,
    private val saveUseCase: GenSaveUseCase_345_,
    private val deleteUseCase: GenDeleteUseCase_345_,
    private val searchUseCase: GenSearchUseCase_345_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_345_>(GenState_345_.Idle)
    val state: StateFlow<GenState_345_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_345_) {
        when (event) {
            is GenEvent_345_.Load -> loadAll()
            is GenEvent_345_.Update -> save(event.model)
            is GenEvent_345_.Delete -> delete(event.id)
            is GenEvent_345_.Refresh -> loadAll()
            is GenEvent_345_.Search -> search(event.query)
            is GenEvent_345_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_345_.Loading; _state.value = GenState_345_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_345_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_345_.Success(searchUseCase(query)) } }
}
