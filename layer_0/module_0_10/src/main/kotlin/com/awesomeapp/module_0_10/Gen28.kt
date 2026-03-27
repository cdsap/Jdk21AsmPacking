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

data class GenModel_28_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_28_ {
    data class Load(val id: Long) : GenEvent_28_()
    data class Update(val model: GenModel_28_) : GenEvent_28_()
    data class Delete(val id: Long) : GenEvent_28_()
    data object Refresh : GenEvent_28_()
    data class Search(val query: String) : GenEvent_28_()
    data class Filter(val predicate: String) : GenEvent_28_()
}

sealed class GenState_28_ {
    data object Idle : GenState_28_()
    data object Loading : GenState_28_()
    data class Success(val items: List<GenModel_28_>) : GenState_28_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_28_()
    data class Partial(val items: List<GenModel_28_>, val hasMore: Boolean) : GenState_28_()
}

interface GenRepository_28_ {
    suspend fun getAll(): List<GenModel_28_>
    suspend fun getById(id: Long): GenModel_28_?
    suspend fun save(model: GenModel_28_): GenModel_28_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_28_>
}

@Singleton
class GenRepositoryImpl_28_ @Inject constructor() : GenRepository_28_ {
    private val store = mutableMapOf<Long, GenModel_28_>()
    override suspend fun getAll(): List<GenModel_28_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_28_? = store[id]
    override suspend fun save(model: GenModel_28_): GenModel_28_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_28_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_28_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_28_ @Inject constructor(
    private val repository: GenRepositoryImpl_28_
) : GenUseCase_28_<Unit, List<GenModel_28_>> {
    override suspend fun invoke(params: Unit): List<GenModel_28_> = repository.getAll()
}

class GenSaveUseCase_28_ @Inject constructor(
    private val repository: GenRepositoryImpl_28_
) : GenUseCase_28_<GenModel_28_, GenModel_28_> {
    override suspend fun invoke(params: GenModel_28_): GenModel_28_ = repository.save(params)
}

class GenDeleteUseCase_28_ @Inject constructor(
    private val repository: GenRepositoryImpl_28_
) : GenUseCase_28_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_28_ @Inject constructor(
    private val repository: GenRepositoryImpl_28_
) : GenUseCase_28_<String, List<GenModel_28_>> {
    override suspend fun invoke(params: String): List<GenModel_28_> = repository.search(params)
}

abstract class GenMapper_28_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_28_ : GenMapper_28_<GenModel_28_, String>() {
    override fun map(input: GenModel_28_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_28_ : GenMapper_28_<String, GenModel_28_>() {
    override fun map(input: String): GenModel_28_ {
        val parts = input.split(":")
        return GenModel_28_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_28_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_28_,
    private val saveUseCase: GenSaveUseCase_28_,
    private val deleteUseCase: GenDeleteUseCase_28_,
    private val searchUseCase: GenSearchUseCase_28_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_28_>(GenState_28_.Idle)
    val state: StateFlow<GenState_28_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_28_) {
        when (event) {
            is GenEvent_28_.Load -> loadAll()
            is GenEvent_28_.Update -> save(event.model)
            is GenEvent_28_.Delete -> delete(event.id)
            is GenEvent_28_.Refresh -> loadAll()
            is GenEvent_28_.Search -> search(event.query)
            is GenEvent_28_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_28_.Loading; _state.value = GenState_28_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_28_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_28_.Success(searchUseCase(query)) } }
}
