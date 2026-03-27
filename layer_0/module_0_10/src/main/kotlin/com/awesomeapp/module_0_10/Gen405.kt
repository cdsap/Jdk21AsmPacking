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

data class GenModel_405_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_405_ {
    data class Load(val id: Long) : GenEvent_405_()
    data class Update(val model: GenModel_405_) : GenEvent_405_()
    data class Delete(val id: Long) : GenEvent_405_()
    data object Refresh : GenEvent_405_()
    data class Search(val query: String) : GenEvent_405_()
    data class Filter(val predicate: String) : GenEvent_405_()
}

sealed class GenState_405_ {
    data object Idle : GenState_405_()
    data object Loading : GenState_405_()
    data class Success(val items: List<GenModel_405_>) : GenState_405_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_405_()
    data class Partial(val items: List<GenModel_405_>, val hasMore: Boolean) : GenState_405_()
}

interface GenRepository_405_ {
    suspend fun getAll(): List<GenModel_405_>
    suspend fun getById(id: Long): GenModel_405_?
    suspend fun save(model: GenModel_405_): GenModel_405_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_405_>
}

@Singleton
class GenRepositoryImpl_405_ @Inject constructor() : GenRepository_405_ {
    private val store = mutableMapOf<Long, GenModel_405_>()
    override suspend fun getAll(): List<GenModel_405_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_405_? = store[id]
    override suspend fun save(model: GenModel_405_): GenModel_405_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_405_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_405_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_405_ @Inject constructor(
    private val repository: GenRepositoryImpl_405_
) : GenUseCase_405_<Unit, List<GenModel_405_>> {
    override suspend fun invoke(params: Unit): List<GenModel_405_> = repository.getAll()
}

class GenSaveUseCase_405_ @Inject constructor(
    private val repository: GenRepositoryImpl_405_
) : GenUseCase_405_<GenModel_405_, GenModel_405_> {
    override suspend fun invoke(params: GenModel_405_): GenModel_405_ = repository.save(params)
}

class GenDeleteUseCase_405_ @Inject constructor(
    private val repository: GenRepositoryImpl_405_
) : GenUseCase_405_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_405_ @Inject constructor(
    private val repository: GenRepositoryImpl_405_
) : GenUseCase_405_<String, List<GenModel_405_>> {
    override suspend fun invoke(params: String): List<GenModel_405_> = repository.search(params)
}

abstract class GenMapper_405_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_405_ : GenMapper_405_<GenModel_405_, String>() {
    override fun map(input: GenModel_405_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_405_ : GenMapper_405_<String, GenModel_405_>() {
    override fun map(input: String): GenModel_405_ {
        val parts = input.split(":")
        return GenModel_405_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_405_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_405_,
    private val saveUseCase: GenSaveUseCase_405_,
    private val deleteUseCase: GenDeleteUseCase_405_,
    private val searchUseCase: GenSearchUseCase_405_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_405_>(GenState_405_.Idle)
    val state: StateFlow<GenState_405_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_405_) {
        when (event) {
            is GenEvent_405_.Load -> loadAll()
            is GenEvent_405_.Update -> save(event.model)
            is GenEvent_405_.Delete -> delete(event.id)
            is GenEvent_405_.Refresh -> loadAll()
            is GenEvent_405_.Search -> search(event.query)
            is GenEvent_405_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_405_.Loading; _state.value = GenState_405_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_405_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_405_.Success(searchUseCase(query)) } }
}
