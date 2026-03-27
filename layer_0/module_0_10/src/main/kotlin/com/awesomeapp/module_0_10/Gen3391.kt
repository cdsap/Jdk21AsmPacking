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

data class GenModel_3391_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3391_ {
    data class Load(val id: Long) : GenEvent_3391_()
    data class Update(val model: GenModel_3391_) : GenEvent_3391_()
    data class Delete(val id: Long) : GenEvent_3391_()
    data object Refresh : GenEvent_3391_()
    data class Search(val query: String) : GenEvent_3391_()
    data class Filter(val predicate: String) : GenEvent_3391_()
}

sealed class GenState_3391_ {
    data object Idle : GenState_3391_()
    data object Loading : GenState_3391_()
    data class Success(val items: List<GenModel_3391_>) : GenState_3391_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3391_()
    data class Partial(val items: List<GenModel_3391_>, val hasMore: Boolean) : GenState_3391_()
}

interface GenRepository_3391_ {
    suspend fun getAll(): List<GenModel_3391_>
    suspend fun getById(id: Long): GenModel_3391_?
    suspend fun save(model: GenModel_3391_): GenModel_3391_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3391_>
}

@Singleton
class GenRepositoryImpl_3391_ @Inject constructor() : GenRepository_3391_ {
    private val store = mutableMapOf<Long, GenModel_3391_>()
    override suspend fun getAll(): List<GenModel_3391_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3391_? = store[id]
    override suspend fun save(model: GenModel_3391_): GenModel_3391_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3391_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3391_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3391_ @Inject constructor(
    private val repository: GenRepositoryImpl_3391_
) : GenUseCase_3391_<Unit, List<GenModel_3391_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3391_> = repository.getAll()
}

class GenSaveUseCase_3391_ @Inject constructor(
    private val repository: GenRepositoryImpl_3391_
) : GenUseCase_3391_<GenModel_3391_, GenModel_3391_> {
    override suspend fun invoke(params: GenModel_3391_): GenModel_3391_ = repository.save(params)
}

class GenDeleteUseCase_3391_ @Inject constructor(
    private val repository: GenRepositoryImpl_3391_
) : GenUseCase_3391_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3391_ @Inject constructor(
    private val repository: GenRepositoryImpl_3391_
) : GenUseCase_3391_<String, List<GenModel_3391_>> {
    override suspend fun invoke(params: String): List<GenModel_3391_> = repository.search(params)
}

abstract class GenMapper_3391_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3391_ : GenMapper_3391_<GenModel_3391_, String>() {
    override fun map(input: GenModel_3391_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3391_ : GenMapper_3391_<String, GenModel_3391_>() {
    override fun map(input: String): GenModel_3391_ {
        val parts = input.split(":")
        return GenModel_3391_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3391_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3391_,
    private val saveUseCase: GenSaveUseCase_3391_,
    private val deleteUseCase: GenDeleteUseCase_3391_,
    private val searchUseCase: GenSearchUseCase_3391_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3391_>(GenState_3391_.Idle)
    val state: StateFlow<GenState_3391_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3391_) {
        when (event) {
            is GenEvent_3391_.Load -> loadAll()
            is GenEvent_3391_.Update -> save(event.model)
            is GenEvent_3391_.Delete -> delete(event.id)
            is GenEvent_3391_.Refresh -> loadAll()
            is GenEvent_3391_.Search -> search(event.query)
            is GenEvent_3391_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3391_.Loading; _state.value = GenState_3391_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3391_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3391_.Success(searchUseCase(query)) } }
}
