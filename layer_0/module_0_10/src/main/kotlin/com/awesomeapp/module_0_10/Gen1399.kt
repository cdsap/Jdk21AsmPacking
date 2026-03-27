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

data class GenModel_1399_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1399_ {
    data class Load(val id: Long) : GenEvent_1399_()
    data class Update(val model: GenModel_1399_) : GenEvent_1399_()
    data class Delete(val id: Long) : GenEvent_1399_()
    data object Refresh : GenEvent_1399_()
    data class Search(val query: String) : GenEvent_1399_()
    data class Filter(val predicate: String) : GenEvent_1399_()
}

sealed class GenState_1399_ {
    data object Idle : GenState_1399_()
    data object Loading : GenState_1399_()
    data class Success(val items: List<GenModel_1399_>) : GenState_1399_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1399_()
    data class Partial(val items: List<GenModel_1399_>, val hasMore: Boolean) : GenState_1399_()
}

interface GenRepository_1399_ {
    suspend fun getAll(): List<GenModel_1399_>
    suspend fun getById(id: Long): GenModel_1399_?
    suspend fun save(model: GenModel_1399_): GenModel_1399_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1399_>
}

@Singleton
class GenRepositoryImpl_1399_ @Inject constructor() : GenRepository_1399_ {
    private val store = mutableMapOf<Long, GenModel_1399_>()
    override suspend fun getAll(): List<GenModel_1399_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1399_? = store[id]
    override suspend fun save(model: GenModel_1399_): GenModel_1399_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1399_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1399_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1399_ @Inject constructor(
    private val repository: GenRepositoryImpl_1399_
) : GenUseCase_1399_<Unit, List<GenModel_1399_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1399_> = repository.getAll()
}

class GenSaveUseCase_1399_ @Inject constructor(
    private val repository: GenRepositoryImpl_1399_
) : GenUseCase_1399_<GenModel_1399_, GenModel_1399_> {
    override suspend fun invoke(params: GenModel_1399_): GenModel_1399_ = repository.save(params)
}

class GenDeleteUseCase_1399_ @Inject constructor(
    private val repository: GenRepositoryImpl_1399_
) : GenUseCase_1399_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1399_ @Inject constructor(
    private val repository: GenRepositoryImpl_1399_
) : GenUseCase_1399_<String, List<GenModel_1399_>> {
    override suspend fun invoke(params: String): List<GenModel_1399_> = repository.search(params)
}

abstract class GenMapper_1399_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1399_ : GenMapper_1399_<GenModel_1399_, String>() {
    override fun map(input: GenModel_1399_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1399_ : GenMapper_1399_<String, GenModel_1399_>() {
    override fun map(input: String): GenModel_1399_ {
        val parts = input.split(":")
        return GenModel_1399_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1399_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1399_,
    private val saveUseCase: GenSaveUseCase_1399_,
    private val deleteUseCase: GenDeleteUseCase_1399_,
    private val searchUseCase: GenSearchUseCase_1399_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1399_>(GenState_1399_.Idle)
    val state: StateFlow<GenState_1399_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1399_) {
        when (event) {
            is GenEvent_1399_.Load -> loadAll()
            is GenEvent_1399_.Update -> save(event.model)
            is GenEvent_1399_.Delete -> delete(event.id)
            is GenEvent_1399_.Refresh -> loadAll()
            is GenEvent_1399_.Search -> search(event.query)
            is GenEvent_1399_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1399_.Loading; _state.value = GenState_1399_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1399_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1399_.Success(searchUseCase(query)) } }
}
