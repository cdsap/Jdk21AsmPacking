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

data class GenModel_54_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_54_ {
    data class Load(val id: Long) : GenEvent_54_()
    data class Update(val model: GenModel_54_) : GenEvent_54_()
    data class Delete(val id: Long) : GenEvent_54_()
    data object Refresh : GenEvent_54_()
    data class Search(val query: String) : GenEvent_54_()
    data class Filter(val predicate: String) : GenEvent_54_()
}

sealed class GenState_54_ {
    data object Idle : GenState_54_()
    data object Loading : GenState_54_()
    data class Success(val items: List<GenModel_54_>) : GenState_54_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_54_()
    data class Partial(val items: List<GenModel_54_>, val hasMore: Boolean) : GenState_54_()
}

interface GenRepository_54_ {
    suspend fun getAll(): List<GenModel_54_>
    suspend fun getById(id: Long): GenModel_54_?
    suspend fun save(model: GenModel_54_): GenModel_54_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_54_>
}

@Singleton
class GenRepositoryImpl_54_ @Inject constructor() : GenRepository_54_ {
    private val store = mutableMapOf<Long, GenModel_54_>()
    override suspend fun getAll(): List<GenModel_54_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_54_? = store[id]
    override suspend fun save(model: GenModel_54_): GenModel_54_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_54_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_54_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_54_ @Inject constructor(
    private val repository: GenRepositoryImpl_54_
) : GenUseCase_54_<Unit, List<GenModel_54_>> {
    override suspend fun invoke(params: Unit): List<GenModel_54_> = repository.getAll()
}

class GenSaveUseCase_54_ @Inject constructor(
    private val repository: GenRepositoryImpl_54_
) : GenUseCase_54_<GenModel_54_, GenModel_54_> {
    override suspend fun invoke(params: GenModel_54_): GenModel_54_ = repository.save(params)
}

class GenDeleteUseCase_54_ @Inject constructor(
    private val repository: GenRepositoryImpl_54_
) : GenUseCase_54_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_54_ @Inject constructor(
    private val repository: GenRepositoryImpl_54_
) : GenUseCase_54_<String, List<GenModel_54_>> {
    override suspend fun invoke(params: String): List<GenModel_54_> = repository.search(params)
}

abstract class GenMapper_54_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_54_ : GenMapper_54_<GenModel_54_, String>() {
    override fun map(input: GenModel_54_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_54_ : GenMapper_54_<String, GenModel_54_>() {
    override fun map(input: String): GenModel_54_ {
        val parts = input.split(":")
        return GenModel_54_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_54_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_54_,
    private val saveUseCase: GenSaveUseCase_54_,
    private val deleteUseCase: GenDeleteUseCase_54_,
    private val searchUseCase: GenSearchUseCase_54_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_54_>(GenState_54_.Idle)
    val state: StateFlow<GenState_54_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_54_) {
        when (event) {
            is GenEvent_54_.Load -> loadAll()
            is GenEvent_54_.Update -> save(event.model)
            is GenEvent_54_.Delete -> delete(event.id)
            is GenEvent_54_.Refresh -> loadAll()
            is GenEvent_54_.Search -> search(event.query)
            is GenEvent_54_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_54_.Loading; _state.value = GenState_54_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_54_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_54_.Success(searchUseCase(query)) } }
}
