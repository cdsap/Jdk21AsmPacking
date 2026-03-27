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

data class GenModel_680_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_680_ {
    data class Load(val id: Long) : GenEvent_680_()
    data class Update(val model: GenModel_680_) : GenEvent_680_()
    data class Delete(val id: Long) : GenEvent_680_()
    data object Refresh : GenEvent_680_()
    data class Search(val query: String) : GenEvent_680_()
    data class Filter(val predicate: String) : GenEvent_680_()
}

sealed class GenState_680_ {
    data object Idle : GenState_680_()
    data object Loading : GenState_680_()
    data class Success(val items: List<GenModel_680_>) : GenState_680_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_680_()
    data class Partial(val items: List<GenModel_680_>, val hasMore: Boolean) : GenState_680_()
}

interface GenRepository_680_ {
    suspend fun getAll(): List<GenModel_680_>
    suspend fun getById(id: Long): GenModel_680_?
    suspend fun save(model: GenModel_680_): GenModel_680_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_680_>
}

@Singleton
class GenRepositoryImpl_680_ @Inject constructor() : GenRepository_680_ {
    private val store = mutableMapOf<Long, GenModel_680_>()
    override suspend fun getAll(): List<GenModel_680_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_680_? = store[id]
    override suspend fun save(model: GenModel_680_): GenModel_680_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_680_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_680_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_680_ @Inject constructor(
    private val repository: GenRepositoryImpl_680_
) : GenUseCase_680_<Unit, List<GenModel_680_>> {
    override suspend fun invoke(params: Unit): List<GenModel_680_> = repository.getAll()
}

class GenSaveUseCase_680_ @Inject constructor(
    private val repository: GenRepositoryImpl_680_
) : GenUseCase_680_<GenModel_680_, GenModel_680_> {
    override suspend fun invoke(params: GenModel_680_): GenModel_680_ = repository.save(params)
}

class GenDeleteUseCase_680_ @Inject constructor(
    private val repository: GenRepositoryImpl_680_
) : GenUseCase_680_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_680_ @Inject constructor(
    private val repository: GenRepositoryImpl_680_
) : GenUseCase_680_<String, List<GenModel_680_>> {
    override suspend fun invoke(params: String): List<GenModel_680_> = repository.search(params)
}

abstract class GenMapper_680_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_680_ : GenMapper_680_<GenModel_680_, String>() {
    override fun map(input: GenModel_680_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_680_ : GenMapper_680_<String, GenModel_680_>() {
    override fun map(input: String): GenModel_680_ {
        val parts = input.split(":")
        return GenModel_680_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_680_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_680_,
    private val saveUseCase: GenSaveUseCase_680_,
    private val deleteUseCase: GenDeleteUseCase_680_,
    private val searchUseCase: GenSearchUseCase_680_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_680_>(GenState_680_.Idle)
    val state: StateFlow<GenState_680_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_680_) {
        when (event) {
            is GenEvent_680_.Load -> loadAll()
            is GenEvent_680_.Update -> save(event.model)
            is GenEvent_680_.Delete -> delete(event.id)
            is GenEvent_680_.Refresh -> loadAll()
            is GenEvent_680_.Search -> search(event.query)
            is GenEvent_680_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_680_.Loading; _state.value = GenState_680_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_680_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_680_.Success(searchUseCase(query)) } }
}
