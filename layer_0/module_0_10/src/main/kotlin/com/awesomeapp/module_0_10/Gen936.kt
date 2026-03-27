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

data class GenModel_936_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_936_ {
    data class Load(val id: Long) : GenEvent_936_()
    data class Update(val model: GenModel_936_) : GenEvent_936_()
    data class Delete(val id: Long) : GenEvent_936_()
    data object Refresh : GenEvent_936_()
    data class Search(val query: String) : GenEvent_936_()
    data class Filter(val predicate: String) : GenEvent_936_()
}

sealed class GenState_936_ {
    data object Idle : GenState_936_()
    data object Loading : GenState_936_()
    data class Success(val items: List<GenModel_936_>) : GenState_936_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_936_()
    data class Partial(val items: List<GenModel_936_>, val hasMore: Boolean) : GenState_936_()
}

interface GenRepository_936_ {
    suspend fun getAll(): List<GenModel_936_>
    suspend fun getById(id: Long): GenModel_936_?
    suspend fun save(model: GenModel_936_): GenModel_936_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_936_>
}

@Singleton
class GenRepositoryImpl_936_ @Inject constructor() : GenRepository_936_ {
    private val store = mutableMapOf<Long, GenModel_936_>()
    override suspend fun getAll(): List<GenModel_936_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_936_? = store[id]
    override suspend fun save(model: GenModel_936_): GenModel_936_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_936_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_936_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_936_ @Inject constructor(
    private val repository: GenRepositoryImpl_936_
) : GenUseCase_936_<Unit, List<GenModel_936_>> {
    override suspend fun invoke(params: Unit): List<GenModel_936_> = repository.getAll()
}

class GenSaveUseCase_936_ @Inject constructor(
    private val repository: GenRepositoryImpl_936_
) : GenUseCase_936_<GenModel_936_, GenModel_936_> {
    override suspend fun invoke(params: GenModel_936_): GenModel_936_ = repository.save(params)
}

class GenDeleteUseCase_936_ @Inject constructor(
    private val repository: GenRepositoryImpl_936_
) : GenUseCase_936_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_936_ @Inject constructor(
    private val repository: GenRepositoryImpl_936_
) : GenUseCase_936_<String, List<GenModel_936_>> {
    override suspend fun invoke(params: String): List<GenModel_936_> = repository.search(params)
}

abstract class GenMapper_936_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_936_ : GenMapper_936_<GenModel_936_, String>() {
    override fun map(input: GenModel_936_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_936_ : GenMapper_936_<String, GenModel_936_>() {
    override fun map(input: String): GenModel_936_ {
        val parts = input.split(":")
        return GenModel_936_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_936_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_936_,
    private val saveUseCase: GenSaveUseCase_936_,
    private val deleteUseCase: GenDeleteUseCase_936_,
    private val searchUseCase: GenSearchUseCase_936_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_936_>(GenState_936_.Idle)
    val state: StateFlow<GenState_936_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_936_) {
        when (event) {
            is GenEvent_936_.Load -> loadAll()
            is GenEvent_936_.Update -> save(event.model)
            is GenEvent_936_.Delete -> delete(event.id)
            is GenEvent_936_.Refresh -> loadAll()
            is GenEvent_936_.Search -> search(event.query)
            is GenEvent_936_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_936_.Loading; _state.value = GenState_936_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_936_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_936_.Success(searchUseCase(query)) } }
}
