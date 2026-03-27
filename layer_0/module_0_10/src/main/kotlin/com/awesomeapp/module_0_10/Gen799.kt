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

data class GenModel_799_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_799_ {
    data class Load(val id: Long) : GenEvent_799_()
    data class Update(val model: GenModel_799_) : GenEvent_799_()
    data class Delete(val id: Long) : GenEvent_799_()
    data object Refresh : GenEvent_799_()
    data class Search(val query: String) : GenEvent_799_()
    data class Filter(val predicate: String) : GenEvent_799_()
}

sealed class GenState_799_ {
    data object Idle : GenState_799_()
    data object Loading : GenState_799_()
    data class Success(val items: List<GenModel_799_>) : GenState_799_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_799_()
    data class Partial(val items: List<GenModel_799_>, val hasMore: Boolean) : GenState_799_()
}

interface GenRepository_799_ {
    suspend fun getAll(): List<GenModel_799_>
    suspend fun getById(id: Long): GenModel_799_?
    suspend fun save(model: GenModel_799_): GenModel_799_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_799_>
}

@Singleton
class GenRepositoryImpl_799_ @Inject constructor() : GenRepository_799_ {
    private val store = mutableMapOf<Long, GenModel_799_>()
    override suspend fun getAll(): List<GenModel_799_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_799_? = store[id]
    override suspend fun save(model: GenModel_799_): GenModel_799_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_799_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_799_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_799_ @Inject constructor(
    private val repository: GenRepositoryImpl_799_
) : GenUseCase_799_<Unit, List<GenModel_799_>> {
    override suspend fun invoke(params: Unit): List<GenModel_799_> = repository.getAll()
}

class GenSaveUseCase_799_ @Inject constructor(
    private val repository: GenRepositoryImpl_799_
) : GenUseCase_799_<GenModel_799_, GenModel_799_> {
    override suspend fun invoke(params: GenModel_799_): GenModel_799_ = repository.save(params)
}

class GenDeleteUseCase_799_ @Inject constructor(
    private val repository: GenRepositoryImpl_799_
) : GenUseCase_799_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_799_ @Inject constructor(
    private val repository: GenRepositoryImpl_799_
) : GenUseCase_799_<String, List<GenModel_799_>> {
    override suspend fun invoke(params: String): List<GenModel_799_> = repository.search(params)
}

abstract class GenMapper_799_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_799_ : GenMapper_799_<GenModel_799_, String>() {
    override fun map(input: GenModel_799_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_799_ : GenMapper_799_<String, GenModel_799_>() {
    override fun map(input: String): GenModel_799_ {
        val parts = input.split(":")
        return GenModel_799_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_799_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_799_,
    private val saveUseCase: GenSaveUseCase_799_,
    private val deleteUseCase: GenDeleteUseCase_799_,
    private val searchUseCase: GenSearchUseCase_799_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_799_>(GenState_799_.Idle)
    val state: StateFlow<GenState_799_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_799_) {
        when (event) {
            is GenEvent_799_.Load -> loadAll()
            is GenEvent_799_.Update -> save(event.model)
            is GenEvent_799_.Delete -> delete(event.id)
            is GenEvent_799_.Refresh -> loadAll()
            is GenEvent_799_.Search -> search(event.query)
            is GenEvent_799_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_799_.Loading; _state.value = GenState_799_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_799_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_799_.Success(searchUseCase(query)) } }
}
