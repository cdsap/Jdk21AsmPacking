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

data class GenModel_290_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_290_ {
    data class Load(val id: Long) : GenEvent_290_()
    data class Update(val model: GenModel_290_) : GenEvent_290_()
    data class Delete(val id: Long) : GenEvent_290_()
    data object Refresh : GenEvent_290_()
    data class Search(val query: String) : GenEvent_290_()
    data class Filter(val predicate: String) : GenEvent_290_()
}

sealed class GenState_290_ {
    data object Idle : GenState_290_()
    data object Loading : GenState_290_()
    data class Success(val items: List<GenModel_290_>) : GenState_290_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_290_()
    data class Partial(val items: List<GenModel_290_>, val hasMore: Boolean) : GenState_290_()
}

interface GenRepository_290_ {
    suspend fun getAll(): List<GenModel_290_>
    suspend fun getById(id: Long): GenModel_290_?
    suspend fun save(model: GenModel_290_): GenModel_290_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_290_>
}

@Singleton
class GenRepositoryImpl_290_ @Inject constructor() : GenRepository_290_ {
    private val store = mutableMapOf<Long, GenModel_290_>()
    override suspend fun getAll(): List<GenModel_290_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_290_? = store[id]
    override suspend fun save(model: GenModel_290_): GenModel_290_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_290_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_290_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_290_ @Inject constructor(
    private val repository: GenRepositoryImpl_290_
) : GenUseCase_290_<Unit, List<GenModel_290_>> {
    override suspend fun invoke(params: Unit): List<GenModel_290_> = repository.getAll()
}

class GenSaveUseCase_290_ @Inject constructor(
    private val repository: GenRepositoryImpl_290_
) : GenUseCase_290_<GenModel_290_, GenModel_290_> {
    override suspend fun invoke(params: GenModel_290_): GenModel_290_ = repository.save(params)
}

class GenDeleteUseCase_290_ @Inject constructor(
    private val repository: GenRepositoryImpl_290_
) : GenUseCase_290_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_290_ @Inject constructor(
    private val repository: GenRepositoryImpl_290_
) : GenUseCase_290_<String, List<GenModel_290_>> {
    override suspend fun invoke(params: String): List<GenModel_290_> = repository.search(params)
}

abstract class GenMapper_290_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_290_ : GenMapper_290_<GenModel_290_, String>() {
    override fun map(input: GenModel_290_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_290_ : GenMapper_290_<String, GenModel_290_>() {
    override fun map(input: String): GenModel_290_ {
        val parts = input.split(":")
        return GenModel_290_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_290_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_290_,
    private val saveUseCase: GenSaveUseCase_290_,
    private val deleteUseCase: GenDeleteUseCase_290_,
    private val searchUseCase: GenSearchUseCase_290_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_290_>(GenState_290_.Idle)
    val state: StateFlow<GenState_290_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_290_) {
        when (event) {
            is GenEvent_290_.Load -> loadAll()
            is GenEvent_290_.Update -> save(event.model)
            is GenEvent_290_.Delete -> delete(event.id)
            is GenEvent_290_.Refresh -> loadAll()
            is GenEvent_290_.Search -> search(event.query)
            is GenEvent_290_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_290_.Loading; _state.value = GenState_290_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_290_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_290_.Success(searchUseCase(query)) } }
}
