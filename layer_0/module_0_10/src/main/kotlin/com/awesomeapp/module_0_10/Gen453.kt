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

data class GenModel_453_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_453_ {
    data class Load(val id: Long) : GenEvent_453_()
    data class Update(val model: GenModel_453_) : GenEvent_453_()
    data class Delete(val id: Long) : GenEvent_453_()
    data object Refresh : GenEvent_453_()
    data class Search(val query: String) : GenEvent_453_()
    data class Filter(val predicate: String) : GenEvent_453_()
}

sealed class GenState_453_ {
    data object Idle : GenState_453_()
    data object Loading : GenState_453_()
    data class Success(val items: List<GenModel_453_>) : GenState_453_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_453_()
    data class Partial(val items: List<GenModel_453_>, val hasMore: Boolean) : GenState_453_()
}

interface GenRepository_453_ {
    suspend fun getAll(): List<GenModel_453_>
    suspend fun getById(id: Long): GenModel_453_?
    suspend fun save(model: GenModel_453_): GenModel_453_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_453_>
}

@Singleton
class GenRepositoryImpl_453_ @Inject constructor() : GenRepository_453_ {
    private val store = mutableMapOf<Long, GenModel_453_>()
    override suspend fun getAll(): List<GenModel_453_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_453_? = store[id]
    override suspend fun save(model: GenModel_453_): GenModel_453_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_453_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_453_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_453_ @Inject constructor(
    private val repository: GenRepositoryImpl_453_
) : GenUseCase_453_<Unit, List<GenModel_453_>> {
    override suspend fun invoke(params: Unit): List<GenModel_453_> = repository.getAll()
}

class GenSaveUseCase_453_ @Inject constructor(
    private val repository: GenRepositoryImpl_453_
) : GenUseCase_453_<GenModel_453_, GenModel_453_> {
    override suspend fun invoke(params: GenModel_453_): GenModel_453_ = repository.save(params)
}

class GenDeleteUseCase_453_ @Inject constructor(
    private val repository: GenRepositoryImpl_453_
) : GenUseCase_453_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_453_ @Inject constructor(
    private val repository: GenRepositoryImpl_453_
) : GenUseCase_453_<String, List<GenModel_453_>> {
    override suspend fun invoke(params: String): List<GenModel_453_> = repository.search(params)
}

abstract class GenMapper_453_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_453_ : GenMapper_453_<GenModel_453_, String>() {
    override fun map(input: GenModel_453_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_453_ : GenMapper_453_<String, GenModel_453_>() {
    override fun map(input: String): GenModel_453_ {
        val parts = input.split(":")
        return GenModel_453_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_453_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_453_,
    private val saveUseCase: GenSaveUseCase_453_,
    private val deleteUseCase: GenDeleteUseCase_453_,
    private val searchUseCase: GenSearchUseCase_453_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_453_>(GenState_453_.Idle)
    val state: StateFlow<GenState_453_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_453_) {
        when (event) {
            is GenEvent_453_.Load -> loadAll()
            is GenEvent_453_.Update -> save(event.model)
            is GenEvent_453_.Delete -> delete(event.id)
            is GenEvent_453_.Refresh -> loadAll()
            is GenEvent_453_.Search -> search(event.query)
            is GenEvent_453_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_453_.Loading; _state.value = GenState_453_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_453_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_453_.Success(searchUseCase(query)) } }
}
