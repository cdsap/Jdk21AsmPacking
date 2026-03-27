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

data class GenModel_3009_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3009_ {
    data class Load(val id: Long) : GenEvent_3009_()
    data class Update(val model: GenModel_3009_) : GenEvent_3009_()
    data class Delete(val id: Long) : GenEvent_3009_()
    data object Refresh : GenEvent_3009_()
    data class Search(val query: String) : GenEvent_3009_()
    data class Filter(val predicate: String) : GenEvent_3009_()
}

sealed class GenState_3009_ {
    data object Idle : GenState_3009_()
    data object Loading : GenState_3009_()
    data class Success(val items: List<GenModel_3009_>) : GenState_3009_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3009_()
    data class Partial(val items: List<GenModel_3009_>, val hasMore: Boolean) : GenState_3009_()
}

interface GenRepository_3009_ {
    suspend fun getAll(): List<GenModel_3009_>
    suspend fun getById(id: Long): GenModel_3009_?
    suspend fun save(model: GenModel_3009_): GenModel_3009_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3009_>
}

@Singleton
class GenRepositoryImpl_3009_ @Inject constructor() : GenRepository_3009_ {
    private val store = mutableMapOf<Long, GenModel_3009_>()
    override suspend fun getAll(): List<GenModel_3009_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3009_? = store[id]
    override suspend fun save(model: GenModel_3009_): GenModel_3009_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3009_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3009_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3009_ @Inject constructor(
    private val repository: GenRepositoryImpl_3009_
) : GenUseCase_3009_<Unit, List<GenModel_3009_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3009_> = repository.getAll()
}

class GenSaveUseCase_3009_ @Inject constructor(
    private val repository: GenRepositoryImpl_3009_
) : GenUseCase_3009_<GenModel_3009_, GenModel_3009_> {
    override suspend fun invoke(params: GenModel_3009_): GenModel_3009_ = repository.save(params)
}

class GenDeleteUseCase_3009_ @Inject constructor(
    private val repository: GenRepositoryImpl_3009_
) : GenUseCase_3009_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3009_ @Inject constructor(
    private val repository: GenRepositoryImpl_3009_
) : GenUseCase_3009_<String, List<GenModel_3009_>> {
    override suspend fun invoke(params: String): List<GenModel_3009_> = repository.search(params)
}

abstract class GenMapper_3009_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3009_ : GenMapper_3009_<GenModel_3009_, String>() {
    override fun map(input: GenModel_3009_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3009_ : GenMapper_3009_<String, GenModel_3009_>() {
    override fun map(input: String): GenModel_3009_ {
        val parts = input.split(":")
        return GenModel_3009_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3009_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3009_,
    private val saveUseCase: GenSaveUseCase_3009_,
    private val deleteUseCase: GenDeleteUseCase_3009_,
    private val searchUseCase: GenSearchUseCase_3009_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3009_>(GenState_3009_.Idle)
    val state: StateFlow<GenState_3009_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3009_) {
        when (event) {
            is GenEvent_3009_.Load -> loadAll()
            is GenEvent_3009_.Update -> save(event.model)
            is GenEvent_3009_.Delete -> delete(event.id)
            is GenEvent_3009_.Refresh -> loadAll()
            is GenEvent_3009_.Search -> search(event.query)
            is GenEvent_3009_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3009_.Loading; _state.value = GenState_3009_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3009_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3009_.Success(searchUseCase(query)) } }
}
