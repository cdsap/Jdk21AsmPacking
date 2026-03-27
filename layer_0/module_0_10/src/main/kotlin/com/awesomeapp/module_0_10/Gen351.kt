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

data class GenModel_351_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_351_ {
    data class Load(val id: Long) : GenEvent_351_()
    data class Update(val model: GenModel_351_) : GenEvent_351_()
    data class Delete(val id: Long) : GenEvent_351_()
    data object Refresh : GenEvent_351_()
    data class Search(val query: String) : GenEvent_351_()
    data class Filter(val predicate: String) : GenEvent_351_()
}

sealed class GenState_351_ {
    data object Idle : GenState_351_()
    data object Loading : GenState_351_()
    data class Success(val items: List<GenModel_351_>) : GenState_351_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_351_()
    data class Partial(val items: List<GenModel_351_>, val hasMore: Boolean) : GenState_351_()
}

interface GenRepository_351_ {
    suspend fun getAll(): List<GenModel_351_>
    suspend fun getById(id: Long): GenModel_351_?
    suspend fun save(model: GenModel_351_): GenModel_351_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_351_>
}

@Singleton
class GenRepositoryImpl_351_ @Inject constructor() : GenRepository_351_ {
    private val store = mutableMapOf<Long, GenModel_351_>()
    override suspend fun getAll(): List<GenModel_351_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_351_? = store[id]
    override suspend fun save(model: GenModel_351_): GenModel_351_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_351_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_351_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_351_ @Inject constructor(
    private val repository: GenRepositoryImpl_351_
) : GenUseCase_351_<Unit, List<GenModel_351_>> {
    override suspend fun invoke(params: Unit): List<GenModel_351_> = repository.getAll()
}

class GenSaveUseCase_351_ @Inject constructor(
    private val repository: GenRepositoryImpl_351_
) : GenUseCase_351_<GenModel_351_, GenModel_351_> {
    override suspend fun invoke(params: GenModel_351_): GenModel_351_ = repository.save(params)
}

class GenDeleteUseCase_351_ @Inject constructor(
    private val repository: GenRepositoryImpl_351_
) : GenUseCase_351_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_351_ @Inject constructor(
    private val repository: GenRepositoryImpl_351_
) : GenUseCase_351_<String, List<GenModel_351_>> {
    override suspend fun invoke(params: String): List<GenModel_351_> = repository.search(params)
}

abstract class GenMapper_351_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_351_ : GenMapper_351_<GenModel_351_, String>() {
    override fun map(input: GenModel_351_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_351_ : GenMapper_351_<String, GenModel_351_>() {
    override fun map(input: String): GenModel_351_ {
        val parts = input.split(":")
        return GenModel_351_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_351_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_351_,
    private val saveUseCase: GenSaveUseCase_351_,
    private val deleteUseCase: GenDeleteUseCase_351_,
    private val searchUseCase: GenSearchUseCase_351_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_351_>(GenState_351_.Idle)
    val state: StateFlow<GenState_351_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_351_) {
        when (event) {
            is GenEvent_351_.Load -> loadAll()
            is GenEvent_351_.Update -> save(event.model)
            is GenEvent_351_.Delete -> delete(event.id)
            is GenEvent_351_.Refresh -> loadAll()
            is GenEvent_351_.Search -> search(event.query)
            is GenEvent_351_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_351_.Loading; _state.value = GenState_351_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_351_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_351_.Success(searchUseCase(query)) } }
}
