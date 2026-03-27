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

data class GenModel_1210_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1210_ {
    data class Load(val id: Long) : GenEvent_1210_()
    data class Update(val model: GenModel_1210_) : GenEvent_1210_()
    data class Delete(val id: Long) : GenEvent_1210_()
    data object Refresh : GenEvent_1210_()
    data class Search(val query: String) : GenEvent_1210_()
    data class Filter(val predicate: String) : GenEvent_1210_()
}

sealed class GenState_1210_ {
    data object Idle : GenState_1210_()
    data object Loading : GenState_1210_()
    data class Success(val items: List<GenModel_1210_>) : GenState_1210_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1210_()
    data class Partial(val items: List<GenModel_1210_>, val hasMore: Boolean) : GenState_1210_()
}

interface GenRepository_1210_ {
    suspend fun getAll(): List<GenModel_1210_>
    suspend fun getById(id: Long): GenModel_1210_?
    suspend fun save(model: GenModel_1210_): GenModel_1210_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1210_>
}

@Singleton
class GenRepositoryImpl_1210_ @Inject constructor() : GenRepository_1210_ {
    private val store = mutableMapOf<Long, GenModel_1210_>()
    override suspend fun getAll(): List<GenModel_1210_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1210_? = store[id]
    override suspend fun save(model: GenModel_1210_): GenModel_1210_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1210_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1210_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1210_ @Inject constructor(
    private val repository: GenRepositoryImpl_1210_
) : GenUseCase_1210_<Unit, List<GenModel_1210_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1210_> = repository.getAll()
}

class GenSaveUseCase_1210_ @Inject constructor(
    private val repository: GenRepositoryImpl_1210_
) : GenUseCase_1210_<GenModel_1210_, GenModel_1210_> {
    override suspend fun invoke(params: GenModel_1210_): GenModel_1210_ = repository.save(params)
}

class GenDeleteUseCase_1210_ @Inject constructor(
    private val repository: GenRepositoryImpl_1210_
) : GenUseCase_1210_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1210_ @Inject constructor(
    private val repository: GenRepositoryImpl_1210_
) : GenUseCase_1210_<String, List<GenModel_1210_>> {
    override suspend fun invoke(params: String): List<GenModel_1210_> = repository.search(params)
}

abstract class GenMapper_1210_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1210_ : GenMapper_1210_<GenModel_1210_, String>() {
    override fun map(input: GenModel_1210_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1210_ : GenMapper_1210_<String, GenModel_1210_>() {
    override fun map(input: String): GenModel_1210_ {
        val parts = input.split(":")
        return GenModel_1210_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1210_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1210_,
    private val saveUseCase: GenSaveUseCase_1210_,
    private val deleteUseCase: GenDeleteUseCase_1210_,
    private val searchUseCase: GenSearchUseCase_1210_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1210_>(GenState_1210_.Idle)
    val state: StateFlow<GenState_1210_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1210_) {
        when (event) {
            is GenEvent_1210_.Load -> loadAll()
            is GenEvent_1210_.Update -> save(event.model)
            is GenEvent_1210_.Delete -> delete(event.id)
            is GenEvent_1210_.Refresh -> loadAll()
            is GenEvent_1210_.Search -> search(event.query)
            is GenEvent_1210_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1210_.Loading; _state.value = GenState_1210_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1210_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1210_.Success(searchUseCase(query)) } }
}
