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

data class GenModel_1171_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1171_ {
    data class Load(val id: Long) : GenEvent_1171_()
    data class Update(val model: GenModel_1171_) : GenEvent_1171_()
    data class Delete(val id: Long) : GenEvent_1171_()
    data object Refresh : GenEvent_1171_()
    data class Search(val query: String) : GenEvent_1171_()
    data class Filter(val predicate: String) : GenEvent_1171_()
}

sealed class GenState_1171_ {
    data object Idle : GenState_1171_()
    data object Loading : GenState_1171_()
    data class Success(val items: List<GenModel_1171_>) : GenState_1171_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1171_()
    data class Partial(val items: List<GenModel_1171_>, val hasMore: Boolean) : GenState_1171_()
}

interface GenRepository_1171_ {
    suspend fun getAll(): List<GenModel_1171_>
    suspend fun getById(id: Long): GenModel_1171_?
    suspend fun save(model: GenModel_1171_): GenModel_1171_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1171_>
}

@Singleton
class GenRepositoryImpl_1171_ @Inject constructor() : GenRepository_1171_ {
    private val store = mutableMapOf<Long, GenModel_1171_>()
    override suspend fun getAll(): List<GenModel_1171_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1171_? = store[id]
    override suspend fun save(model: GenModel_1171_): GenModel_1171_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1171_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1171_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1171_ @Inject constructor(
    private val repository: GenRepositoryImpl_1171_
) : GenUseCase_1171_<Unit, List<GenModel_1171_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1171_> = repository.getAll()
}

class GenSaveUseCase_1171_ @Inject constructor(
    private val repository: GenRepositoryImpl_1171_
) : GenUseCase_1171_<GenModel_1171_, GenModel_1171_> {
    override suspend fun invoke(params: GenModel_1171_): GenModel_1171_ = repository.save(params)
}

class GenDeleteUseCase_1171_ @Inject constructor(
    private val repository: GenRepositoryImpl_1171_
) : GenUseCase_1171_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1171_ @Inject constructor(
    private val repository: GenRepositoryImpl_1171_
) : GenUseCase_1171_<String, List<GenModel_1171_>> {
    override suspend fun invoke(params: String): List<GenModel_1171_> = repository.search(params)
}

abstract class GenMapper_1171_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1171_ : GenMapper_1171_<GenModel_1171_, String>() {
    override fun map(input: GenModel_1171_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1171_ : GenMapper_1171_<String, GenModel_1171_>() {
    override fun map(input: String): GenModel_1171_ {
        val parts = input.split(":")
        return GenModel_1171_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1171_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1171_,
    private val saveUseCase: GenSaveUseCase_1171_,
    private val deleteUseCase: GenDeleteUseCase_1171_,
    private val searchUseCase: GenSearchUseCase_1171_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1171_>(GenState_1171_.Idle)
    val state: StateFlow<GenState_1171_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1171_) {
        when (event) {
            is GenEvent_1171_.Load -> loadAll()
            is GenEvent_1171_.Update -> save(event.model)
            is GenEvent_1171_.Delete -> delete(event.id)
            is GenEvent_1171_.Refresh -> loadAll()
            is GenEvent_1171_.Search -> search(event.query)
            is GenEvent_1171_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1171_.Loading; _state.value = GenState_1171_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1171_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1171_.Success(searchUseCase(query)) } }
}
