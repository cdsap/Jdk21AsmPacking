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

data class GenModel_2020_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2020_ {
    data class Load(val id: Long) : GenEvent_2020_()
    data class Update(val model: GenModel_2020_) : GenEvent_2020_()
    data class Delete(val id: Long) : GenEvent_2020_()
    data object Refresh : GenEvent_2020_()
    data class Search(val query: String) : GenEvent_2020_()
    data class Filter(val predicate: String) : GenEvent_2020_()
}

sealed class GenState_2020_ {
    data object Idle : GenState_2020_()
    data object Loading : GenState_2020_()
    data class Success(val items: List<GenModel_2020_>) : GenState_2020_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2020_()
    data class Partial(val items: List<GenModel_2020_>, val hasMore: Boolean) : GenState_2020_()
}

interface GenRepository_2020_ {
    suspend fun getAll(): List<GenModel_2020_>
    suspend fun getById(id: Long): GenModel_2020_?
    suspend fun save(model: GenModel_2020_): GenModel_2020_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2020_>
}

@Singleton
class GenRepositoryImpl_2020_ @Inject constructor() : GenRepository_2020_ {
    private val store = mutableMapOf<Long, GenModel_2020_>()
    override suspend fun getAll(): List<GenModel_2020_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2020_? = store[id]
    override suspend fun save(model: GenModel_2020_): GenModel_2020_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2020_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2020_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2020_ @Inject constructor(
    private val repository: GenRepositoryImpl_2020_
) : GenUseCase_2020_<Unit, List<GenModel_2020_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2020_> = repository.getAll()
}

class GenSaveUseCase_2020_ @Inject constructor(
    private val repository: GenRepositoryImpl_2020_
) : GenUseCase_2020_<GenModel_2020_, GenModel_2020_> {
    override suspend fun invoke(params: GenModel_2020_): GenModel_2020_ = repository.save(params)
}

class GenDeleteUseCase_2020_ @Inject constructor(
    private val repository: GenRepositoryImpl_2020_
) : GenUseCase_2020_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2020_ @Inject constructor(
    private val repository: GenRepositoryImpl_2020_
) : GenUseCase_2020_<String, List<GenModel_2020_>> {
    override suspend fun invoke(params: String): List<GenModel_2020_> = repository.search(params)
}

abstract class GenMapper_2020_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2020_ : GenMapper_2020_<GenModel_2020_, String>() {
    override fun map(input: GenModel_2020_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2020_ : GenMapper_2020_<String, GenModel_2020_>() {
    override fun map(input: String): GenModel_2020_ {
        val parts = input.split(":")
        return GenModel_2020_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2020_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2020_,
    private val saveUseCase: GenSaveUseCase_2020_,
    private val deleteUseCase: GenDeleteUseCase_2020_,
    private val searchUseCase: GenSearchUseCase_2020_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2020_>(GenState_2020_.Idle)
    val state: StateFlow<GenState_2020_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2020_) {
        when (event) {
            is GenEvent_2020_.Load -> loadAll()
            is GenEvent_2020_.Update -> save(event.model)
            is GenEvent_2020_.Delete -> delete(event.id)
            is GenEvent_2020_.Refresh -> loadAll()
            is GenEvent_2020_.Search -> search(event.query)
            is GenEvent_2020_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2020_.Loading; _state.value = GenState_2020_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2020_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2020_.Success(searchUseCase(query)) } }
}
