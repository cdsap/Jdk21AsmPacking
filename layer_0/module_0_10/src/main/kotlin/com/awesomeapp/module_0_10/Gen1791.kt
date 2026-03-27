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

data class GenModel_1791_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1791_ {
    data class Load(val id: Long) : GenEvent_1791_()
    data class Update(val model: GenModel_1791_) : GenEvent_1791_()
    data class Delete(val id: Long) : GenEvent_1791_()
    data object Refresh : GenEvent_1791_()
    data class Search(val query: String) : GenEvent_1791_()
    data class Filter(val predicate: String) : GenEvent_1791_()
}

sealed class GenState_1791_ {
    data object Idle : GenState_1791_()
    data object Loading : GenState_1791_()
    data class Success(val items: List<GenModel_1791_>) : GenState_1791_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1791_()
    data class Partial(val items: List<GenModel_1791_>, val hasMore: Boolean) : GenState_1791_()
}

interface GenRepository_1791_ {
    suspend fun getAll(): List<GenModel_1791_>
    suspend fun getById(id: Long): GenModel_1791_?
    suspend fun save(model: GenModel_1791_): GenModel_1791_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1791_>
}

@Singleton
class GenRepositoryImpl_1791_ @Inject constructor() : GenRepository_1791_ {
    private val store = mutableMapOf<Long, GenModel_1791_>()
    override suspend fun getAll(): List<GenModel_1791_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1791_? = store[id]
    override suspend fun save(model: GenModel_1791_): GenModel_1791_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1791_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1791_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1791_ @Inject constructor(
    private val repository: GenRepositoryImpl_1791_
) : GenUseCase_1791_<Unit, List<GenModel_1791_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1791_> = repository.getAll()
}

class GenSaveUseCase_1791_ @Inject constructor(
    private val repository: GenRepositoryImpl_1791_
) : GenUseCase_1791_<GenModel_1791_, GenModel_1791_> {
    override suspend fun invoke(params: GenModel_1791_): GenModel_1791_ = repository.save(params)
}

class GenDeleteUseCase_1791_ @Inject constructor(
    private val repository: GenRepositoryImpl_1791_
) : GenUseCase_1791_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1791_ @Inject constructor(
    private val repository: GenRepositoryImpl_1791_
) : GenUseCase_1791_<String, List<GenModel_1791_>> {
    override suspend fun invoke(params: String): List<GenModel_1791_> = repository.search(params)
}

abstract class GenMapper_1791_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1791_ : GenMapper_1791_<GenModel_1791_, String>() {
    override fun map(input: GenModel_1791_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1791_ : GenMapper_1791_<String, GenModel_1791_>() {
    override fun map(input: String): GenModel_1791_ {
        val parts = input.split(":")
        return GenModel_1791_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1791_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1791_,
    private val saveUseCase: GenSaveUseCase_1791_,
    private val deleteUseCase: GenDeleteUseCase_1791_,
    private val searchUseCase: GenSearchUseCase_1791_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1791_>(GenState_1791_.Idle)
    val state: StateFlow<GenState_1791_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1791_) {
        when (event) {
            is GenEvent_1791_.Load -> loadAll()
            is GenEvent_1791_.Update -> save(event.model)
            is GenEvent_1791_.Delete -> delete(event.id)
            is GenEvent_1791_.Refresh -> loadAll()
            is GenEvent_1791_.Search -> search(event.query)
            is GenEvent_1791_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1791_.Loading; _state.value = GenState_1791_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1791_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1791_.Success(searchUseCase(query)) } }
}
