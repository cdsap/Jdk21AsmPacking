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

data class GenModel_446_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_446_ {
    data class Load(val id: Long) : GenEvent_446_()
    data class Update(val model: GenModel_446_) : GenEvent_446_()
    data class Delete(val id: Long) : GenEvent_446_()
    data object Refresh : GenEvent_446_()
    data class Search(val query: String) : GenEvent_446_()
    data class Filter(val predicate: String) : GenEvent_446_()
}

sealed class GenState_446_ {
    data object Idle : GenState_446_()
    data object Loading : GenState_446_()
    data class Success(val items: List<GenModel_446_>) : GenState_446_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_446_()
    data class Partial(val items: List<GenModel_446_>, val hasMore: Boolean) : GenState_446_()
}

interface GenRepository_446_ {
    suspend fun getAll(): List<GenModel_446_>
    suspend fun getById(id: Long): GenModel_446_?
    suspend fun save(model: GenModel_446_): GenModel_446_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_446_>
}

@Singleton
class GenRepositoryImpl_446_ @Inject constructor() : GenRepository_446_ {
    private val store = mutableMapOf<Long, GenModel_446_>()
    override suspend fun getAll(): List<GenModel_446_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_446_? = store[id]
    override suspend fun save(model: GenModel_446_): GenModel_446_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_446_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_446_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_446_ @Inject constructor(
    private val repository: GenRepositoryImpl_446_
) : GenUseCase_446_<Unit, List<GenModel_446_>> {
    override suspend fun invoke(params: Unit): List<GenModel_446_> = repository.getAll()
}

class GenSaveUseCase_446_ @Inject constructor(
    private val repository: GenRepositoryImpl_446_
) : GenUseCase_446_<GenModel_446_, GenModel_446_> {
    override suspend fun invoke(params: GenModel_446_): GenModel_446_ = repository.save(params)
}

class GenDeleteUseCase_446_ @Inject constructor(
    private val repository: GenRepositoryImpl_446_
) : GenUseCase_446_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_446_ @Inject constructor(
    private val repository: GenRepositoryImpl_446_
) : GenUseCase_446_<String, List<GenModel_446_>> {
    override suspend fun invoke(params: String): List<GenModel_446_> = repository.search(params)
}

abstract class GenMapper_446_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_446_ : GenMapper_446_<GenModel_446_, String>() {
    override fun map(input: GenModel_446_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_446_ : GenMapper_446_<String, GenModel_446_>() {
    override fun map(input: String): GenModel_446_ {
        val parts = input.split(":")
        return GenModel_446_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_446_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_446_,
    private val saveUseCase: GenSaveUseCase_446_,
    private val deleteUseCase: GenDeleteUseCase_446_,
    private val searchUseCase: GenSearchUseCase_446_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_446_>(GenState_446_.Idle)
    val state: StateFlow<GenState_446_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_446_) {
        when (event) {
            is GenEvent_446_.Load -> loadAll()
            is GenEvent_446_.Update -> save(event.model)
            is GenEvent_446_.Delete -> delete(event.id)
            is GenEvent_446_.Refresh -> loadAll()
            is GenEvent_446_.Search -> search(event.query)
            is GenEvent_446_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_446_.Loading; _state.value = GenState_446_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_446_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_446_.Success(searchUseCase(query)) } }
}
