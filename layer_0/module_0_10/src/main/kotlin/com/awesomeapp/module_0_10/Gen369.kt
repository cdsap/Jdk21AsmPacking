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

data class GenModel_369_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_369_ {
    data class Load(val id: Long) : GenEvent_369_()
    data class Update(val model: GenModel_369_) : GenEvent_369_()
    data class Delete(val id: Long) : GenEvent_369_()
    data object Refresh : GenEvent_369_()
    data class Search(val query: String) : GenEvent_369_()
    data class Filter(val predicate: String) : GenEvent_369_()
}

sealed class GenState_369_ {
    data object Idle : GenState_369_()
    data object Loading : GenState_369_()
    data class Success(val items: List<GenModel_369_>) : GenState_369_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_369_()
    data class Partial(val items: List<GenModel_369_>, val hasMore: Boolean) : GenState_369_()
}

interface GenRepository_369_ {
    suspend fun getAll(): List<GenModel_369_>
    suspend fun getById(id: Long): GenModel_369_?
    suspend fun save(model: GenModel_369_): GenModel_369_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_369_>
}

@Singleton
class GenRepositoryImpl_369_ @Inject constructor() : GenRepository_369_ {
    private val store = mutableMapOf<Long, GenModel_369_>()
    override suspend fun getAll(): List<GenModel_369_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_369_? = store[id]
    override suspend fun save(model: GenModel_369_): GenModel_369_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_369_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_369_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_369_ @Inject constructor(
    private val repository: GenRepositoryImpl_369_
) : GenUseCase_369_<Unit, List<GenModel_369_>> {
    override suspend fun invoke(params: Unit): List<GenModel_369_> = repository.getAll()
}

class GenSaveUseCase_369_ @Inject constructor(
    private val repository: GenRepositoryImpl_369_
) : GenUseCase_369_<GenModel_369_, GenModel_369_> {
    override suspend fun invoke(params: GenModel_369_): GenModel_369_ = repository.save(params)
}

class GenDeleteUseCase_369_ @Inject constructor(
    private val repository: GenRepositoryImpl_369_
) : GenUseCase_369_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_369_ @Inject constructor(
    private val repository: GenRepositoryImpl_369_
) : GenUseCase_369_<String, List<GenModel_369_>> {
    override suspend fun invoke(params: String): List<GenModel_369_> = repository.search(params)
}

abstract class GenMapper_369_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_369_ : GenMapper_369_<GenModel_369_, String>() {
    override fun map(input: GenModel_369_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_369_ : GenMapper_369_<String, GenModel_369_>() {
    override fun map(input: String): GenModel_369_ {
        val parts = input.split(":")
        return GenModel_369_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_369_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_369_,
    private val saveUseCase: GenSaveUseCase_369_,
    private val deleteUseCase: GenDeleteUseCase_369_,
    private val searchUseCase: GenSearchUseCase_369_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_369_>(GenState_369_.Idle)
    val state: StateFlow<GenState_369_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_369_) {
        when (event) {
            is GenEvent_369_.Load -> loadAll()
            is GenEvent_369_.Update -> save(event.model)
            is GenEvent_369_.Delete -> delete(event.id)
            is GenEvent_369_.Refresh -> loadAll()
            is GenEvent_369_.Search -> search(event.query)
            is GenEvent_369_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_369_.Loading; _state.value = GenState_369_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_369_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_369_.Success(searchUseCase(query)) } }
}
