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

data class GenModel_2696_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2696_ {
    data class Load(val id: Long) : GenEvent_2696_()
    data class Update(val model: GenModel_2696_) : GenEvent_2696_()
    data class Delete(val id: Long) : GenEvent_2696_()
    data object Refresh : GenEvent_2696_()
    data class Search(val query: String) : GenEvent_2696_()
    data class Filter(val predicate: String) : GenEvent_2696_()
}

sealed class GenState_2696_ {
    data object Idle : GenState_2696_()
    data object Loading : GenState_2696_()
    data class Success(val items: List<GenModel_2696_>) : GenState_2696_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2696_()
    data class Partial(val items: List<GenModel_2696_>, val hasMore: Boolean) : GenState_2696_()
}

interface GenRepository_2696_ {
    suspend fun getAll(): List<GenModel_2696_>
    suspend fun getById(id: Long): GenModel_2696_?
    suspend fun save(model: GenModel_2696_): GenModel_2696_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2696_>
}

@Singleton
class GenRepositoryImpl_2696_ @Inject constructor() : GenRepository_2696_ {
    private val store = mutableMapOf<Long, GenModel_2696_>()
    override suspend fun getAll(): List<GenModel_2696_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2696_? = store[id]
    override suspend fun save(model: GenModel_2696_): GenModel_2696_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2696_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2696_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2696_ @Inject constructor(
    private val repository: GenRepositoryImpl_2696_
) : GenUseCase_2696_<Unit, List<GenModel_2696_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2696_> = repository.getAll()
}

class GenSaveUseCase_2696_ @Inject constructor(
    private val repository: GenRepositoryImpl_2696_
) : GenUseCase_2696_<GenModel_2696_, GenModel_2696_> {
    override suspend fun invoke(params: GenModel_2696_): GenModel_2696_ = repository.save(params)
}

class GenDeleteUseCase_2696_ @Inject constructor(
    private val repository: GenRepositoryImpl_2696_
) : GenUseCase_2696_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2696_ @Inject constructor(
    private val repository: GenRepositoryImpl_2696_
) : GenUseCase_2696_<String, List<GenModel_2696_>> {
    override suspend fun invoke(params: String): List<GenModel_2696_> = repository.search(params)
}

abstract class GenMapper_2696_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2696_ : GenMapper_2696_<GenModel_2696_, String>() {
    override fun map(input: GenModel_2696_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2696_ : GenMapper_2696_<String, GenModel_2696_>() {
    override fun map(input: String): GenModel_2696_ {
        val parts = input.split(":")
        return GenModel_2696_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2696_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2696_,
    private val saveUseCase: GenSaveUseCase_2696_,
    private val deleteUseCase: GenDeleteUseCase_2696_,
    private val searchUseCase: GenSearchUseCase_2696_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2696_>(GenState_2696_.Idle)
    val state: StateFlow<GenState_2696_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2696_) {
        when (event) {
            is GenEvent_2696_.Load -> loadAll()
            is GenEvent_2696_.Update -> save(event.model)
            is GenEvent_2696_.Delete -> delete(event.id)
            is GenEvent_2696_.Refresh -> loadAll()
            is GenEvent_2696_.Search -> search(event.query)
            is GenEvent_2696_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2696_.Loading; _state.value = GenState_2696_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2696_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2696_.Success(searchUseCase(query)) } }
}
