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

data class GenModel_3454_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3454_ {
    data class Load(val id: Long) : GenEvent_3454_()
    data class Update(val model: GenModel_3454_) : GenEvent_3454_()
    data class Delete(val id: Long) : GenEvent_3454_()
    data object Refresh : GenEvent_3454_()
    data class Search(val query: String) : GenEvent_3454_()
    data class Filter(val predicate: String) : GenEvent_3454_()
}

sealed class GenState_3454_ {
    data object Idle : GenState_3454_()
    data object Loading : GenState_3454_()
    data class Success(val items: List<GenModel_3454_>) : GenState_3454_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3454_()
    data class Partial(val items: List<GenModel_3454_>, val hasMore: Boolean) : GenState_3454_()
}

interface GenRepository_3454_ {
    suspend fun getAll(): List<GenModel_3454_>
    suspend fun getById(id: Long): GenModel_3454_?
    suspend fun save(model: GenModel_3454_): GenModel_3454_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3454_>
}

@Singleton
class GenRepositoryImpl_3454_ @Inject constructor() : GenRepository_3454_ {
    private val store = mutableMapOf<Long, GenModel_3454_>()
    override suspend fun getAll(): List<GenModel_3454_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3454_? = store[id]
    override suspend fun save(model: GenModel_3454_): GenModel_3454_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3454_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3454_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3454_ @Inject constructor(
    private val repository: GenRepositoryImpl_3454_
) : GenUseCase_3454_<Unit, List<GenModel_3454_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3454_> = repository.getAll()
}

class GenSaveUseCase_3454_ @Inject constructor(
    private val repository: GenRepositoryImpl_3454_
) : GenUseCase_3454_<GenModel_3454_, GenModel_3454_> {
    override suspend fun invoke(params: GenModel_3454_): GenModel_3454_ = repository.save(params)
}

class GenDeleteUseCase_3454_ @Inject constructor(
    private val repository: GenRepositoryImpl_3454_
) : GenUseCase_3454_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3454_ @Inject constructor(
    private val repository: GenRepositoryImpl_3454_
) : GenUseCase_3454_<String, List<GenModel_3454_>> {
    override suspend fun invoke(params: String): List<GenModel_3454_> = repository.search(params)
}

abstract class GenMapper_3454_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3454_ : GenMapper_3454_<GenModel_3454_, String>() {
    override fun map(input: GenModel_3454_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3454_ : GenMapper_3454_<String, GenModel_3454_>() {
    override fun map(input: String): GenModel_3454_ {
        val parts = input.split(":")
        return GenModel_3454_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3454_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3454_,
    private val saveUseCase: GenSaveUseCase_3454_,
    private val deleteUseCase: GenDeleteUseCase_3454_,
    private val searchUseCase: GenSearchUseCase_3454_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3454_>(GenState_3454_.Idle)
    val state: StateFlow<GenState_3454_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3454_) {
        when (event) {
            is GenEvent_3454_.Load -> loadAll()
            is GenEvent_3454_.Update -> save(event.model)
            is GenEvent_3454_.Delete -> delete(event.id)
            is GenEvent_3454_.Refresh -> loadAll()
            is GenEvent_3454_.Search -> search(event.query)
            is GenEvent_3454_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3454_.Loading; _state.value = GenState_3454_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3454_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3454_.Success(searchUseCase(query)) } }
}
