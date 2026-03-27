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

data class GenModel_2328_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2328_ {
    data class Load(val id: Long) : GenEvent_2328_()
    data class Update(val model: GenModel_2328_) : GenEvent_2328_()
    data class Delete(val id: Long) : GenEvent_2328_()
    data object Refresh : GenEvent_2328_()
    data class Search(val query: String) : GenEvent_2328_()
    data class Filter(val predicate: String) : GenEvent_2328_()
}

sealed class GenState_2328_ {
    data object Idle : GenState_2328_()
    data object Loading : GenState_2328_()
    data class Success(val items: List<GenModel_2328_>) : GenState_2328_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2328_()
    data class Partial(val items: List<GenModel_2328_>, val hasMore: Boolean) : GenState_2328_()
}

interface GenRepository_2328_ {
    suspend fun getAll(): List<GenModel_2328_>
    suspend fun getById(id: Long): GenModel_2328_?
    suspend fun save(model: GenModel_2328_): GenModel_2328_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2328_>
}

@Singleton
class GenRepositoryImpl_2328_ @Inject constructor() : GenRepository_2328_ {
    private val store = mutableMapOf<Long, GenModel_2328_>()
    override suspend fun getAll(): List<GenModel_2328_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2328_? = store[id]
    override suspend fun save(model: GenModel_2328_): GenModel_2328_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2328_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2328_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2328_ @Inject constructor(
    private val repository: GenRepositoryImpl_2328_
) : GenUseCase_2328_<Unit, List<GenModel_2328_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2328_> = repository.getAll()
}

class GenSaveUseCase_2328_ @Inject constructor(
    private val repository: GenRepositoryImpl_2328_
) : GenUseCase_2328_<GenModel_2328_, GenModel_2328_> {
    override suspend fun invoke(params: GenModel_2328_): GenModel_2328_ = repository.save(params)
}

class GenDeleteUseCase_2328_ @Inject constructor(
    private val repository: GenRepositoryImpl_2328_
) : GenUseCase_2328_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2328_ @Inject constructor(
    private val repository: GenRepositoryImpl_2328_
) : GenUseCase_2328_<String, List<GenModel_2328_>> {
    override suspend fun invoke(params: String): List<GenModel_2328_> = repository.search(params)
}

abstract class GenMapper_2328_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2328_ : GenMapper_2328_<GenModel_2328_, String>() {
    override fun map(input: GenModel_2328_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2328_ : GenMapper_2328_<String, GenModel_2328_>() {
    override fun map(input: String): GenModel_2328_ {
        val parts = input.split(":")
        return GenModel_2328_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2328_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2328_,
    private val saveUseCase: GenSaveUseCase_2328_,
    private val deleteUseCase: GenDeleteUseCase_2328_,
    private val searchUseCase: GenSearchUseCase_2328_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2328_>(GenState_2328_.Idle)
    val state: StateFlow<GenState_2328_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2328_) {
        when (event) {
            is GenEvent_2328_.Load -> loadAll()
            is GenEvent_2328_.Update -> save(event.model)
            is GenEvent_2328_.Delete -> delete(event.id)
            is GenEvent_2328_.Refresh -> loadAll()
            is GenEvent_2328_.Search -> search(event.query)
            is GenEvent_2328_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2328_.Loading; _state.value = GenState_2328_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2328_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2328_.Success(searchUseCase(query)) } }
}
