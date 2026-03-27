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

data class GenModel_29_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_29_ {
    data class Load(val id: Long) : GenEvent_29_()
    data class Update(val model: GenModel_29_) : GenEvent_29_()
    data class Delete(val id: Long) : GenEvent_29_()
    data object Refresh : GenEvent_29_()
    data class Search(val query: String) : GenEvent_29_()
    data class Filter(val predicate: String) : GenEvent_29_()
}

sealed class GenState_29_ {
    data object Idle : GenState_29_()
    data object Loading : GenState_29_()
    data class Success(val items: List<GenModel_29_>) : GenState_29_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_29_()
    data class Partial(val items: List<GenModel_29_>, val hasMore: Boolean) : GenState_29_()
}

interface GenRepository_29_ {
    suspend fun getAll(): List<GenModel_29_>
    suspend fun getById(id: Long): GenModel_29_?
    suspend fun save(model: GenModel_29_): GenModel_29_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_29_>
}

@Singleton
class GenRepositoryImpl_29_ @Inject constructor() : GenRepository_29_ {
    private val store = mutableMapOf<Long, GenModel_29_>()
    override suspend fun getAll(): List<GenModel_29_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_29_? = store[id]
    override suspend fun save(model: GenModel_29_): GenModel_29_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_29_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_29_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_29_ @Inject constructor(
    private val repository: GenRepositoryImpl_29_
) : GenUseCase_29_<Unit, List<GenModel_29_>> {
    override suspend fun invoke(params: Unit): List<GenModel_29_> = repository.getAll()
}

class GenSaveUseCase_29_ @Inject constructor(
    private val repository: GenRepositoryImpl_29_
) : GenUseCase_29_<GenModel_29_, GenModel_29_> {
    override suspend fun invoke(params: GenModel_29_): GenModel_29_ = repository.save(params)
}

class GenDeleteUseCase_29_ @Inject constructor(
    private val repository: GenRepositoryImpl_29_
) : GenUseCase_29_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_29_ @Inject constructor(
    private val repository: GenRepositoryImpl_29_
) : GenUseCase_29_<String, List<GenModel_29_>> {
    override suspend fun invoke(params: String): List<GenModel_29_> = repository.search(params)
}

abstract class GenMapper_29_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_29_ : GenMapper_29_<GenModel_29_, String>() {
    override fun map(input: GenModel_29_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_29_ : GenMapper_29_<String, GenModel_29_>() {
    override fun map(input: String): GenModel_29_ {
        val parts = input.split(":")
        return GenModel_29_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_29_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_29_,
    private val saveUseCase: GenSaveUseCase_29_,
    private val deleteUseCase: GenDeleteUseCase_29_,
    private val searchUseCase: GenSearchUseCase_29_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_29_>(GenState_29_.Idle)
    val state: StateFlow<GenState_29_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_29_) {
        when (event) {
            is GenEvent_29_.Load -> loadAll()
            is GenEvent_29_.Update -> save(event.model)
            is GenEvent_29_.Delete -> delete(event.id)
            is GenEvent_29_.Refresh -> loadAll()
            is GenEvent_29_.Search -> search(event.query)
            is GenEvent_29_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_29_.Loading; _state.value = GenState_29_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_29_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_29_.Success(searchUseCase(query)) } }
}
