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

data class GenModel_186_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_186_ {
    data class Load(val id: Long) : GenEvent_186_()
    data class Update(val model: GenModel_186_) : GenEvent_186_()
    data class Delete(val id: Long) : GenEvent_186_()
    data object Refresh : GenEvent_186_()
    data class Search(val query: String) : GenEvent_186_()
    data class Filter(val predicate: String) : GenEvent_186_()
}

sealed class GenState_186_ {
    data object Idle : GenState_186_()
    data object Loading : GenState_186_()
    data class Success(val items: List<GenModel_186_>) : GenState_186_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_186_()
    data class Partial(val items: List<GenModel_186_>, val hasMore: Boolean) : GenState_186_()
}

interface GenRepository_186_ {
    suspend fun getAll(): List<GenModel_186_>
    suspend fun getById(id: Long): GenModel_186_?
    suspend fun save(model: GenModel_186_): GenModel_186_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_186_>
}

@Singleton
class GenRepositoryImpl_186_ @Inject constructor() : GenRepository_186_ {
    private val store = mutableMapOf<Long, GenModel_186_>()
    override suspend fun getAll(): List<GenModel_186_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_186_? = store[id]
    override suspend fun save(model: GenModel_186_): GenModel_186_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_186_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_186_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_186_ @Inject constructor(
    private val repository: GenRepositoryImpl_186_
) : GenUseCase_186_<Unit, List<GenModel_186_>> {
    override suspend fun invoke(params: Unit): List<GenModel_186_> = repository.getAll()
}

class GenSaveUseCase_186_ @Inject constructor(
    private val repository: GenRepositoryImpl_186_
) : GenUseCase_186_<GenModel_186_, GenModel_186_> {
    override suspend fun invoke(params: GenModel_186_): GenModel_186_ = repository.save(params)
}

class GenDeleteUseCase_186_ @Inject constructor(
    private val repository: GenRepositoryImpl_186_
) : GenUseCase_186_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_186_ @Inject constructor(
    private val repository: GenRepositoryImpl_186_
) : GenUseCase_186_<String, List<GenModel_186_>> {
    override suspend fun invoke(params: String): List<GenModel_186_> = repository.search(params)
}

abstract class GenMapper_186_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_186_ : GenMapper_186_<GenModel_186_, String>() {
    override fun map(input: GenModel_186_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_186_ : GenMapper_186_<String, GenModel_186_>() {
    override fun map(input: String): GenModel_186_ {
        val parts = input.split(":")
        return GenModel_186_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_186_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_186_,
    private val saveUseCase: GenSaveUseCase_186_,
    private val deleteUseCase: GenDeleteUseCase_186_,
    private val searchUseCase: GenSearchUseCase_186_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_186_>(GenState_186_.Idle)
    val state: StateFlow<GenState_186_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_186_) {
        when (event) {
            is GenEvent_186_.Load -> loadAll()
            is GenEvent_186_.Update -> save(event.model)
            is GenEvent_186_.Delete -> delete(event.id)
            is GenEvent_186_.Refresh -> loadAll()
            is GenEvent_186_.Search -> search(event.query)
            is GenEvent_186_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_186_.Loading; _state.value = GenState_186_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_186_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_186_.Success(searchUseCase(query)) } }
}
