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

data class GenModel_627_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_627_ {
    data class Load(val id: Long) : GenEvent_627_()
    data class Update(val model: GenModel_627_) : GenEvent_627_()
    data class Delete(val id: Long) : GenEvent_627_()
    data object Refresh : GenEvent_627_()
    data class Search(val query: String) : GenEvent_627_()
    data class Filter(val predicate: String) : GenEvent_627_()
}

sealed class GenState_627_ {
    data object Idle : GenState_627_()
    data object Loading : GenState_627_()
    data class Success(val items: List<GenModel_627_>) : GenState_627_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_627_()
    data class Partial(val items: List<GenModel_627_>, val hasMore: Boolean) : GenState_627_()
}

interface GenRepository_627_ {
    suspend fun getAll(): List<GenModel_627_>
    suspend fun getById(id: Long): GenModel_627_?
    suspend fun save(model: GenModel_627_): GenModel_627_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_627_>
}

@Singleton
class GenRepositoryImpl_627_ @Inject constructor() : GenRepository_627_ {
    private val store = mutableMapOf<Long, GenModel_627_>()
    override suspend fun getAll(): List<GenModel_627_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_627_? = store[id]
    override suspend fun save(model: GenModel_627_): GenModel_627_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_627_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_627_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_627_ @Inject constructor(
    private val repository: GenRepositoryImpl_627_
) : GenUseCase_627_<Unit, List<GenModel_627_>> {
    override suspend fun invoke(params: Unit): List<GenModel_627_> = repository.getAll()
}

class GenSaveUseCase_627_ @Inject constructor(
    private val repository: GenRepositoryImpl_627_
) : GenUseCase_627_<GenModel_627_, GenModel_627_> {
    override suspend fun invoke(params: GenModel_627_): GenModel_627_ = repository.save(params)
}

class GenDeleteUseCase_627_ @Inject constructor(
    private val repository: GenRepositoryImpl_627_
) : GenUseCase_627_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_627_ @Inject constructor(
    private val repository: GenRepositoryImpl_627_
) : GenUseCase_627_<String, List<GenModel_627_>> {
    override suspend fun invoke(params: String): List<GenModel_627_> = repository.search(params)
}

abstract class GenMapper_627_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_627_ : GenMapper_627_<GenModel_627_, String>() {
    override fun map(input: GenModel_627_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_627_ : GenMapper_627_<String, GenModel_627_>() {
    override fun map(input: String): GenModel_627_ {
        val parts = input.split(":")
        return GenModel_627_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_627_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_627_,
    private val saveUseCase: GenSaveUseCase_627_,
    private val deleteUseCase: GenDeleteUseCase_627_,
    private val searchUseCase: GenSearchUseCase_627_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_627_>(GenState_627_.Idle)
    val state: StateFlow<GenState_627_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_627_) {
        when (event) {
            is GenEvent_627_.Load -> loadAll()
            is GenEvent_627_.Update -> save(event.model)
            is GenEvent_627_.Delete -> delete(event.id)
            is GenEvent_627_.Refresh -> loadAll()
            is GenEvent_627_.Search -> search(event.query)
            is GenEvent_627_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_627_.Loading; _state.value = GenState_627_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_627_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_627_.Success(searchUseCase(query)) } }
}
