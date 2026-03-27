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

data class GenModel_3099_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3099_ {
    data class Load(val id: Long) : GenEvent_3099_()
    data class Update(val model: GenModel_3099_) : GenEvent_3099_()
    data class Delete(val id: Long) : GenEvent_3099_()
    data object Refresh : GenEvent_3099_()
    data class Search(val query: String) : GenEvent_3099_()
    data class Filter(val predicate: String) : GenEvent_3099_()
}

sealed class GenState_3099_ {
    data object Idle : GenState_3099_()
    data object Loading : GenState_3099_()
    data class Success(val items: List<GenModel_3099_>) : GenState_3099_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3099_()
    data class Partial(val items: List<GenModel_3099_>, val hasMore: Boolean) : GenState_3099_()
}

interface GenRepository_3099_ {
    suspend fun getAll(): List<GenModel_3099_>
    suspend fun getById(id: Long): GenModel_3099_?
    suspend fun save(model: GenModel_3099_): GenModel_3099_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3099_>
}

@Singleton
class GenRepositoryImpl_3099_ @Inject constructor() : GenRepository_3099_ {
    private val store = mutableMapOf<Long, GenModel_3099_>()
    override suspend fun getAll(): List<GenModel_3099_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3099_? = store[id]
    override suspend fun save(model: GenModel_3099_): GenModel_3099_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3099_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3099_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3099_ @Inject constructor(
    private val repository: GenRepositoryImpl_3099_
) : GenUseCase_3099_<Unit, List<GenModel_3099_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3099_> = repository.getAll()
}

class GenSaveUseCase_3099_ @Inject constructor(
    private val repository: GenRepositoryImpl_3099_
) : GenUseCase_3099_<GenModel_3099_, GenModel_3099_> {
    override suspend fun invoke(params: GenModel_3099_): GenModel_3099_ = repository.save(params)
}

class GenDeleteUseCase_3099_ @Inject constructor(
    private val repository: GenRepositoryImpl_3099_
) : GenUseCase_3099_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3099_ @Inject constructor(
    private val repository: GenRepositoryImpl_3099_
) : GenUseCase_3099_<String, List<GenModel_3099_>> {
    override suspend fun invoke(params: String): List<GenModel_3099_> = repository.search(params)
}

abstract class GenMapper_3099_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3099_ : GenMapper_3099_<GenModel_3099_, String>() {
    override fun map(input: GenModel_3099_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3099_ : GenMapper_3099_<String, GenModel_3099_>() {
    override fun map(input: String): GenModel_3099_ {
        val parts = input.split(":")
        return GenModel_3099_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3099_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3099_,
    private val saveUseCase: GenSaveUseCase_3099_,
    private val deleteUseCase: GenDeleteUseCase_3099_,
    private val searchUseCase: GenSearchUseCase_3099_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3099_>(GenState_3099_.Idle)
    val state: StateFlow<GenState_3099_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3099_) {
        when (event) {
            is GenEvent_3099_.Load -> loadAll()
            is GenEvent_3099_.Update -> save(event.model)
            is GenEvent_3099_.Delete -> delete(event.id)
            is GenEvent_3099_.Refresh -> loadAll()
            is GenEvent_3099_.Search -> search(event.query)
            is GenEvent_3099_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3099_.Loading; _state.value = GenState_3099_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3099_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3099_.Success(searchUseCase(query)) } }
}
