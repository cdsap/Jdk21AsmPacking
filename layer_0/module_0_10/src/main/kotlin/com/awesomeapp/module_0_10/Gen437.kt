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

data class GenModel_437_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_437_ {
    data class Load(val id: Long) : GenEvent_437_()
    data class Update(val model: GenModel_437_) : GenEvent_437_()
    data class Delete(val id: Long) : GenEvent_437_()
    data object Refresh : GenEvent_437_()
    data class Search(val query: String) : GenEvent_437_()
    data class Filter(val predicate: String) : GenEvent_437_()
}

sealed class GenState_437_ {
    data object Idle : GenState_437_()
    data object Loading : GenState_437_()
    data class Success(val items: List<GenModel_437_>) : GenState_437_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_437_()
    data class Partial(val items: List<GenModel_437_>, val hasMore: Boolean) : GenState_437_()
}

interface GenRepository_437_ {
    suspend fun getAll(): List<GenModel_437_>
    suspend fun getById(id: Long): GenModel_437_?
    suspend fun save(model: GenModel_437_): GenModel_437_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_437_>
}

@Singleton
class GenRepositoryImpl_437_ @Inject constructor() : GenRepository_437_ {
    private val store = mutableMapOf<Long, GenModel_437_>()
    override suspend fun getAll(): List<GenModel_437_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_437_? = store[id]
    override suspend fun save(model: GenModel_437_): GenModel_437_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_437_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_437_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_437_ @Inject constructor(
    private val repository: GenRepositoryImpl_437_
) : GenUseCase_437_<Unit, List<GenModel_437_>> {
    override suspend fun invoke(params: Unit): List<GenModel_437_> = repository.getAll()
}

class GenSaveUseCase_437_ @Inject constructor(
    private val repository: GenRepositoryImpl_437_
) : GenUseCase_437_<GenModel_437_, GenModel_437_> {
    override suspend fun invoke(params: GenModel_437_): GenModel_437_ = repository.save(params)
}

class GenDeleteUseCase_437_ @Inject constructor(
    private val repository: GenRepositoryImpl_437_
) : GenUseCase_437_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_437_ @Inject constructor(
    private val repository: GenRepositoryImpl_437_
) : GenUseCase_437_<String, List<GenModel_437_>> {
    override suspend fun invoke(params: String): List<GenModel_437_> = repository.search(params)
}

abstract class GenMapper_437_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_437_ : GenMapper_437_<GenModel_437_, String>() {
    override fun map(input: GenModel_437_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_437_ : GenMapper_437_<String, GenModel_437_>() {
    override fun map(input: String): GenModel_437_ {
        val parts = input.split(":")
        return GenModel_437_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_437_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_437_,
    private val saveUseCase: GenSaveUseCase_437_,
    private val deleteUseCase: GenDeleteUseCase_437_,
    private val searchUseCase: GenSearchUseCase_437_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_437_>(GenState_437_.Idle)
    val state: StateFlow<GenState_437_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_437_) {
        when (event) {
            is GenEvent_437_.Load -> loadAll()
            is GenEvent_437_.Update -> save(event.model)
            is GenEvent_437_.Delete -> delete(event.id)
            is GenEvent_437_.Refresh -> loadAll()
            is GenEvent_437_.Search -> search(event.query)
            is GenEvent_437_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_437_.Loading; _state.value = GenState_437_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_437_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_437_.Success(searchUseCase(query)) } }
}
