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

data class GenModel_683_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_683_ {
    data class Load(val id: Long) : GenEvent_683_()
    data class Update(val model: GenModel_683_) : GenEvent_683_()
    data class Delete(val id: Long) : GenEvent_683_()
    data object Refresh : GenEvent_683_()
    data class Search(val query: String) : GenEvent_683_()
    data class Filter(val predicate: String) : GenEvent_683_()
}

sealed class GenState_683_ {
    data object Idle : GenState_683_()
    data object Loading : GenState_683_()
    data class Success(val items: List<GenModel_683_>) : GenState_683_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_683_()
    data class Partial(val items: List<GenModel_683_>, val hasMore: Boolean) : GenState_683_()
}

interface GenRepository_683_ {
    suspend fun getAll(): List<GenModel_683_>
    suspend fun getById(id: Long): GenModel_683_?
    suspend fun save(model: GenModel_683_): GenModel_683_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_683_>
}

@Singleton
class GenRepositoryImpl_683_ @Inject constructor() : GenRepository_683_ {
    private val store = mutableMapOf<Long, GenModel_683_>()
    override suspend fun getAll(): List<GenModel_683_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_683_? = store[id]
    override suspend fun save(model: GenModel_683_): GenModel_683_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_683_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_683_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_683_ @Inject constructor(
    private val repository: GenRepositoryImpl_683_
) : GenUseCase_683_<Unit, List<GenModel_683_>> {
    override suspend fun invoke(params: Unit): List<GenModel_683_> = repository.getAll()
}

class GenSaveUseCase_683_ @Inject constructor(
    private val repository: GenRepositoryImpl_683_
) : GenUseCase_683_<GenModel_683_, GenModel_683_> {
    override suspend fun invoke(params: GenModel_683_): GenModel_683_ = repository.save(params)
}

class GenDeleteUseCase_683_ @Inject constructor(
    private val repository: GenRepositoryImpl_683_
) : GenUseCase_683_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_683_ @Inject constructor(
    private val repository: GenRepositoryImpl_683_
) : GenUseCase_683_<String, List<GenModel_683_>> {
    override suspend fun invoke(params: String): List<GenModel_683_> = repository.search(params)
}

abstract class GenMapper_683_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_683_ : GenMapper_683_<GenModel_683_, String>() {
    override fun map(input: GenModel_683_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_683_ : GenMapper_683_<String, GenModel_683_>() {
    override fun map(input: String): GenModel_683_ {
        val parts = input.split(":")
        return GenModel_683_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_683_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_683_,
    private val saveUseCase: GenSaveUseCase_683_,
    private val deleteUseCase: GenDeleteUseCase_683_,
    private val searchUseCase: GenSearchUseCase_683_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_683_>(GenState_683_.Idle)
    val state: StateFlow<GenState_683_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_683_) {
        when (event) {
            is GenEvent_683_.Load -> loadAll()
            is GenEvent_683_.Update -> save(event.model)
            is GenEvent_683_.Delete -> delete(event.id)
            is GenEvent_683_.Refresh -> loadAll()
            is GenEvent_683_.Search -> search(event.query)
            is GenEvent_683_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_683_.Loading; _state.value = GenState_683_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_683_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_683_.Success(searchUseCase(query)) } }
}
