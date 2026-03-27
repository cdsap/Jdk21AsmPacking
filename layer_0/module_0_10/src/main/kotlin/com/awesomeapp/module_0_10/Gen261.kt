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

data class GenModel_261_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_261_ {
    data class Load(val id: Long) : GenEvent_261_()
    data class Update(val model: GenModel_261_) : GenEvent_261_()
    data class Delete(val id: Long) : GenEvent_261_()
    data object Refresh : GenEvent_261_()
    data class Search(val query: String) : GenEvent_261_()
    data class Filter(val predicate: String) : GenEvent_261_()
}

sealed class GenState_261_ {
    data object Idle : GenState_261_()
    data object Loading : GenState_261_()
    data class Success(val items: List<GenModel_261_>) : GenState_261_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_261_()
    data class Partial(val items: List<GenModel_261_>, val hasMore: Boolean) : GenState_261_()
}

interface GenRepository_261_ {
    suspend fun getAll(): List<GenModel_261_>
    suspend fun getById(id: Long): GenModel_261_?
    suspend fun save(model: GenModel_261_): GenModel_261_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_261_>
}

@Singleton
class GenRepositoryImpl_261_ @Inject constructor() : GenRepository_261_ {
    private val store = mutableMapOf<Long, GenModel_261_>()
    override suspend fun getAll(): List<GenModel_261_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_261_? = store[id]
    override suspend fun save(model: GenModel_261_): GenModel_261_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_261_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_261_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_261_ @Inject constructor(
    private val repository: GenRepositoryImpl_261_
) : GenUseCase_261_<Unit, List<GenModel_261_>> {
    override suspend fun invoke(params: Unit): List<GenModel_261_> = repository.getAll()
}

class GenSaveUseCase_261_ @Inject constructor(
    private val repository: GenRepositoryImpl_261_
) : GenUseCase_261_<GenModel_261_, GenModel_261_> {
    override suspend fun invoke(params: GenModel_261_): GenModel_261_ = repository.save(params)
}

class GenDeleteUseCase_261_ @Inject constructor(
    private val repository: GenRepositoryImpl_261_
) : GenUseCase_261_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_261_ @Inject constructor(
    private val repository: GenRepositoryImpl_261_
) : GenUseCase_261_<String, List<GenModel_261_>> {
    override suspend fun invoke(params: String): List<GenModel_261_> = repository.search(params)
}

abstract class GenMapper_261_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_261_ : GenMapper_261_<GenModel_261_, String>() {
    override fun map(input: GenModel_261_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_261_ : GenMapper_261_<String, GenModel_261_>() {
    override fun map(input: String): GenModel_261_ {
        val parts = input.split(":")
        return GenModel_261_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_261_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_261_,
    private val saveUseCase: GenSaveUseCase_261_,
    private val deleteUseCase: GenDeleteUseCase_261_,
    private val searchUseCase: GenSearchUseCase_261_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_261_>(GenState_261_.Idle)
    val state: StateFlow<GenState_261_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_261_) {
        when (event) {
            is GenEvent_261_.Load -> loadAll()
            is GenEvent_261_.Update -> save(event.model)
            is GenEvent_261_.Delete -> delete(event.id)
            is GenEvent_261_.Refresh -> loadAll()
            is GenEvent_261_.Search -> search(event.query)
            is GenEvent_261_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_261_.Loading; _state.value = GenState_261_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_261_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_261_.Success(searchUseCase(query)) } }
}
