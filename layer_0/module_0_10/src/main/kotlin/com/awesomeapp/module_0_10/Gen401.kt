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

data class GenModel_401_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_401_ {
    data class Load(val id: Long) : GenEvent_401_()
    data class Update(val model: GenModel_401_) : GenEvent_401_()
    data class Delete(val id: Long) : GenEvent_401_()
    data object Refresh : GenEvent_401_()
    data class Search(val query: String) : GenEvent_401_()
    data class Filter(val predicate: String) : GenEvent_401_()
}

sealed class GenState_401_ {
    data object Idle : GenState_401_()
    data object Loading : GenState_401_()
    data class Success(val items: List<GenModel_401_>) : GenState_401_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_401_()
    data class Partial(val items: List<GenModel_401_>, val hasMore: Boolean) : GenState_401_()
}

interface GenRepository_401_ {
    suspend fun getAll(): List<GenModel_401_>
    suspend fun getById(id: Long): GenModel_401_?
    suspend fun save(model: GenModel_401_): GenModel_401_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_401_>
}

@Singleton
class GenRepositoryImpl_401_ @Inject constructor() : GenRepository_401_ {
    private val store = mutableMapOf<Long, GenModel_401_>()
    override suspend fun getAll(): List<GenModel_401_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_401_? = store[id]
    override suspend fun save(model: GenModel_401_): GenModel_401_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_401_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_401_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_401_ @Inject constructor(
    private val repository: GenRepositoryImpl_401_
) : GenUseCase_401_<Unit, List<GenModel_401_>> {
    override suspend fun invoke(params: Unit): List<GenModel_401_> = repository.getAll()
}

class GenSaveUseCase_401_ @Inject constructor(
    private val repository: GenRepositoryImpl_401_
) : GenUseCase_401_<GenModel_401_, GenModel_401_> {
    override suspend fun invoke(params: GenModel_401_): GenModel_401_ = repository.save(params)
}

class GenDeleteUseCase_401_ @Inject constructor(
    private val repository: GenRepositoryImpl_401_
) : GenUseCase_401_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_401_ @Inject constructor(
    private val repository: GenRepositoryImpl_401_
) : GenUseCase_401_<String, List<GenModel_401_>> {
    override suspend fun invoke(params: String): List<GenModel_401_> = repository.search(params)
}

abstract class GenMapper_401_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_401_ : GenMapper_401_<GenModel_401_, String>() {
    override fun map(input: GenModel_401_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_401_ : GenMapper_401_<String, GenModel_401_>() {
    override fun map(input: String): GenModel_401_ {
        val parts = input.split(":")
        return GenModel_401_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_401_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_401_,
    private val saveUseCase: GenSaveUseCase_401_,
    private val deleteUseCase: GenDeleteUseCase_401_,
    private val searchUseCase: GenSearchUseCase_401_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_401_>(GenState_401_.Idle)
    val state: StateFlow<GenState_401_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_401_) {
        when (event) {
            is GenEvent_401_.Load -> loadAll()
            is GenEvent_401_.Update -> save(event.model)
            is GenEvent_401_.Delete -> delete(event.id)
            is GenEvent_401_.Refresh -> loadAll()
            is GenEvent_401_.Search -> search(event.query)
            is GenEvent_401_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_401_.Loading; _state.value = GenState_401_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_401_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_401_.Success(searchUseCase(query)) } }
}
