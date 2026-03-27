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

data class GenModel_353_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_353_ {
    data class Load(val id: Long) : GenEvent_353_()
    data class Update(val model: GenModel_353_) : GenEvent_353_()
    data class Delete(val id: Long) : GenEvent_353_()
    data object Refresh : GenEvent_353_()
    data class Search(val query: String) : GenEvent_353_()
    data class Filter(val predicate: String) : GenEvent_353_()
}

sealed class GenState_353_ {
    data object Idle : GenState_353_()
    data object Loading : GenState_353_()
    data class Success(val items: List<GenModel_353_>) : GenState_353_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_353_()
    data class Partial(val items: List<GenModel_353_>, val hasMore: Boolean) : GenState_353_()
}

interface GenRepository_353_ {
    suspend fun getAll(): List<GenModel_353_>
    suspend fun getById(id: Long): GenModel_353_?
    suspend fun save(model: GenModel_353_): GenModel_353_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_353_>
}

@Singleton
class GenRepositoryImpl_353_ @Inject constructor() : GenRepository_353_ {
    private val store = mutableMapOf<Long, GenModel_353_>()
    override suspend fun getAll(): List<GenModel_353_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_353_? = store[id]
    override suspend fun save(model: GenModel_353_): GenModel_353_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_353_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_353_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_353_ @Inject constructor(
    private val repository: GenRepositoryImpl_353_
) : GenUseCase_353_<Unit, List<GenModel_353_>> {
    override suspend fun invoke(params: Unit): List<GenModel_353_> = repository.getAll()
}

class GenSaveUseCase_353_ @Inject constructor(
    private val repository: GenRepositoryImpl_353_
) : GenUseCase_353_<GenModel_353_, GenModel_353_> {
    override suspend fun invoke(params: GenModel_353_): GenModel_353_ = repository.save(params)
}

class GenDeleteUseCase_353_ @Inject constructor(
    private val repository: GenRepositoryImpl_353_
) : GenUseCase_353_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_353_ @Inject constructor(
    private val repository: GenRepositoryImpl_353_
) : GenUseCase_353_<String, List<GenModel_353_>> {
    override suspend fun invoke(params: String): List<GenModel_353_> = repository.search(params)
}

abstract class GenMapper_353_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_353_ : GenMapper_353_<GenModel_353_, String>() {
    override fun map(input: GenModel_353_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_353_ : GenMapper_353_<String, GenModel_353_>() {
    override fun map(input: String): GenModel_353_ {
        val parts = input.split(":")
        return GenModel_353_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_353_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_353_,
    private val saveUseCase: GenSaveUseCase_353_,
    private val deleteUseCase: GenDeleteUseCase_353_,
    private val searchUseCase: GenSearchUseCase_353_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_353_>(GenState_353_.Idle)
    val state: StateFlow<GenState_353_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_353_) {
        when (event) {
            is GenEvent_353_.Load -> loadAll()
            is GenEvent_353_.Update -> save(event.model)
            is GenEvent_353_.Delete -> delete(event.id)
            is GenEvent_353_.Refresh -> loadAll()
            is GenEvent_353_.Search -> search(event.query)
            is GenEvent_353_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_353_.Loading; _state.value = GenState_353_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_353_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_353_.Success(searchUseCase(query)) } }
}
