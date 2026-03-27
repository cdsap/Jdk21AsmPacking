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

data class GenModel_302_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_302_ {
    data class Load(val id: Long) : GenEvent_302_()
    data class Update(val model: GenModel_302_) : GenEvent_302_()
    data class Delete(val id: Long) : GenEvent_302_()
    data object Refresh : GenEvent_302_()
    data class Search(val query: String) : GenEvent_302_()
    data class Filter(val predicate: String) : GenEvent_302_()
}

sealed class GenState_302_ {
    data object Idle : GenState_302_()
    data object Loading : GenState_302_()
    data class Success(val items: List<GenModel_302_>) : GenState_302_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_302_()
    data class Partial(val items: List<GenModel_302_>, val hasMore: Boolean) : GenState_302_()
}

interface GenRepository_302_ {
    suspend fun getAll(): List<GenModel_302_>
    suspend fun getById(id: Long): GenModel_302_?
    suspend fun save(model: GenModel_302_): GenModel_302_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_302_>
}

@Singleton
class GenRepositoryImpl_302_ @Inject constructor() : GenRepository_302_ {
    private val store = mutableMapOf<Long, GenModel_302_>()
    override suspend fun getAll(): List<GenModel_302_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_302_? = store[id]
    override suspend fun save(model: GenModel_302_): GenModel_302_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_302_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_302_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_302_ @Inject constructor(
    private val repository: GenRepositoryImpl_302_
) : GenUseCase_302_<Unit, List<GenModel_302_>> {
    override suspend fun invoke(params: Unit): List<GenModel_302_> = repository.getAll()
}

class GenSaveUseCase_302_ @Inject constructor(
    private val repository: GenRepositoryImpl_302_
) : GenUseCase_302_<GenModel_302_, GenModel_302_> {
    override suspend fun invoke(params: GenModel_302_): GenModel_302_ = repository.save(params)
}

class GenDeleteUseCase_302_ @Inject constructor(
    private val repository: GenRepositoryImpl_302_
) : GenUseCase_302_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_302_ @Inject constructor(
    private val repository: GenRepositoryImpl_302_
) : GenUseCase_302_<String, List<GenModel_302_>> {
    override suspend fun invoke(params: String): List<GenModel_302_> = repository.search(params)
}

abstract class GenMapper_302_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_302_ : GenMapper_302_<GenModel_302_, String>() {
    override fun map(input: GenModel_302_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_302_ : GenMapper_302_<String, GenModel_302_>() {
    override fun map(input: String): GenModel_302_ {
        val parts = input.split(":")
        return GenModel_302_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_302_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_302_,
    private val saveUseCase: GenSaveUseCase_302_,
    private val deleteUseCase: GenDeleteUseCase_302_,
    private val searchUseCase: GenSearchUseCase_302_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_302_>(GenState_302_.Idle)
    val state: StateFlow<GenState_302_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_302_) {
        when (event) {
            is GenEvent_302_.Load -> loadAll()
            is GenEvent_302_.Update -> save(event.model)
            is GenEvent_302_.Delete -> delete(event.id)
            is GenEvent_302_.Refresh -> loadAll()
            is GenEvent_302_.Search -> search(event.query)
            is GenEvent_302_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_302_.Loading; _state.value = GenState_302_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_302_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_302_.Success(searchUseCase(query)) } }
}
