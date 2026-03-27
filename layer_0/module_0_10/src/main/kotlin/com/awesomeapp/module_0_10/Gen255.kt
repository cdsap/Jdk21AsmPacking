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

data class GenModel_255_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_255_ {
    data class Load(val id: Long) : GenEvent_255_()
    data class Update(val model: GenModel_255_) : GenEvent_255_()
    data class Delete(val id: Long) : GenEvent_255_()
    data object Refresh : GenEvent_255_()
    data class Search(val query: String) : GenEvent_255_()
    data class Filter(val predicate: String) : GenEvent_255_()
}

sealed class GenState_255_ {
    data object Idle : GenState_255_()
    data object Loading : GenState_255_()
    data class Success(val items: List<GenModel_255_>) : GenState_255_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_255_()
    data class Partial(val items: List<GenModel_255_>, val hasMore: Boolean) : GenState_255_()
}

interface GenRepository_255_ {
    suspend fun getAll(): List<GenModel_255_>
    suspend fun getById(id: Long): GenModel_255_?
    suspend fun save(model: GenModel_255_): GenModel_255_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_255_>
}

@Singleton
class GenRepositoryImpl_255_ @Inject constructor() : GenRepository_255_ {
    private val store = mutableMapOf<Long, GenModel_255_>()
    override suspend fun getAll(): List<GenModel_255_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_255_? = store[id]
    override suspend fun save(model: GenModel_255_): GenModel_255_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_255_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_255_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_255_ @Inject constructor(
    private val repository: GenRepositoryImpl_255_
) : GenUseCase_255_<Unit, List<GenModel_255_>> {
    override suspend fun invoke(params: Unit): List<GenModel_255_> = repository.getAll()
}

class GenSaveUseCase_255_ @Inject constructor(
    private val repository: GenRepositoryImpl_255_
) : GenUseCase_255_<GenModel_255_, GenModel_255_> {
    override suspend fun invoke(params: GenModel_255_): GenModel_255_ = repository.save(params)
}

class GenDeleteUseCase_255_ @Inject constructor(
    private val repository: GenRepositoryImpl_255_
) : GenUseCase_255_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_255_ @Inject constructor(
    private val repository: GenRepositoryImpl_255_
) : GenUseCase_255_<String, List<GenModel_255_>> {
    override suspend fun invoke(params: String): List<GenModel_255_> = repository.search(params)
}

abstract class GenMapper_255_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_255_ : GenMapper_255_<GenModel_255_, String>() {
    override fun map(input: GenModel_255_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_255_ : GenMapper_255_<String, GenModel_255_>() {
    override fun map(input: String): GenModel_255_ {
        val parts = input.split(":")
        return GenModel_255_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_255_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_255_,
    private val saveUseCase: GenSaveUseCase_255_,
    private val deleteUseCase: GenDeleteUseCase_255_,
    private val searchUseCase: GenSearchUseCase_255_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_255_>(GenState_255_.Idle)
    val state: StateFlow<GenState_255_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_255_) {
        when (event) {
            is GenEvent_255_.Load -> loadAll()
            is GenEvent_255_.Update -> save(event.model)
            is GenEvent_255_.Delete -> delete(event.id)
            is GenEvent_255_.Refresh -> loadAll()
            is GenEvent_255_.Search -> search(event.query)
            is GenEvent_255_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_255_.Loading; _state.value = GenState_255_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_255_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_255_.Success(searchUseCase(query)) } }
}
