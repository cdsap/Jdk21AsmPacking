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

data class GenModel_943_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_943_ {
    data class Load(val id: Long) : GenEvent_943_()
    data class Update(val model: GenModel_943_) : GenEvent_943_()
    data class Delete(val id: Long) : GenEvent_943_()
    data object Refresh : GenEvent_943_()
    data class Search(val query: String) : GenEvent_943_()
    data class Filter(val predicate: String) : GenEvent_943_()
}

sealed class GenState_943_ {
    data object Idle : GenState_943_()
    data object Loading : GenState_943_()
    data class Success(val items: List<GenModel_943_>) : GenState_943_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_943_()
    data class Partial(val items: List<GenModel_943_>, val hasMore: Boolean) : GenState_943_()
}

interface GenRepository_943_ {
    suspend fun getAll(): List<GenModel_943_>
    suspend fun getById(id: Long): GenModel_943_?
    suspend fun save(model: GenModel_943_): GenModel_943_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_943_>
}

@Singleton
class GenRepositoryImpl_943_ @Inject constructor() : GenRepository_943_ {
    private val store = mutableMapOf<Long, GenModel_943_>()
    override suspend fun getAll(): List<GenModel_943_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_943_? = store[id]
    override suspend fun save(model: GenModel_943_): GenModel_943_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_943_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_943_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_943_ @Inject constructor(
    private val repository: GenRepositoryImpl_943_
) : GenUseCase_943_<Unit, List<GenModel_943_>> {
    override suspend fun invoke(params: Unit): List<GenModel_943_> = repository.getAll()
}

class GenSaveUseCase_943_ @Inject constructor(
    private val repository: GenRepositoryImpl_943_
) : GenUseCase_943_<GenModel_943_, GenModel_943_> {
    override suspend fun invoke(params: GenModel_943_): GenModel_943_ = repository.save(params)
}

class GenDeleteUseCase_943_ @Inject constructor(
    private val repository: GenRepositoryImpl_943_
) : GenUseCase_943_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_943_ @Inject constructor(
    private val repository: GenRepositoryImpl_943_
) : GenUseCase_943_<String, List<GenModel_943_>> {
    override suspend fun invoke(params: String): List<GenModel_943_> = repository.search(params)
}

abstract class GenMapper_943_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_943_ : GenMapper_943_<GenModel_943_, String>() {
    override fun map(input: GenModel_943_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_943_ : GenMapper_943_<String, GenModel_943_>() {
    override fun map(input: String): GenModel_943_ {
        val parts = input.split(":")
        return GenModel_943_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_943_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_943_,
    private val saveUseCase: GenSaveUseCase_943_,
    private val deleteUseCase: GenDeleteUseCase_943_,
    private val searchUseCase: GenSearchUseCase_943_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_943_>(GenState_943_.Idle)
    val state: StateFlow<GenState_943_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_943_) {
        when (event) {
            is GenEvent_943_.Load -> loadAll()
            is GenEvent_943_.Update -> save(event.model)
            is GenEvent_943_.Delete -> delete(event.id)
            is GenEvent_943_.Refresh -> loadAll()
            is GenEvent_943_.Search -> search(event.query)
            is GenEvent_943_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_943_.Loading; _state.value = GenState_943_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_943_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_943_.Success(searchUseCase(query)) } }
}
