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

data class GenModel_3241_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3241_ {
    data class Load(val id: Long) : GenEvent_3241_()
    data class Update(val model: GenModel_3241_) : GenEvent_3241_()
    data class Delete(val id: Long) : GenEvent_3241_()
    data object Refresh : GenEvent_3241_()
    data class Search(val query: String) : GenEvent_3241_()
    data class Filter(val predicate: String) : GenEvent_3241_()
}

sealed class GenState_3241_ {
    data object Idle : GenState_3241_()
    data object Loading : GenState_3241_()
    data class Success(val items: List<GenModel_3241_>) : GenState_3241_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3241_()
    data class Partial(val items: List<GenModel_3241_>, val hasMore: Boolean) : GenState_3241_()
}

interface GenRepository_3241_ {
    suspend fun getAll(): List<GenModel_3241_>
    suspend fun getById(id: Long): GenModel_3241_?
    suspend fun save(model: GenModel_3241_): GenModel_3241_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3241_>
}

@Singleton
class GenRepositoryImpl_3241_ @Inject constructor() : GenRepository_3241_ {
    private val store = mutableMapOf<Long, GenModel_3241_>()
    override suspend fun getAll(): List<GenModel_3241_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3241_? = store[id]
    override suspend fun save(model: GenModel_3241_): GenModel_3241_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3241_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3241_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3241_ @Inject constructor(
    private val repository: GenRepositoryImpl_3241_
) : GenUseCase_3241_<Unit, List<GenModel_3241_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3241_> = repository.getAll()
}

class GenSaveUseCase_3241_ @Inject constructor(
    private val repository: GenRepositoryImpl_3241_
) : GenUseCase_3241_<GenModel_3241_, GenModel_3241_> {
    override suspend fun invoke(params: GenModel_3241_): GenModel_3241_ = repository.save(params)
}

class GenDeleteUseCase_3241_ @Inject constructor(
    private val repository: GenRepositoryImpl_3241_
) : GenUseCase_3241_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3241_ @Inject constructor(
    private val repository: GenRepositoryImpl_3241_
) : GenUseCase_3241_<String, List<GenModel_3241_>> {
    override suspend fun invoke(params: String): List<GenModel_3241_> = repository.search(params)
}

abstract class GenMapper_3241_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3241_ : GenMapper_3241_<GenModel_3241_, String>() {
    override fun map(input: GenModel_3241_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3241_ : GenMapper_3241_<String, GenModel_3241_>() {
    override fun map(input: String): GenModel_3241_ {
        val parts = input.split(":")
        return GenModel_3241_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3241_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3241_,
    private val saveUseCase: GenSaveUseCase_3241_,
    private val deleteUseCase: GenDeleteUseCase_3241_,
    private val searchUseCase: GenSearchUseCase_3241_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3241_>(GenState_3241_.Idle)
    val state: StateFlow<GenState_3241_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3241_) {
        when (event) {
            is GenEvent_3241_.Load -> loadAll()
            is GenEvent_3241_.Update -> save(event.model)
            is GenEvent_3241_.Delete -> delete(event.id)
            is GenEvent_3241_.Refresh -> loadAll()
            is GenEvent_3241_.Search -> search(event.query)
            is GenEvent_3241_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3241_.Loading; _state.value = GenState_3241_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3241_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3241_.Success(searchUseCase(query)) } }
}
