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

data class GenModel_825_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_825_ {
    data class Load(val id: Long) : GenEvent_825_()
    data class Update(val model: GenModel_825_) : GenEvent_825_()
    data class Delete(val id: Long) : GenEvent_825_()
    data object Refresh : GenEvent_825_()
    data class Search(val query: String) : GenEvent_825_()
    data class Filter(val predicate: String) : GenEvent_825_()
}

sealed class GenState_825_ {
    data object Idle : GenState_825_()
    data object Loading : GenState_825_()
    data class Success(val items: List<GenModel_825_>) : GenState_825_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_825_()
    data class Partial(val items: List<GenModel_825_>, val hasMore: Boolean) : GenState_825_()
}

interface GenRepository_825_ {
    suspend fun getAll(): List<GenModel_825_>
    suspend fun getById(id: Long): GenModel_825_?
    suspend fun save(model: GenModel_825_): GenModel_825_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_825_>
}

@Singleton
class GenRepositoryImpl_825_ @Inject constructor() : GenRepository_825_ {
    private val store = mutableMapOf<Long, GenModel_825_>()
    override suspend fun getAll(): List<GenModel_825_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_825_? = store[id]
    override suspend fun save(model: GenModel_825_): GenModel_825_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_825_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_825_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_825_ @Inject constructor(
    private val repository: GenRepositoryImpl_825_
) : GenUseCase_825_<Unit, List<GenModel_825_>> {
    override suspend fun invoke(params: Unit): List<GenModel_825_> = repository.getAll()
}

class GenSaveUseCase_825_ @Inject constructor(
    private val repository: GenRepositoryImpl_825_
) : GenUseCase_825_<GenModel_825_, GenModel_825_> {
    override suspend fun invoke(params: GenModel_825_): GenModel_825_ = repository.save(params)
}

class GenDeleteUseCase_825_ @Inject constructor(
    private val repository: GenRepositoryImpl_825_
) : GenUseCase_825_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_825_ @Inject constructor(
    private val repository: GenRepositoryImpl_825_
) : GenUseCase_825_<String, List<GenModel_825_>> {
    override suspend fun invoke(params: String): List<GenModel_825_> = repository.search(params)
}

abstract class GenMapper_825_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_825_ : GenMapper_825_<GenModel_825_, String>() {
    override fun map(input: GenModel_825_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_825_ : GenMapper_825_<String, GenModel_825_>() {
    override fun map(input: String): GenModel_825_ {
        val parts = input.split(":")
        return GenModel_825_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_825_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_825_,
    private val saveUseCase: GenSaveUseCase_825_,
    private val deleteUseCase: GenDeleteUseCase_825_,
    private val searchUseCase: GenSearchUseCase_825_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_825_>(GenState_825_.Idle)
    val state: StateFlow<GenState_825_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_825_) {
        when (event) {
            is GenEvent_825_.Load -> loadAll()
            is GenEvent_825_.Update -> save(event.model)
            is GenEvent_825_.Delete -> delete(event.id)
            is GenEvent_825_.Refresh -> loadAll()
            is GenEvent_825_.Search -> search(event.query)
            is GenEvent_825_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_825_.Loading; _state.value = GenState_825_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_825_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_825_.Success(searchUseCase(query)) } }
}
