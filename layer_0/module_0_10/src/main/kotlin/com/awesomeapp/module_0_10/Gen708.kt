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

data class GenModel_708_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_708_ {
    data class Load(val id: Long) : GenEvent_708_()
    data class Update(val model: GenModel_708_) : GenEvent_708_()
    data class Delete(val id: Long) : GenEvent_708_()
    data object Refresh : GenEvent_708_()
    data class Search(val query: String) : GenEvent_708_()
    data class Filter(val predicate: String) : GenEvent_708_()
}

sealed class GenState_708_ {
    data object Idle : GenState_708_()
    data object Loading : GenState_708_()
    data class Success(val items: List<GenModel_708_>) : GenState_708_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_708_()
    data class Partial(val items: List<GenModel_708_>, val hasMore: Boolean) : GenState_708_()
}

interface GenRepository_708_ {
    suspend fun getAll(): List<GenModel_708_>
    suspend fun getById(id: Long): GenModel_708_?
    suspend fun save(model: GenModel_708_): GenModel_708_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_708_>
}

@Singleton
class GenRepositoryImpl_708_ @Inject constructor() : GenRepository_708_ {
    private val store = mutableMapOf<Long, GenModel_708_>()
    override suspend fun getAll(): List<GenModel_708_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_708_? = store[id]
    override suspend fun save(model: GenModel_708_): GenModel_708_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_708_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_708_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_708_ @Inject constructor(
    private val repository: GenRepositoryImpl_708_
) : GenUseCase_708_<Unit, List<GenModel_708_>> {
    override suspend fun invoke(params: Unit): List<GenModel_708_> = repository.getAll()
}

class GenSaveUseCase_708_ @Inject constructor(
    private val repository: GenRepositoryImpl_708_
) : GenUseCase_708_<GenModel_708_, GenModel_708_> {
    override suspend fun invoke(params: GenModel_708_): GenModel_708_ = repository.save(params)
}

class GenDeleteUseCase_708_ @Inject constructor(
    private val repository: GenRepositoryImpl_708_
) : GenUseCase_708_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_708_ @Inject constructor(
    private val repository: GenRepositoryImpl_708_
) : GenUseCase_708_<String, List<GenModel_708_>> {
    override suspend fun invoke(params: String): List<GenModel_708_> = repository.search(params)
}

abstract class GenMapper_708_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_708_ : GenMapper_708_<GenModel_708_, String>() {
    override fun map(input: GenModel_708_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_708_ : GenMapper_708_<String, GenModel_708_>() {
    override fun map(input: String): GenModel_708_ {
        val parts = input.split(":")
        return GenModel_708_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_708_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_708_,
    private val saveUseCase: GenSaveUseCase_708_,
    private val deleteUseCase: GenDeleteUseCase_708_,
    private val searchUseCase: GenSearchUseCase_708_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_708_>(GenState_708_.Idle)
    val state: StateFlow<GenState_708_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_708_) {
        when (event) {
            is GenEvent_708_.Load -> loadAll()
            is GenEvent_708_.Update -> save(event.model)
            is GenEvent_708_.Delete -> delete(event.id)
            is GenEvent_708_.Refresh -> loadAll()
            is GenEvent_708_.Search -> search(event.query)
            is GenEvent_708_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_708_.Loading; _state.value = GenState_708_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_708_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_708_.Success(searchUseCase(query)) } }
}
