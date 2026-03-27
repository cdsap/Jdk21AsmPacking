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

data class GenModel_57_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_57_ {
    data class Load(val id: Long) : GenEvent_57_()
    data class Update(val model: GenModel_57_) : GenEvent_57_()
    data class Delete(val id: Long) : GenEvent_57_()
    data object Refresh : GenEvent_57_()
    data class Search(val query: String) : GenEvent_57_()
    data class Filter(val predicate: String) : GenEvent_57_()
}

sealed class GenState_57_ {
    data object Idle : GenState_57_()
    data object Loading : GenState_57_()
    data class Success(val items: List<GenModel_57_>) : GenState_57_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_57_()
    data class Partial(val items: List<GenModel_57_>, val hasMore: Boolean) : GenState_57_()
}

interface GenRepository_57_ {
    suspend fun getAll(): List<GenModel_57_>
    suspend fun getById(id: Long): GenModel_57_?
    suspend fun save(model: GenModel_57_): GenModel_57_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_57_>
}

@Singleton
class GenRepositoryImpl_57_ @Inject constructor() : GenRepository_57_ {
    private val store = mutableMapOf<Long, GenModel_57_>()
    override suspend fun getAll(): List<GenModel_57_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_57_? = store[id]
    override suspend fun save(model: GenModel_57_): GenModel_57_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_57_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_57_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_57_ @Inject constructor(
    private val repository: GenRepositoryImpl_57_
) : GenUseCase_57_<Unit, List<GenModel_57_>> {
    override suspend fun invoke(params: Unit): List<GenModel_57_> = repository.getAll()
}

class GenSaveUseCase_57_ @Inject constructor(
    private val repository: GenRepositoryImpl_57_
) : GenUseCase_57_<GenModel_57_, GenModel_57_> {
    override suspend fun invoke(params: GenModel_57_): GenModel_57_ = repository.save(params)
}

class GenDeleteUseCase_57_ @Inject constructor(
    private val repository: GenRepositoryImpl_57_
) : GenUseCase_57_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_57_ @Inject constructor(
    private val repository: GenRepositoryImpl_57_
) : GenUseCase_57_<String, List<GenModel_57_>> {
    override suspend fun invoke(params: String): List<GenModel_57_> = repository.search(params)
}

abstract class GenMapper_57_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_57_ : GenMapper_57_<GenModel_57_, String>() {
    override fun map(input: GenModel_57_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_57_ : GenMapper_57_<String, GenModel_57_>() {
    override fun map(input: String): GenModel_57_ {
        val parts = input.split(":")
        return GenModel_57_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_57_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_57_,
    private val saveUseCase: GenSaveUseCase_57_,
    private val deleteUseCase: GenDeleteUseCase_57_,
    private val searchUseCase: GenSearchUseCase_57_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_57_>(GenState_57_.Idle)
    val state: StateFlow<GenState_57_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_57_) {
        when (event) {
            is GenEvent_57_.Load -> loadAll()
            is GenEvent_57_.Update -> save(event.model)
            is GenEvent_57_.Delete -> delete(event.id)
            is GenEvent_57_.Refresh -> loadAll()
            is GenEvent_57_.Search -> search(event.query)
            is GenEvent_57_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_57_.Loading; _state.value = GenState_57_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_57_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_57_.Success(searchUseCase(query)) } }
}
