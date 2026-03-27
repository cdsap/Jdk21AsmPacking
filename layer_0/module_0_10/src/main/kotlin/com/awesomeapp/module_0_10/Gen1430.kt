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

data class GenModel_1430_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1430_ {
    data class Load(val id: Long) : GenEvent_1430_()
    data class Update(val model: GenModel_1430_) : GenEvent_1430_()
    data class Delete(val id: Long) : GenEvent_1430_()
    data object Refresh : GenEvent_1430_()
    data class Search(val query: String) : GenEvent_1430_()
    data class Filter(val predicate: String) : GenEvent_1430_()
}

sealed class GenState_1430_ {
    data object Idle : GenState_1430_()
    data object Loading : GenState_1430_()
    data class Success(val items: List<GenModel_1430_>) : GenState_1430_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1430_()
    data class Partial(val items: List<GenModel_1430_>, val hasMore: Boolean) : GenState_1430_()
}

interface GenRepository_1430_ {
    suspend fun getAll(): List<GenModel_1430_>
    suspend fun getById(id: Long): GenModel_1430_?
    suspend fun save(model: GenModel_1430_): GenModel_1430_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1430_>
}

@Singleton
class GenRepositoryImpl_1430_ @Inject constructor() : GenRepository_1430_ {
    private val store = mutableMapOf<Long, GenModel_1430_>()
    override suspend fun getAll(): List<GenModel_1430_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1430_? = store[id]
    override suspend fun save(model: GenModel_1430_): GenModel_1430_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1430_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1430_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1430_ @Inject constructor(
    private val repository: GenRepositoryImpl_1430_
) : GenUseCase_1430_<Unit, List<GenModel_1430_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1430_> = repository.getAll()
}

class GenSaveUseCase_1430_ @Inject constructor(
    private val repository: GenRepositoryImpl_1430_
) : GenUseCase_1430_<GenModel_1430_, GenModel_1430_> {
    override suspend fun invoke(params: GenModel_1430_): GenModel_1430_ = repository.save(params)
}

class GenDeleteUseCase_1430_ @Inject constructor(
    private val repository: GenRepositoryImpl_1430_
) : GenUseCase_1430_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1430_ @Inject constructor(
    private val repository: GenRepositoryImpl_1430_
) : GenUseCase_1430_<String, List<GenModel_1430_>> {
    override suspend fun invoke(params: String): List<GenModel_1430_> = repository.search(params)
}

abstract class GenMapper_1430_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1430_ : GenMapper_1430_<GenModel_1430_, String>() {
    override fun map(input: GenModel_1430_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1430_ : GenMapper_1430_<String, GenModel_1430_>() {
    override fun map(input: String): GenModel_1430_ {
        val parts = input.split(":")
        return GenModel_1430_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1430_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1430_,
    private val saveUseCase: GenSaveUseCase_1430_,
    private val deleteUseCase: GenDeleteUseCase_1430_,
    private val searchUseCase: GenSearchUseCase_1430_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1430_>(GenState_1430_.Idle)
    val state: StateFlow<GenState_1430_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1430_) {
        when (event) {
            is GenEvent_1430_.Load -> loadAll()
            is GenEvent_1430_.Update -> save(event.model)
            is GenEvent_1430_.Delete -> delete(event.id)
            is GenEvent_1430_.Refresh -> loadAll()
            is GenEvent_1430_.Search -> search(event.query)
            is GenEvent_1430_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1430_.Loading; _state.value = GenState_1430_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1430_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1430_.Success(searchUseCase(query)) } }
}
