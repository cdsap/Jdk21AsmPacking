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

data class GenModel_313_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_313_ {
    data class Load(val id: Long) : GenEvent_313_()
    data class Update(val model: GenModel_313_) : GenEvent_313_()
    data class Delete(val id: Long) : GenEvent_313_()
    data object Refresh : GenEvent_313_()
    data class Search(val query: String) : GenEvent_313_()
    data class Filter(val predicate: String) : GenEvent_313_()
}

sealed class GenState_313_ {
    data object Idle : GenState_313_()
    data object Loading : GenState_313_()
    data class Success(val items: List<GenModel_313_>) : GenState_313_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_313_()
    data class Partial(val items: List<GenModel_313_>, val hasMore: Boolean) : GenState_313_()
}

interface GenRepository_313_ {
    suspend fun getAll(): List<GenModel_313_>
    suspend fun getById(id: Long): GenModel_313_?
    suspend fun save(model: GenModel_313_): GenModel_313_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_313_>
}

@Singleton
class GenRepositoryImpl_313_ @Inject constructor() : GenRepository_313_ {
    private val store = mutableMapOf<Long, GenModel_313_>()
    override suspend fun getAll(): List<GenModel_313_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_313_? = store[id]
    override suspend fun save(model: GenModel_313_): GenModel_313_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_313_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_313_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_313_ @Inject constructor(
    private val repository: GenRepositoryImpl_313_
) : GenUseCase_313_<Unit, List<GenModel_313_>> {
    override suspend fun invoke(params: Unit): List<GenModel_313_> = repository.getAll()
}

class GenSaveUseCase_313_ @Inject constructor(
    private val repository: GenRepositoryImpl_313_
) : GenUseCase_313_<GenModel_313_, GenModel_313_> {
    override suspend fun invoke(params: GenModel_313_): GenModel_313_ = repository.save(params)
}

class GenDeleteUseCase_313_ @Inject constructor(
    private val repository: GenRepositoryImpl_313_
) : GenUseCase_313_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_313_ @Inject constructor(
    private val repository: GenRepositoryImpl_313_
) : GenUseCase_313_<String, List<GenModel_313_>> {
    override suspend fun invoke(params: String): List<GenModel_313_> = repository.search(params)
}

abstract class GenMapper_313_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_313_ : GenMapper_313_<GenModel_313_, String>() {
    override fun map(input: GenModel_313_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_313_ : GenMapper_313_<String, GenModel_313_>() {
    override fun map(input: String): GenModel_313_ {
        val parts = input.split(":")
        return GenModel_313_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_313_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_313_,
    private val saveUseCase: GenSaveUseCase_313_,
    private val deleteUseCase: GenDeleteUseCase_313_,
    private val searchUseCase: GenSearchUseCase_313_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_313_>(GenState_313_.Idle)
    val state: StateFlow<GenState_313_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_313_) {
        when (event) {
            is GenEvent_313_.Load -> loadAll()
            is GenEvent_313_.Update -> save(event.model)
            is GenEvent_313_.Delete -> delete(event.id)
            is GenEvent_313_.Refresh -> loadAll()
            is GenEvent_313_.Search -> search(event.query)
            is GenEvent_313_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_313_.Loading; _state.value = GenState_313_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_313_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_313_.Success(searchUseCase(query)) } }
}
