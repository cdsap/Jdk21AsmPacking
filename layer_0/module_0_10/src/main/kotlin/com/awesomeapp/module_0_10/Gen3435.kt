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

data class GenModel_3435_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3435_ {
    data class Load(val id: Long) : GenEvent_3435_()
    data class Update(val model: GenModel_3435_) : GenEvent_3435_()
    data class Delete(val id: Long) : GenEvent_3435_()
    data object Refresh : GenEvent_3435_()
    data class Search(val query: String) : GenEvent_3435_()
    data class Filter(val predicate: String) : GenEvent_3435_()
}

sealed class GenState_3435_ {
    data object Idle : GenState_3435_()
    data object Loading : GenState_3435_()
    data class Success(val items: List<GenModel_3435_>) : GenState_3435_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3435_()
    data class Partial(val items: List<GenModel_3435_>, val hasMore: Boolean) : GenState_3435_()
}

interface GenRepository_3435_ {
    suspend fun getAll(): List<GenModel_3435_>
    suspend fun getById(id: Long): GenModel_3435_?
    suspend fun save(model: GenModel_3435_): GenModel_3435_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3435_>
}

@Singleton
class GenRepositoryImpl_3435_ @Inject constructor() : GenRepository_3435_ {
    private val store = mutableMapOf<Long, GenModel_3435_>()
    override suspend fun getAll(): List<GenModel_3435_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3435_? = store[id]
    override suspend fun save(model: GenModel_3435_): GenModel_3435_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3435_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3435_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3435_ @Inject constructor(
    private val repository: GenRepositoryImpl_3435_
) : GenUseCase_3435_<Unit, List<GenModel_3435_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3435_> = repository.getAll()
}

class GenSaveUseCase_3435_ @Inject constructor(
    private val repository: GenRepositoryImpl_3435_
) : GenUseCase_3435_<GenModel_3435_, GenModel_3435_> {
    override suspend fun invoke(params: GenModel_3435_): GenModel_3435_ = repository.save(params)
}

class GenDeleteUseCase_3435_ @Inject constructor(
    private val repository: GenRepositoryImpl_3435_
) : GenUseCase_3435_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3435_ @Inject constructor(
    private val repository: GenRepositoryImpl_3435_
) : GenUseCase_3435_<String, List<GenModel_3435_>> {
    override suspend fun invoke(params: String): List<GenModel_3435_> = repository.search(params)
}

abstract class GenMapper_3435_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3435_ : GenMapper_3435_<GenModel_3435_, String>() {
    override fun map(input: GenModel_3435_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3435_ : GenMapper_3435_<String, GenModel_3435_>() {
    override fun map(input: String): GenModel_3435_ {
        val parts = input.split(":")
        return GenModel_3435_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3435_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3435_,
    private val saveUseCase: GenSaveUseCase_3435_,
    private val deleteUseCase: GenDeleteUseCase_3435_,
    private val searchUseCase: GenSearchUseCase_3435_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3435_>(GenState_3435_.Idle)
    val state: StateFlow<GenState_3435_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3435_) {
        when (event) {
            is GenEvent_3435_.Load -> loadAll()
            is GenEvent_3435_.Update -> save(event.model)
            is GenEvent_3435_.Delete -> delete(event.id)
            is GenEvent_3435_.Refresh -> loadAll()
            is GenEvent_3435_.Search -> search(event.query)
            is GenEvent_3435_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3435_.Loading; _state.value = GenState_3435_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3435_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3435_.Success(searchUseCase(query)) } }
}
