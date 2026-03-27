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

data class GenModel_701_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_701_ {
    data class Load(val id: Long) : GenEvent_701_()
    data class Update(val model: GenModel_701_) : GenEvent_701_()
    data class Delete(val id: Long) : GenEvent_701_()
    data object Refresh : GenEvent_701_()
    data class Search(val query: String) : GenEvent_701_()
    data class Filter(val predicate: String) : GenEvent_701_()
}

sealed class GenState_701_ {
    data object Idle : GenState_701_()
    data object Loading : GenState_701_()
    data class Success(val items: List<GenModel_701_>) : GenState_701_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_701_()
    data class Partial(val items: List<GenModel_701_>, val hasMore: Boolean) : GenState_701_()
}

interface GenRepository_701_ {
    suspend fun getAll(): List<GenModel_701_>
    suspend fun getById(id: Long): GenModel_701_?
    suspend fun save(model: GenModel_701_): GenModel_701_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_701_>
}

@Singleton
class GenRepositoryImpl_701_ @Inject constructor() : GenRepository_701_ {
    private val store = mutableMapOf<Long, GenModel_701_>()
    override suspend fun getAll(): List<GenModel_701_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_701_? = store[id]
    override suspend fun save(model: GenModel_701_): GenModel_701_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_701_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_701_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_701_ @Inject constructor(
    private val repository: GenRepositoryImpl_701_
) : GenUseCase_701_<Unit, List<GenModel_701_>> {
    override suspend fun invoke(params: Unit): List<GenModel_701_> = repository.getAll()
}

class GenSaveUseCase_701_ @Inject constructor(
    private val repository: GenRepositoryImpl_701_
) : GenUseCase_701_<GenModel_701_, GenModel_701_> {
    override suspend fun invoke(params: GenModel_701_): GenModel_701_ = repository.save(params)
}

class GenDeleteUseCase_701_ @Inject constructor(
    private val repository: GenRepositoryImpl_701_
) : GenUseCase_701_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_701_ @Inject constructor(
    private val repository: GenRepositoryImpl_701_
) : GenUseCase_701_<String, List<GenModel_701_>> {
    override suspend fun invoke(params: String): List<GenModel_701_> = repository.search(params)
}

abstract class GenMapper_701_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_701_ : GenMapper_701_<GenModel_701_, String>() {
    override fun map(input: GenModel_701_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_701_ : GenMapper_701_<String, GenModel_701_>() {
    override fun map(input: String): GenModel_701_ {
        val parts = input.split(":")
        return GenModel_701_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_701_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_701_,
    private val saveUseCase: GenSaveUseCase_701_,
    private val deleteUseCase: GenDeleteUseCase_701_,
    private val searchUseCase: GenSearchUseCase_701_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_701_>(GenState_701_.Idle)
    val state: StateFlow<GenState_701_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_701_) {
        when (event) {
            is GenEvent_701_.Load -> loadAll()
            is GenEvent_701_.Update -> save(event.model)
            is GenEvent_701_.Delete -> delete(event.id)
            is GenEvent_701_.Refresh -> loadAll()
            is GenEvent_701_.Search -> search(event.query)
            is GenEvent_701_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_701_.Loading; _state.value = GenState_701_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_701_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_701_.Success(searchUseCase(query)) } }
}
