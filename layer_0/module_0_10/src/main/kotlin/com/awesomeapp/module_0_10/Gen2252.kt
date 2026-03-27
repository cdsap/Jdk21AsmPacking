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

data class GenModel_2252_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2252_ {
    data class Load(val id: Long) : GenEvent_2252_()
    data class Update(val model: GenModel_2252_) : GenEvent_2252_()
    data class Delete(val id: Long) : GenEvent_2252_()
    data object Refresh : GenEvent_2252_()
    data class Search(val query: String) : GenEvent_2252_()
    data class Filter(val predicate: String) : GenEvent_2252_()
}

sealed class GenState_2252_ {
    data object Idle : GenState_2252_()
    data object Loading : GenState_2252_()
    data class Success(val items: List<GenModel_2252_>) : GenState_2252_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2252_()
    data class Partial(val items: List<GenModel_2252_>, val hasMore: Boolean) : GenState_2252_()
}

interface GenRepository_2252_ {
    suspend fun getAll(): List<GenModel_2252_>
    suspend fun getById(id: Long): GenModel_2252_?
    suspend fun save(model: GenModel_2252_): GenModel_2252_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2252_>
}

@Singleton
class GenRepositoryImpl_2252_ @Inject constructor() : GenRepository_2252_ {
    private val store = mutableMapOf<Long, GenModel_2252_>()
    override suspend fun getAll(): List<GenModel_2252_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2252_? = store[id]
    override suspend fun save(model: GenModel_2252_): GenModel_2252_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2252_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2252_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2252_ @Inject constructor(
    private val repository: GenRepositoryImpl_2252_
) : GenUseCase_2252_<Unit, List<GenModel_2252_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2252_> = repository.getAll()
}

class GenSaveUseCase_2252_ @Inject constructor(
    private val repository: GenRepositoryImpl_2252_
) : GenUseCase_2252_<GenModel_2252_, GenModel_2252_> {
    override suspend fun invoke(params: GenModel_2252_): GenModel_2252_ = repository.save(params)
}

class GenDeleteUseCase_2252_ @Inject constructor(
    private val repository: GenRepositoryImpl_2252_
) : GenUseCase_2252_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2252_ @Inject constructor(
    private val repository: GenRepositoryImpl_2252_
) : GenUseCase_2252_<String, List<GenModel_2252_>> {
    override suspend fun invoke(params: String): List<GenModel_2252_> = repository.search(params)
}

abstract class GenMapper_2252_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2252_ : GenMapper_2252_<GenModel_2252_, String>() {
    override fun map(input: GenModel_2252_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2252_ : GenMapper_2252_<String, GenModel_2252_>() {
    override fun map(input: String): GenModel_2252_ {
        val parts = input.split(":")
        return GenModel_2252_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2252_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2252_,
    private val saveUseCase: GenSaveUseCase_2252_,
    private val deleteUseCase: GenDeleteUseCase_2252_,
    private val searchUseCase: GenSearchUseCase_2252_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2252_>(GenState_2252_.Idle)
    val state: StateFlow<GenState_2252_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2252_) {
        when (event) {
            is GenEvent_2252_.Load -> loadAll()
            is GenEvent_2252_.Update -> save(event.model)
            is GenEvent_2252_.Delete -> delete(event.id)
            is GenEvent_2252_.Refresh -> loadAll()
            is GenEvent_2252_.Search -> search(event.query)
            is GenEvent_2252_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2252_.Loading; _state.value = GenState_2252_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2252_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2252_.Success(searchUseCase(query)) } }
}
