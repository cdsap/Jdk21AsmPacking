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

data class GenModel_3007_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3007_ {
    data class Load(val id: Long) : GenEvent_3007_()
    data class Update(val model: GenModel_3007_) : GenEvent_3007_()
    data class Delete(val id: Long) : GenEvent_3007_()
    data object Refresh : GenEvent_3007_()
    data class Search(val query: String) : GenEvent_3007_()
    data class Filter(val predicate: String) : GenEvent_3007_()
}

sealed class GenState_3007_ {
    data object Idle : GenState_3007_()
    data object Loading : GenState_3007_()
    data class Success(val items: List<GenModel_3007_>) : GenState_3007_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3007_()
    data class Partial(val items: List<GenModel_3007_>, val hasMore: Boolean) : GenState_3007_()
}

interface GenRepository_3007_ {
    suspend fun getAll(): List<GenModel_3007_>
    suspend fun getById(id: Long): GenModel_3007_?
    suspend fun save(model: GenModel_3007_): GenModel_3007_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3007_>
}

@Singleton
class GenRepositoryImpl_3007_ @Inject constructor() : GenRepository_3007_ {
    private val store = mutableMapOf<Long, GenModel_3007_>()
    override suspend fun getAll(): List<GenModel_3007_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3007_? = store[id]
    override suspend fun save(model: GenModel_3007_): GenModel_3007_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3007_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3007_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3007_ @Inject constructor(
    private val repository: GenRepositoryImpl_3007_
) : GenUseCase_3007_<Unit, List<GenModel_3007_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3007_> = repository.getAll()
}

class GenSaveUseCase_3007_ @Inject constructor(
    private val repository: GenRepositoryImpl_3007_
) : GenUseCase_3007_<GenModel_3007_, GenModel_3007_> {
    override suspend fun invoke(params: GenModel_3007_): GenModel_3007_ = repository.save(params)
}

class GenDeleteUseCase_3007_ @Inject constructor(
    private val repository: GenRepositoryImpl_3007_
) : GenUseCase_3007_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3007_ @Inject constructor(
    private val repository: GenRepositoryImpl_3007_
) : GenUseCase_3007_<String, List<GenModel_3007_>> {
    override suspend fun invoke(params: String): List<GenModel_3007_> = repository.search(params)
}

abstract class GenMapper_3007_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3007_ : GenMapper_3007_<GenModel_3007_, String>() {
    override fun map(input: GenModel_3007_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3007_ : GenMapper_3007_<String, GenModel_3007_>() {
    override fun map(input: String): GenModel_3007_ {
        val parts = input.split(":")
        return GenModel_3007_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3007_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3007_,
    private val saveUseCase: GenSaveUseCase_3007_,
    private val deleteUseCase: GenDeleteUseCase_3007_,
    private val searchUseCase: GenSearchUseCase_3007_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3007_>(GenState_3007_.Idle)
    val state: StateFlow<GenState_3007_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3007_) {
        when (event) {
            is GenEvent_3007_.Load -> loadAll()
            is GenEvent_3007_.Update -> save(event.model)
            is GenEvent_3007_.Delete -> delete(event.id)
            is GenEvent_3007_.Refresh -> loadAll()
            is GenEvent_3007_.Search -> search(event.query)
            is GenEvent_3007_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3007_.Loading; _state.value = GenState_3007_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3007_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3007_.Success(searchUseCase(query)) } }
}
