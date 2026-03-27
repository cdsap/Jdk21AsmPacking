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

data class GenModel_328_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_328_ {
    data class Load(val id: Long) : GenEvent_328_()
    data class Update(val model: GenModel_328_) : GenEvent_328_()
    data class Delete(val id: Long) : GenEvent_328_()
    data object Refresh : GenEvent_328_()
    data class Search(val query: String) : GenEvent_328_()
    data class Filter(val predicate: String) : GenEvent_328_()
}

sealed class GenState_328_ {
    data object Idle : GenState_328_()
    data object Loading : GenState_328_()
    data class Success(val items: List<GenModel_328_>) : GenState_328_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_328_()
    data class Partial(val items: List<GenModel_328_>, val hasMore: Boolean) : GenState_328_()
}

interface GenRepository_328_ {
    suspend fun getAll(): List<GenModel_328_>
    suspend fun getById(id: Long): GenModel_328_?
    suspend fun save(model: GenModel_328_): GenModel_328_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_328_>
}

@Singleton
class GenRepositoryImpl_328_ @Inject constructor() : GenRepository_328_ {
    private val store = mutableMapOf<Long, GenModel_328_>()
    override suspend fun getAll(): List<GenModel_328_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_328_? = store[id]
    override suspend fun save(model: GenModel_328_): GenModel_328_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_328_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_328_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_328_ @Inject constructor(
    private val repository: GenRepositoryImpl_328_
) : GenUseCase_328_<Unit, List<GenModel_328_>> {
    override suspend fun invoke(params: Unit): List<GenModel_328_> = repository.getAll()
}

class GenSaveUseCase_328_ @Inject constructor(
    private val repository: GenRepositoryImpl_328_
) : GenUseCase_328_<GenModel_328_, GenModel_328_> {
    override suspend fun invoke(params: GenModel_328_): GenModel_328_ = repository.save(params)
}

class GenDeleteUseCase_328_ @Inject constructor(
    private val repository: GenRepositoryImpl_328_
) : GenUseCase_328_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_328_ @Inject constructor(
    private val repository: GenRepositoryImpl_328_
) : GenUseCase_328_<String, List<GenModel_328_>> {
    override suspend fun invoke(params: String): List<GenModel_328_> = repository.search(params)
}

abstract class GenMapper_328_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_328_ : GenMapper_328_<GenModel_328_, String>() {
    override fun map(input: GenModel_328_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_328_ : GenMapper_328_<String, GenModel_328_>() {
    override fun map(input: String): GenModel_328_ {
        val parts = input.split(":")
        return GenModel_328_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_328_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_328_,
    private val saveUseCase: GenSaveUseCase_328_,
    private val deleteUseCase: GenDeleteUseCase_328_,
    private val searchUseCase: GenSearchUseCase_328_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_328_>(GenState_328_.Idle)
    val state: StateFlow<GenState_328_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_328_) {
        when (event) {
            is GenEvent_328_.Load -> loadAll()
            is GenEvent_328_.Update -> save(event.model)
            is GenEvent_328_.Delete -> delete(event.id)
            is GenEvent_328_.Refresh -> loadAll()
            is GenEvent_328_.Search -> search(event.query)
            is GenEvent_328_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_328_.Loading; _state.value = GenState_328_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_328_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_328_.Success(searchUseCase(query)) } }
}
