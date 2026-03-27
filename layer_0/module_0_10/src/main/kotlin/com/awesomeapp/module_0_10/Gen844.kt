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

data class GenModel_844_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_844_ {
    data class Load(val id: Long) : GenEvent_844_()
    data class Update(val model: GenModel_844_) : GenEvent_844_()
    data class Delete(val id: Long) : GenEvent_844_()
    data object Refresh : GenEvent_844_()
    data class Search(val query: String) : GenEvent_844_()
    data class Filter(val predicate: String) : GenEvent_844_()
}

sealed class GenState_844_ {
    data object Idle : GenState_844_()
    data object Loading : GenState_844_()
    data class Success(val items: List<GenModel_844_>) : GenState_844_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_844_()
    data class Partial(val items: List<GenModel_844_>, val hasMore: Boolean) : GenState_844_()
}

interface GenRepository_844_ {
    suspend fun getAll(): List<GenModel_844_>
    suspend fun getById(id: Long): GenModel_844_?
    suspend fun save(model: GenModel_844_): GenModel_844_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_844_>
}

@Singleton
class GenRepositoryImpl_844_ @Inject constructor() : GenRepository_844_ {
    private val store = mutableMapOf<Long, GenModel_844_>()
    override suspend fun getAll(): List<GenModel_844_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_844_? = store[id]
    override suspend fun save(model: GenModel_844_): GenModel_844_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_844_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_844_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_844_ @Inject constructor(
    private val repository: GenRepositoryImpl_844_
) : GenUseCase_844_<Unit, List<GenModel_844_>> {
    override suspend fun invoke(params: Unit): List<GenModel_844_> = repository.getAll()
}

class GenSaveUseCase_844_ @Inject constructor(
    private val repository: GenRepositoryImpl_844_
) : GenUseCase_844_<GenModel_844_, GenModel_844_> {
    override suspend fun invoke(params: GenModel_844_): GenModel_844_ = repository.save(params)
}

class GenDeleteUseCase_844_ @Inject constructor(
    private val repository: GenRepositoryImpl_844_
) : GenUseCase_844_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_844_ @Inject constructor(
    private val repository: GenRepositoryImpl_844_
) : GenUseCase_844_<String, List<GenModel_844_>> {
    override suspend fun invoke(params: String): List<GenModel_844_> = repository.search(params)
}

abstract class GenMapper_844_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_844_ : GenMapper_844_<GenModel_844_, String>() {
    override fun map(input: GenModel_844_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_844_ : GenMapper_844_<String, GenModel_844_>() {
    override fun map(input: String): GenModel_844_ {
        val parts = input.split(":")
        return GenModel_844_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_844_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_844_,
    private val saveUseCase: GenSaveUseCase_844_,
    private val deleteUseCase: GenDeleteUseCase_844_,
    private val searchUseCase: GenSearchUseCase_844_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_844_>(GenState_844_.Idle)
    val state: StateFlow<GenState_844_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_844_) {
        when (event) {
            is GenEvent_844_.Load -> loadAll()
            is GenEvent_844_.Update -> save(event.model)
            is GenEvent_844_.Delete -> delete(event.id)
            is GenEvent_844_.Refresh -> loadAll()
            is GenEvent_844_.Search -> search(event.query)
            is GenEvent_844_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_844_.Loading; _state.value = GenState_844_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_844_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_844_.Success(searchUseCase(query)) } }
}
