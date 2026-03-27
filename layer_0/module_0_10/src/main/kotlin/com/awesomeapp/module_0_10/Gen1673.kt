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

data class GenModel_1673_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1673_ {
    data class Load(val id: Long) : GenEvent_1673_()
    data class Update(val model: GenModel_1673_) : GenEvent_1673_()
    data class Delete(val id: Long) : GenEvent_1673_()
    data object Refresh : GenEvent_1673_()
    data class Search(val query: String) : GenEvent_1673_()
    data class Filter(val predicate: String) : GenEvent_1673_()
}

sealed class GenState_1673_ {
    data object Idle : GenState_1673_()
    data object Loading : GenState_1673_()
    data class Success(val items: List<GenModel_1673_>) : GenState_1673_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1673_()
    data class Partial(val items: List<GenModel_1673_>, val hasMore: Boolean) : GenState_1673_()
}

interface GenRepository_1673_ {
    suspend fun getAll(): List<GenModel_1673_>
    suspend fun getById(id: Long): GenModel_1673_?
    suspend fun save(model: GenModel_1673_): GenModel_1673_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1673_>
}

@Singleton
class GenRepositoryImpl_1673_ @Inject constructor() : GenRepository_1673_ {
    private val store = mutableMapOf<Long, GenModel_1673_>()
    override suspend fun getAll(): List<GenModel_1673_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1673_? = store[id]
    override suspend fun save(model: GenModel_1673_): GenModel_1673_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1673_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1673_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1673_ @Inject constructor(
    private val repository: GenRepositoryImpl_1673_
) : GenUseCase_1673_<Unit, List<GenModel_1673_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1673_> = repository.getAll()
}

class GenSaveUseCase_1673_ @Inject constructor(
    private val repository: GenRepositoryImpl_1673_
) : GenUseCase_1673_<GenModel_1673_, GenModel_1673_> {
    override suspend fun invoke(params: GenModel_1673_): GenModel_1673_ = repository.save(params)
}

class GenDeleteUseCase_1673_ @Inject constructor(
    private val repository: GenRepositoryImpl_1673_
) : GenUseCase_1673_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1673_ @Inject constructor(
    private val repository: GenRepositoryImpl_1673_
) : GenUseCase_1673_<String, List<GenModel_1673_>> {
    override suspend fun invoke(params: String): List<GenModel_1673_> = repository.search(params)
}

abstract class GenMapper_1673_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1673_ : GenMapper_1673_<GenModel_1673_, String>() {
    override fun map(input: GenModel_1673_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1673_ : GenMapper_1673_<String, GenModel_1673_>() {
    override fun map(input: String): GenModel_1673_ {
        val parts = input.split(":")
        return GenModel_1673_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1673_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1673_,
    private val saveUseCase: GenSaveUseCase_1673_,
    private val deleteUseCase: GenDeleteUseCase_1673_,
    private val searchUseCase: GenSearchUseCase_1673_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1673_>(GenState_1673_.Idle)
    val state: StateFlow<GenState_1673_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1673_) {
        when (event) {
            is GenEvent_1673_.Load -> loadAll()
            is GenEvent_1673_.Update -> save(event.model)
            is GenEvent_1673_.Delete -> delete(event.id)
            is GenEvent_1673_.Refresh -> loadAll()
            is GenEvent_1673_.Search -> search(event.query)
            is GenEvent_1673_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1673_.Loading; _state.value = GenState_1673_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1673_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1673_.Success(searchUseCase(query)) } }
}
