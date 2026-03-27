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

data class GenModel_41_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_41_ {
    data class Load(val id: Long) : GenEvent_41_()
    data class Update(val model: GenModel_41_) : GenEvent_41_()
    data class Delete(val id: Long) : GenEvent_41_()
    data object Refresh : GenEvent_41_()
    data class Search(val query: String) : GenEvent_41_()
    data class Filter(val predicate: String) : GenEvent_41_()
}

sealed class GenState_41_ {
    data object Idle : GenState_41_()
    data object Loading : GenState_41_()
    data class Success(val items: List<GenModel_41_>) : GenState_41_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_41_()
    data class Partial(val items: List<GenModel_41_>, val hasMore: Boolean) : GenState_41_()
}

interface GenRepository_41_ {
    suspend fun getAll(): List<GenModel_41_>
    suspend fun getById(id: Long): GenModel_41_?
    suspend fun save(model: GenModel_41_): GenModel_41_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_41_>
}

@Singleton
class GenRepositoryImpl_41_ @Inject constructor() : GenRepository_41_ {
    private val store = mutableMapOf<Long, GenModel_41_>()
    override suspend fun getAll(): List<GenModel_41_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_41_? = store[id]
    override suspend fun save(model: GenModel_41_): GenModel_41_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_41_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_41_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_41_ @Inject constructor(
    private val repository: GenRepositoryImpl_41_
) : GenUseCase_41_<Unit, List<GenModel_41_>> {
    override suspend fun invoke(params: Unit): List<GenModel_41_> = repository.getAll()
}

class GenSaveUseCase_41_ @Inject constructor(
    private val repository: GenRepositoryImpl_41_
) : GenUseCase_41_<GenModel_41_, GenModel_41_> {
    override suspend fun invoke(params: GenModel_41_): GenModel_41_ = repository.save(params)
}

class GenDeleteUseCase_41_ @Inject constructor(
    private val repository: GenRepositoryImpl_41_
) : GenUseCase_41_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_41_ @Inject constructor(
    private val repository: GenRepositoryImpl_41_
) : GenUseCase_41_<String, List<GenModel_41_>> {
    override suspend fun invoke(params: String): List<GenModel_41_> = repository.search(params)
}

abstract class GenMapper_41_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_41_ : GenMapper_41_<GenModel_41_, String>() {
    override fun map(input: GenModel_41_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_41_ : GenMapper_41_<String, GenModel_41_>() {
    override fun map(input: String): GenModel_41_ {
        val parts = input.split(":")
        return GenModel_41_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_41_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_41_,
    private val saveUseCase: GenSaveUseCase_41_,
    private val deleteUseCase: GenDeleteUseCase_41_,
    private val searchUseCase: GenSearchUseCase_41_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_41_>(GenState_41_.Idle)
    val state: StateFlow<GenState_41_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_41_) {
        when (event) {
            is GenEvent_41_.Load -> loadAll()
            is GenEvent_41_.Update -> save(event.model)
            is GenEvent_41_.Delete -> delete(event.id)
            is GenEvent_41_.Refresh -> loadAll()
            is GenEvent_41_.Search -> search(event.query)
            is GenEvent_41_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_41_.Loading; _state.value = GenState_41_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_41_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_41_.Success(searchUseCase(query)) } }
}
