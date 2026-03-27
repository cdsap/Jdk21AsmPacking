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

data class GenModel_121_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_121_ {
    data class Load(val id: Long) : GenEvent_121_()
    data class Update(val model: GenModel_121_) : GenEvent_121_()
    data class Delete(val id: Long) : GenEvent_121_()
    data object Refresh : GenEvent_121_()
    data class Search(val query: String) : GenEvent_121_()
    data class Filter(val predicate: String) : GenEvent_121_()
}

sealed class GenState_121_ {
    data object Idle : GenState_121_()
    data object Loading : GenState_121_()
    data class Success(val items: List<GenModel_121_>) : GenState_121_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_121_()
    data class Partial(val items: List<GenModel_121_>, val hasMore: Boolean) : GenState_121_()
}

interface GenRepository_121_ {
    suspend fun getAll(): List<GenModel_121_>
    suspend fun getById(id: Long): GenModel_121_?
    suspend fun save(model: GenModel_121_): GenModel_121_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_121_>
}

@Singleton
class GenRepositoryImpl_121_ @Inject constructor() : GenRepository_121_ {
    private val store = mutableMapOf<Long, GenModel_121_>()
    override suspend fun getAll(): List<GenModel_121_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_121_? = store[id]
    override suspend fun save(model: GenModel_121_): GenModel_121_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_121_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_121_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_121_ @Inject constructor(
    private val repository: GenRepositoryImpl_121_
) : GenUseCase_121_<Unit, List<GenModel_121_>> {
    override suspend fun invoke(params: Unit): List<GenModel_121_> = repository.getAll()
}

class GenSaveUseCase_121_ @Inject constructor(
    private val repository: GenRepositoryImpl_121_
) : GenUseCase_121_<GenModel_121_, GenModel_121_> {
    override suspend fun invoke(params: GenModel_121_): GenModel_121_ = repository.save(params)
}

class GenDeleteUseCase_121_ @Inject constructor(
    private val repository: GenRepositoryImpl_121_
) : GenUseCase_121_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_121_ @Inject constructor(
    private val repository: GenRepositoryImpl_121_
) : GenUseCase_121_<String, List<GenModel_121_>> {
    override suspend fun invoke(params: String): List<GenModel_121_> = repository.search(params)
}

abstract class GenMapper_121_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_121_ : GenMapper_121_<GenModel_121_, String>() {
    override fun map(input: GenModel_121_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_121_ : GenMapper_121_<String, GenModel_121_>() {
    override fun map(input: String): GenModel_121_ {
        val parts = input.split(":")
        return GenModel_121_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_121_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_121_,
    private val saveUseCase: GenSaveUseCase_121_,
    private val deleteUseCase: GenDeleteUseCase_121_,
    private val searchUseCase: GenSearchUseCase_121_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_121_>(GenState_121_.Idle)
    val state: StateFlow<GenState_121_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_121_) {
        when (event) {
            is GenEvent_121_.Load -> loadAll()
            is GenEvent_121_.Update -> save(event.model)
            is GenEvent_121_.Delete -> delete(event.id)
            is GenEvent_121_.Refresh -> loadAll()
            is GenEvent_121_.Search -> search(event.query)
            is GenEvent_121_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_121_.Loading; _state.value = GenState_121_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_121_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_121_.Success(searchUseCase(query)) } }
}
