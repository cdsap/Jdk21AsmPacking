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

data class GenModel_126_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_126_ {
    data class Load(val id: Long) : GenEvent_126_()
    data class Update(val model: GenModel_126_) : GenEvent_126_()
    data class Delete(val id: Long) : GenEvent_126_()
    data object Refresh : GenEvent_126_()
    data class Search(val query: String) : GenEvent_126_()
    data class Filter(val predicate: String) : GenEvent_126_()
}

sealed class GenState_126_ {
    data object Idle : GenState_126_()
    data object Loading : GenState_126_()
    data class Success(val items: List<GenModel_126_>) : GenState_126_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_126_()
    data class Partial(val items: List<GenModel_126_>, val hasMore: Boolean) : GenState_126_()
}

interface GenRepository_126_ {
    suspend fun getAll(): List<GenModel_126_>
    suspend fun getById(id: Long): GenModel_126_?
    suspend fun save(model: GenModel_126_): GenModel_126_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_126_>
}

@Singleton
class GenRepositoryImpl_126_ @Inject constructor() : GenRepository_126_ {
    private val store = mutableMapOf<Long, GenModel_126_>()
    override suspend fun getAll(): List<GenModel_126_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_126_? = store[id]
    override suspend fun save(model: GenModel_126_): GenModel_126_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_126_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_126_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_126_ @Inject constructor(
    private val repository: GenRepositoryImpl_126_
) : GenUseCase_126_<Unit, List<GenModel_126_>> {
    override suspend fun invoke(params: Unit): List<GenModel_126_> = repository.getAll()
}

class GenSaveUseCase_126_ @Inject constructor(
    private val repository: GenRepositoryImpl_126_
) : GenUseCase_126_<GenModel_126_, GenModel_126_> {
    override suspend fun invoke(params: GenModel_126_): GenModel_126_ = repository.save(params)
}

class GenDeleteUseCase_126_ @Inject constructor(
    private val repository: GenRepositoryImpl_126_
) : GenUseCase_126_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_126_ @Inject constructor(
    private val repository: GenRepositoryImpl_126_
) : GenUseCase_126_<String, List<GenModel_126_>> {
    override suspend fun invoke(params: String): List<GenModel_126_> = repository.search(params)
}

abstract class GenMapper_126_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_126_ : GenMapper_126_<GenModel_126_, String>() {
    override fun map(input: GenModel_126_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_126_ : GenMapper_126_<String, GenModel_126_>() {
    override fun map(input: String): GenModel_126_ {
        val parts = input.split(":")
        return GenModel_126_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_126_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_126_,
    private val saveUseCase: GenSaveUseCase_126_,
    private val deleteUseCase: GenDeleteUseCase_126_,
    private val searchUseCase: GenSearchUseCase_126_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_126_>(GenState_126_.Idle)
    val state: StateFlow<GenState_126_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_126_) {
        when (event) {
            is GenEvent_126_.Load -> loadAll()
            is GenEvent_126_.Update -> save(event.model)
            is GenEvent_126_.Delete -> delete(event.id)
            is GenEvent_126_.Refresh -> loadAll()
            is GenEvent_126_.Search -> search(event.query)
            is GenEvent_126_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_126_.Loading; _state.value = GenState_126_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_126_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_126_.Success(searchUseCase(query)) } }
}
