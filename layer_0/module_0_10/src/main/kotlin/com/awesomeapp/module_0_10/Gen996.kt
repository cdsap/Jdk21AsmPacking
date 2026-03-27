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

data class GenModel_996_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_996_ {
    data class Load(val id: Long) : GenEvent_996_()
    data class Update(val model: GenModel_996_) : GenEvent_996_()
    data class Delete(val id: Long) : GenEvent_996_()
    data object Refresh : GenEvent_996_()
    data class Search(val query: String) : GenEvent_996_()
    data class Filter(val predicate: String) : GenEvent_996_()
}

sealed class GenState_996_ {
    data object Idle : GenState_996_()
    data object Loading : GenState_996_()
    data class Success(val items: List<GenModel_996_>) : GenState_996_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_996_()
    data class Partial(val items: List<GenModel_996_>, val hasMore: Boolean) : GenState_996_()
}

interface GenRepository_996_ {
    suspend fun getAll(): List<GenModel_996_>
    suspend fun getById(id: Long): GenModel_996_?
    suspend fun save(model: GenModel_996_): GenModel_996_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_996_>
}

@Singleton
class GenRepositoryImpl_996_ @Inject constructor() : GenRepository_996_ {
    private val store = mutableMapOf<Long, GenModel_996_>()
    override suspend fun getAll(): List<GenModel_996_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_996_? = store[id]
    override suspend fun save(model: GenModel_996_): GenModel_996_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_996_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_996_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_996_ @Inject constructor(
    private val repository: GenRepositoryImpl_996_
) : GenUseCase_996_<Unit, List<GenModel_996_>> {
    override suspend fun invoke(params: Unit): List<GenModel_996_> = repository.getAll()
}

class GenSaveUseCase_996_ @Inject constructor(
    private val repository: GenRepositoryImpl_996_
) : GenUseCase_996_<GenModel_996_, GenModel_996_> {
    override suspend fun invoke(params: GenModel_996_): GenModel_996_ = repository.save(params)
}

class GenDeleteUseCase_996_ @Inject constructor(
    private val repository: GenRepositoryImpl_996_
) : GenUseCase_996_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_996_ @Inject constructor(
    private val repository: GenRepositoryImpl_996_
) : GenUseCase_996_<String, List<GenModel_996_>> {
    override suspend fun invoke(params: String): List<GenModel_996_> = repository.search(params)
}

abstract class GenMapper_996_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_996_ : GenMapper_996_<GenModel_996_, String>() {
    override fun map(input: GenModel_996_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_996_ : GenMapper_996_<String, GenModel_996_>() {
    override fun map(input: String): GenModel_996_ {
        val parts = input.split(":")
        return GenModel_996_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_996_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_996_,
    private val saveUseCase: GenSaveUseCase_996_,
    private val deleteUseCase: GenDeleteUseCase_996_,
    private val searchUseCase: GenSearchUseCase_996_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_996_>(GenState_996_.Idle)
    val state: StateFlow<GenState_996_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_996_) {
        when (event) {
            is GenEvent_996_.Load -> loadAll()
            is GenEvent_996_.Update -> save(event.model)
            is GenEvent_996_.Delete -> delete(event.id)
            is GenEvent_996_.Refresh -> loadAll()
            is GenEvent_996_.Search -> search(event.query)
            is GenEvent_996_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_996_.Loading; _state.value = GenState_996_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_996_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_996_.Success(searchUseCase(query)) } }
}
