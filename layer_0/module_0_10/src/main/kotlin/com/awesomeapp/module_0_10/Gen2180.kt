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

data class GenModel_2180_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2180_ {
    data class Load(val id: Long) : GenEvent_2180_()
    data class Update(val model: GenModel_2180_) : GenEvent_2180_()
    data class Delete(val id: Long) : GenEvent_2180_()
    data object Refresh : GenEvent_2180_()
    data class Search(val query: String) : GenEvent_2180_()
    data class Filter(val predicate: String) : GenEvent_2180_()
}

sealed class GenState_2180_ {
    data object Idle : GenState_2180_()
    data object Loading : GenState_2180_()
    data class Success(val items: List<GenModel_2180_>) : GenState_2180_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2180_()
    data class Partial(val items: List<GenModel_2180_>, val hasMore: Boolean) : GenState_2180_()
}

interface GenRepository_2180_ {
    suspend fun getAll(): List<GenModel_2180_>
    suspend fun getById(id: Long): GenModel_2180_?
    suspend fun save(model: GenModel_2180_): GenModel_2180_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2180_>
}

@Singleton
class GenRepositoryImpl_2180_ @Inject constructor() : GenRepository_2180_ {
    private val store = mutableMapOf<Long, GenModel_2180_>()
    override suspend fun getAll(): List<GenModel_2180_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2180_? = store[id]
    override suspend fun save(model: GenModel_2180_): GenModel_2180_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2180_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2180_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2180_ @Inject constructor(
    private val repository: GenRepositoryImpl_2180_
) : GenUseCase_2180_<Unit, List<GenModel_2180_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2180_> = repository.getAll()
}

class GenSaveUseCase_2180_ @Inject constructor(
    private val repository: GenRepositoryImpl_2180_
) : GenUseCase_2180_<GenModel_2180_, GenModel_2180_> {
    override suspend fun invoke(params: GenModel_2180_): GenModel_2180_ = repository.save(params)
}

class GenDeleteUseCase_2180_ @Inject constructor(
    private val repository: GenRepositoryImpl_2180_
) : GenUseCase_2180_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2180_ @Inject constructor(
    private val repository: GenRepositoryImpl_2180_
) : GenUseCase_2180_<String, List<GenModel_2180_>> {
    override suspend fun invoke(params: String): List<GenModel_2180_> = repository.search(params)
}

abstract class GenMapper_2180_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2180_ : GenMapper_2180_<GenModel_2180_, String>() {
    override fun map(input: GenModel_2180_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2180_ : GenMapper_2180_<String, GenModel_2180_>() {
    override fun map(input: String): GenModel_2180_ {
        val parts = input.split(":")
        return GenModel_2180_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2180_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2180_,
    private val saveUseCase: GenSaveUseCase_2180_,
    private val deleteUseCase: GenDeleteUseCase_2180_,
    private val searchUseCase: GenSearchUseCase_2180_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2180_>(GenState_2180_.Idle)
    val state: StateFlow<GenState_2180_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2180_) {
        when (event) {
            is GenEvent_2180_.Load -> loadAll()
            is GenEvent_2180_.Update -> save(event.model)
            is GenEvent_2180_.Delete -> delete(event.id)
            is GenEvent_2180_.Refresh -> loadAll()
            is GenEvent_2180_.Search -> search(event.query)
            is GenEvent_2180_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2180_.Loading; _state.value = GenState_2180_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2180_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2180_.Success(searchUseCase(query)) } }
}
