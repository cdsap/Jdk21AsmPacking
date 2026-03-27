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

data class GenModel_1039_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1039_ {
    data class Load(val id: Long) : GenEvent_1039_()
    data class Update(val model: GenModel_1039_) : GenEvent_1039_()
    data class Delete(val id: Long) : GenEvent_1039_()
    data object Refresh : GenEvent_1039_()
    data class Search(val query: String) : GenEvent_1039_()
    data class Filter(val predicate: String) : GenEvent_1039_()
}

sealed class GenState_1039_ {
    data object Idle : GenState_1039_()
    data object Loading : GenState_1039_()
    data class Success(val items: List<GenModel_1039_>) : GenState_1039_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1039_()
    data class Partial(val items: List<GenModel_1039_>, val hasMore: Boolean) : GenState_1039_()
}

interface GenRepository_1039_ {
    suspend fun getAll(): List<GenModel_1039_>
    suspend fun getById(id: Long): GenModel_1039_?
    suspend fun save(model: GenModel_1039_): GenModel_1039_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1039_>
}

@Singleton
class GenRepositoryImpl_1039_ @Inject constructor() : GenRepository_1039_ {
    private val store = mutableMapOf<Long, GenModel_1039_>()
    override suspend fun getAll(): List<GenModel_1039_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1039_? = store[id]
    override suspend fun save(model: GenModel_1039_): GenModel_1039_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1039_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1039_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1039_ @Inject constructor(
    private val repository: GenRepositoryImpl_1039_
) : GenUseCase_1039_<Unit, List<GenModel_1039_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1039_> = repository.getAll()
}

class GenSaveUseCase_1039_ @Inject constructor(
    private val repository: GenRepositoryImpl_1039_
) : GenUseCase_1039_<GenModel_1039_, GenModel_1039_> {
    override suspend fun invoke(params: GenModel_1039_): GenModel_1039_ = repository.save(params)
}

class GenDeleteUseCase_1039_ @Inject constructor(
    private val repository: GenRepositoryImpl_1039_
) : GenUseCase_1039_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1039_ @Inject constructor(
    private val repository: GenRepositoryImpl_1039_
) : GenUseCase_1039_<String, List<GenModel_1039_>> {
    override suspend fun invoke(params: String): List<GenModel_1039_> = repository.search(params)
}

abstract class GenMapper_1039_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1039_ : GenMapper_1039_<GenModel_1039_, String>() {
    override fun map(input: GenModel_1039_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1039_ : GenMapper_1039_<String, GenModel_1039_>() {
    override fun map(input: String): GenModel_1039_ {
        val parts = input.split(":")
        return GenModel_1039_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1039_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1039_,
    private val saveUseCase: GenSaveUseCase_1039_,
    private val deleteUseCase: GenDeleteUseCase_1039_,
    private val searchUseCase: GenSearchUseCase_1039_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1039_>(GenState_1039_.Idle)
    val state: StateFlow<GenState_1039_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1039_) {
        when (event) {
            is GenEvent_1039_.Load -> loadAll()
            is GenEvent_1039_.Update -> save(event.model)
            is GenEvent_1039_.Delete -> delete(event.id)
            is GenEvent_1039_.Refresh -> loadAll()
            is GenEvent_1039_.Search -> search(event.query)
            is GenEvent_1039_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1039_.Loading; _state.value = GenState_1039_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1039_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1039_.Success(searchUseCase(query)) } }
}
