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

data class GenModel_347_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_347_ {
    data class Load(val id: Long) : GenEvent_347_()
    data class Update(val model: GenModel_347_) : GenEvent_347_()
    data class Delete(val id: Long) : GenEvent_347_()
    data object Refresh : GenEvent_347_()
    data class Search(val query: String) : GenEvent_347_()
    data class Filter(val predicate: String) : GenEvent_347_()
}

sealed class GenState_347_ {
    data object Idle : GenState_347_()
    data object Loading : GenState_347_()
    data class Success(val items: List<GenModel_347_>) : GenState_347_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_347_()
    data class Partial(val items: List<GenModel_347_>, val hasMore: Boolean) : GenState_347_()
}

interface GenRepository_347_ {
    suspend fun getAll(): List<GenModel_347_>
    suspend fun getById(id: Long): GenModel_347_?
    suspend fun save(model: GenModel_347_): GenModel_347_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_347_>
}

@Singleton
class GenRepositoryImpl_347_ @Inject constructor() : GenRepository_347_ {
    private val store = mutableMapOf<Long, GenModel_347_>()
    override suspend fun getAll(): List<GenModel_347_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_347_? = store[id]
    override suspend fun save(model: GenModel_347_): GenModel_347_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_347_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_347_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_347_ @Inject constructor(
    private val repository: GenRepositoryImpl_347_
) : GenUseCase_347_<Unit, List<GenModel_347_>> {
    override suspend fun invoke(params: Unit): List<GenModel_347_> = repository.getAll()
}

class GenSaveUseCase_347_ @Inject constructor(
    private val repository: GenRepositoryImpl_347_
) : GenUseCase_347_<GenModel_347_, GenModel_347_> {
    override suspend fun invoke(params: GenModel_347_): GenModel_347_ = repository.save(params)
}

class GenDeleteUseCase_347_ @Inject constructor(
    private val repository: GenRepositoryImpl_347_
) : GenUseCase_347_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_347_ @Inject constructor(
    private val repository: GenRepositoryImpl_347_
) : GenUseCase_347_<String, List<GenModel_347_>> {
    override suspend fun invoke(params: String): List<GenModel_347_> = repository.search(params)
}

abstract class GenMapper_347_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_347_ : GenMapper_347_<GenModel_347_, String>() {
    override fun map(input: GenModel_347_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_347_ : GenMapper_347_<String, GenModel_347_>() {
    override fun map(input: String): GenModel_347_ {
        val parts = input.split(":")
        return GenModel_347_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_347_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_347_,
    private val saveUseCase: GenSaveUseCase_347_,
    private val deleteUseCase: GenDeleteUseCase_347_,
    private val searchUseCase: GenSearchUseCase_347_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_347_>(GenState_347_.Idle)
    val state: StateFlow<GenState_347_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_347_) {
        when (event) {
            is GenEvent_347_.Load -> loadAll()
            is GenEvent_347_.Update -> save(event.model)
            is GenEvent_347_.Delete -> delete(event.id)
            is GenEvent_347_.Refresh -> loadAll()
            is GenEvent_347_.Search -> search(event.query)
            is GenEvent_347_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_347_.Loading; _state.value = GenState_347_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_347_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_347_.Success(searchUseCase(query)) } }
}
