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

data class GenModel_791_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_791_ {
    data class Load(val id: Long) : GenEvent_791_()
    data class Update(val model: GenModel_791_) : GenEvent_791_()
    data class Delete(val id: Long) : GenEvent_791_()
    data object Refresh : GenEvent_791_()
    data class Search(val query: String) : GenEvent_791_()
    data class Filter(val predicate: String) : GenEvent_791_()
}

sealed class GenState_791_ {
    data object Idle : GenState_791_()
    data object Loading : GenState_791_()
    data class Success(val items: List<GenModel_791_>) : GenState_791_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_791_()
    data class Partial(val items: List<GenModel_791_>, val hasMore: Boolean) : GenState_791_()
}

interface GenRepository_791_ {
    suspend fun getAll(): List<GenModel_791_>
    suspend fun getById(id: Long): GenModel_791_?
    suspend fun save(model: GenModel_791_): GenModel_791_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_791_>
}

@Singleton
class GenRepositoryImpl_791_ @Inject constructor() : GenRepository_791_ {
    private val store = mutableMapOf<Long, GenModel_791_>()
    override suspend fun getAll(): List<GenModel_791_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_791_? = store[id]
    override suspend fun save(model: GenModel_791_): GenModel_791_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_791_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_791_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_791_ @Inject constructor(
    private val repository: GenRepositoryImpl_791_
) : GenUseCase_791_<Unit, List<GenModel_791_>> {
    override suspend fun invoke(params: Unit): List<GenModel_791_> = repository.getAll()
}

class GenSaveUseCase_791_ @Inject constructor(
    private val repository: GenRepositoryImpl_791_
) : GenUseCase_791_<GenModel_791_, GenModel_791_> {
    override suspend fun invoke(params: GenModel_791_): GenModel_791_ = repository.save(params)
}

class GenDeleteUseCase_791_ @Inject constructor(
    private val repository: GenRepositoryImpl_791_
) : GenUseCase_791_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_791_ @Inject constructor(
    private val repository: GenRepositoryImpl_791_
) : GenUseCase_791_<String, List<GenModel_791_>> {
    override suspend fun invoke(params: String): List<GenModel_791_> = repository.search(params)
}

abstract class GenMapper_791_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_791_ : GenMapper_791_<GenModel_791_, String>() {
    override fun map(input: GenModel_791_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_791_ : GenMapper_791_<String, GenModel_791_>() {
    override fun map(input: String): GenModel_791_ {
        val parts = input.split(":")
        return GenModel_791_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_791_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_791_,
    private val saveUseCase: GenSaveUseCase_791_,
    private val deleteUseCase: GenDeleteUseCase_791_,
    private val searchUseCase: GenSearchUseCase_791_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_791_>(GenState_791_.Idle)
    val state: StateFlow<GenState_791_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_791_) {
        when (event) {
            is GenEvent_791_.Load -> loadAll()
            is GenEvent_791_.Update -> save(event.model)
            is GenEvent_791_.Delete -> delete(event.id)
            is GenEvent_791_.Refresh -> loadAll()
            is GenEvent_791_.Search -> search(event.query)
            is GenEvent_791_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_791_.Loading; _state.value = GenState_791_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_791_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_791_.Success(searchUseCase(query)) } }
}
