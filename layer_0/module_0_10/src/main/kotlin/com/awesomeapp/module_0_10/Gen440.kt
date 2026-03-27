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

data class GenModel_440_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_440_ {
    data class Load(val id: Long) : GenEvent_440_()
    data class Update(val model: GenModel_440_) : GenEvent_440_()
    data class Delete(val id: Long) : GenEvent_440_()
    data object Refresh : GenEvent_440_()
    data class Search(val query: String) : GenEvent_440_()
    data class Filter(val predicate: String) : GenEvent_440_()
}

sealed class GenState_440_ {
    data object Idle : GenState_440_()
    data object Loading : GenState_440_()
    data class Success(val items: List<GenModel_440_>) : GenState_440_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_440_()
    data class Partial(val items: List<GenModel_440_>, val hasMore: Boolean) : GenState_440_()
}

interface GenRepository_440_ {
    suspend fun getAll(): List<GenModel_440_>
    suspend fun getById(id: Long): GenModel_440_?
    suspend fun save(model: GenModel_440_): GenModel_440_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_440_>
}

@Singleton
class GenRepositoryImpl_440_ @Inject constructor() : GenRepository_440_ {
    private val store = mutableMapOf<Long, GenModel_440_>()
    override suspend fun getAll(): List<GenModel_440_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_440_? = store[id]
    override suspend fun save(model: GenModel_440_): GenModel_440_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_440_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_440_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_440_ @Inject constructor(
    private val repository: GenRepositoryImpl_440_
) : GenUseCase_440_<Unit, List<GenModel_440_>> {
    override suspend fun invoke(params: Unit): List<GenModel_440_> = repository.getAll()
}

class GenSaveUseCase_440_ @Inject constructor(
    private val repository: GenRepositoryImpl_440_
) : GenUseCase_440_<GenModel_440_, GenModel_440_> {
    override suspend fun invoke(params: GenModel_440_): GenModel_440_ = repository.save(params)
}

class GenDeleteUseCase_440_ @Inject constructor(
    private val repository: GenRepositoryImpl_440_
) : GenUseCase_440_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_440_ @Inject constructor(
    private val repository: GenRepositoryImpl_440_
) : GenUseCase_440_<String, List<GenModel_440_>> {
    override suspend fun invoke(params: String): List<GenModel_440_> = repository.search(params)
}

abstract class GenMapper_440_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_440_ : GenMapper_440_<GenModel_440_, String>() {
    override fun map(input: GenModel_440_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_440_ : GenMapper_440_<String, GenModel_440_>() {
    override fun map(input: String): GenModel_440_ {
        val parts = input.split(":")
        return GenModel_440_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_440_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_440_,
    private val saveUseCase: GenSaveUseCase_440_,
    private val deleteUseCase: GenDeleteUseCase_440_,
    private val searchUseCase: GenSearchUseCase_440_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_440_>(GenState_440_.Idle)
    val state: StateFlow<GenState_440_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_440_) {
        when (event) {
            is GenEvent_440_.Load -> loadAll()
            is GenEvent_440_.Update -> save(event.model)
            is GenEvent_440_.Delete -> delete(event.id)
            is GenEvent_440_.Refresh -> loadAll()
            is GenEvent_440_.Search -> search(event.query)
            is GenEvent_440_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_440_.Loading; _state.value = GenState_440_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_440_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_440_.Success(searchUseCase(query)) } }
}
