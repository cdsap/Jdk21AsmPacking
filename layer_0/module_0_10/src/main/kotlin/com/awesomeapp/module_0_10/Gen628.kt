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

data class GenModel_628_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_628_ {
    data class Load(val id: Long) : GenEvent_628_()
    data class Update(val model: GenModel_628_) : GenEvent_628_()
    data class Delete(val id: Long) : GenEvent_628_()
    data object Refresh : GenEvent_628_()
    data class Search(val query: String) : GenEvent_628_()
    data class Filter(val predicate: String) : GenEvent_628_()
}

sealed class GenState_628_ {
    data object Idle : GenState_628_()
    data object Loading : GenState_628_()
    data class Success(val items: List<GenModel_628_>) : GenState_628_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_628_()
    data class Partial(val items: List<GenModel_628_>, val hasMore: Boolean) : GenState_628_()
}

interface GenRepository_628_ {
    suspend fun getAll(): List<GenModel_628_>
    suspend fun getById(id: Long): GenModel_628_?
    suspend fun save(model: GenModel_628_): GenModel_628_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_628_>
}

@Singleton
class GenRepositoryImpl_628_ @Inject constructor() : GenRepository_628_ {
    private val store = mutableMapOf<Long, GenModel_628_>()
    override suspend fun getAll(): List<GenModel_628_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_628_? = store[id]
    override suspend fun save(model: GenModel_628_): GenModel_628_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_628_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_628_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_628_ @Inject constructor(
    private val repository: GenRepositoryImpl_628_
) : GenUseCase_628_<Unit, List<GenModel_628_>> {
    override suspend fun invoke(params: Unit): List<GenModel_628_> = repository.getAll()
}

class GenSaveUseCase_628_ @Inject constructor(
    private val repository: GenRepositoryImpl_628_
) : GenUseCase_628_<GenModel_628_, GenModel_628_> {
    override suspend fun invoke(params: GenModel_628_): GenModel_628_ = repository.save(params)
}

class GenDeleteUseCase_628_ @Inject constructor(
    private val repository: GenRepositoryImpl_628_
) : GenUseCase_628_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_628_ @Inject constructor(
    private val repository: GenRepositoryImpl_628_
) : GenUseCase_628_<String, List<GenModel_628_>> {
    override suspend fun invoke(params: String): List<GenModel_628_> = repository.search(params)
}

abstract class GenMapper_628_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_628_ : GenMapper_628_<GenModel_628_, String>() {
    override fun map(input: GenModel_628_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_628_ : GenMapper_628_<String, GenModel_628_>() {
    override fun map(input: String): GenModel_628_ {
        val parts = input.split(":")
        return GenModel_628_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_628_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_628_,
    private val saveUseCase: GenSaveUseCase_628_,
    private val deleteUseCase: GenDeleteUseCase_628_,
    private val searchUseCase: GenSearchUseCase_628_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_628_>(GenState_628_.Idle)
    val state: StateFlow<GenState_628_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_628_) {
        when (event) {
            is GenEvent_628_.Load -> loadAll()
            is GenEvent_628_.Update -> save(event.model)
            is GenEvent_628_.Delete -> delete(event.id)
            is GenEvent_628_.Refresh -> loadAll()
            is GenEvent_628_.Search -> search(event.query)
            is GenEvent_628_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_628_.Loading; _state.value = GenState_628_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_628_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_628_.Success(searchUseCase(query)) } }
}
