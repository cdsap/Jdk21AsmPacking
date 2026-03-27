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

data class GenModel_855_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_855_ {
    data class Load(val id: Long) : GenEvent_855_()
    data class Update(val model: GenModel_855_) : GenEvent_855_()
    data class Delete(val id: Long) : GenEvent_855_()
    data object Refresh : GenEvent_855_()
    data class Search(val query: String) : GenEvent_855_()
    data class Filter(val predicate: String) : GenEvent_855_()
}

sealed class GenState_855_ {
    data object Idle : GenState_855_()
    data object Loading : GenState_855_()
    data class Success(val items: List<GenModel_855_>) : GenState_855_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_855_()
    data class Partial(val items: List<GenModel_855_>, val hasMore: Boolean) : GenState_855_()
}

interface GenRepository_855_ {
    suspend fun getAll(): List<GenModel_855_>
    suspend fun getById(id: Long): GenModel_855_?
    suspend fun save(model: GenModel_855_): GenModel_855_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_855_>
}

@Singleton
class GenRepositoryImpl_855_ @Inject constructor() : GenRepository_855_ {
    private val store = mutableMapOf<Long, GenModel_855_>()
    override suspend fun getAll(): List<GenModel_855_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_855_? = store[id]
    override suspend fun save(model: GenModel_855_): GenModel_855_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_855_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_855_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_855_ @Inject constructor(
    private val repository: GenRepositoryImpl_855_
) : GenUseCase_855_<Unit, List<GenModel_855_>> {
    override suspend fun invoke(params: Unit): List<GenModel_855_> = repository.getAll()
}

class GenSaveUseCase_855_ @Inject constructor(
    private val repository: GenRepositoryImpl_855_
) : GenUseCase_855_<GenModel_855_, GenModel_855_> {
    override suspend fun invoke(params: GenModel_855_): GenModel_855_ = repository.save(params)
}

class GenDeleteUseCase_855_ @Inject constructor(
    private val repository: GenRepositoryImpl_855_
) : GenUseCase_855_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_855_ @Inject constructor(
    private val repository: GenRepositoryImpl_855_
) : GenUseCase_855_<String, List<GenModel_855_>> {
    override suspend fun invoke(params: String): List<GenModel_855_> = repository.search(params)
}

abstract class GenMapper_855_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_855_ : GenMapper_855_<GenModel_855_, String>() {
    override fun map(input: GenModel_855_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_855_ : GenMapper_855_<String, GenModel_855_>() {
    override fun map(input: String): GenModel_855_ {
        val parts = input.split(":")
        return GenModel_855_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_855_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_855_,
    private val saveUseCase: GenSaveUseCase_855_,
    private val deleteUseCase: GenDeleteUseCase_855_,
    private val searchUseCase: GenSearchUseCase_855_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_855_>(GenState_855_.Idle)
    val state: StateFlow<GenState_855_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_855_) {
        when (event) {
            is GenEvent_855_.Load -> loadAll()
            is GenEvent_855_.Update -> save(event.model)
            is GenEvent_855_.Delete -> delete(event.id)
            is GenEvent_855_.Refresh -> loadAll()
            is GenEvent_855_.Search -> search(event.query)
            is GenEvent_855_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_855_.Loading; _state.value = GenState_855_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_855_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_855_.Success(searchUseCase(query)) } }
}
