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

data class GenModel_612_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_612_ {
    data class Load(val id: Long) : GenEvent_612_()
    data class Update(val model: GenModel_612_) : GenEvent_612_()
    data class Delete(val id: Long) : GenEvent_612_()
    data object Refresh : GenEvent_612_()
    data class Search(val query: String) : GenEvent_612_()
    data class Filter(val predicate: String) : GenEvent_612_()
}

sealed class GenState_612_ {
    data object Idle : GenState_612_()
    data object Loading : GenState_612_()
    data class Success(val items: List<GenModel_612_>) : GenState_612_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_612_()
    data class Partial(val items: List<GenModel_612_>, val hasMore: Boolean) : GenState_612_()
}

interface GenRepository_612_ {
    suspend fun getAll(): List<GenModel_612_>
    suspend fun getById(id: Long): GenModel_612_?
    suspend fun save(model: GenModel_612_): GenModel_612_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_612_>
}

@Singleton
class GenRepositoryImpl_612_ @Inject constructor() : GenRepository_612_ {
    private val store = mutableMapOf<Long, GenModel_612_>()
    override suspend fun getAll(): List<GenModel_612_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_612_? = store[id]
    override suspend fun save(model: GenModel_612_): GenModel_612_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_612_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_612_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_612_ @Inject constructor(
    private val repository: GenRepositoryImpl_612_
) : GenUseCase_612_<Unit, List<GenModel_612_>> {
    override suspend fun invoke(params: Unit): List<GenModel_612_> = repository.getAll()
}

class GenSaveUseCase_612_ @Inject constructor(
    private val repository: GenRepositoryImpl_612_
) : GenUseCase_612_<GenModel_612_, GenModel_612_> {
    override suspend fun invoke(params: GenModel_612_): GenModel_612_ = repository.save(params)
}

class GenDeleteUseCase_612_ @Inject constructor(
    private val repository: GenRepositoryImpl_612_
) : GenUseCase_612_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_612_ @Inject constructor(
    private val repository: GenRepositoryImpl_612_
) : GenUseCase_612_<String, List<GenModel_612_>> {
    override suspend fun invoke(params: String): List<GenModel_612_> = repository.search(params)
}

abstract class GenMapper_612_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_612_ : GenMapper_612_<GenModel_612_, String>() {
    override fun map(input: GenModel_612_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_612_ : GenMapper_612_<String, GenModel_612_>() {
    override fun map(input: String): GenModel_612_ {
        val parts = input.split(":")
        return GenModel_612_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_612_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_612_,
    private val saveUseCase: GenSaveUseCase_612_,
    private val deleteUseCase: GenDeleteUseCase_612_,
    private val searchUseCase: GenSearchUseCase_612_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_612_>(GenState_612_.Idle)
    val state: StateFlow<GenState_612_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_612_) {
        when (event) {
            is GenEvent_612_.Load -> loadAll()
            is GenEvent_612_.Update -> save(event.model)
            is GenEvent_612_.Delete -> delete(event.id)
            is GenEvent_612_.Refresh -> loadAll()
            is GenEvent_612_.Search -> search(event.query)
            is GenEvent_612_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_612_.Loading; _state.value = GenState_612_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_612_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_612_.Success(searchUseCase(query)) } }
}
