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

data class GenModel_343_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_343_ {
    data class Load(val id: Long) : GenEvent_343_()
    data class Update(val model: GenModel_343_) : GenEvent_343_()
    data class Delete(val id: Long) : GenEvent_343_()
    data object Refresh : GenEvent_343_()
    data class Search(val query: String) : GenEvent_343_()
    data class Filter(val predicate: String) : GenEvent_343_()
}

sealed class GenState_343_ {
    data object Idle : GenState_343_()
    data object Loading : GenState_343_()
    data class Success(val items: List<GenModel_343_>) : GenState_343_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_343_()
    data class Partial(val items: List<GenModel_343_>, val hasMore: Boolean) : GenState_343_()
}

interface GenRepository_343_ {
    suspend fun getAll(): List<GenModel_343_>
    suspend fun getById(id: Long): GenModel_343_?
    suspend fun save(model: GenModel_343_): GenModel_343_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_343_>
}

@Singleton
class GenRepositoryImpl_343_ @Inject constructor() : GenRepository_343_ {
    private val store = mutableMapOf<Long, GenModel_343_>()
    override suspend fun getAll(): List<GenModel_343_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_343_? = store[id]
    override suspend fun save(model: GenModel_343_): GenModel_343_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_343_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_343_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_343_ @Inject constructor(
    private val repository: GenRepositoryImpl_343_
) : GenUseCase_343_<Unit, List<GenModel_343_>> {
    override suspend fun invoke(params: Unit): List<GenModel_343_> = repository.getAll()
}

class GenSaveUseCase_343_ @Inject constructor(
    private val repository: GenRepositoryImpl_343_
) : GenUseCase_343_<GenModel_343_, GenModel_343_> {
    override suspend fun invoke(params: GenModel_343_): GenModel_343_ = repository.save(params)
}

class GenDeleteUseCase_343_ @Inject constructor(
    private val repository: GenRepositoryImpl_343_
) : GenUseCase_343_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_343_ @Inject constructor(
    private val repository: GenRepositoryImpl_343_
) : GenUseCase_343_<String, List<GenModel_343_>> {
    override suspend fun invoke(params: String): List<GenModel_343_> = repository.search(params)
}

abstract class GenMapper_343_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_343_ : GenMapper_343_<GenModel_343_, String>() {
    override fun map(input: GenModel_343_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_343_ : GenMapper_343_<String, GenModel_343_>() {
    override fun map(input: String): GenModel_343_ {
        val parts = input.split(":")
        return GenModel_343_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_343_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_343_,
    private val saveUseCase: GenSaveUseCase_343_,
    private val deleteUseCase: GenDeleteUseCase_343_,
    private val searchUseCase: GenSearchUseCase_343_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_343_>(GenState_343_.Idle)
    val state: StateFlow<GenState_343_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_343_) {
        when (event) {
            is GenEvent_343_.Load -> loadAll()
            is GenEvent_343_.Update -> save(event.model)
            is GenEvent_343_.Delete -> delete(event.id)
            is GenEvent_343_.Refresh -> loadAll()
            is GenEvent_343_.Search -> search(event.query)
            is GenEvent_343_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_343_.Loading; _state.value = GenState_343_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_343_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_343_.Success(searchUseCase(query)) } }
}
