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

data class GenModel_109_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_109_ {
    data class Load(val id: Long) : GenEvent_109_()
    data class Update(val model: GenModel_109_) : GenEvent_109_()
    data class Delete(val id: Long) : GenEvent_109_()
    data object Refresh : GenEvent_109_()
    data class Search(val query: String) : GenEvent_109_()
    data class Filter(val predicate: String) : GenEvent_109_()
}

sealed class GenState_109_ {
    data object Idle : GenState_109_()
    data object Loading : GenState_109_()
    data class Success(val items: List<GenModel_109_>) : GenState_109_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_109_()
    data class Partial(val items: List<GenModel_109_>, val hasMore: Boolean) : GenState_109_()
}

interface GenRepository_109_ {
    suspend fun getAll(): List<GenModel_109_>
    suspend fun getById(id: Long): GenModel_109_?
    suspend fun save(model: GenModel_109_): GenModel_109_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_109_>
}

@Singleton
class GenRepositoryImpl_109_ @Inject constructor() : GenRepository_109_ {
    private val store = mutableMapOf<Long, GenModel_109_>()
    override suspend fun getAll(): List<GenModel_109_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_109_? = store[id]
    override suspend fun save(model: GenModel_109_): GenModel_109_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_109_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_109_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_109_ @Inject constructor(
    private val repository: GenRepositoryImpl_109_
) : GenUseCase_109_<Unit, List<GenModel_109_>> {
    override suspend fun invoke(params: Unit): List<GenModel_109_> = repository.getAll()
}

class GenSaveUseCase_109_ @Inject constructor(
    private val repository: GenRepositoryImpl_109_
) : GenUseCase_109_<GenModel_109_, GenModel_109_> {
    override suspend fun invoke(params: GenModel_109_): GenModel_109_ = repository.save(params)
}

class GenDeleteUseCase_109_ @Inject constructor(
    private val repository: GenRepositoryImpl_109_
) : GenUseCase_109_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_109_ @Inject constructor(
    private val repository: GenRepositoryImpl_109_
) : GenUseCase_109_<String, List<GenModel_109_>> {
    override suspend fun invoke(params: String): List<GenModel_109_> = repository.search(params)
}

abstract class GenMapper_109_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_109_ : GenMapper_109_<GenModel_109_, String>() {
    override fun map(input: GenModel_109_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_109_ : GenMapper_109_<String, GenModel_109_>() {
    override fun map(input: String): GenModel_109_ {
        val parts = input.split(":")
        return GenModel_109_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_109_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_109_,
    private val saveUseCase: GenSaveUseCase_109_,
    private val deleteUseCase: GenDeleteUseCase_109_,
    private val searchUseCase: GenSearchUseCase_109_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_109_>(GenState_109_.Idle)
    val state: StateFlow<GenState_109_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_109_) {
        when (event) {
            is GenEvent_109_.Load -> loadAll()
            is GenEvent_109_.Update -> save(event.model)
            is GenEvent_109_.Delete -> delete(event.id)
            is GenEvent_109_.Refresh -> loadAll()
            is GenEvent_109_.Search -> search(event.query)
            is GenEvent_109_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_109_.Loading; _state.value = GenState_109_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_109_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_109_.Success(searchUseCase(query)) } }
}
