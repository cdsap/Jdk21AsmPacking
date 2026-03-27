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

data class GenModel_180_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_180_ {
    data class Load(val id: Long) : GenEvent_180_()
    data class Update(val model: GenModel_180_) : GenEvent_180_()
    data class Delete(val id: Long) : GenEvent_180_()
    data object Refresh : GenEvent_180_()
    data class Search(val query: String) : GenEvent_180_()
    data class Filter(val predicate: String) : GenEvent_180_()
}

sealed class GenState_180_ {
    data object Idle : GenState_180_()
    data object Loading : GenState_180_()
    data class Success(val items: List<GenModel_180_>) : GenState_180_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_180_()
    data class Partial(val items: List<GenModel_180_>, val hasMore: Boolean) : GenState_180_()
}

interface GenRepository_180_ {
    suspend fun getAll(): List<GenModel_180_>
    suspend fun getById(id: Long): GenModel_180_?
    suspend fun save(model: GenModel_180_): GenModel_180_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_180_>
}

@Singleton
class GenRepositoryImpl_180_ @Inject constructor() : GenRepository_180_ {
    private val store = mutableMapOf<Long, GenModel_180_>()
    override suspend fun getAll(): List<GenModel_180_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_180_? = store[id]
    override suspend fun save(model: GenModel_180_): GenModel_180_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_180_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_180_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_180_ @Inject constructor(
    private val repository: GenRepositoryImpl_180_
) : GenUseCase_180_<Unit, List<GenModel_180_>> {
    override suspend fun invoke(params: Unit): List<GenModel_180_> = repository.getAll()
}

class GenSaveUseCase_180_ @Inject constructor(
    private val repository: GenRepositoryImpl_180_
) : GenUseCase_180_<GenModel_180_, GenModel_180_> {
    override suspend fun invoke(params: GenModel_180_): GenModel_180_ = repository.save(params)
}

class GenDeleteUseCase_180_ @Inject constructor(
    private val repository: GenRepositoryImpl_180_
) : GenUseCase_180_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_180_ @Inject constructor(
    private val repository: GenRepositoryImpl_180_
) : GenUseCase_180_<String, List<GenModel_180_>> {
    override suspend fun invoke(params: String): List<GenModel_180_> = repository.search(params)
}

abstract class GenMapper_180_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_180_ : GenMapper_180_<GenModel_180_, String>() {
    override fun map(input: GenModel_180_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_180_ : GenMapper_180_<String, GenModel_180_>() {
    override fun map(input: String): GenModel_180_ {
        val parts = input.split(":")
        return GenModel_180_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_180_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_180_,
    private val saveUseCase: GenSaveUseCase_180_,
    private val deleteUseCase: GenDeleteUseCase_180_,
    private val searchUseCase: GenSearchUseCase_180_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_180_>(GenState_180_.Idle)
    val state: StateFlow<GenState_180_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_180_) {
        when (event) {
            is GenEvent_180_.Load -> loadAll()
            is GenEvent_180_.Update -> save(event.model)
            is GenEvent_180_.Delete -> delete(event.id)
            is GenEvent_180_.Refresh -> loadAll()
            is GenEvent_180_.Search -> search(event.query)
            is GenEvent_180_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_180_.Loading; _state.value = GenState_180_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_180_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_180_.Success(searchUseCase(query)) } }
}
