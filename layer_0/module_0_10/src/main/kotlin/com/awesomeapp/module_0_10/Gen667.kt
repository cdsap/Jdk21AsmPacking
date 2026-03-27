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

data class GenModel_667_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_667_ {
    data class Load(val id: Long) : GenEvent_667_()
    data class Update(val model: GenModel_667_) : GenEvent_667_()
    data class Delete(val id: Long) : GenEvent_667_()
    data object Refresh : GenEvent_667_()
    data class Search(val query: String) : GenEvent_667_()
    data class Filter(val predicate: String) : GenEvent_667_()
}

sealed class GenState_667_ {
    data object Idle : GenState_667_()
    data object Loading : GenState_667_()
    data class Success(val items: List<GenModel_667_>) : GenState_667_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_667_()
    data class Partial(val items: List<GenModel_667_>, val hasMore: Boolean) : GenState_667_()
}

interface GenRepository_667_ {
    suspend fun getAll(): List<GenModel_667_>
    suspend fun getById(id: Long): GenModel_667_?
    suspend fun save(model: GenModel_667_): GenModel_667_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_667_>
}

@Singleton
class GenRepositoryImpl_667_ @Inject constructor() : GenRepository_667_ {
    private val store = mutableMapOf<Long, GenModel_667_>()
    override suspend fun getAll(): List<GenModel_667_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_667_? = store[id]
    override suspend fun save(model: GenModel_667_): GenModel_667_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_667_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_667_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_667_ @Inject constructor(
    private val repository: GenRepositoryImpl_667_
) : GenUseCase_667_<Unit, List<GenModel_667_>> {
    override suspend fun invoke(params: Unit): List<GenModel_667_> = repository.getAll()
}

class GenSaveUseCase_667_ @Inject constructor(
    private val repository: GenRepositoryImpl_667_
) : GenUseCase_667_<GenModel_667_, GenModel_667_> {
    override suspend fun invoke(params: GenModel_667_): GenModel_667_ = repository.save(params)
}

class GenDeleteUseCase_667_ @Inject constructor(
    private val repository: GenRepositoryImpl_667_
) : GenUseCase_667_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_667_ @Inject constructor(
    private val repository: GenRepositoryImpl_667_
) : GenUseCase_667_<String, List<GenModel_667_>> {
    override suspend fun invoke(params: String): List<GenModel_667_> = repository.search(params)
}

abstract class GenMapper_667_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_667_ : GenMapper_667_<GenModel_667_, String>() {
    override fun map(input: GenModel_667_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_667_ : GenMapper_667_<String, GenModel_667_>() {
    override fun map(input: String): GenModel_667_ {
        val parts = input.split(":")
        return GenModel_667_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_667_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_667_,
    private val saveUseCase: GenSaveUseCase_667_,
    private val deleteUseCase: GenDeleteUseCase_667_,
    private val searchUseCase: GenSearchUseCase_667_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_667_>(GenState_667_.Idle)
    val state: StateFlow<GenState_667_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_667_) {
        when (event) {
            is GenEvent_667_.Load -> loadAll()
            is GenEvent_667_.Update -> save(event.model)
            is GenEvent_667_.Delete -> delete(event.id)
            is GenEvent_667_.Refresh -> loadAll()
            is GenEvent_667_.Search -> search(event.query)
            is GenEvent_667_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_667_.Loading; _state.value = GenState_667_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_667_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_667_.Success(searchUseCase(query)) } }
}
