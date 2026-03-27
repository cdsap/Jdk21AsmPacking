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

data class GenModel_361_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_361_ {
    data class Load(val id: Long) : GenEvent_361_()
    data class Update(val model: GenModel_361_) : GenEvent_361_()
    data class Delete(val id: Long) : GenEvent_361_()
    data object Refresh : GenEvent_361_()
    data class Search(val query: String) : GenEvent_361_()
    data class Filter(val predicate: String) : GenEvent_361_()
}

sealed class GenState_361_ {
    data object Idle : GenState_361_()
    data object Loading : GenState_361_()
    data class Success(val items: List<GenModel_361_>) : GenState_361_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_361_()
    data class Partial(val items: List<GenModel_361_>, val hasMore: Boolean) : GenState_361_()
}

interface GenRepository_361_ {
    suspend fun getAll(): List<GenModel_361_>
    suspend fun getById(id: Long): GenModel_361_?
    suspend fun save(model: GenModel_361_): GenModel_361_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_361_>
}

@Singleton
class GenRepositoryImpl_361_ @Inject constructor() : GenRepository_361_ {
    private val store = mutableMapOf<Long, GenModel_361_>()
    override suspend fun getAll(): List<GenModel_361_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_361_? = store[id]
    override suspend fun save(model: GenModel_361_): GenModel_361_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_361_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_361_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_361_ @Inject constructor(
    private val repository: GenRepositoryImpl_361_
) : GenUseCase_361_<Unit, List<GenModel_361_>> {
    override suspend fun invoke(params: Unit): List<GenModel_361_> = repository.getAll()
}

class GenSaveUseCase_361_ @Inject constructor(
    private val repository: GenRepositoryImpl_361_
) : GenUseCase_361_<GenModel_361_, GenModel_361_> {
    override suspend fun invoke(params: GenModel_361_): GenModel_361_ = repository.save(params)
}

class GenDeleteUseCase_361_ @Inject constructor(
    private val repository: GenRepositoryImpl_361_
) : GenUseCase_361_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_361_ @Inject constructor(
    private val repository: GenRepositoryImpl_361_
) : GenUseCase_361_<String, List<GenModel_361_>> {
    override suspend fun invoke(params: String): List<GenModel_361_> = repository.search(params)
}

abstract class GenMapper_361_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_361_ : GenMapper_361_<GenModel_361_, String>() {
    override fun map(input: GenModel_361_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_361_ : GenMapper_361_<String, GenModel_361_>() {
    override fun map(input: String): GenModel_361_ {
        val parts = input.split(":")
        return GenModel_361_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_361_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_361_,
    private val saveUseCase: GenSaveUseCase_361_,
    private val deleteUseCase: GenDeleteUseCase_361_,
    private val searchUseCase: GenSearchUseCase_361_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_361_>(GenState_361_.Idle)
    val state: StateFlow<GenState_361_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_361_) {
        when (event) {
            is GenEvent_361_.Load -> loadAll()
            is GenEvent_361_.Update -> save(event.model)
            is GenEvent_361_.Delete -> delete(event.id)
            is GenEvent_361_.Refresh -> loadAll()
            is GenEvent_361_.Search -> search(event.query)
            is GenEvent_361_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_361_.Loading; _state.value = GenState_361_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_361_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_361_.Success(searchUseCase(query)) } }
}
