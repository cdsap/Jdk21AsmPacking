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

data class GenModel_732_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_732_ {
    data class Load(val id: Long) : GenEvent_732_()
    data class Update(val model: GenModel_732_) : GenEvent_732_()
    data class Delete(val id: Long) : GenEvent_732_()
    data object Refresh : GenEvent_732_()
    data class Search(val query: String) : GenEvent_732_()
    data class Filter(val predicate: String) : GenEvent_732_()
}

sealed class GenState_732_ {
    data object Idle : GenState_732_()
    data object Loading : GenState_732_()
    data class Success(val items: List<GenModel_732_>) : GenState_732_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_732_()
    data class Partial(val items: List<GenModel_732_>, val hasMore: Boolean) : GenState_732_()
}

interface GenRepository_732_ {
    suspend fun getAll(): List<GenModel_732_>
    suspend fun getById(id: Long): GenModel_732_?
    suspend fun save(model: GenModel_732_): GenModel_732_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_732_>
}

@Singleton
class GenRepositoryImpl_732_ @Inject constructor() : GenRepository_732_ {
    private val store = mutableMapOf<Long, GenModel_732_>()
    override suspend fun getAll(): List<GenModel_732_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_732_? = store[id]
    override suspend fun save(model: GenModel_732_): GenModel_732_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_732_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_732_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_732_ @Inject constructor(
    private val repository: GenRepositoryImpl_732_
) : GenUseCase_732_<Unit, List<GenModel_732_>> {
    override suspend fun invoke(params: Unit): List<GenModel_732_> = repository.getAll()
}

class GenSaveUseCase_732_ @Inject constructor(
    private val repository: GenRepositoryImpl_732_
) : GenUseCase_732_<GenModel_732_, GenModel_732_> {
    override suspend fun invoke(params: GenModel_732_): GenModel_732_ = repository.save(params)
}

class GenDeleteUseCase_732_ @Inject constructor(
    private val repository: GenRepositoryImpl_732_
) : GenUseCase_732_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_732_ @Inject constructor(
    private val repository: GenRepositoryImpl_732_
) : GenUseCase_732_<String, List<GenModel_732_>> {
    override suspend fun invoke(params: String): List<GenModel_732_> = repository.search(params)
}

abstract class GenMapper_732_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_732_ : GenMapper_732_<GenModel_732_, String>() {
    override fun map(input: GenModel_732_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_732_ : GenMapper_732_<String, GenModel_732_>() {
    override fun map(input: String): GenModel_732_ {
        val parts = input.split(":")
        return GenModel_732_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_732_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_732_,
    private val saveUseCase: GenSaveUseCase_732_,
    private val deleteUseCase: GenDeleteUseCase_732_,
    private val searchUseCase: GenSearchUseCase_732_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_732_>(GenState_732_.Idle)
    val state: StateFlow<GenState_732_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_732_) {
        when (event) {
            is GenEvent_732_.Load -> loadAll()
            is GenEvent_732_.Update -> save(event.model)
            is GenEvent_732_.Delete -> delete(event.id)
            is GenEvent_732_.Refresh -> loadAll()
            is GenEvent_732_.Search -> search(event.query)
            is GenEvent_732_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_732_.Loading; _state.value = GenState_732_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_732_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_732_.Success(searchUseCase(query)) } }
}
