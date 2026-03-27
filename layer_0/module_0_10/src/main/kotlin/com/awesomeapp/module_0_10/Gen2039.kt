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

data class GenModel_2039_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2039_ {
    data class Load(val id: Long) : GenEvent_2039_()
    data class Update(val model: GenModel_2039_) : GenEvent_2039_()
    data class Delete(val id: Long) : GenEvent_2039_()
    data object Refresh : GenEvent_2039_()
    data class Search(val query: String) : GenEvent_2039_()
    data class Filter(val predicate: String) : GenEvent_2039_()
}

sealed class GenState_2039_ {
    data object Idle : GenState_2039_()
    data object Loading : GenState_2039_()
    data class Success(val items: List<GenModel_2039_>) : GenState_2039_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2039_()
    data class Partial(val items: List<GenModel_2039_>, val hasMore: Boolean) : GenState_2039_()
}

interface GenRepository_2039_ {
    suspend fun getAll(): List<GenModel_2039_>
    suspend fun getById(id: Long): GenModel_2039_?
    suspend fun save(model: GenModel_2039_): GenModel_2039_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2039_>
}

@Singleton
class GenRepositoryImpl_2039_ @Inject constructor() : GenRepository_2039_ {
    private val store = mutableMapOf<Long, GenModel_2039_>()
    override suspend fun getAll(): List<GenModel_2039_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2039_? = store[id]
    override suspend fun save(model: GenModel_2039_): GenModel_2039_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2039_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2039_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2039_ @Inject constructor(
    private val repository: GenRepositoryImpl_2039_
) : GenUseCase_2039_<Unit, List<GenModel_2039_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2039_> = repository.getAll()
}

class GenSaveUseCase_2039_ @Inject constructor(
    private val repository: GenRepositoryImpl_2039_
) : GenUseCase_2039_<GenModel_2039_, GenModel_2039_> {
    override suspend fun invoke(params: GenModel_2039_): GenModel_2039_ = repository.save(params)
}

class GenDeleteUseCase_2039_ @Inject constructor(
    private val repository: GenRepositoryImpl_2039_
) : GenUseCase_2039_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2039_ @Inject constructor(
    private val repository: GenRepositoryImpl_2039_
) : GenUseCase_2039_<String, List<GenModel_2039_>> {
    override suspend fun invoke(params: String): List<GenModel_2039_> = repository.search(params)
}

abstract class GenMapper_2039_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2039_ : GenMapper_2039_<GenModel_2039_, String>() {
    override fun map(input: GenModel_2039_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2039_ : GenMapper_2039_<String, GenModel_2039_>() {
    override fun map(input: String): GenModel_2039_ {
        val parts = input.split(":")
        return GenModel_2039_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2039_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2039_,
    private val saveUseCase: GenSaveUseCase_2039_,
    private val deleteUseCase: GenDeleteUseCase_2039_,
    private val searchUseCase: GenSearchUseCase_2039_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2039_>(GenState_2039_.Idle)
    val state: StateFlow<GenState_2039_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2039_) {
        when (event) {
            is GenEvent_2039_.Load -> loadAll()
            is GenEvent_2039_.Update -> save(event.model)
            is GenEvent_2039_.Delete -> delete(event.id)
            is GenEvent_2039_.Refresh -> loadAll()
            is GenEvent_2039_.Search -> search(event.query)
            is GenEvent_2039_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2039_.Loading; _state.value = GenState_2039_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2039_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2039_.Success(searchUseCase(query)) } }
}
