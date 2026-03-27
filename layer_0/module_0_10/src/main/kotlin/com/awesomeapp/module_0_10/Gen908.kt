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

data class GenModel_908_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_908_ {
    data class Load(val id: Long) : GenEvent_908_()
    data class Update(val model: GenModel_908_) : GenEvent_908_()
    data class Delete(val id: Long) : GenEvent_908_()
    data object Refresh : GenEvent_908_()
    data class Search(val query: String) : GenEvent_908_()
    data class Filter(val predicate: String) : GenEvent_908_()
}

sealed class GenState_908_ {
    data object Idle : GenState_908_()
    data object Loading : GenState_908_()
    data class Success(val items: List<GenModel_908_>) : GenState_908_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_908_()
    data class Partial(val items: List<GenModel_908_>, val hasMore: Boolean) : GenState_908_()
}

interface GenRepository_908_ {
    suspend fun getAll(): List<GenModel_908_>
    suspend fun getById(id: Long): GenModel_908_?
    suspend fun save(model: GenModel_908_): GenModel_908_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_908_>
}

@Singleton
class GenRepositoryImpl_908_ @Inject constructor() : GenRepository_908_ {
    private val store = mutableMapOf<Long, GenModel_908_>()
    override suspend fun getAll(): List<GenModel_908_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_908_? = store[id]
    override suspend fun save(model: GenModel_908_): GenModel_908_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_908_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_908_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_908_ @Inject constructor(
    private val repository: GenRepositoryImpl_908_
) : GenUseCase_908_<Unit, List<GenModel_908_>> {
    override suspend fun invoke(params: Unit): List<GenModel_908_> = repository.getAll()
}

class GenSaveUseCase_908_ @Inject constructor(
    private val repository: GenRepositoryImpl_908_
) : GenUseCase_908_<GenModel_908_, GenModel_908_> {
    override suspend fun invoke(params: GenModel_908_): GenModel_908_ = repository.save(params)
}

class GenDeleteUseCase_908_ @Inject constructor(
    private val repository: GenRepositoryImpl_908_
) : GenUseCase_908_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_908_ @Inject constructor(
    private val repository: GenRepositoryImpl_908_
) : GenUseCase_908_<String, List<GenModel_908_>> {
    override suspend fun invoke(params: String): List<GenModel_908_> = repository.search(params)
}

abstract class GenMapper_908_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_908_ : GenMapper_908_<GenModel_908_, String>() {
    override fun map(input: GenModel_908_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_908_ : GenMapper_908_<String, GenModel_908_>() {
    override fun map(input: String): GenModel_908_ {
        val parts = input.split(":")
        return GenModel_908_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_908_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_908_,
    private val saveUseCase: GenSaveUseCase_908_,
    private val deleteUseCase: GenDeleteUseCase_908_,
    private val searchUseCase: GenSearchUseCase_908_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_908_>(GenState_908_.Idle)
    val state: StateFlow<GenState_908_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_908_) {
        when (event) {
            is GenEvent_908_.Load -> loadAll()
            is GenEvent_908_.Update -> save(event.model)
            is GenEvent_908_.Delete -> delete(event.id)
            is GenEvent_908_.Refresh -> loadAll()
            is GenEvent_908_.Search -> search(event.query)
            is GenEvent_908_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_908_.Loading; _state.value = GenState_908_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_908_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_908_.Success(searchUseCase(query)) } }
}
