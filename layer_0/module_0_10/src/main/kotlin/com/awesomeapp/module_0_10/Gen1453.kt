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

data class GenModel_1453_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1453_ {
    data class Load(val id: Long) : GenEvent_1453_()
    data class Update(val model: GenModel_1453_) : GenEvent_1453_()
    data class Delete(val id: Long) : GenEvent_1453_()
    data object Refresh : GenEvent_1453_()
    data class Search(val query: String) : GenEvent_1453_()
    data class Filter(val predicate: String) : GenEvent_1453_()
}

sealed class GenState_1453_ {
    data object Idle : GenState_1453_()
    data object Loading : GenState_1453_()
    data class Success(val items: List<GenModel_1453_>) : GenState_1453_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1453_()
    data class Partial(val items: List<GenModel_1453_>, val hasMore: Boolean) : GenState_1453_()
}

interface GenRepository_1453_ {
    suspend fun getAll(): List<GenModel_1453_>
    suspend fun getById(id: Long): GenModel_1453_?
    suspend fun save(model: GenModel_1453_): GenModel_1453_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1453_>
}

@Singleton
class GenRepositoryImpl_1453_ @Inject constructor() : GenRepository_1453_ {
    private val store = mutableMapOf<Long, GenModel_1453_>()
    override suspend fun getAll(): List<GenModel_1453_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1453_? = store[id]
    override suspend fun save(model: GenModel_1453_): GenModel_1453_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1453_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1453_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1453_ @Inject constructor(
    private val repository: GenRepositoryImpl_1453_
) : GenUseCase_1453_<Unit, List<GenModel_1453_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1453_> = repository.getAll()
}

class GenSaveUseCase_1453_ @Inject constructor(
    private val repository: GenRepositoryImpl_1453_
) : GenUseCase_1453_<GenModel_1453_, GenModel_1453_> {
    override suspend fun invoke(params: GenModel_1453_): GenModel_1453_ = repository.save(params)
}

class GenDeleteUseCase_1453_ @Inject constructor(
    private val repository: GenRepositoryImpl_1453_
) : GenUseCase_1453_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1453_ @Inject constructor(
    private val repository: GenRepositoryImpl_1453_
) : GenUseCase_1453_<String, List<GenModel_1453_>> {
    override suspend fun invoke(params: String): List<GenModel_1453_> = repository.search(params)
}

abstract class GenMapper_1453_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1453_ : GenMapper_1453_<GenModel_1453_, String>() {
    override fun map(input: GenModel_1453_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1453_ : GenMapper_1453_<String, GenModel_1453_>() {
    override fun map(input: String): GenModel_1453_ {
        val parts = input.split(":")
        return GenModel_1453_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1453_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1453_,
    private val saveUseCase: GenSaveUseCase_1453_,
    private val deleteUseCase: GenDeleteUseCase_1453_,
    private val searchUseCase: GenSearchUseCase_1453_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1453_>(GenState_1453_.Idle)
    val state: StateFlow<GenState_1453_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1453_) {
        when (event) {
            is GenEvent_1453_.Load -> loadAll()
            is GenEvent_1453_.Update -> save(event.model)
            is GenEvent_1453_.Delete -> delete(event.id)
            is GenEvent_1453_.Refresh -> loadAll()
            is GenEvent_1453_.Search -> search(event.query)
            is GenEvent_1453_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1453_.Loading; _state.value = GenState_1453_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1453_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1453_.Success(searchUseCase(query)) } }
}
