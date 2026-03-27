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

data class GenModel_293_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_293_ {
    data class Load(val id: Long) : GenEvent_293_()
    data class Update(val model: GenModel_293_) : GenEvent_293_()
    data class Delete(val id: Long) : GenEvent_293_()
    data object Refresh : GenEvent_293_()
    data class Search(val query: String) : GenEvent_293_()
    data class Filter(val predicate: String) : GenEvent_293_()
}

sealed class GenState_293_ {
    data object Idle : GenState_293_()
    data object Loading : GenState_293_()
    data class Success(val items: List<GenModel_293_>) : GenState_293_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_293_()
    data class Partial(val items: List<GenModel_293_>, val hasMore: Boolean) : GenState_293_()
}

interface GenRepository_293_ {
    suspend fun getAll(): List<GenModel_293_>
    suspend fun getById(id: Long): GenModel_293_?
    suspend fun save(model: GenModel_293_): GenModel_293_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_293_>
}

@Singleton
class GenRepositoryImpl_293_ @Inject constructor() : GenRepository_293_ {
    private val store = mutableMapOf<Long, GenModel_293_>()
    override suspend fun getAll(): List<GenModel_293_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_293_? = store[id]
    override suspend fun save(model: GenModel_293_): GenModel_293_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_293_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_293_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_293_ @Inject constructor(
    private val repository: GenRepositoryImpl_293_
) : GenUseCase_293_<Unit, List<GenModel_293_>> {
    override suspend fun invoke(params: Unit): List<GenModel_293_> = repository.getAll()
}

class GenSaveUseCase_293_ @Inject constructor(
    private val repository: GenRepositoryImpl_293_
) : GenUseCase_293_<GenModel_293_, GenModel_293_> {
    override suspend fun invoke(params: GenModel_293_): GenModel_293_ = repository.save(params)
}

class GenDeleteUseCase_293_ @Inject constructor(
    private val repository: GenRepositoryImpl_293_
) : GenUseCase_293_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_293_ @Inject constructor(
    private val repository: GenRepositoryImpl_293_
) : GenUseCase_293_<String, List<GenModel_293_>> {
    override suspend fun invoke(params: String): List<GenModel_293_> = repository.search(params)
}

abstract class GenMapper_293_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_293_ : GenMapper_293_<GenModel_293_, String>() {
    override fun map(input: GenModel_293_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_293_ : GenMapper_293_<String, GenModel_293_>() {
    override fun map(input: String): GenModel_293_ {
        val parts = input.split(":")
        return GenModel_293_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_293_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_293_,
    private val saveUseCase: GenSaveUseCase_293_,
    private val deleteUseCase: GenDeleteUseCase_293_,
    private val searchUseCase: GenSearchUseCase_293_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_293_>(GenState_293_.Idle)
    val state: StateFlow<GenState_293_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_293_) {
        when (event) {
            is GenEvent_293_.Load -> loadAll()
            is GenEvent_293_.Update -> save(event.model)
            is GenEvent_293_.Delete -> delete(event.id)
            is GenEvent_293_.Refresh -> loadAll()
            is GenEvent_293_.Search -> search(event.query)
            is GenEvent_293_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_293_.Loading; _state.value = GenState_293_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_293_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_293_.Success(searchUseCase(query)) } }
}
