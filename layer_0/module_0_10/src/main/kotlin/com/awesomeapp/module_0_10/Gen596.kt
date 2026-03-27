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

data class GenModel_596_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_596_ {
    data class Load(val id: Long) : GenEvent_596_()
    data class Update(val model: GenModel_596_) : GenEvent_596_()
    data class Delete(val id: Long) : GenEvent_596_()
    data object Refresh : GenEvent_596_()
    data class Search(val query: String) : GenEvent_596_()
    data class Filter(val predicate: String) : GenEvent_596_()
}

sealed class GenState_596_ {
    data object Idle : GenState_596_()
    data object Loading : GenState_596_()
    data class Success(val items: List<GenModel_596_>) : GenState_596_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_596_()
    data class Partial(val items: List<GenModel_596_>, val hasMore: Boolean) : GenState_596_()
}

interface GenRepository_596_ {
    suspend fun getAll(): List<GenModel_596_>
    suspend fun getById(id: Long): GenModel_596_?
    suspend fun save(model: GenModel_596_): GenModel_596_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_596_>
}

@Singleton
class GenRepositoryImpl_596_ @Inject constructor() : GenRepository_596_ {
    private val store = mutableMapOf<Long, GenModel_596_>()
    override suspend fun getAll(): List<GenModel_596_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_596_? = store[id]
    override suspend fun save(model: GenModel_596_): GenModel_596_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_596_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_596_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_596_ @Inject constructor(
    private val repository: GenRepositoryImpl_596_
) : GenUseCase_596_<Unit, List<GenModel_596_>> {
    override suspend fun invoke(params: Unit): List<GenModel_596_> = repository.getAll()
}

class GenSaveUseCase_596_ @Inject constructor(
    private val repository: GenRepositoryImpl_596_
) : GenUseCase_596_<GenModel_596_, GenModel_596_> {
    override suspend fun invoke(params: GenModel_596_): GenModel_596_ = repository.save(params)
}

class GenDeleteUseCase_596_ @Inject constructor(
    private val repository: GenRepositoryImpl_596_
) : GenUseCase_596_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_596_ @Inject constructor(
    private val repository: GenRepositoryImpl_596_
) : GenUseCase_596_<String, List<GenModel_596_>> {
    override suspend fun invoke(params: String): List<GenModel_596_> = repository.search(params)
}

abstract class GenMapper_596_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_596_ : GenMapper_596_<GenModel_596_, String>() {
    override fun map(input: GenModel_596_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_596_ : GenMapper_596_<String, GenModel_596_>() {
    override fun map(input: String): GenModel_596_ {
        val parts = input.split(":")
        return GenModel_596_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_596_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_596_,
    private val saveUseCase: GenSaveUseCase_596_,
    private val deleteUseCase: GenDeleteUseCase_596_,
    private val searchUseCase: GenSearchUseCase_596_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_596_>(GenState_596_.Idle)
    val state: StateFlow<GenState_596_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_596_) {
        when (event) {
            is GenEvent_596_.Load -> loadAll()
            is GenEvent_596_.Update -> save(event.model)
            is GenEvent_596_.Delete -> delete(event.id)
            is GenEvent_596_.Refresh -> loadAll()
            is GenEvent_596_.Search -> search(event.query)
            is GenEvent_596_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_596_.Loading; _state.value = GenState_596_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_596_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_596_.Success(searchUseCase(query)) } }
}
