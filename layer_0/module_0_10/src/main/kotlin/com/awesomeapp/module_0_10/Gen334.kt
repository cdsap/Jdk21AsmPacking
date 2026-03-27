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

data class GenModel_334_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_334_ {
    data class Load(val id: Long) : GenEvent_334_()
    data class Update(val model: GenModel_334_) : GenEvent_334_()
    data class Delete(val id: Long) : GenEvent_334_()
    data object Refresh : GenEvent_334_()
    data class Search(val query: String) : GenEvent_334_()
    data class Filter(val predicate: String) : GenEvent_334_()
}

sealed class GenState_334_ {
    data object Idle : GenState_334_()
    data object Loading : GenState_334_()
    data class Success(val items: List<GenModel_334_>) : GenState_334_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_334_()
    data class Partial(val items: List<GenModel_334_>, val hasMore: Boolean) : GenState_334_()
}

interface GenRepository_334_ {
    suspend fun getAll(): List<GenModel_334_>
    suspend fun getById(id: Long): GenModel_334_?
    suspend fun save(model: GenModel_334_): GenModel_334_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_334_>
}

@Singleton
class GenRepositoryImpl_334_ @Inject constructor() : GenRepository_334_ {
    private val store = mutableMapOf<Long, GenModel_334_>()
    override suspend fun getAll(): List<GenModel_334_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_334_? = store[id]
    override suspend fun save(model: GenModel_334_): GenModel_334_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_334_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_334_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_334_ @Inject constructor(
    private val repository: GenRepositoryImpl_334_
) : GenUseCase_334_<Unit, List<GenModel_334_>> {
    override suspend fun invoke(params: Unit): List<GenModel_334_> = repository.getAll()
}

class GenSaveUseCase_334_ @Inject constructor(
    private val repository: GenRepositoryImpl_334_
) : GenUseCase_334_<GenModel_334_, GenModel_334_> {
    override suspend fun invoke(params: GenModel_334_): GenModel_334_ = repository.save(params)
}

class GenDeleteUseCase_334_ @Inject constructor(
    private val repository: GenRepositoryImpl_334_
) : GenUseCase_334_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_334_ @Inject constructor(
    private val repository: GenRepositoryImpl_334_
) : GenUseCase_334_<String, List<GenModel_334_>> {
    override suspend fun invoke(params: String): List<GenModel_334_> = repository.search(params)
}

abstract class GenMapper_334_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_334_ : GenMapper_334_<GenModel_334_, String>() {
    override fun map(input: GenModel_334_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_334_ : GenMapper_334_<String, GenModel_334_>() {
    override fun map(input: String): GenModel_334_ {
        val parts = input.split(":")
        return GenModel_334_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_334_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_334_,
    private val saveUseCase: GenSaveUseCase_334_,
    private val deleteUseCase: GenDeleteUseCase_334_,
    private val searchUseCase: GenSearchUseCase_334_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_334_>(GenState_334_.Idle)
    val state: StateFlow<GenState_334_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_334_) {
        when (event) {
            is GenEvent_334_.Load -> loadAll()
            is GenEvent_334_.Update -> save(event.model)
            is GenEvent_334_.Delete -> delete(event.id)
            is GenEvent_334_.Refresh -> loadAll()
            is GenEvent_334_.Search -> search(event.query)
            is GenEvent_334_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_334_.Loading; _state.value = GenState_334_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_334_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_334_.Success(searchUseCase(query)) } }
}
