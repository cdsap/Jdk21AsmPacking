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

data class GenModel_915_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_915_ {
    data class Load(val id: Long) : GenEvent_915_()
    data class Update(val model: GenModel_915_) : GenEvent_915_()
    data class Delete(val id: Long) : GenEvent_915_()
    data object Refresh : GenEvent_915_()
    data class Search(val query: String) : GenEvent_915_()
    data class Filter(val predicate: String) : GenEvent_915_()
}

sealed class GenState_915_ {
    data object Idle : GenState_915_()
    data object Loading : GenState_915_()
    data class Success(val items: List<GenModel_915_>) : GenState_915_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_915_()
    data class Partial(val items: List<GenModel_915_>, val hasMore: Boolean) : GenState_915_()
}

interface GenRepository_915_ {
    suspend fun getAll(): List<GenModel_915_>
    suspend fun getById(id: Long): GenModel_915_?
    suspend fun save(model: GenModel_915_): GenModel_915_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_915_>
}

@Singleton
class GenRepositoryImpl_915_ @Inject constructor() : GenRepository_915_ {
    private val store = mutableMapOf<Long, GenModel_915_>()
    override suspend fun getAll(): List<GenModel_915_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_915_? = store[id]
    override suspend fun save(model: GenModel_915_): GenModel_915_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_915_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_915_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_915_ @Inject constructor(
    private val repository: GenRepositoryImpl_915_
) : GenUseCase_915_<Unit, List<GenModel_915_>> {
    override suspend fun invoke(params: Unit): List<GenModel_915_> = repository.getAll()
}

class GenSaveUseCase_915_ @Inject constructor(
    private val repository: GenRepositoryImpl_915_
) : GenUseCase_915_<GenModel_915_, GenModel_915_> {
    override suspend fun invoke(params: GenModel_915_): GenModel_915_ = repository.save(params)
}

class GenDeleteUseCase_915_ @Inject constructor(
    private val repository: GenRepositoryImpl_915_
) : GenUseCase_915_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_915_ @Inject constructor(
    private val repository: GenRepositoryImpl_915_
) : GenUseCase_915_<String, List<GenModel_915_>> {
    override suspend fun invoke(params: String): List<GenModel_915_> = repository.search(params)
}

abstract class GenMapper_915_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_915_ : GenMapper_915_<GenModel_915_, String>() {
    override fun map(input: GenModel_915_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_915_ : GenMapper_915_<String, GenModel_915_>() {
    override fun map(input: String): GenModel_915_ {
        val parts = input.split(":")
        return GenModel_915_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_915_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_915_,
    private val saveUseCase: GenSaveUseCase_915_,
    private val deleteUseCase: GenDeleteUseCase_915_,
    private val searchUseCase: GenSearchUseCase_915_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_915_>(GenState_915_.Idle)
    val state: StateFlow<GenState_915_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_915_) {
        when (event) {
            is GenEvent_915_.Load -> loadAll()
            is GenEvent_915_.Update -> save(event.model)
            is GenEvent_915_.Delete -> delete(event.id)
            is GenEvent_915_.Refresh -> loadAll()
            is GenEvent_915_.Search -> search(event.query)
            is GenEvent_915_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_915_.Loading; _state.value = GenState_915_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_915_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_915_.Success(searchUseCase(query)) } }
}
