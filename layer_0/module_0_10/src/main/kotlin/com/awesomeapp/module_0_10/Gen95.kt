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

data class GenModel_95_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_95_ {
    data class Load(val id: Long) : GenEvent_95_()
    data class Update(val model: GenModel_95_) : GenEvent_95_()
    data class Delete(val id: Long) : GenEvent_95_()
    data object Refresh : GenEvent_95_()
    data class Search(val query: String) : GenEvent_95_()
    data class Filter(val predicate: String) : GenEvent_95_()
}

sealed class GenState_95_ {
    data object Idle : GenState_95_()
    data object Loading : GenState_95_()
    data class Success(val items: List<GenModel_95_>) : GenState_95_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_95_()
    data class Partial(val items: List<GenModel_95_>, val hasMore: Boolean) : GenState_95_()
}

interface GenRepository_95_ {
    suspend fun getAll(): List<GenModel_95_>
    suspend fun getById(id: Long): GenModel_95_?
    suspend fun save(model: GenModel_95_): GenModel_95_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_95_>
}

@Singleton
class GenRepositoryImpl_95_ @Inject constructor() : GenRepository_95_ {
    private val store = mutableMapOf<Long, GenModel_95_>()
    override suspend fun getAll(): List<GenModel_95_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_95_? = store[id]
    override suspend fun save(model: GenModel_95_): GenModel_95_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_95_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_95_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_95_ @Inject constructor(
    private val repository: GenRepositoryImpl_95_
) : GenUseCase_95_<Unit, List<GenModel_95_>> {
    override suspend fun invoke(params: Unit): List<GenModel_95_> = repository.getAll()
}

class GenSaveUseCase_95_ @Inject constructor(
    private val repository: GenRepositoryImpl_95_
) : GenUseCase_95_<GenModel_95_, GenModel_95_> {
    override suspend fun invoke(params: GenModel_95_): GenModel_95_ = repository.save(params)
}

class GenDeleteUseCase_95_ @Inject constructor(
    private val repository: GenRepositoryImpl_95_
) : GenUseCase_95_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_95_ @Inject constructor(
    private val repository: GenRepositoryImpl_95_
) : GenUseCase_95_<String, List<GenModel_95_>> {
    override suspend fun invoke(params: String): List<GenModel_95_> = repository.search(params)
}

abstract class GenMapper_95_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_95_ : GenMapper_95_<GenModel_95_, String>() {
    override fun map(input: GenModel_95_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_95_ : GenMapper_95_<String, GenModel_95_>() {
    override fun map(input: String): GenModel_95_ {
        val parts = input.split(":")
        return GenModel_95_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_95_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_95_,
    private val saveUseCase: GenSaveUseCase_95_,
    private val deleteUseCase: GenDeleteUseCase_95_,
    private val searchUseCase: GenSearchUseCase_95_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_95_>(GenState_95_.Idle)
    val state: StateFlow<GenState_95_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_95_) {
        when (event) {
            is GenEvent_95_.Load -> loadAll()
            is GenEvent_95_.Update -> save(event.model)
            is GenEvent_95_.Delete -> delete(event.id)
            is GenEvent_95_.Refresh -> loadAll()
            is GenEvent_95_.Search -> search(event.query)
            is GenEvent_95_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_95_.Loading; _state.value = GenState_95_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_95_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_95_.Success(searchUseCase(query)) } }
}
