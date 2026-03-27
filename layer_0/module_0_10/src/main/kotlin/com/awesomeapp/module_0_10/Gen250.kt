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

data class GenModel_250_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_250_ {
    data class Load(val id: Long) : GenEvent_250_()
    data class Update(val model: GenModel_250_) : GenEvent_250_()
    data class Delete(val id: Long) : GenEvent_250_()
    data object Refresh : GenEvent_250_()
    data class Search(val query: String) : GenEvent_250_()
    data class Filter(val predicate: String) : GenEvent_250_()
}

sealed class GenState_250_ {
    data object Idle : GenState_250_()
    data object Loading : GenState_250_()
    data class Success(val items: List<GenModel_250_>) : GenState_250_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_250_()
    data class Partial(val items: List<GenModel_250_>, val hasMore: Boolean) : GenState_250_()
}

interface GenRepository_250_ {
    suspend fun getAll(): List<GenModel_250_>
    suspend fun getById(id: Long): GenModel_250_?
    suspend fun save(model: GenModel_250_): GenModel_250_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_250_>
}

@Singleton
class GenRepositoryImpl_250_ @Inject constructor() : GenRepository_250_ {
    private val store = mutableMapOf<Long, GenModel_250_>()
    override suspend fun getAll(): List<GenModel_250_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_250_? = store[id]
    override suspend fun save(model: GenModel_250_): GenModel_250_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_250_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_250_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_250_ @Inject constructor(
    private val repository: GenRepositoryImpl_250_
) : GenUseCase_250_<Unit, List<GenModel_250_>> {
    override suspend fun invoke(params: Unit): List<GenModel_250_> = repository.getAll()
}

class GenSaveUseCase_250_ @Inject constructor(
    private val repository: GenRepositoryImpl_250_
) : GenUseCase_250_<GenModel_250_, GenModel_250_> {
    override suspend fun invoke(params: GenModel_250_): GenModel_250_ = repository.save(params)
}

class GenDeleteUseCase_250_ @Inject constructor(
    private val repository: GenRepositoryImpl_250_
) : GenUseCase_250_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_250_ @Inject constructor(
    private val repository: GenRepositoryImpl_250_
) : GenUseCase_250_<String, List<GenModel_250_>> {
    override suspend fun invoke(params: String): List<GenModel_250_> = repository.search(params)
}

abstract class GenMapper_250_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_250_ : GenMapper_250_<GenModel_250_, String>() {
    override fun map(input: GenModel_250_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_250_ : GenMapper_250_<String, GenModel_250_>() {
    override fun map(input: String): GenModel_250_ {
        val parts = input.split(":")
        return GenModel_250_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_250_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_250_,
    private val saveUseCase: GenSaveUseCase_250_,
    private val deleteUseCase: GenDeleteUseCase_250_,
    private val searchUseCase: GenSearchUseCase_250_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_250_>(GenState_250_.Idle)
    val state: StateFlow<GenState_250_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_250_) {
        when (event) {
            is GenEvent_250_.Load -> loadAll()
            is GenEvent_250_.Update -> save(event.model)
            is GenEvent_250_.Delete -> delete(event.id)
            is GenEvent_250_.Refresh -> loadAll()
            is GenEvent_250_.Search -> search(event.query)
            is GenEvent_250_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_250_.Loading; _state.value = GenState_250_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_250_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_250_.Success(searchUseCase(query)) } }
}
